package rpc.polyhedral.computations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rpc.polyhedral.computations.Inequality.Termino;

public class Thm4InequalityGenerator extends BaseInequalityGenerator {


	private int maxPorNivel = 500000;
	
	public Thm4InequalityGenerator(int filas, int columnas) {
		super(filas, columnas);
	}

	public List<Inequality> build() {

		List<Inequality> res = buildIneqFwdThm4(maxPorNivel);
		res.addAll(buildIneqBackwThm4(maxPorNivel));

		return res;
	}
	
	private List<Inequality> buildIneqFwdThm4(int maxPorNivel) {

		List<Inequality> current = forwardIniciales();

		List<Inequality> res = new ArrayList<Inequality>();

		res.addAll(current);
		boolean termine = false;

		while (!termine) {

			boolean hayCambios = false;

			List<Inequality> nuevos = new ArrayList<Inequality>();
			for (Inequality i : current)
				hayCambios = hayCambios || agregarTuplaForward(nuevos, i);

			termine = !hayCambios;

			/*Collections.shuffle(nuevos);

			int cant = Math.min(maxPorNivel, nuevos.size());

			for (int j = 0; j < cant; j++)
				res.add(nuevos.get(j));*/
			
			res.addAll(nuevos);

			current = nuevos;
		}

		return res;
	}

	private List<Inequality> buildIneqBackwThm4(int maxPorNivel) {

		List<Inequality> current = backwardIniciales();

		List<Inequality> res = new ArrayList<Inequality>();

		res.addAll(current);

		boolean termine = false;

		termine = false;
		while (!termine) {

			boolean hayCambios = false;

			List<Inequality> nuevos = new ArrayList<Inequality>();
			for (Inequality i : current)
				hayCambios = hayCambios || agregarTuplaBackward(nuevos, i);

			termine = !hayCambios;

			/*Collections.shuffle(nuevos);

			/*int cant = Math.min(maxPorNivel, nuevos.size());

			/*for (int j = 0; j < cant; j++)
				res.add(nuevos.get(j));*/
			
			res.addAll(nuevos);
			current = nuevos;
		}

		return res;
	}

	// Devuelve los coef no nulos de la desigualdad.
	private boolean agregarTuplaForward(List<Inequality> ineqs, Inequality ineq) {

		// acá está el último 1
		int f = ineq.terminos.get(ineq.terminos.size() - 1).f;
		int c = ineq.terminos.get(ineq.terminos.size() - 1).c;

		boolean encontramos = false;
		// buscamos ahora el -1
		for (int f1 = f; f1 < filas; f1++)
			for (int c1 = c; c1 < columnas; c1++) {
				if (f1 == f && c1 == c)
					continue;

				// buscamos ahora el segundo 1

				for (int f2 = f1; f2 < filas; f2++)
					for (int c2 = c1; c2 < columnas; c2++) {
						
						if (f2 == f1 && c2 == c1)
							continue;

						Inequality nueva = ineq.clonar();

						nueva.addTermino(f1, c1, -1);
						nueva.addTermino(f2, c2, 1);

						ineqs.add(nueva);
						encontramos = true;

					}
			}

		return encontramos;
	}

	private boolean agregarTuplaBackward(List<Inequality> ineqs, Inequality ineq) {

		boolean encontramos = false;
		// acá está el último 1
		int f = ineq.terminos.get(ineq.terminos.size() - 1).f;
		int c = ineq.terminos.get(ineq.terminos.size() - 1).c;

		// buscamos ahora el -1
		for (int f1 = f; f1 < filas; f1++)
			for (int c1 = 0; c1 <= c; c1++) {
				
				if (f1 == f && c1 == c)
					continue;

				// buscamos ahora el segundo 1

				for (int f2 = f1; f2 < filas; f2++)
					for (int c2 = 0; c2 <= c1; c2++) {

						if (f2 == f1 && c2 == c1)
							continue;

						
						Inequality nueva = ineq.clonar();

						nueva.addTermino(f1, c1, -1);
						nueva.addTermino(f2, c2, 1);

						ineqs.add(nueva);
						encontramos = true;

					}
			}
		return encontramos;
	}

	// Devuelve los coef no nulos de la desigualdad.
	private List<Inequality> forwardIniciales() {

		List<Inequality> res = new ArrayList<Inequality>();

		// para adelante
		for (int f = 0; f < filas; f++)
			for (int c = 0; c < columnas; c++) {

				// buscamos ahora el -1
				for (int f1 = f; f1 < filas; f1++)
					for (int c1 = c; c1 < columnas; c1++) {
						if (f1 == f && c1 == c)
							continue;

						// buscamos ahora el segundo 1

						for (int f2 = f1; f2 < filas; f2++)
							for (int c2 = c1; c2 < columnas; c2++) {
								if (f2 == f1 && c2 == c1)
									continue;

								res.add(buildIneq(f, c, f1, c1, f2, c2));

							}
					}
			}

		return res;
	}

	// Devuelve los coef no nulos de la desigualdad.
	public List<Inequality> backwardIniciales() {

		List<Inequality> res = new ArrayList<Inequality>();
		// para atrás
		for (int f = 0; f < filas; f++)
			for (int c = 0; c < columnas; c++) {

				// buscamos ahora el -1
				for (int f1 = f; f1 < filas; f1++)
					for (int c1 = 0; c1 <= c; c1++) {
						
						if (f1 == f && c1 == c)
							continue;


						// buscamos ahora el segundo 1

						for (int f2 = f1; f2 < filas; f2++)
							for (int c2 = 0; c2 <= c1; c2++) {
								
								if (f2 == f1 && c2 == c1)
									continue;

								res.add(buildIneq(f, c, f1, c1, f2, c2));
							}
					}
			}
		return res;
	}	

	
}
