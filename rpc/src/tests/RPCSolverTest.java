package tests;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Rectangle;
import java.util.Random;

import org.jorlib.frameworks.columnGeneration.io.TimeLimitExceededException;
import org.junit.jupiter.api.Test;

import ilog.concert.IloException;
import rpc.branch.and.price.Matriz;
import rpc.branch.and.price.RPCSolution;
import rpc.branch.and.price.RPCSolver;

class RPCSolverTest {

	// @Test
	void testSolver1() {

		StringBuilder sb = new StringBuilder();
		sb.append("0 1 0 1 0 0 1\n");
		sb.append("0 1 1 1 1 0 1\n");
		sb.append("0 1 1 1 1 0 1\n");
		sb.append("0 1 1 1 1 0 1\n");
		sb.append("0 1 0 1 0 0 1\n");
		sb.append("0 1 0 1 0 0 1\n");

		RPCSolver rpc = new RPCSolver(new Matriz(sb.toString()));
		RPCSolution sol = rpc.solve();

		assertEquals(4.0, sol.getObjective());

	}

	// @Test
	void testSolver3() {

		StringBuilder sb = new StringBuilder();

		sb.append("0 0 0 1 0 0 0 0 0 0\n");
		sb.append("0 0 0 0 0 0 0 0 0 0\n");
		sb.append("0 0 0 0 0 0 0 0 0 0\n");
		sb.append("0 0 0 0 0 0 0 0 0 0\n");
		sb.append("1 0 0 0 0 0 0 0 1 0\n");
		sb.append("0 0 0 0 1 0 0 0 0 0\n");
		sb.append("0 0 0 1 0 0 0 0 0 0\n");
		sb.append("0 0 0 0 1 0 1 0 0 0\n");
		sb.append("0 0 0 0 0 0 0 1 0 0\n");
		sb.append("0 0 0 0 1 0 0 0 0 1\n");
		sb.append("0 1 0 0 0 0 0 0 0 0\n");
		sb.append("0 0 0 0 0 0 0 1 0 0\n");
		sb.append("0 0 0 0 0 1 0 0 0 0\n");

		RPCSolver rpc = new RPCSolver(new Matriz(sb.toString()));
		RPCSolution sol = rpc.solve();

		assertEquals(13.0, sol.getObjective());

	}

	// @Test
	void testSolver4() {

		StringBuilder sb = new StringBuilder();

		sb.append("0 0 0 1 0 0 0 0 0 0\n");
		sb.append("0 0 0 0 0 0 0 0 0 0\n");
		sb.append("0 0 0 0 0 0 0 0 0 0\n");
		sb.append("0 0 0 0 0 0 0 0 0 0\n");
		sb.append("1 0 0 0 0 0 0 0 1 0\n");
		sb.append("0 0 0 0 1 0 0 0 0 0\n");
		sb.append("0 0 0 1 0 0 0 0 0 0\n");
		sb.append("0 0 0 0 1 0 1 0 0 0\n");
		sb.append("0 0 0 0 0 0 0 1 0 0\n");
		sb.append("0 0 0 0 1 0 0 0 0 1\n");
		sb.append("0 1 0 0 0 0 0 0 0 0\n");
		sb.append("0 0 0 0 0 0 0 1 0 0\n");
		sb.append("0 0 0 0 0 1 0 0 0 0\n");

		RPCSolver rpc = new RPCSolver(new Matriz(sb.toString()));
		RPCSolution sol = rpc.solve();

		assertEquals(13.0, sol.getObjective());
		assertTrue(sol.isOptimal());

	}

	//@Test
	void testSolver5() {

		StringBuilder sb = new StringBuilder();

		sb.append("0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0\n");
		sb.append("0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\n");
		sb.append("0 1 0 0 0 1 0 1 0 0 1 0 0 0 0 0 0 0 0 1\n");
		sb.append("0 0 0 0 0 0 0 0 1 1 1 0 0 0 0 0 0 0 0 1\n");
		sb.append("0 0 0 1 1 0 0 0 0 1 0 0 0 1 0 0 0 0 0 0\n");
		sb.append("0 0 0 0 0 0 0 0 0 1 0 0 1 0 0 1 0 0 0 0\n");
		sb.append("0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\n");
		sb.append("1 0 0 0 0 0 0 0 0 0 0 0 1 1 0 0 0 0 0 0\n");
		sb.append("1 1 1 0 0 0 0 0 0 0 0 0 1 1 0 0 0 0 0 1\n");
		sb.append("0 0 0 0 0 0 0 0 0 1 1 1 0 1 0 0 0 1 0 0\n");
		sb.append("1 0 0 1 1 0 1 0 0 0 0 1 0 0 0 0 0 0 0 0\n");
		sb.append("0 0 0 1 1 0 1 1 0 0 0 0 0 1 0 1 0 1 0 0\n");
		sb.append("0 1 0 0 0 0 0 0 0 1 1 1 1 0 0 0 0 0 0 1\n");
		sb.append("0 0 1 1 0 0 0 1 0 0 0 0 0 0 0 1 0 0 0 1\n");
		sb.append("0 0 0 0 1 1 0 0 1 1 0 0 0 0 0 0 0 0 0 0\n");
		sb.append("0 0 0 0 1 1 1 0 0 0 0 1 0 0 1 0 1 0 0 1\n");
		sb.append("1 0 0 0 1 0 0 0 1 0 1 0 0 0 0 0 0 0 0 0\n");
		sb.append("0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0\n");
		sb.append("1 0 0 0 0 0 1 1 0 1 0 0 0 0 0 0 0 0 0 0\n");
		sb.append("1 0 0 1 0 0 0 0 0 0 0 0 1 0 0 0 1 0 0 0\n");
		
		RPCSolver rpc = new RPCSolver(new Matriz(sb.toString()));
		RPCSolution sol = rpc.solve();

		//assertEquals(13.0, sol.getObjective());
		assertTrue(sol.isOptimal());
	}

	@Test
	void testSolver2() throws TimeLimitExceededException, IloException {

		for (int f = 20; f < 26; f++)
			for (int c = 20; c < 26; c++)
				for (int density = 10; density < 100; density += 10) {
					Matriz matrix = MatrixGenerator.generateRandomMatrix(f, c, density);
					
					RPCSolver rpc = new RPCSolver(matrix);
					RPCSolution sol = rpc.solve();
					
					AllRectanglesSolver solver = new AllRectanglesSolver(matrix);
					assertTrue(solver.solve());
					assertEquals(solver.getObjective(), sol.getObjective());					
				}
	}
}
