package rpc.modelos;

import java.io.OutputStream;

import org.jorlib.frameworks.columnGeneration.io.TimeLimitExceededException;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;
import rpc.branch.and.price.Matriz;

public abstract class Modelo {

	/*** Instancia de cplex **/
	protected IloCplex cplex;

	protected Matriz matriz;
	protected double precision;
	protected double objective;

	protected long timeMillis;
	// cota superior rectángulos.
	protected int k;
	protected InfoResolucion info = new InfoResolucion();

	public Modelo(Matriz matrix, int k, double precision, OutputStream out) throws Exception {
		this.matriz = matrix;

		this.precision = precision;
		this.k = k;

		cplex = new IloCplex();
		cplex.setParam(IloCplex.IntParam.AdvInd, 0);
		cplex.setParam(IloCplex.IntParam.Threads, 1);
		// if (out != null)
		cplex.setOut(out);
		
		CPLEXInfoCallBack infocallback = new CPLEXInfoCallBack(info);
		cplex.use(infocallback);
	}

	public abstract void buildModel() throws Exception;

	@FunctionalInterface
	public interface Expr<T, R> {
		public R apply(T t) throws Exception;
	}

	@FunctionalInterface
	public interface Expr2<T, U, R> {
		public R apply(T t, U u) throws Exception;
	}

	@FunctionalInterface
	public interface Expr3<S, T, U, R> {
		public R apply(S s, T t, U u) throws Exception;
	}

	@FunctionalInterface
	public interface Expr4<S, T, U, V, R> {
		public R apply(S s, T t, U u, V v) throws Exception;
	}

	@FunctionalInterface
	public interface Com3<S, T, U> {
		public void apply(S s, T t, U u) throws Exception;
	}

	@FunctionalInterface
	public interface Com4<S, T, U, V> {
		public void apply(S s, T t, U u, V v) throws Exception;
	}

	@FunctionalInterface
	public interface Com5<S, T, U, V, W> {
		public void apply(S s, T t, U u, V v, W w) throws Exception;
	}

	@FunctionalInterface
	public interface Com2<S, T> {
		public void apply(S s, T t) throws Exception;
	}

	@FunctionalInterface
	public interface Com<S> {
		public void apply(S s) throws Exception;
	}

	/***
	 * Itera por la fila, la columna y el rectángulo
	 * 
	 * @param e
	 * @throws Exception
	 */
	protected void forall(Com3<Integer, Integer, Integer> e) throws Exception {
		forall(e, 0, matriz.filas(), 0, matriz.columnas());
	}

	/***
	 * Itera por la fila, la columna y el rectángulo
	 * 
	 * @param e
	 * @throws Exception
	 */
	protected void forall(Com3<Integer, Integer, Integer> e, int fMin, int fMax, int cMin, int cMax) throws Exception {
		for (int f = fMin; f < fMax; f++)
			for (int c = cMin; c < cMax; c++)
				for (int r = 0; r < k; r++)
					e.apply(f, c, r);
	}

	/***
	 * Itera por la fila, la columna y el rectángulo
	 * 
	 * @param e
	 * @throws Exception
	 */
	protected void forall(Com4<Integer, Integer, Integer, Integer> e, int fMin, int fMax, int f1Min, int f1Max,
			int f2Min, int f2Max, int f3Min, int f3Max) throws Exception {
		for (int f = fMin; f < fMax; f++)
			for (int f1 = f1Min; f1 < f1Max; f1++)
				for (int f2 = f2Min; f2 < f2Max; f2++)
					for (int f3 = f3Min; f3 < f3Max; f3++)
						e.apply(f, f1, f2, f3);
	}

	/***
	 * Itera por la fila, la columna y el rectángulo
	 * 
	 * @param e
	 * @throws Exception
	 */
	protected void forall(Com5<Integer, Integer, Integer, Integer, Integer> e, int fMin, int fMax, int f1Min, int f1Max,
			int f2Min, int f2Max, int f3Min, int f3Max, int f4Min, int f4Max) throws Exception {
		for (int f = fMin; f < fMax; f++)
			for (int f1 = f1Min; f1 < f1Max; f1++)
				for (int f2 = f2Min; f2 < f2Max; f2++)
					for (int f3 = f3Min; f3 < f3Max; f3++)
						for (int f4 = f4Min; f4 < f4Max; f4++)
							e.apply(f, f1, f2, f3, f4);
	}

	/***
	 * Itera por la fila, la columna y el rectángulo
	 * 
	 * @param e
	 * @throws Exception
	 */
	protected void forall(Com3<Integer, Integer, Integer> e, int fMin, int fMax, int cMin, int cMax, int rMin, int rMax)
			throws Exception {
		for (int f = fMin; f < fMax; f++)
			for (int c = cMin; c < cMax; c++)
				for (int r = rMin; r < rMax; r++)
					e.apply(f, c, r);
	}

	/***
	 * Itera por la fila y la columna
	 * 
	 * @param e
	 * @throws Exception
	 */
	protected void forall(Com2<Integer, Integer> e) throws Exception {
		forall(e, 0, matriz.filas(), 0, matriz.columnas());
	}

	/***
	 * Itera por la fila y la columna
	 * 
	 * @param e
	 * @throws Exception
	 */
	protected void forall(Com2<Integer, Integer> e, int fMin, int fMax, int cMin, int cMax) throws Exception {
		for (int f = fMin; f < fMax; f++)
			for (int c = cMin; c < cMax; c++)
				e.apply(f, c);
	}

