package rpc.polyhedral.computations;

import java.util.ArrayList;
import java.util.List;

public class Thm9InequalityGenerator extends BaseInequalityGenerator {

	public Thm9InequalityGenerator(int filas, int columnas) {
		super(filas, columnas);
	}

	public List<Inequality> build() {

		List<Inequality> res = new ArrayList<Inequality>();

		//el -1
		for (int i = 1; i < filas; i++) 
			for (int j = 1; j < columnas; j++) 
				//primer 1
				for (int j1 = 0; j1 < j; j1++)
						//segundo 1
						for (int i2 = 0; i2 < i; i2++)
							//tercer 1
							for (int j2 = j + 1; j2 < columnas; j2++)
								//cuarto 1
								for (int i3 = i + 1; i3 < filas; i3++){
			
								
								Inequality ineq = new Inequality();
								ineq.rhs = 1.0;
								ineq.addTermino(i, j, -3.0);
								ineq.addTermino(i, j1, 1.0);
								ineq.addTermino(i2, j, 1.0);
								ineq.addTermino(i, j2, 1.0);
								ineq.addTermino(i3, j, 1.0);
								
								res.add(ineq);
							}

		return res;

	}
}
