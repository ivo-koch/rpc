package rpc.polyhedral.computations;

import java.util.ArrayList;
import java.util.List;

public class Thm8InequalityGenerator extends BaseInequalityGenerator {

	public Thm8InequalityGenerator(int filas, int columnas) {
		super(filas, columnas);
	}

	public List<Inequality> build() {

		List<Inequality> res = new ArrayList<Inequality>();

		for (int i3 = 0; i3 < filas; i3++)
			for (int i1 = i3; i1 < filas; i1++)
				for (int i2 = i1 + 1; i2 < filas; i2++)
					for (int j1 = 0; j1 < columnas; j1++)
						for (int j2 = j1 + 1; j2 < columnas; j2++)
							for (int j3 = j2; j3 < columnas; j3++) {
								
								Inequality ineq = new Inequality();
								ineq.rhs = 1.0;
								ineq.addTermino(i1, j1, 1.0);
								ineq.addTermino(i2, j2, 1.0);
								ineq.addTermino(i3, j3, 1.0);
								ineq.addTermino(i1, j2, -2.0);
								
								res.add(ineq);
							}

		return res;

	}
}
