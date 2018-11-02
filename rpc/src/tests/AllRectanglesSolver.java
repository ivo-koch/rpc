package tests;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jorlib.frameworks.columnGeneration.io.TimeLimitExceededException;
import org.slf4j.Logger;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;
import rpc.branch.and.price.Estadisticas;
import rpc.branch.and.price.Matriz;

/***
 * Implementación de solución exacta para el problema de pricing para un V0 de
 * varios vértices.
 */
public final class AllRectanglesSolver {

	/*** Instancia de cplex **/
	private IloCplex cplex;

	private Map<Rectangle, IloNumVar> rectVsVar = new HashMap<Rectangle, IloNumVar>();

	private Matriz matriz;
	private double objective;

	/** Mantenemos acá las variables de la función objetivo */
	IloNumVar[] varsEnFobj;

	/***
	 * Crea un nuevo solver de pricing.
	 * 
	 * @param dataModel
	 * @param pricingProblem
	 */
	public AllRectanglesSolver(Matriz matrix) {
		this.matriz = matrix;
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

			Set<Rectangle> maxRects = matriz.allMaximals();

			for (Rectangle r : maxRects) {
				IloNumVar rectVar = cplex.boolVar("a[" + r.toString() + "]");
				rectVsVar.put(r, rectVar);
			}

			// función objetivo vacía

			IloNumExpr fobj = cplex.linearIntExpr();
			for (Rectangle r : maxRects)
				fobj = cplex.sum(fobj, cplex.prod(1, rectVsVar.get(r)));

			cplex.addMinimize(fobj);

			// constraints
			for (Point p : matriz.unos()) {
				IloNumExpr lhs = cplex.linearIntExpr();
				for (Rectangle r : maxRects) {
					if (r.contains(p))
						lhs = cplex.sum(lhs, cplex.prod(1.0, rectVsVar.get(r)));
				}
				cplex.addGe(lhs, 1, "Const1");
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

		// Resolvemos el problema
		if (!cplex.solve() || cplex.getStatus() != IloCplex.Status.Optimal) {
			if (cplex.getCplexStatus() == IloCplex.CplexStatus.AbortTimeLim) {
				throw new TimeLimitExceededException();
			} else if (cplex.getStatus() == IloCplex.Status.Infeasible) {
				this.objective = Double.MIN_VALUE;
				return false;
			} else {
				throw new RuntimeException("Solve failed! Status: " + cplex.getStatus());
			}
		} else { // Encontramos un óptimo
			this.objective = cplex.getObjValue();
			return true;
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
}
