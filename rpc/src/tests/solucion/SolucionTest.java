package tests.solucion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import rpc.branch.and.price.Matriz;
import rpc.modelos.InfoResolucion;
import rpc.modelos.Solucion;

public class SolucionTest {

	@Test
	public void mergeADerechaTest() throws Exception {

		StringBuilder sb = new StringBuilder();
		sb.append("0 1 0 1 0 0 1 1 0 0 0 0 0\n");
		sb.append("0 1 1 1 1 0 0 1 0 1 1 1 0\n");
		sb.append("0 1 1 1 1 1 1 1 0 1 1 1 0\n");
		sb.append("0 1 1 1 1 1 0 1 0 1 1 1 0\n");
		sb.append("0 1 0 1 0 1 1 1 0 0 0 0 0\n");	
		
		Matriz m = new Matriz(sb.toString()); 
		Matriz m1 = new Matriz(m, new Rectangle(0, 0, 6, 5));
		Matriz m2 = new Matriz(m, new Rectangle(6, 0, 7, 5));
		
		
		List<Rectangle> rects = new ArrayList<Rectangle>();
		rects.add(new Rectangle(1, 0, 1, 5));
		rects.add(new Rectangle(1, 1, 4, 3));
		rects.add(new Rectangle(3, 0, 1, 5));
		rects.add(new Rectangle(5, 2, 1, 3));		
				
		Solucion s1 = new Solucion(m1, rects);
		assertTrue(s1.esValida());
				
		List<Rectangle> rects2 = new ArrayList<Rectangle>();
		rects2.add(new Rectangle(1, 0, 1, 5));
		rects2.add(new Rectangle(0, 0, 1, 1));
		rects2.add(new Rectangle(0, 2, 2, 1));
		rects2.add(new Rectangle(0, 4, 2, 1));
		rects2.add(new Rectangle(3, 1, 3, 3));
		
		Solucion s2 = new Solucion(m2, rects2);
		assertTrue(s2.esValida());
		
		//0 1 0 1 0 0 | 1 1 0 0 0 0 0
		//0 1 1 1 1 0 | 0 1 0 1 1 1 0
		//0 1 1 1 1 1 | 1 1 0 1 1 1 0
		//0 1 1 1 1 1 | 0 1 0 1 1 1 0
		//0 1 0 1 0 1 | 1 1 0 0 0 0 0	
			
		Solucion s3 = s1.mergeADerecha(s2, new InfoResolucion());
		
		assertEquals(5, s3.getMatriz().filas());
		assertEquals(13, s3.getMatriz().columnas());
		
		//rects intactos.
		assertTrue(s3.getRectangulos().contains(new Rectangle(1, 0, 1, 5)));
		assertTrue(s3.getRectangulos().contains(new Rectangle(1, 1, 4, 3)));
		assertTrue(s3.getRectangulos().contains(new Rectangle(3, 0, 1, 5)));
		
		//rects intactos con offset
		assertTrue(s3.getRectangulos().contains(new Rectangle(9, 1, 3, 3)));
		assertTrue(s3.getRectangulos().contains(new Rectangle(7,0, 1, 5)));
		
		//rects en el borde
		assertTrue(s3.getRectangulos().contains(new Rectangle(5, 2, 1, 3)));
		assertTrue(s3.getRectangulos().contains(new Rectangle(5, 4, 3, 1)));
		assertTrue(s3.getRectangulos().contains(new Rectangle(1, 2, 7, 1)));
		assertTrue(s3.getRectangulos().contains(new Rectangle(6, 0, 2, 1)));
		
		assertTrue(s3.esValida());
		
	}

	

}
