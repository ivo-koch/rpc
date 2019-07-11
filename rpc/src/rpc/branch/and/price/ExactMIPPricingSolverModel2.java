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
public final class ExactMIPPricingSolverModel2 {

	/*** Instancia de cplex **/
	private IloCplex cplex;
	/** Funcion objetivo */
	private IloObjective obj;

	/*** Variables del modelo **/
	private IloNumVar[][] x;
	private IloNumVar[] col;
	private IloNumVar[] row;

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
	public ExactMIPPricingSolverModel2(Matriz matrix, Logger logger, double precision) {
		this.matriz = matrix;
		this.logger = logger;
		this.precision = precision;
		this.buildModel();
	}

	/**
	 * Construye el problema de pricing: en nuestro caso, encontrar un árbol
	 * generador de mínimo peso, con pesos en los vértices.
	 */
	public void buildModel() {
		try {
			cplex = new IloCplex();
			cplex.setParam(IloCplex.IntParam.AdvInd, 0);
			cplex.setParam(IloCplex.IntParam.Threads, 1);
			cplex.setOut(null);

			// variable x
			x = new IloNumVar[matriz.filas()][matriz.columnas()];

			for (int f = 0; f < matriz.filas(); f++)
				for (int c = 0; c < matriz.columnas(); c++)
					x[f][c] = cplex.boolVar("x[" + f + "," + c + "]");

			// variable col
			col = new IloNumVar[matriz.columnas()];

			for (int c = 0; c < matriz.columnas(); c++)
				col[c] = cplex.boolVar("c[" + c + "]");

			// variable row
			row = new IloNumVar[matriz.filas()];

			for (int f = 0; f < matriz.filas(); f++)
				row[f] = cplex.boolVar("r[" + f + "]");

			// función objetivo vacía
			// vamos a dar la expresión de la f.obj. en el método setObjective()
			obj = cplex.addMaximize();

			/*
			 * # Los unos en cada fila deben ser consecutivos subto consecutivosfila: forall
			 * <j,t> in C*C with j>2 and t<j-1: c[j] <= c[j-1] + (1 - c[t]);
			 */
			for (int j = 2; j < matriz.columnas(); j++)
				for (int t = 0; t < matriz.columnas() - 1; t++) {
					IloNumExpr lhs = cplex.linearIntExpr();
					lhs = cplex.sum(lhs, cplex.prod(1.0, col[j]));
					lhs = cplex.sum(lhs, cplex.prod(-1.0, col[j - 1]));
					lhs = cplex.sum(lhs, cplex.prod(1.0, col[t]));
					cplex.addLe(lhs, 1.0, "Const1");
				}

			/*
			 * # Los unos en cada columna deben ser consecutivos subto consecutivoscolumna:
			 * forall <i, t> in R*R with i>2 and t<i-1: r[i] <= r[i-1] + (1 - r[t]);
			 */
			for (int i = 2; i < matriz.filas(); i++)
				for (int t = 0; t < matriz.filas() - 1; t++) {
					IloNumExpr lhs = cplex.linearIntExpr();
					lhs = cplex.sum(lhs, cplex.prod(1.0, row[i]));
					lhs = cplex.sum(lhs, cplex.prod(-1.0, row[i - 1]));
					lhs = cplex.sum(lhs, cplex.prod(1.0, row[t]));
					cplex.addLe(lhs, 1.0, "Const1");
				}
			/*
			 * # x[k,i,j] = 1 solamente si el rectangulo k incluye la fila i y la columna j
			 * subto defx: forall <i,j> in R*C: x[i,j] >= r[i] + c[j] - 1;
			 */
			for (int i = 0; i < matriz.filas(); i++)
				for (int j = 0; j < matriz.columnas(); j++) {
					IloNumExpr lhs = cplex.linearIntExpr();
					lhs = cplex.sum(lhs, cplex.prod(-1.0, x[i][j]));
					lhs = cplex.sum(lhs, cplex.prod(1.0, row[i]));
					lhs = cplex.sum(lhs, cplex.prod(1.0, col[j]));
					cplex.addLe(lhs, 1.0, "Const1");
				}

			/*
			 * # x[i,j] = 0 si no incluye la fila i subto defxfila: forall <i,j> in R*C:
			 * x[i,j] <= r[i];
			 */
			for (int i = 0; i < matriz.filas(); i++)
				for (int j = 0; j < matriz.columnas(); j++) {
					IloNumExpr lhs = cplex.linearIntExpr();
					lhs = cplex.sum(lhs, cplex.prod(1.0, x[i][j]));
					lhs = cplex.sum(lhs, cplex.prod(-1.0, row[i]));
					cplex.addLe(lhs, 0.0, "Const1");
				}

			/*
			 * # x[i,j] = 0 si el rectangulo no incluye la columna j subto defxcolumna:
			 * forall <i,j> in R*C: x[i,j] <= c[j];
			 */
			for (int i = 0; i < matriz.filas(); i++)
				for (int j = 0; j < matriz.columnas(); j++) {
					IloNumExpr lhs = cplex.linearIntExpr();
					lhs = cplex.sum(lhs, cplex.prod(1.0, x[i][j]));
					lhs = cplex.sum(lhs, cplex.prod(-1.0, col[j]));
					cplex.addLe(lhs, 0.0, "Const1");
				}

			/*
			 * # Ningun punto con M[i,j] = 0 debe estar cubierto subto nocubiertos: forall
			 * <i,j> in R*C: x[i,j] <= M[i, j];
			 */
			for (int i = 0; i < matriz.filas(); i++)
				for (int j = 0; j < matriz.columnas(); j++) {
					IloNumExpr lhs = cplex.linearIntExpr();
					lhs = cplex.sum(lhs, cplex.prod(1.0, x[i][j]));
					cplex.addLe(lhs, matriz.get(i, j) ? -1 : 0, "Const1");
				}

			/*
			 * subto maxim1: forall <j> in C with j > 1: c[j] - c[j - 1] <= sum <i> in R:
			 * x[i, j] - sum <i> in R: x[i, j] * M[i, j - 1];
			 */
			for (int j = 1; j < matriz.columnas(); j++) {
				IloNumExpr lhs = cplex.linearIntExpr();
				lhs = cplex.sum(lhs, cplex.prod(1.0, col[j]));
				lhs = cplex.sum(lhs, cplex.prod(-1.0, col[j - 1]));

				for (int i = 0; i < matriz.filas(); i++)
					lhs = cplex.sum(lhs, cplex.prod(-1.0, x[i][j]));

				for (int i = 0; i < matriz.filas(); i++)
					lhs = cplex.sum(lhs, cplex.prod(matriz.get(i, j - 1) ? 1 : 0, x[i][j]));

				cplex.addLe(lhs, 0.0, "Const1");
			}

			/*
			 * subto maxim2: forall <j> in C with j < columnas: c[j] - c[j + 1] <= sum <i>
			 * in R: x[i, j] - sum <i> in R: x[i, j] * M[i, j + 1];
			 */
			for (int j = 0; j < matriz.columnas() - 1; j++) {
				IloNumExpr lhs = cplex.linearIntExpr();
				lhs = cplex.sum(lhs, cplex.prod(1.0, col[j]));
				lhs = cplex.sum(lhs, cplex.prod(-1.0, col[j + 1]));

				for (int i = 0; i < matriz.filas(); i++)
					lhs = cplex.sum(lhs, cplex.prod(-1.0, x[i][j]));

				for (int i = 0; i < matriz.filas(); i++)
					lhs = cplex.sum(lhs, cplex.prod(matriz.get(i, j + 1) ? 1 : 0, x[i][j]));

				cplex.addLe(lhs, 0.0, "Const1");
			}

			/*
			 * subto maxim3: forall <i> in R with i > 1: r[i] - r[i - 1] <= sum <j> in C:
			 * x[i, j] - sum <j> in C: x[i, j] * M[i - 1, j];
			 */
			for (int i = 1; i < matriz.filas(); i++) {
				IloNumExpr lhs = cplex.linearIntExpr();
				lhs = cplex.sum(lhs, cplex.prod(1.0, row[i]));
				lhs = cplex.sum(lhs, cplex.prod(-1.0, row[i - 1]));

				for (int j = 0; j < matriz.columnas(); j++)
					lhs = cplex.sum(lhs, cplex.prod(-1.0, x[i][j]));

				for (int j = 0; j < matriz.columnas(); j++)
					lhs = cplex.sum(lhs, cplex.prod(matriz.get(i - 1, j) ? 1 : 0, x[i][j]));

				cplex.addLe(lhs, 0.0, "Const1");
			}
			/*
			 * subto maxim4: forall <i> in R with i < filas: 
			 * r[i] - r[i + 1] <= sum <j> in C: x[i, j] - sum <j> in C: x[i, j] * M[i + 1, j];
			 */
			for (int i = 0; i < matriz.filas() - 1; i++) {
				IloNumExpr lhs = cplex.linearIntExpr();
				lhs = cplex.sum(lhs, cplex.prod(1.0, row[i]));
				lhs = cplex.sum(lhs, cplex.prod(-1.0, row[i + 1]));

				for (int j = 0; j < matriz.columnas(); j++)
					lhs = cplex.sum(lhs, cplex.prod(-1.0, x[i][j]));

				for (int j = 0; j < matriz.columnas(); j++)
					lhs = cplex.sum(lhs, cplex.prod(matriz.get(i + 1, j) ? 1 : 0, x[i][j]));

				cplex.addLe(lhs, 0.0, "Const1");
			}

		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Método principal que resuelve el problema de pricing.
	 * 
	 * @return List of columns (independent sets) with negative reduced cost.
	 * @throws TimeLimitExceededException
	 *             TimeLimitExceededException
	 */
	public boolean solve() throws TimeLimitExceededException, IloException {

		Estadisticas.llamadasExacto++;
		this.objective = Double.MIN_VALUE;

		logger.debug("Resolviendo pricing...");
		// Resolvemos el problema
		if (!cplex.solve() || cplex.getStatus() != IloCplex.Status.Optimal) {
			if (cplex.getCplexStatus() == IloCplex.CplexStatus.AbortTimeLim) {
				throw new TimeLimitExceededException();
			} else if (cplex.getStatus() == IloCplex.Status.Infeasible) {
				this.objective = Double.MIN_VALUE;
				return false;
			} else {
				throw new RuntimeException("Pricing problem solve failed! Status: " + cplex.getStatus());
			}
		} else { // Encontramos un óptimo
			logger.debug("Pricing resuelto");
			this.objective = cplex.getObjValue();

			return true;
		}
	}

	public Rectangle getColumn() throws UnknownObjectException, IloException {

		// SI
		// si es así, agregamos una nueva columna representada por este rectángulo
		// a la base
		int topX = -1;
		int topY = -1;

		int bottomRightX = -1;
		int bottomRightY = -1;
		boolean found = false;
		StringBuilder rectSol = new StringBuilder();
		rectSol.append("\n");

		for (int f = 0; f < matriz.filas(); f++) {
			for (int c = 0; c < matriz.columnas(); c++) {
				if (cplex.getValue(x[f][c]) >= 1 - precision) {
					topX = c;
					topY = f;
				}
				if (cplex.getValue(x[f][c]) >= 1 - precision) {
					bottomRightX = c;
					bottomRightY = f;
				}
				if (cplex.getValue(x[f][c]) >= 1 - precision) {
					rectSol.append("1* ");
					found = true;
				} else if (matriz.get(f, c))
					rectSol.append("1  ");
				else
					rectSol.append("0  ");
			}
			rectSol.append("\n");
		}

		if (!found)
			throw new RuntimeException("No encontramos rectángulo en la solución!");

		logger.debug(rectSol.toString());
		logger.debug("Obj: " + objective);
		// logger.debug(dumpModel());

		Rectangle encontrado = new Rectangle(topX, topY, bottomRightX - topX + 1, bottomRightY - topY + 1);

		Estadisticas.columnasExacto++;
		return encontrado;

	}

	/**
	 * Actualizamos la función objetivo del problema con la nueva solución dual que
	 * viene del master.
	 */

	public void setObjective(double[] fobjCoef) {

		try {

			logger.debug("Duals: " + Arrays.toString(fobjCoef));
			IloNumExpr fobjExpr = cplex.linearIntExpr();
			int i = 0;
			for (Point p : matriz.unos()) {
				double x0 = fobjCoef[i++];
				fobjExpr = cplex.sum(fobjExpr, cplex.prod(x0, x[p.y][p.x]));
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
	public void close() {
		cplex.end();
	}

	public double getObjective() {
		return objective;
	}

	public double getPrecision() {
		return precision;
	}
}
