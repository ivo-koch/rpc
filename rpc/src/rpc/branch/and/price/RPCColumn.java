package rpc.branch.and.price;

import java.awt.Rectangle;

import org.jorlib.frameworks.columnGeneration.colgenMain.AbstractColumn;

/**
 * Definición de una columna/árbol. No está pensada para ser modificada una vez
 * construída.
 *
 */
public final class RPCColumn extends AbstractColumn<RPCDataModel, RPCPricingProblem> {

	public final Rectangle rectangle;

	/**
	 * Construye una nueva columna, que será un árbol
	 *
	 * @param associatedPricingProblem
	 *            Pricing problem to which this column belongs
	 * @param isArtificial
	 *            Is this an artificial column?
	 * @param creator
	 *            Who/What created this column?
	 * @param vertices
	 *            Vertices in the independent set
	 * @param cost
	 *            cost of the independent set
	 */
	public RPCColumn(RPCPricingProblem associatedPricingProblem, boolean isArtificial, String creator, Rectangle rect) {
		super(associatedPricingProblem, isArtificial, creator);
		this.rectangle = rect;
	}

	@Override
	public String toString() {
		return this.rectangle.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rectangle == null) ? 0 : rectangle.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RPCColumn other = (RPCColumn) obj;
		if (rectangle == null) {
			if (other.rectangle != null)
				return false;
		} else if (!rectangle.equals(other.rectangle))
			return false;
		return true;
	}
	

}
