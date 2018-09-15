package rpc.branch.and.price;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jorlib.frameworks.columnGeneration.branchAndPrice.branchingDecisions.BranchingDecision;
import org.jorlib.frameworks.columnGeneration.io.TimeLimitExceededException;
import org.jorlib.frameworks.columnGeneration.master.AbstractMaster;
import org.jorlib.frameworks.columnGeneration.master.OptimizationSense;
import org.jorlib.frameworks.columnGeneration.util.OrderedBiMap;

import ilog.concert.IloColumn;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

/**
 * Esta clase define el problema master.
 * 
 */
public final class RPCMaster extends AbstractMaster<RPCDataModel, RPCColumn, RPCPricingProblem, RPCMasterData> {

	private static final double TOLERANCIA = 0.00001;

	private IloObjective obj; // Función objetivo.

	private Map<Point, IloRange> constraints;
	private List<IloNumVar> vars = new ArrayList<IloNumVar>();
	private Map<Point, Interval> constraintRanges;

	public RPCMaster(RPCDataModel dataModel, RPCPricingProblem pricingProblem) {
		super(dataModel, pricingProblem, OptimizationSense.MINIMIZE);	
		System.out.println("Master constructor. Columns: " + masterData.getNrColumns());
	}

	/**
	 * Crea el modelo del problema master.
	 * 
	 * @return Devuelve un MBTMasterData. Esto es un contenedor para información del
	 *         master.
	 * 
	 */
	@Override
	protected RPCMasterData buildModel() {
		IloCplex cplex = null;

		if (constraints == null)
			constraints = new HashMap<Point, IloRange>();
		if (constraintRanges == null)
			constraintRanges = new HashMap<Point, Interval>();
		try {
			if (vars != null)
				vars.clear();
			cplex = new IloCplex(); // Nueva instancia de cplex.
			cplex.setOut(null); // Deshabilitamos el output.
			// Setea el máx. número de threads.
			cplex.setParam(IloCplex.IntParam.Threads, config.MAXTHREADS);

			// Función objetivo
			obj = cplex.addMinimize();

			// Definimos constraints
			for (Point p : dataModel.matriz.unos())
				// si el punto está en los constraints que salen del branching, usamos ese rango
				if (constraintRanges.containsKey(p)) {
					Interval i = constraintRanges.get(p);
					constraints.put(p, cplex.addRange(i.lower, i.upper,
							"Pos " + p.x + "," + p.y + " cubierta por " + i.toString()));
				} else
					// sino, usamos el rango default.
					constraints.put(p, cplex.addRange(1.0, Integer.MAX_VALUE, "Pos " + p.x + "," + p.y + " cubierta "));

		} catch (IloException e) {
			e.printStackTrace();
		}

		// esto es código técnico del framework, ni lo tocamos.
		Map<RPCPricingProblem, OrderedBiMap<RPCColumn, IloNumVar>> varMap = new LinkedHashMap<>();
		RPCPricingProblem pricingProblem = this.pricingProblems.get(0);
		varMap.put(pricingProblem, new OrderedBiMap<RPCColumn, IloNumVar>());

		return new RPCMasterData(cplex, varMap);
	}

