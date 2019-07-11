package rpc.branch.and.price;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;

import org.jorlib.frameworks.columnGeneration.io.TimeLimitExceededException;
import org.slf4j.Logger;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

/***
 * Implementación de solución exacta para el problema de pricing para un V0 de
 * varios vértices.
 */
public final class HeuristicMIPPricingSolver {


	private Matriz matriz;	
	private Logger logger;
	private double precision;
	private double objective;

	/** Mantenemos acá las variables de la función objetivo */
	IloNumVar[] varsEnFobj;

	/***
	 * Crea un nuevo solver de pricing.
	 * 
	 * @param dataModel
	 * @param pricingProblem
	 */
	public HeuristicMIPPricingSolver(Matriz matrix, Logger logger, double precision) {
		this.matriz = matrix;
		this.logger = logger;
		this.precision = precision;
		//this.buildModel();
	}

//	/**
//	 * Construye el problema de pricing: en nuestro caso, encontrar un árbol
//	 * generador de mínimo peso, con pesos en los vértices.
//	 */
//	public void buildModel() {
//		try {
//			cplex = new IloCplex();
//			cplex.setParam(IloCplex.IntParam.AdvInd, 0);
//			cplex.setParam(IloCplex.IntParam.Threads, 1);
//			cplex.setOut(null);
//
//			// variable x
//			a = new IloNumVar[matriz.filas()][matriz.columnas()];
//			b = new IloNumVar[matriz.filas()][matriz.columnas()];
//			x = new IloNumVar[matriz.filas()][matriz.columnas()];
//
//			for (int f = 0; f < matriz.filas(); f++)
//				for (int c = 0; c < matriz.columnas(); c++) {
//					a[f][c] = cplex.boolVar("a[" + f + "," + c + "]");
//					b[f][c] = cplex.boolVar("b[" + f + "," + c + "]");
//					x[f][c] = cplex.boolVar("x[" + f + "," + c + "]");
//				}
//			// función objetivo vacía
//			// vamos a dar la expresión de la f.obj. en el método setObjective()
//			obj = cplex.addMaximize();
//
//			// constraints
//
//			// subto posicionDeTopVertex: forall <i,j> in R*C:
//			// 2*x[i,j] <= sum <i1, j1> in R*C with i1 <= i and j1 <= j: a[i1, j1] + sum
//			// <i1, j1> in R*C with i1 >= i and j1 >= j : b[i1, j1];
//			for (int f = 0; f < matriz.filas(); f++)
//				for (int c = 0; c < matriz.columnas(); c++) {
//					IloNumExpr lhs = cplex.linearIntExpr();
//					lhs = cplex.sum(lhs, cplex.prod(2.0, x[f][c]));
//
//					for (int f1 = 0; f1 <= f; f1++)
//						for (int c1 = 0; c1 <= c; c1++)
//							lhs = cplex.sum(lhs, cplex.prod(-1.0, a[f1][c1]));
//
//					for (int f1 = f; f1 < matriz.filas(); f1++)
//						for (int c1 = c; c1 < matriz.columnas(); c1++)
//							lhs = cplex.sum(lhs, cplex.prod(-1.0, b[f1][c1]));
//
//					cplex.addLe(lhs, 0.0, "Const1");
//				}
//
//			// subto cerosEnRectangulos: forall <i,j> in R*C:
//			// 1 - x[i,j] <= 2 - (sum <i1, j1> in R*C with i1 <= i and j1 <= j: a[i1, j1]) -
//			// (sum <i1, j1> in R*C with i1 >= i and j1 >= j : b[i1, j1]);
//			for (int f = 0; f < matriz.filas(); f++)
//				for (int c = 0; c < matriz.columnas(); c++) {
//					IloNumExpr lhs = cplex.linearIntExpr();
//					lhs = cplex.sum(lhs, cplex.prod(-1.0, x[f][c]));
//
//					for (int f1 = 0; f1 <= f; f1++)
//						for (int c1 = 0; c1 <= c; c1++)
//							lhs = cplex.sum(lhs, cplex.prod(1.0, a[f1][c1]));
//
//					for (int f1 = f; f1 < matriz.filas(); f1++)
//						for (int c1 = c; c1 < matriz.columnas(); c1++)
//							lhs = cplex.sum(lhs, cplex.prod(1.0, b[f1][c1]));
//
//					cplex.addLe(lhs, 1.0, "Const2");
//				}
//
//			// subto unos: forall <i,j,k> in R*C:
//			// x[i,j] <= M[i,j];
//			for (int f = 0; f < matriz.filas(); f++)
//				for (int c = 0; c < matriz.columnas(); c++) {
//					IloNumExpr lhs = cplex.linearIntExpr();
//					lhs = cplex.sum(lhs, cplex.prod(1.0, x[f][c]));
//
//					cplex.addLe(lhs, matriz.get(f, c) ? 1.0 : 0.0, "Const3");
//				}
//
//			// subto unInicioRectanguloPorPosicion:
//			// sum <i, j> in R*C : a[i, j] == 1;
//			IloNumExpr lhsC4 = cplex.linearIntExpr();
//
//			for (int f = 0; f < matriz.filas(); f++)
//				for (int c = 0; c < matriz.columnas(); c++)
//					lhsC4 = cplex.sum(lhsC4, cplex.prod(1.0, a[f][c]));
//
//			cplex.addEq(lhsC4, 1.0, "Const4");
//
//			// subto unFinRectanguloPorPosicion:
//			// sum <i, j> in R*C : b[i, j] == 1;
//
//			IloNumExpr lhsC5 = cplex.linearIntExpr();
//
//			for (int f = 0; f < matriz.filas(); f++)
//				for (int c = 0; c < matriz.columnas(); c++)
//					lhsC5 = cplex.sum(lhsC5, cplex.prod(1.0, b[f][c]));
//
//			cplex.addEq(lhsC5, 1.0, "Const5");
//
//		} catch (IloException e) {
//			e.printStackTrace();
//		}
//	}
//
//	/**
//	 * Método principal que resuelve el problema de pricing.
//	 * 
//	 * @return List of columns (independent sets) with negative reduced cost.
//	 * @throws TimeLimitExceededException
//	 *             TimeLimitExceededException
//	 */
//	public boolean solve() throws TimeLimitExceededException, IloException {
//
//		Estadisticas.llamadasExacto++;
//		this.objective = Double.MIN_VALUE;
//
//		logger.debug("Resolviendo pricing...");
//		// Resolvemos el problema
//		if (!cplex.solve() || cplex.getStatus() != IloCplex.Status.Optimal) {
//			if (cplex.getCplexStatus() == IloCplex.CplexStatus.AbortTimeLim) {
//				throw new TimeLimitExceededException();
//			} else if (cplex.getStatus() == IloCplex.Status.Infeasible) {
//				this.objective = Double.MIN_VALUE;
//				return false;
//			} else {
//				throw new RuntimeException("Pricing problem solve failed! Status: " + cplex.getStatus());
//			}
//		} else { // Encontramos un óptimo
//			logger.debug("Pricing resuelto");
//			this.objective = cplex.getObjValue();
//
//			return true;
//		}
//	}
//
//	public Rectangle getColumn() throws UnknownObjectException, IloException {
//
//		// SI
//		// si es así, agregamos una nueva columna representada por este rectángulo
//		// a la base
//		int topX = -1;
//		int topY = -1;
//
//		int bottomRightX = -1;
//		int bottomRightY = -1;
//		boolean found = false;
//		StringBuilder rectSol = new StringBuilder();
//		rectSol.append("\n");
//
//		for (int f = 0; f < matriz.filas(); f++) {
//			for (int c = 0; c < matriz.columnas(); c++) {
//				if (cplex.getValue(a[f][c]) >= 1 - precision) {
//					topX = c;
//					topY = f;
//				}
//				if (cplex.getValue(b[f][c]) >= 1 - precision) {
//					bottomRightX = c;
//					bottomRightY = f;
//				}
//				if (cplex.getValue(x[f][c]) >= 1 - precision) {
//					rectSol.append("1* ");
//					found = true;
//				} else if (matriz.get(f, c))
//					rectSol.append("1  ");
//				else
//					rectSol.append("0  ");
//			}
//			rectSol.append("\n");
//		}
//
//		if (!found)
//			throw new RuntimeException("No encontramos rectángulo en la solución!");
//
//		logger.debug(rectSol.toString());
//		logger.debug("Obj: " + objective);
//		// logger.debug(dumpModel());
//
//		Rectangle encontrado = new Rectangle(topX, topY, bottomRightX - topX + 1, bottomRightY - topY + 1);
//
//		Estadisticas.columnasExacto++;
//		return encontrado;
//
//	}
//
//	/**
//	 * Actualizamos la función objetivo del problema con la nueva solución dual que
//	 * viene del master.
//	 */
//
//	public void setObjective(double[] fobjCoef) {
//
//		try {
//			
//			// maximize fobj: sum <i, j> in R*C: (start[i, j] + diag[i, j] + up[i, j] +
//			// left[i, j])*X0[i, j];
//			logger.debug("Duals: " + Arrays.toString(fobjCoef));
//			IloNumExpr fobjExpr = cplex.linearIntExpr();
//			int i = 0;
//			for (Point p : matriz.unos()) {
//
//				double x0 = fobjCoef[i];
//				if (x0 < 0)
//					throw new RuntimeException("Valor negativo como coef. de la solucion" + x0);
//				fobjExpr = cplex.sum(fobjExpr, cplex.prod(x0, x[p.y][p.x]));
//				i++;
//			}
//
//			obj.setExpr(fobjExpr);
//		} catch (IloException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	/**
//	 * Cerrar el problema de pricing.
//	 */
//	public void close() {
//		cplex.end();
//	}
//
//	public double getObjective() {
//		return objective;
//	}
//
//	public double getPrecision() {
//		return precision;
//	}
}
