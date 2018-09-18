package rpc.branch.and.price;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.jorlib.frameworks.columnGeneration.branchAndPrice.AbstractBranchAndPrice;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.AbstractBranchCreator;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.BAPNode;
import org.jorlib.frameworks.columnGeneration.master.AbstractMaster;
import org.jorlib.frameworks.columnGeneration.master.MasterData;
import org.jorlib.frameworks.columnGeneration.pricing.AbstractPricingProblemSolver;

/**
 * Clase principal del branch and price.
 */
public final class BranchAndPrice extends AbstractBranchAndPrice<RPCDataModel, RPCColumn, RPCPricingProblem> {

    public BranchAndPrice(RPCDataModel dataModel,
                          @SuppressWarnings("rawtypes") AbstractMaster<RPCDataModel, RPCColumn, RPCPricingProblem, ? extends MasterData> master,
                          RPCPricingProblem pricingProblem,
                          List<Class<? extends AbstractPricingProblemSolver<RPCDataModel, RPCColumn, RPCPricingProblem>>> solvers,
                          List<? extends AbstractBranchCreator<RPCDataModel, RPCColumn, RPCPricingProblem>> abstractBranchCreators,
                          double lowerBoundOnObjective,
                          double upperBoundOnObjective) {
        super(dataModel, master, pricingProblem, solvers, abstractBranchCreators, lowerBoundOnObjective, upperBoundOnObjective);
    }

    /**
     * Generates an artificial solution. Columns in the artificial solution are of high cost such that they never end up in the final solution
     * if a feasible solution exists, since any feasible solution is assumed to be cheaper than the artificial solution. The artificial solution is used
     * to guarantee that the master problem has a feasible solution.
     * Generamos una solucion inicial.
     *
     * @return artificial solution
     */
    @Override
    protected List<RPCColumn> generateInitialFeasibleSolution(BAPNode<RPCDataModel, RPCColumn> node) {
        List<RPCColumn> artificialSolution=new ArrayList<>();
                                               
        //esta solución que nos pide el framework es un punto por cada rectángulo.                
        for (Point p: dataModel.matriz.unos()) {
        	RPCColumn nueva = new RPCColumn(pricingProblems.get(0), false, "generateInitialFeasibleSolution", new Rectangle(p.x, p.y, 1, 1));        
        	if (!node.getInitialColumns().contains(nueva))       
        		artificialSolution.add(nueva);
        }
        
        return artificialSolution;
        
    }

    /**
     * Chequea si la solución del nodo actual es entera. 
     * Una solución es entera si todo todo vértice aparece en exactamente un árbol
     * */
    @Override
    protected boolean isIntegerNode(BAPNode<RPCDataModel, RPCColumn> node) {
            	
    	return dataModel.MijConCubrimientoFrac == null;
    }
}
