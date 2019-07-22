package tests.matriz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import rpc.branch.and.price.MatrizComprimida;

public class MatrizComprimidaTest {

	@Test
	void testConstruccionMatriz() {

		List<Rectangle> rectangulos = new ArrayList<Rectangle>();
		Rectangle r1 = new Rectangle(0, 0, 10, 4);
		Rectangle r2 = new Rectangle(5, 2, 4, 4);
		Rectangle r3 = new Rectangle(3, 3, 3, 3);
		rectangulos.add(r1);
		rectangulos.add(r2);
		rectangulos.add(r3);

		Rectangle f1c1 = new Rectangle(0, 0, 3, 2); // fila 1
		Rectangle f1c2 = new Rectangle(3, 0, 2, 2);
		Rectangle f1c3 = new Rectangle(5, 0, 1, 2);
		Rectangle f1c4 = new Rectangle(6, 0, 3, 2);
		Rectangle f1c5 = new Rectangle(9, 0, 1, 2);
		Rectangle f2c1 = new Rectangle(0, 2, 3, 1); // fila 2
		Rectangle f2c2 = new Rectangle(3, 2, 2, 1);
		Rectangle f2c3 = new Rectangle(5, 2, 1, 1);
		Rectangle f2c4 = new Rectangle(6, 2, 3, 1);
		Rectangle f2c5 = new Rectangle(9, 2, 1, 1);
		Rectangle f3c1 = new Rectangle(0, 3, 3, 1); // fila 3
		Rectangle f3c2 = new Rectangle(3, 3, 2, 1);
		Rectangle f3c3 = new Rectangle(5, 3, 1, 1);
		Rectangle f3c4 = new Rectangle(6, 3, 3, 1);
		Rectangle f3c5 = new Rectangle(9, 3, 1, 1);
		Rectangle f4c1 = new Rectangle(3, 4, 2, 2); // fila 4
		Rectangle f4c2 = new Rectangle(5, 4, 1, 2);
		Rectangle f4c3 = new Rectangle(6, 4, 3, 2);
		
		MatrizComprimida mc = new MatrizComprimida(rectangulos);

		Set<Rectangle> unos = mc.getUnos();
		assertTrue(unos.contains(f1c1)); // fila 1
		assertTrue(unos.contains(f1c2));
		assertTrue(unos.contains(f1c3));
		assertTrue(unos.contains(f1c4));
		assertTrue(unos.contains(f1c5));
		assertTrue(unos.contains(f2c1));
		assertTrue(unos.contains(f2c2));
		assertTrue(unos.contains(f2c3));
		assertTrue(unos.contains(f2c4));
		assertTrue(unos.contains(f2c5));
		assertTrue(unos.contains(f3c1));
		assertTrue(unos.contains(f3c2));
		assertTrue(unos.contains(f3c3));
		assertTrue(unos.contains(f3c4));
		assertTrue(unos.contains(f3c5));
		assertTrue(unos.contains(f4c1));
		assertTrue(unos.contains(f4c2));
		assertTrue(unos.contains(f4c3));
		assertEquals(18, unos.size());

			
		assertTrue(contains(mc, r1, f1c1));		
		assertTrue(contains(mc, r1, f1c2));
		assertTrue(contains(mc, r1, f1c3));
		assertTrue(contains(mc, r1, f1c4));
		assertTrue(contains(mc, r1, f1c5));
		assertTrue(contains(mc, r1, f2c1));		
		assertTrue(contains(mc, r1, f2c2));
		assertTrue(contains(mc, r1, f2c3));
		assertTrue(contains(mc, r1, f2c4));
		assertTrue(contains(mc, r1, f2c5));		
		assertTrue(contains(mc, r1, f3c1));		
		assertTrue(contains(mc, r1, f3c2));
		assertTrue(contains(mc, r1, f3c3));
		assertTrue(contains(mc, r1, f3c4));
		assertTrue(contains(mc, r1, f3c5));			
	
		assertTrue(contains(mc, r2, f2c3));		
		assertTrue(contains(mc, r2, f2c4));
		assertTrue(contains(mc, r2, f3c3));		
		assertTrue(contains(mc, r2, f3c4));
		assertTrue(contains(mc, r2, f4c2));		
		assertTrue(contains(mc, r2, f4c3));

		assertTrue(contains(mc, r3, f3c2));		
		assertTrue(contains(mc, r3, f3c3));
		assertTrue(contains(mc, r3, f4c1));
		assertTrue(contains(mc, r3, f4c2));
		
		assertEquals(1, sizeRects(mc, f1c1));
		assertEquals(1, sizeRects(mc, f1c2));
		assertEquals(1, sizeRects(mc, f1c3));
		assertEquals(1, sizeRects(mc, f1c4));
		assertEquals(1, sizeRects(mc, f1c5));
		assertEquals(1, sizeRects(mc, f2c1));
		assertEquals(1, sizeRects(mc, f2c2));
		assertEquals(2, sizeRects(mc, f2c3));
		assertEquals(2, sizeRects(mc, f2c4));
		assertEquals(1, sizeRects(mc, f2c5));
		assertEquals(1, sizeRects(mc, f3c1));
		assertEquals(2, sizeRects(mc, f3c2));
		assertEquals(3, sizeRects(mc, f3c3));
		assertEquals(2, sizeRects(mc, f3c4));
		assertEquals(1, sizeRects(mc, f3c5));
		assertEquals(1, sizeRects(mc, f4c1));
		assertEquals(2, sizeRects(mc, f4c2));
		assertEquals(1, sizeRects(mc, f4c3));
		
		
	}
	
	private boolean contains(MatrizComprimida mc, Rectangle r, Rectangle uno) {
		return mc.getUnosEnRectangulos().get(uno).contains(r);
	}

	private int sizeRects(MatrizComprimida mc, Rectangle uno) {
		return mc.getUnosEnRectangulos().get(uno).size();
	}
}
