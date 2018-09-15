package tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Random;

import org.junit.jupiter.api.Test;

import rpc.branch.and.price.Matriz;


class RectangleUtilsTest {

	@Test
	void testIsMaximal() {

		StringBuilder sb = new StringBuilder();
		sb.append("0 1 0 1 0 0 1\n");
		sb.append("0 1 1 1 1 0 1\n");
		sb.append("1 1 1 1 1 0 1\n");
		sb.append("0 1 1 1 1 0 1\n");
		sb.append("0 1 0 1 0 0 1\n");
		sb.append("0 1 0 1 0 0 1\n");

		Rectangle r = new Rectangle(1, 1, 4, 3);

		assertTrue(new Matriz(sb.toString()).isMaximal(r));

	}

	@Test
	void testIsMaximal2() {

		StringBuilder sb = new StringBuilder();
		sb.append("0 1 0 1 0 0 1\n");
		sb.append("0 1 1 1 1 0 1\n");
		sb.append("1 1 1 1 1 0 1\n");
		sb.append("0 1 1 1 1 1 1\n");
		sb.append("1 1 0 1 0 0 1\n");
		sb.append("0 1 1 1 0 0 1\n");

		Rectangle r = new Rectangle(2, 2, 3, 2);

		assertFalse(new Matriz(sb.toString()).isMaximal(r));
	}

	@Test
	void testBuildMaximal() {

		StringBuilder sb = new StringBuilder();
		sb.append("0 1 0 1 0 0 1 0\n");
		sb.append("0 1 1 1 1 0 1 1\n");
		sb.append("1 1 1 1 1 1 1 0\n");
		sb.append("0 1 0 1 1 1 1 1\n");
		sb.append("1 1 0 1 1 0 1 1\n");
		sb.append("0 1 1 1 0 0 1 0\n");		

		Point p = new Point(4, 3);
		
		Matriz matrix = new Matriz(sb.toString());
		Rectangle res = matrix.buildMaximal(p);

		assertTrue(matrix.isMaximal(res));
	}

	@Test
	void testBuildMaximal2() {

		Random r = new Random();
		for (int f = 10; f < 150; f++)
			for (int c = 10; c < 150; c++)
				for (int density = 10; density < 100; density += 10) {
					Matriz matrix = MatrixGenerator.generateRandomMatrix(f, c, density);
					Rectangle res = matrix.buildMaximal(matrix.unos().get((r.nextInt(matrix.unos().size()))));
					assertTrue(matrix.isMaximal(res));
				}
	}

}
