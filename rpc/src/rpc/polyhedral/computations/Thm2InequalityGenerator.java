package rpc.polyhedral.computations;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Thm2InequalityGenerator extends BaseInequalityGenerator {

	public Thm2InequalityGenerator(int filas, int columnas) {
		super(filas, columnas);
	}

	public List<Inequality> build() {
		List<Inequality> res = new ArrayList<Inequality>();

		Random r = new Random();

		for (int f = 0; f < filas; f++)
			for (int c = 0; c < columnas; c++)
				for (int f1 = f + 1; f1 < filas; f1++)
					for (int c1 = c + 2; c1 < columnas; c1++)

						// considero el rectángulo R1 = \box((f, c), (f1,c1))
						for (int f2 = f1 + 1; f2 < filas; f2++)
							for (int c2 = c; c2 < c1; c2++) {

								// y el rectángulo R2 = \box((f, c), (f2,c2))

								if (f2 - f1 <= 0 || c2 - c <= 0 || f1 - f + 1 <= 0 || c1 - c2 - 1 <= 0)
									continue;
								// ahora, ponemos un -1 en R2 \ R1
								int f3 = f1 + 1 + r.nextInt(f2 - f1);
								int c3 = c + r.nextInt(c2 - c);

								// ahora, ponemos un -1 en R1 \ R2

								int f4 = f + r.nextInt(f1 - f + 1);
								int c4 = c2 + 1 + r.nextInt(c1 - c2 - 1);

								int f5 = f1 + 1 + r.nextInt(f2 - f1);
								int c5 = c2 + 1 + r.nextInt(c1 - c2 - 1);

								/*
								 * for (int f3 = f1 + 1; f3 <= f2; f3++) for (int c3 = c; c3 < c2; c3++)
								 * 
								 * // ahora, ponemos un -1 en R1 \ R2 for (int f4 = f; f4 <= f1; f4++) for (int
								 * c4 = c2 + 1; c4 < c1; c4++)
								 * 
								 * // ahora, ponemos un -1 en R1 \ R2 for (int f5 = f1 + 1; f5 <= f2; f5++) for
								 * (int c5 = c2 + 1; c5 < c1; c5++)
								 */

								Inequality ineq = new Inequality();
								ineq.rhs = 1.0;

								ineq.addTermino(f, c, 1.0);
								ineq.addTermino(f1, c1, 1.0);
								ineq.addTermino(f2, c2, 1.0);

								ineq.addTermino(f3, c3, -1.0);
								ineq.addTermino(f4, c4, -1.0);
								ineq.addTermino(f5, c5, -1.0);
								res.add(ineq);
							}

		return res;

	}

}
