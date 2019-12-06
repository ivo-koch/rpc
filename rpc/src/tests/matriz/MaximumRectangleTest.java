package tests.matriz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import rpc.branch.and.price.Matriz;
import rpc.branch.and.price.MaximalRectangleFinder;
import tests.MatrixGenerator;

class MaximumRectangleTest {

	@Test
	void testMaximumRect() {
		
		StringBuilder sb = new StringBuilder();
		sb.append("0 1 0 1 0 0 1\n");
		sb.append("0 1 1 1 1 0 1\n");
		sb.append("1 1 1 1 1 0 1\n");
		sb.append("0 1 1 1 1 0 1\n");
		sb.append("0 1 0 1 0 0 1\n");
		sb.append("0 1 0 1 0 0 1\n");

		Matriz m = new Matriz(sb.toString());
		
		MaximalRectangleFinder mrf = new MaximalRectangleFinder(m);
				
		Rectangle r = mrf.maximumRectangle(new Point (3, 3));
		
		
		assertEquals(new Rectangle(1, 1, 4, 3), r);
	}
	
	

//	@Test
//	void testMaximum2() {
//
//		Random r = new Random();
//		for (int f = 5; f < 10; f++)
//			for (int c = 5; c < 10; c++)
//				for (int density = 10; density < 100; density += 10) {
//					Matriz matrix = MatrixGenerator.generateRandomMatrix(f, c, density);
//					int maxArea = 0;
//					for (Rectangle maximal : matrix.allMaximals())
//						maxArea = Math.max(maxArea, maximal.height * maximal.width);
//					
//					
//					
//					MaximumRectangleFinder mrf = new MaximumRectangleFinder(matrix, p);
//					Rectangle solucion = mrf.maximumRectangle();
//					assertEquals(maxArea, solucion.height*solucion.width);
//				}
//	}

}
