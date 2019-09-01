package tests.matriz;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import rpc.branch.and.price.Matriz;
import rpc.modelos.Solucion;
import tests.MatrixGenerator;

class MatrizTests {

	//@Test
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

	//@Test
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

	//@Test
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

	// @Test
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

	//@Test
	void testAllMaximals() {

		StringBuilder sb = new StringBuilder();
		sb.append("0 1 0 1 0 0 1\n");
		sb.append("0 1 1 1 1 0 1\n");
		sb.append("1 1 1 1 1 0 1\n");
		sb.append("0 1 1 1 1 0 1\n");
		sb.append("0 1 0 1 0 0 1\n");
		sb.append("0 1 0 1 0 0 1\n");

		Matriz m = new Matriz(sb.toString());

		Set<Rectangle> maximals = m.allMaximals();

		assertEquals(5, maximals.size());
		assertTrue(maximals.contains(new Rectangle(1, 0, 1, 6)));
		assertTrue(maximals.contains(new Rectangle(3, 0, 1, 6)));
		assertTrue(maximals.contains(new Rectangle(6, 0, 1, 6)));
		assertTrue(maximals.contains(new Rectangle(0, 2, 5, 1)));
		assertTrue(maximals.contains(new Rectangle(1, 1, 4, 3)));
	}

	//@Test
	void testDescomponer() {

		StringBuilder sb = new StringBuilder();
		sb.append("0 1 0 1 0 0 1\n");
		sb.append("0 1 1 1 1 0 1\n");
		sb.append("1 1 1 1 1 0 1\n");
		sb.append("0 1 1 1 1 0 1\n");
		sb.append("0 1 0 1 0 0 1\n");
		sb.append("0 1 0 1 0 0 1\n");

		Matriz m = new Matriz(sb.toString());
		List<List<Matriz>> lista = m.descomponer(3);

		Matriz m1 = lista.get(0).get(0);
		Matriz m2 = lista.get(0).get(1);
		Matriz m3 = lista.get(0).get(2);
		Matriz m4 = lista.get(1).get(0);
		Matriz m5 = lista.get(1).get(1);
		Matriz m6 = lista.get(1).get(2);

		assertEquals(m1.filas(), 3);
		assertEquals(m1.columnas(), 3);
		assertEquals(m2.filas(), 3);
		assertEquals(m2.columnas(), 3);
		assertEquals(m3.filas(), 3);
		assertEquals(m3.columnas(), 1);
		assertEquals(m4.filas(), 3);
		assertEquals(m4.columnas(), 3);
		assertEquals(m5.filas(), 3);
		assertEquals(m5.columnas(), 3);
		assertEquals(m6.filas(), 3);
		assertEquals(m6.columnas(), 1);
	}

	//@Test
	void testHeurCover() {

		StringBuilder sb = new StringBuilder();
		sb.append("0 1 0 1 0 0 1\n");
		sb.append("0 1 1 1 1 0 1\n");
		sb.append("1 1 1 1 1 0 1\n");
		sb.append("0 1 1 1 1 0 1\n");
		sb.append("0 1 0 1 0 0 1\n");
		sb.append("0 1 0 1 0 0 1\n");

		Matriz m = new Matriz(sb.toString());

		Solucion res = new Solucion(m, m.coverStandardM());
		Solucion res1 = new Solucion(m, m.coverInvM());
		Solucion res2 = new Solucion(m, m.coverInv2M());
		Solucion res3 = new Solucion(m, m.coverInv3M());
		Solucion res4 = new Solucion(m, m.coverShuffleM());		

		assertTrue(res.esValida());
		assertTrue(res1.esValida());
		assertTrue(res2.esValida());
		assertTrue(res3.esValida());
		assertTrue(res4.esValida());

	}
	
	@Test
	void testHeurCover4() {

		StringBuilder sb = new StringBuilder();
		sb.append("0 1 0 0 1 0 0 0 0 0\n");
		sb.append("0 0 0 0 0 0 1 0 0 0\n");
		sb.append("0 0 0 0 1 1 0 0 0 0\n");
		sb.append("1 0 0 0 1 0 0 0 1 0\n");
		sb.append("0 0 0 0 0 0 1 0 0 0\n");
		sb.append("0 0 0 0 1 1 0 0 0 0\n");
		sb.append("1 1 1 1 0 0 1 0 0 0\n");
		sb.append("0 0 0 0 0 0 0 0 1 0\n");
		sb.append("0 0 0 0 0 0 1 0 0 0\n");
		sb.append("1 0 0 0 0 0 0 0 0 1\n");

		Matriz m = new Matriz(sb.toString());

		Solucion res = new Solucion(m, m.coverStandardM());
		Solucion res1 = new Solucion(m, m.coverInvM());
		Solucion res2 = new Solucion(m, m.coverInv2M());
		Solucion res3 = new Solucion(m, m.coverInv3M());
		Solucion res4 = new Solucion(m, m.coverShuffleM());		

		assertTrue(res.esValida());
		assertTrue(res1.esValida());
		assertTrue(res2.esValida());
		assertTrue(res3.esValida());
		assertTrue(res4.esValida());

	}


	@Test
	void testHeurCover2() {

		for (int f = 10; f < 150; f++)
			for (int c = 10; c < 150; c++)
				for (int density = 10; density < 100; density += 10) {
					Matriz m = MatrixGenerator.generateRandomMatrix(f, c, density);					
					System.out.println(m.toString());
					Solucion res = new Solucion(m, m.coverStandardM());
					Solucion res1 = new Solucion(m, m.coverInvM());
					Solucion res2 = new Solucion(m, m.coverInv2M());
					Solucion res3 = new Solucion(m, m.coverInv3M());
					Solucion res4 = new Solucion(m, m.coverShuffleM());		

					assertTrue(res.esValida());
					assertTrue(res1.esValida());
					assertTrue(res2.esValida());
					assertTrue(res3.esValida());
					assertTrue(res4.esValida());
				}
	}

	//@Test
	void testBuildMaximal3() {

		StringBuilder sb = new StringBuilder();
		sb.append("0 1 0 1 0 0 1 0\n");
		sb.append("0 1 1 1 1 0 1 1\n");
		sb.append("1 1 1 1 1 1 1 0\n");
		sb.append("0 1 0 1 1 1 1 1\n");
		sb.append("1 1 0 1 1 0 1 1\n");
		sb.append("0 1 1 1 0 0 1 0\n");

		Point p = new Point(5, 5);

		Matriz matrix = new Matriz(sb.toString());
		Rectangle res = matrix.buildMaximal(p);

		assertTrue(matrix.isMaximal(res));
	}
}
