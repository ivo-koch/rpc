package rpc.polyhedral.computations;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jorlib.frameworks.columnGeneration.io.TimeLimitExceededException;

import ilog.concert.IloColumn;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import rpc.branch.and.price.Matriz;
import rpc.modelos.CPLEXInfoCallBack;
import rpc.modelos.InfoResolucion;

public class ModeloMaster2 {

	/*** Instancia de cplex **/
	private IloCplex cplex;

	private Matriz matrix;
	private double precision;
	private double objective;

	private long timeMillis;
	// cota superior rectángulos.
	private InfoResolucion info = new InfoResolucion();

	private Set<Rectangle> rectangulos;

	// private Map<Rectangle, IloNumVar> rectToVar = new HashMap<Rectangle,
	// IloNumVar>();

	private IloObjective obj; // Función objetivo.

	private Map<Point, IloRange> constraints = new HashMap<Point, IloRange>();

	public ModeloMaster2(Matriz matrix, OutputStream out) throws Exception {
		this.matrix = matrix;

		// this.precision = precision;

		cplex = new IloCplex();
		cplex.setParam(IloCplex.IntParam.AdvInd, 0);
		cplex.setParam(IloCplex.IntParam.Threads, 1);
		// if (out != null)
		cplex.setOut(out);

		CPLEXInfoCallBack infocallback = new CPLEXInfoCallBack(info);
		cplex.use(infocallback);

		this.rectangulos = matrix.allRects();
	}

	public void buildModel() throws Exception {

		// Función objetivo
		obj = cplex.addMinimize();

		// Definimos constraints
		for (Point p : matrix.unos())
			constraints.put(p, cplex.addRange(1.0, Double.MAX_VALUE, "Pos " + p.x + "," + p.y + " cubierta "));

		// armamos el modelo por columnas

		int i = 0;
		for (Rectangle rectangle : this.rectangulos) {
			// Registramos la nueva columna con la función objetivo
			IloColumn iloColumn = cplex.column(obj, 1.0);

			// Registramos la nueva columna con los constraints
			// Esto es, tiene que figurar en cada constraint correspondiente a cada uno de
			// los unos.
			for (int f = rectangle.y; f < rectangle.y + rectangle.height; f++)
				for (int c = rectangle.x; c < rectangle.x + rectangle.width; c++) {
					IloRange range = constraints.get(new Point(c, f));
					if (range == null)
						throw new RuntimeException("El punto " + c + "," + f + "no está en el map");
					iloColumn = iloColumn.and(cplex.column(range, 1.0));
				}

			// Creamos la variable para esa columna, y la guardamos
			IloNumVar var = cplex.numVar(iloColumn, 0, 1, "r" + i++);
			cplex.add(var);
			// rectToVar.put(rectangle, var);

			//cplex.exportModel("test.lp");
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
			timeMillis = System.currentTimeMillis();
			// Resolvemos el problema
			boolean resuelto = cplex.solve();
			timeMillis = System.currentTimeMillis() - timeMillis;

			if (!resuelto || cplex.getStatus() != IloCplex.Status.Optimal) {
				this.objective = Double.MAX_VALUE;
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

	public double[][] getDualMatrix() throws Exception {

		double[][] res = new double[matrix.filas()][matrix.columnas()];

		for (int f = 0; f < matrix.filas(); f++)
			for (int c = 0; c < matrix.columnas(); c++) {
				if (matrix.get(f, c))
					res[f][c] = cplex.getDual(constraints.get(new Point(c, f)));
				else
					res[f][c] = 0.0;

			}

		return res;

	}
	/*
	public double[][] getResultMatrix() throws Exception {

		double[][] res = new double[matrix.filas()][matrix.columnas()];

		for (int f = 0; f < matrix.filas(); f++)
			for (int c = 0; c < matrix.columnas(); c++) {
				
				res[f][c] = cplex.getDual(constraints.get(new Point(c, f)));
				

			}

		return res;
*/

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
