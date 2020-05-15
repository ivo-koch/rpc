package rpc.polyhedral.computations;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jorlib.frameworks.columnGeneration.io.TimeLimitExceededException;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import rpc.branch.and.price.Matriz;
import rpc.modelos.CPLEXInfoCallBack;
import rpc.modelos.InfoResolucion;

public class ModeloMaster {

	/*** Instancia de cplex **/
	private IloCplex cplex;

	private Matriz matrix;
	private double precision;
	private double objective;

	private long timeMillis;
	
	private IloNumVar[] x;
	
	private List<IloRange> restricciones = new ArrayList<IloRange>();
	
	// cota superior rectángulos.
	private InfoResolucion info = new InfoResolucion();

	private Set<Rectangle> rectangulos;

	//private IloObjective obj; // Función objetivo.

	private Map<Point, IloRange> constraints = new HashMap<Point, IloRange>();
	
	private Map<Rectangle, IloNumVar> rectToVar = new HashMap<Rectangle, IloNumVar>();

	public ModeloMaster(Matriz matrix, OutputStream out) throws Exception {
		this.matrix = matrix;

		// this.precision = precision;

		cplex = new IloCplex();
		//cplex.setParam(IloCplex.IntParam.AdvInd, 0);
		cplex.setParam(IloCplex.IntParam.NodeLim, 0);
		
		// if (out != null)
		cplex.setOut(out);

		CPLEXInfoCallBack infocallback = new CPLEXInfoCallBack(info);
		cplex.use(infocallback);

		this.rectangulos = matrix.allRects();
	}

	public void buildModel() throws Exception {
		x = new IloNumVar[this.rectangulos.size()];

		int i = 0;
		for (Rectangle r : this.rectangulos) {
			x[i] = cplex.numVar(0, 1, "x[" + r + "]");
			rectToVar.put(r, x[i]);
			i++;
		}

		// funcion objetivo

		IloNumExpr fobj = cplex.linearIntExpr();

		for (int j = 0; j < x.length; j++)
			fobj = cplex.sum(fobj, cplex.prod(1, x[j]));

		cplex.addMinimize(fobj);

		// restricciones
		for (Point uno : matrix.unos()) {
			IloNumExpr rest = cplex.linearIntExpr();
			for (Rectangle r : matrix.allRects(this.rectangulos, uno))
				rest = cplex.sum(rest, cplex.prod(1.0, rectToVar.get(r)));

			
			IloRange constraint = cplex.addLe(1.0, rest);
			restricciones.add(constraint);
						
			constraints.put(uno, constraint);
			
		}

		//cplex.exportModel("master.lp");

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

//		for (IloRange cons : this.restricciones) {
//			System.out.println(cplex.getDual(cons));
//		}
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
