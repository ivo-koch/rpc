package tests.rpc.modelos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jorlib.frameworks.columnGeneration.io.TimeLimitExceededException;
import org.junit.jupiter.api.Test;

import rpc.branch.and.price.Matriz;
import rpc.modelos.ModeloABC;
import rpc.modelos.ModeloRC;
import rpc.modelos.ModeloSUDL1;
import rpc.modelos.ModeloXY;
import tests.MatrixGenerator;

class ModelosTest {

	//@Test
	void testSolve() throws Exception {

		for (int f = 3; f < 7; f++)
			for (int c = 3; c < 7; c++)
				for (int density = 10; density < 100; density += 10) {
					Matriz matrix = MatrixGenerator.generateRandomMatrix(f, c, density);
					
					resolverModelos(matrix);
				}
	}
	
	
	
	
	@Test
	public void testSolveSingle() throws Exception
	{
		Matriz matrix = new Matriz(
				
				"1 1 0 1\n" + 
				"1 0 1 1\n" + 
				"0 0 0 1\n" + 
				"0 1 1 1\n" + 
				"0 1 0 1\n");
		
		resolverSUDL(matrix);
	
	}

	private void resolverSUDL(Matriz matrix) throws Exception, TimeLimitExceededException {
		int maximales = matrix.allMaximals().size();
		
		ModeloSUDL1 modelo = new ModeloSUDL1(matrix, maximales);
		modelo.buildModel();
		assertTrue(modelo.solve());
		
		double sol1 = modelo.getObjective();	
		assertTrue(modelo.getSolution().esValida());
		modelo.close();
	}
//		
//		ModeloSUDL modelo2 = new ModeloSUDL(matrix, maximales);
//		modelo2.buildModel();
//		assertTrue(modelo2.solve());
//		
//		double sol2 = modelo2.getObjective();					
//		modelo2.close();
		
	private void resolverModelos(Matriz matrix) throws Exception, TimeLimitExceededException {
		int maximales = matrix.allMaximals().size();
		
		ModeloABC modelo = new ModeloABC(matrix, maximales);
		modelo.buildModel();
		assertTrue(modelo.solve());
		
		double sol1 = modelo.getObjective();	
		assertTrue(modelo.getSolution().esValida());
		modelo.close();
		
		ModeloSUDL1 modelo2 = new ModeloSUDL1(matrix, maximales);
		modelo2.buildModel();
		assertTrue(modelo2.solve());
		
		assertTrue(modelo2.getSolution().esValida());
		double sol2 = modelo2.getObjective();					
		modelo2.close();
		
		ModeloRC modelo3 = new ModeloRC(matrix, maximales);
		modelo3.buildModel();
		assertTrue(modelo3.solve());
		assertTrue(modelo3.getSolution().esValida());
		double sol3 = modelo3.getObjective();					
		modelo3.close();
		
		ModeloXY modelo4 = new ModeloXY(matrix, maximales);
		modelo4.buildModel();
		assertTrue(modelo4.solve());
		assertTrue(modelo4.getSolution().esValida());
		double sol4 = modelo4.getObjective();					
		modelo4.close();
		
		assertEquals(sol1, sol4, 0.001);
		assertEquals(sol1, sol2, 0.001);
		assertEquals(sol3, sol4, 0.001);
		
		
	}

}
