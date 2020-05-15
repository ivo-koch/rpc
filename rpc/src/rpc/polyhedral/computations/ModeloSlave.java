package rpc.polyhedral.computations;

import java.util.Collections;
import java.util.List;

import org.jorlib.frameworks.columnGeneration.io.TimeLimitExceededException;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import rpc.modelos.CPLEXInfoCallBack;
import rpc.modelos.InfoResolucion;
import rpc.polyhedral.computations.Inequality.Termino;

public class ModeloSlave {

	private IloNumVar[][] x;

	private int filas;
	private int columnas;

	/*** Instancia de cplex **/
	private IloCplex cplex;

	private double[][] matrix;
	private double precision;
	private double objective;

	private long timeMillis = 0;

	private long timeLimit = 120000;
	// cota superior rectángulos.
	private InfoResolucion info = new InfoResolucion();

	public long cutsTh2;
	public long cutsTh3;
	public long cutsTh4;
	public long cutsTh5;
	public long cutsTh6;
	public long cutsTh7;
	public long cutsTh8;
	public long cutsTh9;
	private boolean modeloEntero = false;

	public ModeloSlave(double[][] matrix, boolean modeloEntero) throws Exception {
		this.modeloEntero = modeloEntero;
		this.matrix = matrix;

		cplex = new IloCplex();
		// if (out != null)
		cplex.setOut(null);

		CPLEXInfoCallBack infocallback = new CPLEXInfoCallBack(info, false);
		cplex.use(infocallback);

		this.filas = matrix.length;
		this.columnas = matrix[0].length;

	}

	public ModeloSlave(double[][] matrix) throws Exception {
		this.matrix = matrix;

		cplex = new IloCplex();
		// if (out != null)
		cplex.setOut(null);

		CPLEXInfoCallBack infocallback = new CPLEXInfoCallBack(info, false);
		cplex.use(infocallback);

		this.filas = matrix.length;
		this.columnas = matrix[0].length;
	}

	public void printSolution() throws Exception {

		for (int f = 0; f < filas; f++) {
			for (int c = 0; c < columnas; c++)
				System.out.print(cplex.getValue(x[f][c]) + " ");
			System.out.println();

		}
	}

	public void agregarDesigualdadesPaper() throws Exception {
		Thm2InequalityGenerator t2ig = new Thm2InequalityGenerator(filas, columnas);

		List<Inequality> ineqsT2 = t2ig.build();

		cutsTh2 = ineqsT2.size();

		// agregarIneqs(ineqsT2);

		// agregarIneqs(ineqsT2, 50000);

		Thm3InequalityGenerator t3ig = new Thm3InequalityGenerator(filas, columnas);

		List<Inequality> ineqsT3 = t3ig.build();

		cutsTh3 = ineqsT3.size();

		// agregarIneqs(ineqsT3);
		// agregarIneqs(ineqsT3, 50000);

		Thm4InequalityGenerator t4ig = new Thm4InequalityGenerator(filas, columnas);

		List<Inequality> ineqsT4 = t4ig.build();

		cutsTh4 = ineqsT4.size();

		//agregarIneqs(ineqsT4);
		// agregarIneqs(ineqsT4, 50000);

		Thm5InequalityGenerator t5ig = new Thm5InequalityGenerator(filas, columnas);

		List<Inequality> ineqsT5 = t5ig.build();

		cutsTh5 = ineqsT5.size();

		//agregarIneqs(ineqsT5);
		

		Thm7InequalityGenerator t7ig = new Thm7InequalityGenerator(filas, columnas);

		List<Inequality> ineqsT7 = t7ig.build();

		cutsTh7 = ineqsT7.size();

		//agregarIneqs(ineqsT7);
		
		Thm8InequalityGenerator t8ig = new Thm8InequalityGenerator(filas, columnas);

		List<Inequality> ineqsT8 = t8ig.build();

		cutsTh8 = ineqsT8.size();

		//agregarIneqs(ineqsT8);
		
		Thm9InequalityGenerator t9ig = new Thm9InequalityGenerator(filas, columnas);

		List<Inequality> ineqsT9 = t9ig.build();

		cutsTh9 = ineqsT9.size();

		agregarIneqs(ineqsT9);
	}

	public void agregarIneqs(List<Inequality> ineqs) throws Exception {

		int i = 0;
		for (Inequality ineq : ineqs) {

			IloNumExpr res = cplex.linearIntExpr();
			for (Termino t : ineq.terminos)
				res = cplex.sum(res, cplex.prod(t.coef, x[t.f][t.c]));

			cplex.addGe(ineq.rhs, res, "corte" + i++);
			// r.setName("corte" + i);
		}
	}

