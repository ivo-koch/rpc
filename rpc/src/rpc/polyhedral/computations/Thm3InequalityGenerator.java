package rpc.polyhedral.computations;

import java.util.ArrayList;
import java.util.List;

public class Thm3InequalityGenerator extends BaseInequalityGenerator {

	
	public Thm3InequalityGenerator(int filas, int columnas) {
		super(filas, columnas);
	}

	public List<Inequality> build() {

		List<Inequality> res = new ArrayList<Inequality>();

		for (int f = 0; f < filas; f++)
			for (int c = 0; c < columnas; c++)
				for (int f1 = f; f1 < filas; f1++)
					for (int c1 = c; c1 < columnas; c1++) {

						if (f == f1 && c == c1)
							continue;

						// considero el rectángulo (f, c), (f1,c1)
						for (int f2 = f; f2 <= f1; f2++)
							for (int c2 = c; c2 <= c1; c2++) {
								if ((f2 == f && c2 == c) || (f2 == f1 && c2 == c1))
									continue;

								Inequality ineq = new Inequality();
								ineq.rhs = 1.0;
								ineq.addTermino(f, c, 1.0);
								ineq.addTermino(f1, c1, 1.0);
								ineq.addTermino(f2, c2, -1.0);
								res.add(ineq);
							}
					}
		return res;

	}
}
