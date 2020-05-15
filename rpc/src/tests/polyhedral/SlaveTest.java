package tests.polyhedral;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import rpc.branch.and.price.Matriz;
import rpc.polyhedral.computations.DualMask;
import rpc.polyhedral.computations.Inequality;
import rpc.polyhedral.computations.ModeloMaster;
import rpc.polyhedral.computations.ModeloSlave;
import tests.MatrixGenerator;

class SlaveTest {

	// @Test
	void test() throws Exception {

		int size = 25;
		double[][] duales = new double[size][size];

		Random r = new Random();

		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				if (r.nextBoolean())
					duales[i][j] = r.nextDouble();
				else
					duales[i][j] = 0;

		ModeloSlave ms = new ModeloSlave(duales);

		ms.buildModel();

		if (!ms.solve())
			throw new RuntimeException("cagada");

		ms.close();
	}

	// @Test
	void test3() throws Exception {

		for (int f = 20; f < 26; f++)
			for (int c = 20; c < 26; c++)
				for (int density = 20; density < 100; density += 20) {
					Matriz matrix = MatrixGenerator.generateRandomMatrix(f, c, density);
					// System.out.println(matrix);
					resolver(matrix);
				}

	}

	private void resolver(Matriz matrix) throws Exception {

		ModeloMaster mm = new ModeloMaster(matrix, null);
		mm.buildModel();

		System.out.println("Modelo construido");
		boolean res = mm.solve();
		if (!res)
			throw new RuntimeException("Master no resuelto");

		double[][] duals = mm.getDualMatrix();

		DualMask.mask(matrix, duals);

//		for (int f = 0; f < duals.length; f++) {
//			for (int c = 0; c < duals[0].length; c++)
//				System.out.print(duals[f][c] + " ");
//			System.out.println();
//		}

		ModeloSlave ms = new ModeloSlave(duals);

		ms.buildModel();

		if (!ms.solve())
			throw new RuntimeException("cagada");

		ms.close();
	}

	// @Test
	void testSingle2() throws Exception {
		double[][] m = new double[5][6];

		m[0] = new double[] { 1, 0, 1, 0, 1 };
		m[1] = new double[] { 0, 0, 0, 1, 0 };
		m[2] = new double[] { 0, 0, 1, 1, 1 };
		m[3] = new double[] { 1, 1, 1, 1, 0 };
		m[4] = new double[] { 0, 0, 1, 1, 0 };

		ModeloSlave ms = new ModeloSlave(m, false);

		ms.buildModel();

		Inequality ineq = new Inequality();
		ineq.rhs = 1.0;
		ineq.addTermino(1, 1, 3.0);
		ineq.addTermino(2, 2, 3.0);
		ineq.addTermino(3, 3, -3.0);
		List<Inequality> res = new ArrayList<Inequality>();
		res.add(ineq);
		ms.agregarIneqs(res);

		if (!ms.solve())
			throw new RuntimeException("cagada");

		ms.printSolution();

		// assertEquals(9.0, ms.getObjective());

		ms.close();

	}

	@Test
	void testSingle3() throws Exception {

		StringBuilder sb = new StringBuilder();

		Random r = new Random();
		for (int f = 10; f < 21; f++) {
			int c = f;
			// for (int c = 15; c < 17; c++)
			for (int density = 20; density < 100; density += 20) {
				Matriz matrix = MatrixGenerator.generateRandomMatrix(f, c, density);

				double[][] m = new double[f][c];

				for (int f1 = 0; f1 < matrix.filas(); f1++)
					for (int c1 = 0; c1 < matrix.columnas(); c1++)
						m[f1][c1] = (r.nextBoolean() ? -1 : 1) * r.nextInt(100);

				// DualMask.mask(matrix, m);
				// System.out.println("SIN DESIGUALDADES");
				// sb.append("f: " + f + " c:" + c + " d: " + density);
				// System.out.print("f: " + f + " c:" + c + " d: " + density);

//				ModeloSlave ms = new ModeloSlave(m, true);
//
//				ms.buildModel();
//				ms.solve();
//
//				System.out.print("Modelo entero: " + f + ", c:" + c + ", d:" + density + ", t:" + ms.info().tiempoRes
//						+ ", sol:" + ms.getObjective());
//
//				ms.close();

				ModeloSlave ms = new ModeloSlave(m);

				ms.buildModel();
				ms.solve();

				// ms.printSolution();
				// sb.append(" Sin des: " + f + " c:" + c + " d: " + ms.info());
				System.out.print(" Sin des t:" + ms.info().tiempoRes + ", sol:" + ms.getObjective());
				ms.close();

				ms = new ModeloSlave(m);

				ms.buildModel();
				ms.agregarDesigualdadesPaper();
				// System.out.println("CON DESIGUALDADES");
				ms.solve();

				// sb.append(" Con des: " + f + " c:" + c + " d: " + ms.info() + "\n");
				System.out.println(", Con des: " + " t:" + ms.info().tiempoRes + ", sol:" + ms.getObjective()
						+ ", cutsTh2: " + ms.cutsTh2 + ", cutsTh3: " + ms.cutsTh3 + ", cutsTh4: " + ms.cutsTh4
						+ ", cutsTh5: " + ms.cutsTh5 + ", cutsTh7: " + ms.cutsTh7 + ", cutsTh8: " + ms.cutsTh9 + ", cutsTh9: " + ms.cutsTh9);
				ms.close();

//					if (!ms.solve())
//						throw new RuntimeException("cagada");

				// ms.printSolution();

				// Rectangle r = matrix.pixelsMaxRectangle();
				// assertEquals(r.height * r.width, ms.getObjective(), 0.001);

			}
		}
		System.out.println(sb.toString());

	}
}
