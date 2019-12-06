package rpc.modelos;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.jorlib.frameworks.columnGeneration.io.TimeLimitExceededException;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;
import rpc.branch.and.price.Matriz;

public final class ModeloMaximal {

	/*** Instancia de cplex **/
	protected IloCplex cplex;

	protected Matriz matriz;
	protected double precision;
	protected double objective;

	protected long timeMillis;

	protected InfoResolucion info = new InfoResolucion();

	private IloNumVar[][] x;
	private Point p;

	public ModeloMaximal(Matriz matrix, Point p, OutputStream out) throws Exception {
		this.matriz = matrix;

		this.p = p;

		cplex = new IloCplex();
		cplex.setParam(IloCplex.IntParam.AdvInd, 0);
		cplex.setParam(IloCplex.IntParam.Threads, 1);
		// if (out != null)
		cplex.setOut(out);

		CPLEXInfoCallBack infocallback = new CPLEXInfoCallBack(info);
		cplex.use(infocallback);
	}

	public void buildModel() throws Exception {

		int filas = matriz.filas();
		int cols = matriz.columnas();

		// inicializo las variables.
		x = new IloNumVar[filas][cols];

		for (int f = 0; f < matriz.filas(); f++)
			for (int c = 0; c < matriz.columnas(); c++)
				x[f][c] = cplex.boolVar("x[" + f + "," + c + "]");

		// # Maximizar el tam del rectangulo
		// maximize fobj: sum <i, j> in R*C: x[i, j];

		IloNumExpr fobj = cplex.linearIntExpr();
		for (int f = 0; f < matriz.filas(); f++)
			for (int c = 0; c < matriz.columnas(); c++)
				fobj = cplex.sum(fobj, cplex.prod(1.0, x[f][c]));

		cplex.addMaximize(fobj);

		// subto rect3: forall <i, i1, i2, j, j1, j2> in R*R*R*C*C*C with i <= i1 and j
		// <= j1 and i <= i2 and i2 <= i1 and j <= j2 and j2 <= j1:
		// x[i, j] + x[i1, j1] <= 1 + x[i2, j2];
		for (int i = 0; i < matriz.filas(); i++)
			for (int i1 = i; i1 < matriz.filas(); i1++)
				for (int i2 = i; i2 <= i1; i2++)
					for (int j = 0; j < matriz.columnas(); j++)
						for (int j1 = j; j1 < matriz.columnas(); j1++)
							for (int j2 = j; j2 <= j1; j2++) {
								IloNumExpr res = cplex.linearIntExpr();
								res = cplex.sum(res, cplex.prod(1.0, x[i2][j2]));
								res = cplex.sum(res, cplex.prod(-1.0, x[i][j]));
								res = cplex.sum(res, cplex.prod(-1.0, x[i1][j1]));
								cplex.addLe(-1.0, res);
							}

		// subto rect4: forall <i, i1, i2, j, j1, j2> in R*R*R*C*C*C with i <= i1 and j1
		// <= j and i <= i2 and i2 <= i1 and j1 <= j2 and j2 <= j:
		// x[i, j] + x[i1, j1] <= 1 + x[i2, j2];
		for (int i = 0; i < matriz.filas(); i++)
			for (int i1 = i; i1 < matriz.filas(); i1++)
				for (int i2 = i; i2 <= i1; i2++)
					for (int j = 0; j < matriz.columnas(); j++)
						for (int j1 = 0; j1 <= j; j1++)
							for (int j2 = j1; j2 <= j; j2++) {
								IloNumExpr res = cplex.linearIntExpr();
								res = cplex.sum(res, cplex.prod(1.0, x[i2][j2]));
								res = cplex.sum(res, cplex.prod(-1.0, x[i][j]));
								res = cplex.sum(res, cplex.prod(-1.0, x[i1][j1]));
								cplex.addLe(-1.0, res);
							}

		// subto puntoCubierto:
		// x[pr, pc] == 1;_
//
//		IloNumExpr resP = cplex.linearIntExpr();
//		resP = cplex.sum(resP, cplex.prod(1.0, x[p.y][p.x]));
//		cplex.addEq(1.0, resP);
//
		// subto ningun0Cubierto: forall <i,j> in R*C with M[i, j] == 0:
		// 0 == x[i, j];

		for (int f = 0; f < matriz.filas(); f++)
			for (int c = 0; c < matriz.columnas(); c++)
				if (!matriz.get(f, c)) {
					IloNumExpr res = cplex.linearIntExpr();
					res = cplex.sum(res, cplex.prod(1.0, x[f][c]));
					cplex.addEq(0.0, res);
				}
	}

	public Rectangle getSolution() throws UnknownObjectException, IloException {

		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		for (int f = 0; f < matriz.filas(); f++)
			for (int c = 0; c < matriz.columnas(); c++)
				if (cplex.getValue(x[f][c]) > precision) {
					minX = Integer.min(minX, c);
					minY = Integer.min(minY, f);
					maxX = Integer.max(maxX, c);
					maxY = Integer.max(maxY, f);
				}

		if (minX == Integer.MAX_VALUE || minY == Integer.MAX_VALUE || maxX == Integer.MIN_VALUE
				|| maxY == Integer.MIN_VALUE)
			throw new RuntimeException("Error en la solución");

		return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
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
			timeMillis = System.currentTimeMillis();
			// Resolvemos el problema
			boolean resuelto = cplex.solve();
			timeMillis = System.currentTimeMillis() - timeMillis;

			if (!resuelto || cplex.getStatus() != IloCplex.Status.Optimal) {
				this.objective = Double.MIN_VALUE;
				return false;
			} else { // Encontramos un óptimo
				this.objective = cplex.getObjValue();
				return true;
			}
		} catch (IloException e) {
			timeMillis = System.currentTimeMillis() - timeMillis;
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Cerrar el problema de pricing.
	 */
	public void close() {
		cplex.end();
	}


}
