package rpc.branch.and.price;

import org.jorlib.frameworks.columnGeneration.pricing.AbstractPricingProblem;

/***
 * Esta clase se usa únicamente para guardar información del problema de
 * pricing.
 * 
 * 
 * @author ik
 *
 */
public final class RPCPricingProblem extends AbstractPricingProblem<RPCDataModel> {

	public RPCPricingProblem(RPCDataModel dataModel, String name) {
		super(dataModel, name);
	}

}