	private void agregarIneqs(List<Inequality> ineqs, int tope) throws Exception {

		Collections.shuffle(ineqs);

		int i = 0;
		for (Inequality ineq : ineqs) {

			if (i == tope)
				return;
			IloNumExpr res = cplex.linearIntExpr();
			for (Termino t : ineq.terminos)
				res = cplex.sum(res, cplex.prod(t.coef, x[t.f][t.c]));

			cplex.addGe(ineq.rhs, res, "corte" + i);
			i++;
		}

	}

	public void buildModel() throws Exception {

		// cplex.setParam(IloCplex.DoubleParam.TiLim, timeLimit);
		if (!modeloEntero)
			cplex.setParam(IloCplex.IntParam.NodeLim, 0);

		x = new IloNumVar[filas][columnas];
		for (int f = 0; f < filas; f++)
			for (int c = 0; c < columnas; c++)
				if (modeloEntero)
					x[f][c] = cplex.boolVar("x[" + f + ", " + c + "]");
				else
					x[f][c] = cplex.numVar(0, 1, "x[" + f + ", " + c + "]");

		// funcion objetivo

		IloNumExpr fobj = cplex.linearIntExpr();

		for (int f = 0; f < filas; f++)
			for (int c = 0; c < columnas; c++)
				fobj = cplex.sum(fobj, cplex.prod(matrix[f][c], x[f][c]));

		cplex.addMaximize(fobj);

		// restricciones
		// columnas contiguas en la misma fila
		for (int i = 0; i < filas; i++)
			for (int j = 0; j < columnas; j++)
				for (int j2 = 0; j2 <= j - 2; j2++) {
					IloNumExpr res = cplex.linearIntExpr();
					res = cplex.sum(res, cplex.prod(1.0, x[i][j]));
					res = cplex.sum(res, cplex.prod(1.0, x[i][j2]));
					res = cplex.sum(res, cplex.prod(-1.0, x[i][j - 1]));
					cplex.addGe(1.0, res, "col" + i + "," + j);
				}

		// filas contiguas en la misma columna
		for (int i = 0; i < filas; i++)
			for (int j = 0; j < columnas; j++)
				for (int i2 = 0; i2 <= i - 2; i2++) {
					IloNumExpr res = cplex.linearIntExpr();
					res = cplex.sum(res, cplex.prod(1.0, x[i][j]));
					res = cplex.sum(res, cplex.prod(1.0, x[i2][j]));
					res = cplex.sum(res, cplex.prod(-1.0, x[i - 1][j]));
					cplex.addGe(1.0, res, "fil" + i + "," + j);
				}

		// diagonales.
		for (int i = 0; i < filas; i++)
			for (int j = 0; j < columnas; j++)
				for (int i2 = i + 1; i2 < filas; i2++)
					for (int j2 = 0; j2 < columnas; j2++) {
						if (j2 == j)
							continue;

						IloNumExpr res = cplex.linearIntExpr();
						res = cplex.sum(res, cplex.prod(1.0, x[i][j]));
						res = cplex.sum(res, cplex.prod(1.0, x[i2][j2]));
						res = cplex.sum(res, cplex.prod(-0.5, x[i][j2]));
						res = cplex.sum(res, cplex.prod(-0.5, x[i2][j]));
						cplex.addGe(1.0, res, "diag" + i + "," + j + "," + i2 + "," + j2);
					}

	}

	/**
	 * Método principal que resuelve el problema de pricing.
	 * 
	 * @return List of columns (independent sets) with negative reduced cost.
	 * @throws TimeLimitExceededException TimeLimitExceededException
	 */
	public boolean solve() throws TimeLimitExceededException {

		try {

			// cplex.exportModel("slave.lp");
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

	public double getObjective() {
		return objective;
	}

	public double getPrecision() {
		return precision;
	}

	public InfoResolucion info() throws IloException {
		double gap = 0.0;
		try {
			gap = cplex.getMIPRelativeGap();
		} catch (Exception e) {
		}
		info.gap = gap;
		info.cplexStatus = cplex.getStatus();
		info.nodos = cplex.getNnodes();
		info.tiempoRes = timeMillis;
		return info;// return new InfoResolucion(cplex.getStatus(), cplex.getNnodes(), gap,
					// timeMillis);
	}

}