	/***
	 * Itera por los rectángulos
	 * 
	 * @param e
	 * @throws Exception
	 */
	protected void forall(Com<Integer> e) throws Exception {
		for (int r = 0; r < k; r++)
			e.apply(r);
	}

	/***
	 * Suma por sobre las filas, columnas y rectángulos
	 * 
	 * @param e
	 * @return
	 * @throws Exception
	 */
	protected IloNumExpr sum(Expr3<Integer, Integer, Integer, IloNumExpr> e) throws Exception {

		return sum(e, (f, c, r) -> {
			return true;
		});
	}

	/***
	 * Suma por sobre las filas, columnas y rectángulos
	 * 
	 * @param e
	 * @return
	 * @throws Exception
	 */
	protected IloNumExpr sum(Expr3<Integer, Integer, Integer, IloNumExpr> e, int fMin, int fMax, int cMin, int cMax,
			int rMin, int rMax) throws Exception {
		IloNumExpr res = cplex.linearIntExpr();
		for (int f = fMin; f < fMax; f++)
			for (int c = cMin; c < cMax; c++)
				for (int r = rMin; r < rMax; r++)
					res = cplex.sum(res, e.apply(f, c, r));

		return res;
	}

	/***
	 * Suma por sobre las filas, columnas y rectángulos
	 * 
	 * @param e
	 * @return
	 * @throws Exception
	 */
	protected IloNumExpr sum(Expr3<Integer, Integer, Integer, IloNumExpr> e,
			Expr3<Integer, Integer, Integer, Boolean> cond) throws Exception {
		IloNumExpr res = cplex.linearIntExpr();
		for (int f = 0; f < matriz.filas(); f++)
			for (int c = 0; c < matriz.columnas(); c++)
				for (int r = 0; r < k; r++)
					if (cond.apply(f, c, r))
						res = cplex.sum(res, e.apply(f, c, r));

		return res;
	}

	/***
	 * Suma por todos los rectángulos.
	 * 
	 * @param e
	 * @return
	 * @throws Exception
	 */
	protected IloNumExpr sum(Expr<Integer, IloNumExpr> e, Expr<Integer, Boolean> cond) throws Exception {
		IloNumExpr res = cplex.linearIntExpr();
		for (int r = 0; r < k; r++)
			if (cond.apply(r))
				res = cplex.sum(res, e.apply(r));
		return res;
	}

	/***
	 * Suma por todos los rectángulos.
	 * 
	 * @param e
	 * @return
	 * @throws Exception
	 */
	protected IloNumExpr sum(Expr<Integer, IloNumExpr> e) throws Exception {
		return sum(e, 0, k);
	}

	/***
	 * Suma por todos los rectángulos.
	 * 
	 * @param e
	 * @return
	 * @throws Exception
	 */
	protected IloNumExpr sum(Expr<Integer, IloNumExpr> e, int rMin, int rMax) throws Exception {
		IloNumExpr res = cplex.linearIntExpr();
		for (int r = rMin; r < rMax; r++)
			res = cplex.sum(res, e.apply(r));
		return res;
	}

	/***
	 * Suma sobre la fila, la columna y el rectángulo.
	 * 
	 * @param e
	 * @return
	 * @throws Exception
	 */
	protected IloNumExpr sum(Expr2<Integer, Integer, IloNumExpr> e) throws Exception {

		return sum(e, 0, matriz.filas(), 0, matriz.columnas());
	}

	protected IloNumExpr sum(Expr2<Integer, Integer, IloNumExpr> e, int fMin, int fMax, int cMin, int cMax)
			throws Exception {
		IloNumExpr res = cplex.linearIntExpr();
		for (int f = fMin; f < fMax; f++)
			for (int c = cMin; c < cMax; c++)
				res = cplex.sum(res, e.apply(f, c));

		return res;
	}

	protected IloNumExpr sum(Expr2<Integer, Integer, IloNumExpr> e, Expr2<Integer, Integer, Boolean> cond)
			throws Exception {
		IloNumExpr res = cplex.linearIntExpr();
		for (int f = 0; f < matriz.filas(); f++)
			for (int c = 0; c < matriz.columnas(); c++)
				if (cond.apply(f, c))
					res = cplex.sum(res, e.apply(f, c));

		return res;
	}

	protected void addLe(double lhs, IloNumExpr... exps) throws Exception {

		cplex.addLe(lhs, sum(exps));
	}

	protected void addLe(double lhs, String nombre, IloNumExpr... exps) throws Exception {

		cplex.addLe(lhs, sum(exps), nombre);
	}

	protected void addEq(double lhs, IloNumExpr... exps) throws Exception {

		cplex.addEq(lhs, sum(exps));
	}

	protected void addEq(double lhs, String nombre, IloNumExpr... exps) throws Exception {

		cplex.addEq(lhs, sum(exps), nombre);
	}

	protected IloNumExpr sum(IloNumExpr... exps) throws Exception {

		IloNumExpr res = null;
		for (IloNumExpr e : exps)
			if (res == null)
				res = e;
			else
				res = cplex.sum(res, e);

		return res;
	}

	public void buildModel(double timeLimit) throws Exception {

		cplex.setParam(IloCplex.DoubleParam.TiLim, timeLimit);
		buildModel();
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

	public abstract Solucion getSolution() throws UnknownObjectException, IloException;

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
		return info;// return new InfoResolucion(cplex.getStatus(), cplex.getNnodes(), gap, timeMillis);
	}

}
