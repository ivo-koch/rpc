package rpc.polyhedral.computations;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Thm5InequalityGenerator extends BaseInequalityGenerator {

	public Thm5InequalityGenerator(int filas, int columnas) {
		super(filas, columnas);
	}

	public List<Inequality> build() {

		List<Inequality> res = new ArrayList<Inequality>();

		Random r = new Random();

		for (int i1 = 0; i1 < filas; i1++)
			for (int j1 = 0; j1 < columnas; j1++)
				for (int i2 = i1; i2 < filas; i2++)
					for (int j3 = j1 + 1; j3 < columnas; j3++)
						for (int i3 = i2; i3 < filas; i3++)
							for (int j2 = j3 + 1; j2 < columnas; j2++) {

								if (i2 - i1 + 1 <= 0 || j3 - j1 + 1 <= 0 || i3 - i2 <= 0 || j2 - j3 <= 0)
									continue;

								// q1 es el primer -1
								int q1f = i1 + r.nextInt(i2 - i1 + 1);
								int q1c = j1 + r.nextInt(j3 - j1 + 1);

								// q2 es el segundo -1
								int q2f = i2 + 1 + r.nextInt(i3 - i2);
								int q2c = j3 + 1 + r.nextInt(j2 - j3);

								Inequality ineq = new Inequality();
								ineq.rhs = 1.0;
								ineq.addTermino(i1, j1, 1.0);
								ineq.addTermino(i2, j2, 1.0);
								ineq.addTermino(i3, j3, 1.0);
								ineq.addTermino(q1f, q1c, -1.0);
								ineq.addTermino(q2f, q2c, -1.0);
								res.add(ineq);
							}

		return res;

	}

}
