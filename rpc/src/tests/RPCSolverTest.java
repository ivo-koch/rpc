package tests;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Rectangle;
import java.util.Random;

import org.junit.jupiter.api.Test;

import rpc.branch.and.price.Matriz;
import rpc.branch.and.price.RPCSolution;
import rpc.branch.and.price.RPCSolver;

class RPCSolverTest {

	@Test
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

	@Test
	void testSolver2() {

		for (int f = 40; f < 41; f++)
			for (int c = 40; c < 41; c++)
				for (int density = 10; density < 100; density += 10) {
					Matriz matrix = MatrixGenerator.generateRandomMatrix(f, c, density);
					RPCSolver rpc = new RPCSolver(matrix);
					RPCSolution sol = rpc.solve();
					assertTrue (!sol.getColumns().isEmpty());
				}
	}
}
