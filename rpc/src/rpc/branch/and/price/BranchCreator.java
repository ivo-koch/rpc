package rpc.branch.and.price;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.jorlib.frameworks.columnGeneration.branchAndPrice.AbstractBranchCreator;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.BAPNode;

/**
 * Clase responsable de crear los branches.
 * 
 */
public final class BranchCreator extends AbstractBranchCreator<RPCDataModel, RPCColumn, RPCPricingProblem> {

	public BranchCreator(RPCDataModel dataModel, RPCPricingProblem pricingProblem) {
		super(dataModel, pricingProblem);
	}

	/**
	 * Podemos hacer un branching si hay un v fuera de V0.
	 */
	@Override
	protected boolean canPerformBranching(List<RPCColumn> solution) {

		return dataModel.MijConCubrimientoFrac != null;
	}

	/**
	 * Creamos los branches
	 * 
	 * 
	 * @param parentNode
	 *            Nodo fraccionario a partir del cual brancheamos.
	 * 
	 * @return Lista de hijos para el branch.
	 */
	@Override
	protected List<BAPNode<RPCDataModel, RPCColumn>> getBranches(BAPNode<RPCDataModel, RPCColumn> parentNode) {

		List<BAPNode<RPCDataModel, RPCColumn>> branches = new ArrayList<BAPNode<RPCDataModel, RPCColumn>>();

		// creamos un branch para [1, floor(cubrimientoFraccionario)] y
		// para [floor(cubrimientoFraccionario) + 1, cota]

		Point punto = dataModel.MijConCubrimientoFrac;
		int lower = dataModel.intActual.lower;
		int upper = dataModel.intActual.upper;

		int nuevo = (int) Math.floor(dataModel.valorCubrimientoFrac);
		Interval int1 = new Interval(lower, nuevo);
		Interval int2 = new Interval(nuevo + 1, upper);

		RPCBranchingDecision bd = new RPCBranchingDecision(punto, int1, dataModel.intActual);
		BAPNode<RPCDataModel, RPCColumn> node = this.createBranch(parentNode, bd, parentNode.getSolution(),
				parentNode.getInequalities());
		branches.add(node);

		RPCBranchingDecision bd2 = new RPCBranchingDecision(punto, int2, dataModel.intActual);
		BAPNode<RPCDataModel, RPCColumn> node2 = this.createBranch(parentNode, bd2, parentNode.getSolution(),
				parentNode.getInequalities());
		branches.add(node2);
	
		return branches;
	}
}
