package rpc.branch.and.price;

import java.util.Map;

import org.jorlib.frameworks.columnGeneration.master.MasterData;
import org.jorlib.frameworks.columnGeneration.util.OrderedBiMap;

import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

/**
 * Esta clase está pensada solamente como un contenedor de información. No la
 * usamos.
 */
public final class RPCMasterData extends MasterData<RPCDataModel, RPCColumn, RPCPricingProblem, IloNumVar> {

	/** Cplex instance **/
	public final IloCplex cplex;

	public RPCMasterData(IloCplex cplex, Map<RPCPricingProblem, OrderedBiMap<RPCColumn, IloNumVar>> varMap) {
		super(varMap);
		this.cplex = cplex;
	}
}
