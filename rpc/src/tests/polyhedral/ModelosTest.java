package tests.polyhedral;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jorlib.frameworks.columnGeneration.io.TimeLimitExceededException;
import org.junit.jupiter.api.Test;

import rpc.branch.and.price.Matriz;
import rpc.modelos.ModeloXY;
import rpc.polyhedral.computations.ModeloMaster;
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
				}
	}

	private void resolverModelos(Matriz matrix) throws Exception, TimeLimitExceededException {
		int maximales = matrix.allMaximals().size();


		ModeloXY modelo4 = new ModeloXY(matrix, maximales);
		modelo4.buildModel();
		assertTrue(modelo4.solve());
		assertTrue(modelo4.getSolution().esValida());
		double sol4 = modelo4.getObjective();
		modelo4.close();

		ModeloMaster modelo5 = new ModeloMaster(matrix, null);
		modelo5.buildModel();
		assertTrue(modelo5.solve());
		//assertTrue(modelo5.getSolution().esValida());
		double sol5 = modelo5.getObjective();
		modelo5.close();

		assertEquals(sol4, sol5, 0.001);
	}

}
