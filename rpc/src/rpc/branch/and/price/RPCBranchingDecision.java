package rpc.branch.and.price;

import java.awt.Point;

import org.jorlib.frameworks.columnGeneration.branchAndPrice.branchingDecisions.BranchingDecision;
import org.jorlib.frameworks.columnGeneration.master.cutGeneration.AbstractInequality;

/**
 * Una decisión de branching para nosotros es: Vamos del vértice origen (que
 * debe estar en V0) de la arista al destino.
 */
public final class RPCBranchingDecision implements BranchingDecision<RPCDataModel, RPCColumn> {

	/***
	 * la coordenada de M (que contiene un uno) para el cual vamos a partir la
	 * cantidad de rectángulos que la cubren
	 ***/
	private final Interval interval;
	private final Interval prevInterval;
	
	private final Point point;


	public RPCBranchingDecision(Point p, Interval interval, Interval prevInterval) {
		this.interval = interval;
		this.point = p;
		this.prevInterval = prevInterval;
	}

	/**
	 * Determina si una columna es compatible con el branching actual.
	 * 
	 * 
	 * @param column
	 *            column
	 * @return true
	 */
	@Override
	public boolean columnIsCompatibleWithBranchingDecision(RPCColumn column) {
		return true;
	}

	/**
	 * Determina si la desigualdad parámetro permanece válida Importa sólo para el
	 * branch and price and cut.
	 * 
	 * @param inequality
	 *            inequality
	 * @return true
	 */
	@Override
	public boolean inEqualityIsCompatibleWithBranchingDecision(AbstractInequality inequality) {
		return true;
	}

	@Override
	public String toString() {
		return "Branching " + this.interval.toString();				
	}

	public Interval getInterval() {
		return interval;
	}

	public Point getPoint() {
		return point;
	}


	public Interval getPrevInterval() {
		return prevInterval;
	}
}
