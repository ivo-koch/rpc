package rpc.modelos;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jorlib.frameworks.columnGeneration.io.TimeLimitExceededException;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;
import rpc.branch.and.price.MatrizComprimida;

public final class ModeloR {

	private IloNumVar[] r;

	private MatrizComprimida matriz;
	/*** Instancia de cplex **/
	protected IloCplex cplex;
	protected double precision;
	protected double objective;

	public ModeloR(MatrizComprimida matrix, double precision) throws Exception {
		this.matriz = matrix;
		this.precision = precision;

		cplex = new IloCplex();
		cplex.setParam(IloCplex.IntParam.AdvInd, 0);
		cplex.setParam(IloCplex.IntParam.Threads, 1);
		cplex.setOut(null);
	}

	private Map<Rectangle, IloNumVar> variables = new HashMap<Rectangle, IloNumVar>();

	public void buildModel() throws Exception {

		List<Rectangle> rectangulos = matriz.getRectangulosOriginales();

		r = new IloNumVar[rectangulos.size()];

		int i = 0;
		for (Rectangle rect : rectangulos) {
			r[i] = cplex.boolVar("r[" + rect + "]");
			variables.put(rect, r[i]);
			i++;
		}

		// función objetivo1 ok
		IloNumExpr fobj = cplex.linearIntExpr();

		for (int j = 0; j < rectangulos.size(); j++)
			fobj = cplex.sum(fobj, r[j]);

		cplex.addMinimize(fobj);

		// restricciones
		for (Rectangle uno : matriz.getUnos()) {
			Set<Rectangle> rectQueLoContienen = matriz.getUnosEnRectangulos().get(uno);
			IloNumExpr rest = cplex.linearIntExpr();
			for (Rectangle rect : rectQueLoContienen)
				rest = cplex.sum(rest, variables.get(rect));
			cplex.addLe(1.0, rest);
		}

		cplex.exportModel("modelor.lp");
	}

	/**
	 * Método principal que resuelve el problema de pricing.
	 * 
	 * @return List of columns (independent sets) with negative reduced cost.
	 * @throws TimeLimitExceededException
	 *             TimeLimitExceededException
	 */
	public boolean solve() throws TimeLimitExceededException {

		try {

			return cplex.solve();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public SolucionModeloR getSolution() throws UnknownObjectException, IloException {

		List<Rectangle> res = new ArrayList<Rectangle>();

		for (Rectangle rect : matriz.getRectangulosOriginales())
			if (cplex.getValue(variables.get(rect)) > precision)
				res.add(rect);

		if (res.isEmpty())
			throw new RuntimeException("Solucion vacia");

		return new SolucionModeloR(matriz, res);
	}

	/**
	 * Cerrar el problema de pricing.
	 */
	public void close() {
		cplex.end();
	}

}