	/**
	 * Resolvemos el master.
	 * 
	 * @param timeLimit
	 * 
	 * @return true si se resolvió el modelo.
	 * @throws TimeLimitExceededException
	 *             TimeLimitExceededException
	 */
	@Override
	protected boolean solveMasterProblem(long timeLimit) throws TimeLimitExceededException {
		try {
			// Eliminamos el último dato de cubrimiento fraccionario de algun constraint
			dataModel.MijConCubrimientoFrac = null;
			dataModel.valorCubrimientoFrac = -1;

			// El time limit
			double timeRemaining = Math.max(1, (timeLimit - System.currentTimeMillis()) / 1000.0);
			masterData.cplex.setParam(IloCplex.DoubleParam.TiLim, timeRemaining);

			// Exportación del modelo.
			if (config.EXPORT_MODEL)
				masterData.cplex.exportModel("master_" + this.getIterationCount() + ".lp");

			// Resolvemos el modelo.
			if (!masterData.cplex.solve() || masterData.cplex.getStatus() != IloCplex.Status.Optimal) {
				if (masterData.cplex.getCplexStatus() == IloCplex.CplexStatus.AbortTimeLim)
					throw new TimeLimitExceededException();
				else
					throw new RuntimeException("Master problem solve failed! Status: " + masterData.cplex.getStatus());
			} else {
				masterData.objectiveValue = masterData.cplex.getObjValue();

				// Nos guardamos la información de cubrimiento.
				for (Point p : dataModel.matriz.unos()) {
					//TODO: Ver esto con Javi
					IloRange range = constraints.get(p);		
					double cantRectQueCubre = masterData.cplex.getValue(range.getExpr());
					// nos fijamos si el resultado es fraccionario.
					if ((cantRectQueCubre > Math.floor(cantRectQueCubre) + TOLERANCIA)) {
						// si es así, tenemos ya el candidato para branchear.
						dataModel.MijConCubrimientoFrac = p;
						dataModel.valorCubrimientoFrac = cantRectQueCubre;

						if (this.constraintRanges.containsKey(p))
							dataModel.intActual = this.constraintRanges.get(p).clonar();
						else
							dataModel.intActual = new Interval(1, Integer.MAX_VALUE);

						break;
					}
				}
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Le pasamos las variables duales al problema de pricing.
	 * 
	 * @param pricingProblem
	 *            pricing problem
	 */
	@Override
	public void initializePricingProblem(RPCPricingProblem pricingProblem) {
		try {

			double[] duales = new double[dataModel.matriz.cantUnos()];

			int i = 0;
			for (Point p : dataModel.matriz.unos())
				duales[i++] = masterData.cplex.getDual(constraints.get(p));

			// le pasamos los duales al pricing.
			pricingProblem.initPricingProblem(duales);

		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Agregamos una columna al problema master.
	 * 
	 * @param column
	 * 
	 */
	@Override
	public void addColumn(RPCColumn column) {
		try {

			// Registramos la nueva columna con la función objetivo
			IloColumn iloColumn = masterData.cplex.column(obj, 1.0);

			// Registramos la nueva columna con los constraints
			// Esto es, tiene que figurar en cada constraint correspondiente a cada uno de
			// los unos.
			for (int f = column.rectangle.y; f < column.rectangle.y + column.rectangle.height; f++)
				for (int c = column.rectangle.x; c < column.rectangle.x + column.rectangle.width; c++)
					iloColumn = iloColumn.and(masterData.cplex.column(constraints.get(new Point(c, f)), 1.0));

			// Creamos la variable para esa columna, y la guardamos
			IloNumVar var = masterData.cplex.numVar(iloColumn, 0, Double.MAX_VALUE, column.toString());
			masterData.cplex.add(var);
			masterData.addColumn(column, var);

			vars.add(var);
		} catch (IloException e) {
		}
	}

	/**
	 * Obtiene la solución del master.
	 * 
	 * @return Devuelve todas las columnas distintas de 0
	 */
	@Override
	public List<RPCColumn> getSolution() {
		List<RPCColumn> solution = new ArrayList<>();
		try {
			RPCColumn[] colsEnLaSolucion = masterData.getColumnsForPricingProblemAsList()
					.toArray(new RPCColumn[masterData.getNrColumns()]);
			IloNumVar[] vars = masterData.getVarMap().getValuesAsArray(new IloNumVar[masterData.getNrColumns()]);
			double[] values = masterData.cplex.getValues(vars);
			for (int i = 0; i < colsEnLaSolucion.length; i++) {
				colsEnLaSolucion[i].value = values[i];
				if (values[i] >= config.PRECISION)
					solution.add(colsEnLaSolucion[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return solution;
	}

	/**
	 * Solución por consola.
	 */
	@Override
	public void printSolution() {
		List<RPCColumn> solution = this.getSolution();
		for (RPCColumn is : solution)
			System.out.println(is);
	}

	/**
	 * Cierra el problema master.
	 */
	@Override
	public void close() {
		masterData.cplex.end();
	}

	/**
	 * Listener para cuando se realizan las decisiones de branching
	 * 
	 * @param bd
	 *            Branching decision
	 */
	@Override
	public void branchingDecisionPerformed(@SuppressWarnings("rawtypes") BranchingDecision bd) {

		RPCBranchingDecision decision = (RPCBranchingDecision) bd;

		Point p = decision.getPoint();

		this.constraintRanges.put(p, new Interval(decision.getInterval().lower, decision.getInterval().upper));

		// // acá cerramos el modelo de cplex y lo creamos otra vez.
		this.close();
		masterData = this.buildModel();
	}

	/**
	 * Listener para cuando se backtrackean las decisiones de branching.
	 * 
	 * @param bd
	 *            Branching decision
	 */
	@Override
	public void branchingDecisionReversed(@SuppressWarnings("rawtypes") BranchingDecision bd) {

		RPCBranchingDecision decision = (RPCBranchingDecision) bd;

		Point p = decision.getPoint();

		this.constraintRanges.put(p, decision.getPrevInterval());

		// // acá cerramos el modelo de cplex y lo creamos otra vez.
		this.close();
		masterData = this.buildModel();
	}
}
