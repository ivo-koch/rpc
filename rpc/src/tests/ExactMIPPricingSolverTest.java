package tests;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Rectangle;
import java.util.Random;
import java.util.Set;

import org.jorlib.frameworks.columnGeneration.io.TimeLimitExceededException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ilog.concert.IloException;
import ilog.cplex.IloCplex.UnknownObjectException;
import rpc.branch.and.price.ExactMIPPricingSolver;
import rpc.branch.and.price.ExactMIPPricingSolverModel1;
import rpc.branch.and.price.Matriz;

class ExactMIPPricingSolverTest {

	

	@Test
	void testSolve() throws TimeLimitExceededException, UnknownObjectException, IloException {
		// OPTIONAL: Attach a logger to the Branch-and-Price procedur

		Logger logger = LoggerFactory.getLogger(ExactMIPPricingSolver.class);
		Random rand = new Random();
		for (int f = 10; f < 21; f++)
			for (int c = 10; c < 21; c++)
				for (int density =10; density < 100; density += 10) {
					Matriz matrix = MatrixGenerator.generateRandomMatrix(f, c, density);
					double[] fobjCoef = new double[matrix.cantUnos()];
					for(int i = 0; i < matrix.cantUnos();i++)
						fobjCoef[i] = rand.nextDouble();
					
					ExactMIPPricingSolverModel1 solver = new ExactMIPPricingSolverModel1(matrix, logger, 0.000001);
					solver.setObjective(fobjCoef);
					
					if (!solver.solve())
						throw new RuntimeException("Infactible");
					
					//ahora, vamos a enumerar todos los rectángulos maximales, y comparar la solución del exacto
					Set<Rectangle> maximals = matrix.allMaximals();
					
					Rectangle best = null;
					double maxWeight = 0;
					for (Rectangle r : maximals) {
						double val = matrix.weight(r, fobjCoef);
						
						if (val > maxWeight)
						{
							maxWeight = val;
							best = r;
						}						
					}
					
					if (maxWeight <= 1 - solver.getPrecision()) {
						assertTrue(solver.getColumn() == null);
						assertTrue(solver.getObjective() <= 1 - solver.getPrecision());
					}
					else {						
						assertTrue(solver.getColumn() != null);
						//assertTrue(maximals.contains(solver.getColumn()));
						double w = matrix.weight(best, fobjCoef);
						if (Math.abs(w - solver.getObjective()) > solver.getPrecision())
							throw new RuntimeException("Para poner un breakpoint");
						assertEquals(maxWeight, solver.getObjective(), solver.getPrecision());
					}
					
					solver.close();
				}
	}

}
