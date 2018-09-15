package rpc.branch.and.price;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.jorlib.frameworks.columnGeneration.branchAndPrice.branchingDecisions.BranchingDecision;
import org.jorlib.frameworks.columnGeneration.io.TimeLimitExceededException;
import org.jorlib.frameworks.columnGeneration.pricing.AbstractPricingProblemSolver;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.cplex.IloCplex;

/***
 * Implementación de solución exacta para el problema de pricing para un V0 de
 * varios vértices.
 */
public final class RPCMIPPricingSolver
		extends AbstractPricingProblemSolver<RPCDataModel, RPCColumn, RPCPricingProblem> {

	/*** Instancia de cplex **/
	private IloCplex cplex;
	/** Funcion objetivo */
	private IloObjective obj;

	/*** Variables del modelo **/
	private IloNumVar[][] left;
	private IloNumVar[][] up;
	private IloNumVar[][] diag;
	private IloNumVar[][] start;

	/** Mantenemos acá las variables de la función objetivo */
	IloNumVar[] varsEnFobj;

	/***
	 * Crea un nuevo solver de pricing.
	 * 
	 * @param dataModel
	 * @param pricingProblem
	 */
	public RPCMIPPricingSolver(RPCDataModel dataModel, RPCPricingProblem pricingProblem) {
		super(dataModel, pricingProblem);
		this.name = "ExactPricingProblemSolver";
		this.buildModel();
	}

	/**
	 * Construye el problema de pricing: en nuestro caso, encontrar un árbol
	 * generador de mínimo peso, con pesos en los vértices.
	 */
	private void buildModel() {
		try {
			cplex = new IloCplex();
			cplex.setParam(IloCplex.IntParam.AdvInd, 0);
			cplex.setParam(IloCplex.IntParam.Threads, 1);
			cplex.setOut(null);

			// variable x
			left = new IloNumVar[dataModel.matriz.filas()][dataModel.matriz.columnas()];
			up = new IloNumVar[dataModel.matriz.filas()][dataModel.matriz.columnas()];
			diag = new IloNumVar[dataModel.matriz.filas()][dataModel.matriz.columnas()];
			start = new IloNumVar[dataModel.matriz.filas()][dataModel.matriz.columnas()];

			for (int f = 0; f < dataModel.matriz.filas(); f++)
				for (int c = 0; c < dataModel.matriz.columnas(); c++) {
					left[f][c]  = cplex.boolVar("left[" + f + "," + c + "]");
					up[f][c]  = cplex.boolVar("up[" + f + "," + c + "]");
					diag[f][c]  = cplex.boolVar("start[" + f + "," + c + "]");
					start[f][c]  = cplex.boolVar("diag[" + f + "," + c + "]");
				}
			// función objetivo vacía
			// vamos a dar la expresión de la f.obj. en el método setObjective()
			obj = cplex.addMaximize();

			// constraints

			// forall <i,j> in RAmp*CAmp:
			// start[i,j] + left[i, j] + up[i, j] + diag[i,j]) <= M[i, j];
			for (int f = 0; f < dataModel.matriz.filas(); f++)
				for (int c = 0; c < dataModel.matriz.columnas(); c++) {
					IloNumExpr lhs = cplex.linearIntExpr();
					lhs = cplex.sum(lhs, cplex.prod(1.0, start[f][c]));
					lhs = cplex.sum(lhs, cplex.prod(1.0, left[f][c]));
					lhs = cplex.sum(lhs, cplex.prod(1.0, up[f][c]));
					lhs = cplex.sum(lhs, cplex.prod(1.0, diag[f][c]));

					cplex.addLe(lhs, dataModel.matriz.get(f, c) ? 1.0 : 0.0, "Const1");
				}

			// sum <i1, j1> in R*C:start[i1, j1] == 1;
			IloNumExpr lhsC2 = cplex.linearIntExpr();
			for (int f = 0; f < dataModel.matriz.filas(); f++)
				for (int c = 0; c < dataModel.matriz.columnas(); c++)
					lhsC2 = cplex.sum(lhsC2, cplex.prod(1.0, start[f][c]));
				
			cplex.addEq(lhsC2, 1, "Const2");

			// forall <i1, j1> in R*C:
			// forall <i2, j2> in R*C with i1 < i2 and j2 < j1:
			// left[i1, j1] + up[i2, j2] <= diag[i2, j1] + 1;
			for (int f = 0; f < dataModel.matriz.filas() - 1; f++)
				for (int c = 0; c < dataModel.matriz.columnas() - 1; c++)
					for (int f1 = f + 1; f1 < dataModel.matriz.filas(); f1++)
						for (int c1 = c + 1; c1 < dataModel.matriz.columnas(); c1++) {
							IloNumExpr lhs = cplex.linearIntExpr();
							lhs = cplex.sum(lhs, cplex.prod(1.0, left[f][c1]));
							lhs = cplex.sum(lhs, cplex.prod(1.0, up[f1][c]));
							lhs = cplex.sum(lhs, cplex.prod(-1.0, diag[f1][c1]));
							cplex.addLe(lhs, 1, "Const3");
						}

			// forall <i,j> in R*CAmp:
			// up[i, j] <= up[i-1,j] + start[i-1, j];
			for (int f = 1; f < dataModel.matriz.filas(); f++)
				for (int c = 0; c < dataModel.matriz.columnas(); c++) {
					IloNumExpr lhs = cplex.linearIntExpr();
					lhs = cplex.sum(lhs, cplex.prod(1.0, up[f][c]));
					lhs = cplex.sum(lhs, cplex.prod(-1.0, up[f - 1][c]));
					lhs = cplex.sum(lhs, cplex.prod(-1.0, start[f - 1][c]));
					cplex.addLe(lhs, 0, "Const4");
				}

			// forall <i,j> in RAmp*C:
			// left[i, j] <= left[i,j-1] + start[i, j-1];
			for (int f = 0; f < dataModel.matriz.filas(); f++)
				for (int c = 1; c < dataModel.matriz.columnas(); c++) {
					IloNumExpr lhs = cplex.linearIntExpr();
					lhs = cplex.sum(lhs, cplex.prod(1.0, left[f][c]));
					lhs = cplex.sum(lhs, cplex.prod(-1.0, left[f][c - 1]));
					lhs = cplex.sum(lhs, cplex.prod(-1.0, start[f][c - 1]));
					cplex.addLe(lhs, 0, "Const4");
				}

			// forall <i,j> in R*C:
			// diag[i, j] <= diag[i, j-1] + up[i, j-1];
			for (int f = 0; f < dataModel.matriz.filas(); f++)
				for (int c = 1; c < dataModel.matriz.columnas(); c++) {
					IloNumExpr lhs = cplex.linearIntExpr();
					lhs = cplex.sum(lhs, cplex.prod(1.0, diag[f][c]));
					lhs = cplex.sum(lhs, cplex.prod(-1.0, diag[f][c - 1]));
					lhs = cplex.sum(lhs, cplex.prod(-1.0, up[f][c - 1]));
					cplex.addLe(lhs, 0, "Const5");
				}

			// forall <i,j> in R*C:
			// diag[i, j] <= diag[i-1, j-1] + left[i-1, j-1] + up[i-1, j-1] + start[i-1,
			// j-1];
			for (int f = 1; f < dataModel.matriz.filas(); f++)
				for (int c = 1; c < dataModel.matriz.columnas(); c++) {
					IloNumExpr lhs = cplex.linearIntExpr();
					lhs = cplex.sum(lhs, cplex.prod(1.0, diag[f][c]));
					lhs = cplex.sum(lhs, cplex.prod(-1.0, diag[f - 1][c - 1]));
					lhs = cplex.sum(lhs, cplex.prod(-1.0, left[f - 1][c - 1]));
					lhs = cplex.sum(lhs, cplex.prod(-1.0, up[f - 1][c - 1]));
					lhs = cplex.sum(lhs, cplex.prod(-1.0, start[f - 1][c - 1]));
					cplex.addLe(lhs, 0, "Const6");
				}

			// forall <i,j> in R*C:
			// diag[i, j] <= diag[i-1, j] + left[i-1, j];
			for (int f = 1; f < dataModel.matriz.filas(); f++)
				for (int c = 0; c < dataModel.matriz.columnas(); c++) {
					IloNumExpr lhs = cplex.linearIntExpr();
					lhs = cplex.sum(lhs, cplex.prod(1.0, diag[f][c]));
					lhs = cplex.sum(lhs, cplex.prod(-1.0, diag[f - 1][c]));
					lhs = cplex.sum(lhs, cplex.prod(-1.0, left[f - 1][c]));
					cplex.addLe(lhs, 0, "Const7");
				}

		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	private int activacion = 1;

	/**
	 * Método principal que resuelve el problema de pricing.
	 * 
	 * @return List of columns (independent sets) with negative reduced cost.
	 * @throws TimeLimitExceededException
	 *             TimeLimitExceededException
	 */
	@Override
	public List<RPCColumn> generateNewColumns() throws TimeLimitExceededException {
		List<RPCColumn> newPatterns = new ArrayList<>();
		try {

			logger.debug("Resolviendo exacto...");
			Estadisticas.llamadasExacto++;

			if (config.EXPORT_MODEL)
				cplex.exportModel("pricingProblem" + activacion + ".lp");
			activacion++;
			logger.debug("Resolviendo pricing...");
			// Resolvemos el problema
			if (!cplex.solve() || cplex.getStatus() != IloCplex.Status.Optimal) {
				if (cplex.getCplexStatus() == IloCplex.CplexStatus.AbortTimeLim) {
					throw new TimeLimitExceededException();
				} else if (cplex.getStatus() == IloCplex.Status.Infeasible) {

					pricingProblemInfeasible = true;
					this.objective = Double.MIN_VALUE;
					throw new RuntimeException("Pricing problem infeasible");
				} else {
					throw new RuntimeException("Pricing problem solve failed! Status: " + cplex.getStatus());
				}
			} else { // Encontramos un óptimo
				logger.debug("Pricing resuelto");
				this.pricingProblemInfeasible = false;
				this.objective = cplex.getObjValue();

				// podemos agregar el resultado a la base?
				if (objective > 1 - config.PRECISION) { // - config.PRECISION) {
					// SI
					// si es así, agregamos una nueva columna representada por este rectángulo
					// a la base
					int topX = -1;
					int topY = -1;

					int bottomRightX = -1;
					int bottomRightY = -1;
					boolean found = false;
					for (int f = 0; f < dataModel.matriz.filas(); f++)
						for (int c = 0; c < dataModel.matriz.columnas(); c++) {
							if (cplex.getValue(start[f][c]) >= 1 - config.PRECISION) {
								topX = c;
								topY = f;
								found = true;
							}
							if (found && (cplex.getValue(left[f][c]) >= 1 - config.PRECISION
									|| cplex.getValue(up[f][c]) >= 1 - config.PRECISION
									|| cplex.getValue(diag[f][c]) >= 1 - config.PRECISION)) {

								bottomRightX = c;
								bottomRightY = f;
							}

						}

					if (!found)
						throw new RuntimeException("No encontramos rectángulo en la solución!");

					logger.debug("Obj: " + objective);
					// logger.debug(dumpModel());

					Rectangle encontrado  = new Rectangle(topX, topY, bottomRightX - topX + 1, bottomRightY - topY + 1);
					RPCColumn columna = new RPCColumn(pricingProblem, false, this.getName(),
							dataModel.matriz.buildMaximal(encontrado));
					newPatterns.add(columna);
					Estadisticas.columnasExacto++;

				}
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
		return newPatterns;
	}

	/**
	 * Actualizamos la función objetivo del problema con la nueva solución dual que
	 * viene del master.
	 */
	@Override
	public void setObjective() {

		try {

			// maximize fobj: sum <i, j> in R*C: (start[i, j] + diag[i, j] + up[i, j] +
			// left[i, j])*X0[i, j];

			IloNumExpr fobjExpr = cplex.linearIntExpr();
			int i = 0;
			for (Point p : dataModel.matriz.unos()) {

				double x0 = pricingProblem.dualCosts[i];

				cplex.sum(fobjExpr, cplex.prod(x0, start[p.y][p.x]));
				cplex.sum(fobjExpr, cplex.prod(x0, diag[p.y][p.x]));
				cplex.sum(fobjExpr, cplex.prod(x0, up[p.y][p.x]));
				cplex.sum(fobjExpr, cplex.prod(x0, left[p.y][p.x]));
				i++;
			}

			obj.setExpr(fobjExpr);
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Cerrar el problema de pricing.
	 */
	@Override
	public void close() {
		cplex.end();
	}

	/**
	 * Aplicamos esta decisión de branching al problema
	 * 
	 * 
	 * @param bd
	 *            BranchingDecision
	 */
	@Override
	public void branchingDecisionPerformed(@SuppressWarnings("rawtypes") BranchingDecision bd) {
	}

	/**
	 * Volvemos atrás la decisión de branchear (cuando hacemos el backtracking)
	 * 
	 * 
	 * @param bd
	 *            BranchingDecision
	 */
	@Override
	public void branchingDecisionReversed(@SuppressWarnings("rawtypes") BranchingDecision bd) {
	}
}
