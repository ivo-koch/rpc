package tests.rpc.modelos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Rectangle;

import org.jorlib.frameworks.columnGeneration.io.TimeLimitExceededException;
import org.junit.jupiter.api.Test;

import rpc.branch.and.price.Matriz;
import rpc.modelos.ModeloMaximal;
import rpc.modelos.ModeloSUDL1;
import rpc.modelos.ModeloXY;
import tests.MatrixGenerator;

class ModelosTest {

	@Test
	void testSolve() throws Exception {

		for (int f = 5; f < 10; f++)
			for (int c = 5; c < 10; c++)
				for (int density = 20; density < 100; density += 20) {
					Matriz matrix = MatrixGenerator.generateRandomMatrix(f, c, density);
					System.out.println(matrix);
					
				
					resolverModelos(matrix);

					/*ModeloMaximal mm = new ModeloMaximal(matrix, null, null);
					mm.buildModel();
					if (!mm.solve())
						throw new RuntimeException("El modelo no resolvió");
					
					System.out.println("Resolvimos f:" + f + "c:" + c + "d:" + density);
					//Rectangle r = mm.getSolution();
					mm.close();*/
				}
	}

	// @Test
	public void testSolveSingle() throws Exception {
		Matriz matrix = new Matriz(

				"1 1 0 1\n" + "1 0 1 1\n" + "0 0 0 1\n" + "0 1 1 1\n" + "0 1 0 1\n");

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
	// ModeloSUDL modelo2 = new ModeloSUDL(matrix, maximales);
	// modelo2.buildModel();
	// assertTrue(modelo2.solve());
	//
	// double sol2 = modelo2.getObjective();
	// modelo2.close();

	private void resolverModelos(Matriz matrix) throws Exception, TimeLimitExceededException {
		int maximales = matrix.allMaximals().size();
		//
		// ModeloABC modelo = new ModeloABC(matrix, maximales);
		// modelo.buildModel();
		// assertTrue(modelo.solve());
		//
		// double sol1 = modelo.getObjective();
		// assertTrue(modelo.getSolution().esValida());
		// modelo.close();
		//
		// ModeloSUDL1 modelo2 = new ModeloSUDL1(matrix, maximales);
		// modelo2.buildModel();
		// assertTrue(modelo2.solve());
		//
		// assertTrue(modelo2.getSolution().esValida());
		// double sol2 = modelo2.getObjective();
		// modelo2.close();
		//
		// ModeloRC modelo3 = new ModeloRC(matrix, maximales);
		// modelo3.buildModel();
		// assertTrue(modelo3.solve());
		// assertTrue(modelo3.getSolution().esValida());
		// double sol3 = modelo3.getObjective();
		// modelo3.close();

		ModeloXY modelo4 = new ModeloXY(matrix, maximales);
		modelo4.buildModel();
		assertTrue(modelo4.solve());
		assertTrue(modelo4.getSolution().esValida());
		double sol4 = modelo4.getObjective();
		modelo4.close();

		ModeloSUDL1 modelo5 = new ModeloSUDL1(matrix, maximales);
		modelo5.buildModel();
		assertTrue(modelo5.solve());
		assertTrue(modelo5.getSolution().esValida());
		double sol5 = modelo5.getObjective();
		modelo5.close();

		assertEquals(sol4, sol5, 0.001);
	}

}
