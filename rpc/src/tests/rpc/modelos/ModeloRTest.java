package tests.rpc.modelos;

import static org.junit.Assert.assertTrue;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import rpc.branch.and.price.MatrizComprimida;
import rpc.modelos.ModeloR;

public class ModeloRTest {
	
	
	@Test
	void testModeloR() throws Exception {
		
		List<Rectangle> rectangulos = new ArrayList<Rectangle>();
		rectangulos.add(new Rectangle(0, 0, 10, 4));
		rectangulos.add(new Rectangle(5, 2, 4, 4));
		rectangulos.add(new Rectangle(3, 3, 3, 3));
		
		MatrizComprimida mc = new MatrizComprimida(rectangulos);
		
		ModeloR modelo = new ModeloR(mc, 0.01);
		modelo.buildModel();
		assertTrue(modelo.solve());					
		assertTrue(modelo.getSolution().esValida());
		modelo.close();
	}


}
