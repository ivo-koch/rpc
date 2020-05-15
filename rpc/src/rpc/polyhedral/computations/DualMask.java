package rpc.polyhedral.computations;

import rpc.branch.and.price.Matriz;

public class DualMask {

	// M grande para enmascarar los ceros.
	private static double M = -100000;

	public static void mask(Matriz matrix, double[][] duals) {

		for (int f = 0; f < matrix.filas(); f++)
			for (int c = 0; c < matrix.columnas(); c++)
				if (!matrix.get(f, c))
					duals[f][c] = M;
	}
}
