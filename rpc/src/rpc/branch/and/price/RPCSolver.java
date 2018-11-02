package rpc.branch.and.price;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.jorlib.frameworks.columnGeneration.branchAndPrice.AbstractBranchCreator;
import org.jorlib.frameworks.columnGeneration.io.SimpleBAPLogger;
import org.jorlib.frameworks.columnGeneration.pricing.AbstractPricingProblemSolver;
import org.jorlib.frameworks.columnGeneration.util.Configuration;

public class RPCSolver {

	// private final Grafo grafo;

	private BranchAndPrice bap;

	public static void initConfig() {

		Properties properties = new Properties();
		properties.setProperty("EXPORT_MODEL", "false");
		// properties.setProperty("MAXTHREADS", "5");
		Configuration.readFromFile(properties);

	}

	public RPCSolver(Matriz matriz) {

		// el data model con los datos de V0 y el grafo
		RPCDataModel dataModel = new RPCDataModel(matriz);

		// el problema de pricing (sólo sirve para las variables duales)
		RPCPricingProblem pricingProblem = new RPCPricingProblem(dataModel, "testPricingProblem");

		// // El solver para el problema de pricing.
		// ExactPricingProblemSolverMultipleV0 exactPricingSolver = new
		// ExactPricingProblemSolverMultipleV0(dataModel,
		// pricingProblem);

		// El Master.
		RPCMaster master = new RPCMaster(dataModel, pricingProblem);

		// Define which solvers to use for the pricing problem
		// List<Class<? extends AbstractPricingProblemSolver<DataModel, MBTColumn,
		// MBTPricingProblem>>> solvers = Collections
		// .singletonList(ExactPricingProblemSolverMultipleV0.class);

		List<Class<? extends AbstractPricingProblemSolver<RPCDataModel, RPCColumn, RPCPricingProblem>>> solvers = new ArrayList<Class<? extends AbstractPricingProblemSolver<RPCDataModel, RPCColumn, RPCPricingProblem>>>();

		solvers.add(RPCMIPPricingSolver.class);

		 // Optional: Get an initial solution
		 List<RPCColumn> initSolution = this.getInitialSolution(dataModel,
		 pricingProblem);
		 		 
		// Define Branch creators
		List<? extends AbstractBranchCreator<RPCDataModel, RPCColumn, RPCPricingProblem>> branchCreators = Collections
				.singletonList(new BranchCreator(dataModel, pricingProblem));

		// Create a Branch-and-Price instance, and provide the initial solution as a
		// warm-start
		bap = new BranchAndPrice(dataModel, master, pricingProblem, solvers, branchCreators, 1,
				dataModel.matriz.cantUnos());

		bap.warmStart(initSolution.size(), initSolution);

		// OPTIONAL: Attach a debugger
		//SimpleDebugger sd = new SimpleDebugger(bap, true);

		// OPTIONAL: Attach a logger to the Branch-and-Price procedure.
		new SimpleBAPLogger(bap, new File("output.log"));

	}

	public RPCSolution solve() {
		// Solve the Graph Coloring problem through Branch-and-Price
		bap.runBranchAndPrice(System.currentTimeMillis() + 8000000L);
		Estadisticas.stopTime = System.currentTimeMillis();

		// master.printSolution();
		RPCSolution rpcSolution = new RPCSolution(bap.getObjective(), bap.getTotalNrIterations(),
				bap.getNumberOfProcessedNodes(), bap.getMasterSolveTime(), bap.getPricingSolveTime(), bap.isOptimal(),
				bap.getSolution());

		//rpcSolution.print();

		Estadisticas.print();

		// Clean up:
		bap.close(); // Close master and pricing problems

		return rpcSolution;
	}

	public static void main(String[] args) throws IOException {

		boolean[][] matrix = null;

		RPCSolver mbt = new RPCSolver(new Matriz(matrix));
		mbt.solve();

	}

	// ------------------ Helper methods -----------------

	/**
	 * Calculate a feasible graph coloring using a greedy algorithm.
	 * 
	 * @param pricingProblem
	 *            Pricing problem
	 * @return Feasible coloring.
	 */
	public List<RPCColumn> getInitialSolution(RPCDataModel dataModel, RPCPricingProblem pricingProblem) {
		List<RPCColumn> solInicial = new ArrayList<RPCColumn>();

		List<Rectangle> rects = new ArrayList<Rectangle>();
		for (Point p : dataModel.matriz.unos()) {
			boolean cubierto = false;
			for (Rectangle r : rects)
				if (r.contains(p)) {
					cubierto = true;
					break;
				}

			if (!cubierto)
				rects.add(dataModel.matriz.buildMaximal(p));
		}
		for (Rectangle r : rects)
			solInicial.add(new RPCColumn(pricingProblem, false, "generateInitialFeasibleSolution", r));

		return solInicial;

	}
}
