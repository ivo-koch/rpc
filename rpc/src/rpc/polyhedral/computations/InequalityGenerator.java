package rpc.polyhedral.computations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rpc.polyhedral.computations.Inequality.Termino;

@Deprecated
public class InequalityGenerator {

	private int filas;
	private int columnas;

	public InequalityGenerator(int filas, int columnas) {

		this.filas = filas;
		this.columnas = columnas;
	}

	public List<Inequality> buildIneqThm2(int maxPorNivel) {

		List<Inequality> res = buildIneqFwdThm2(maxPorNivel);
		res.addAll(buildIneqBackwThm2(maxPorNivel));

		return res;
	}

	private List<Inequality> buildIneqFwdThm2(int maxPorNivel) {

		List<Inequality> current = forwardIniciales();

		List<Inequality> res = new ArrayList<Inequality>();

		//res.addAll(current);

		boolean termine = false;

		while (!termine) {

			boolean hayCambios = false;

			List<Inequality> nuevos = new ArrayList<Inequality>();
			for (Inequality i : current)
				hayCambios = hayCambios || agregarTuplaForward(nuevos, i);

			termine = !hayCambios;

			Collections.shuffle(nuevos);

			int cant = Math.min(maxPorNivel, nuevos.size());

			for (int j = 0; j < cant; j++)
				res.add(nuevos.get(j));

			current = nuevos;
		}

		return res;
	}

	private List<Inequality> buildIneqBackwThm2(int maxPorNivel) {

		List<Inequality> current = backwardIniciales();

		List<Inequality> res = new ArrayList<Inequality>();

		//res.addAll(current);

		boolean termine = false;

		termine = false;
		while (!termine) {

			boolean hayCambios = false;

			List<Inequality> nuevos = new ArrayList<Inequality>();
			for (Inequality i : current)
				hayCambios = hayCambios || agregarTuplaBackward(nuevos, i);

			termine = !hayCambios;

			Collections.shuffle(nuevos);

			int cant = Math.min(maxPorNivel, nuevos.size());

			for (int j = 0; j < cant; j++)
				res.add(nuevos.get(j));

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
		for (int f1 = f + 1; f1 < filas; f1++)
			for (int c1 = c + 1; c1 < columnas; c1++) {

				// buscamos ahora el segundo 1

				for (int f2 = f1 + 1; f2 < filas; f2++)
					for (int c2 = c1 + 1; c2 < columnas; c2++) {

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
		for (int f1 = f + 1; f1 < filas; f1++)
			for (int c1 = 0; c1 < c; c1++) {

				// buscamos ahora el segundo 1

				for (int f2 = f1 + 1; f2 < filas; f2++)
					for (int c2 = 0; c2 < c1; c2++) {

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
				for (int f1 = f + 1; f1 < filas; f1++)
					for (int c1 = c + 1; c1 < columnas; c1++) {

						// buscamos ahora el segundo 1

						for (int f2 = f1 + 1; f2 < filas; f2++)
							for (int c2 = c1 + 1; c2 < columnas; c2++) {

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
				for (int f1 = f + 1; f1 < filas; f1++)
					for (int c1 = 0; c1 < c; c1++) {

						// buscamos ahora el segundo 1

						for (int f2 = f1 + 1; f2 < filas; f2++)
							for (int c2 = 0; c2 < c1; c2++)
								res.add(buildIneq(f, c, f1, c1, f2, c2));
					}
			}
		return res;
	}

	// Devuelve los coef no nulos de la desigualdad.
	private List<Inequality> theorem2Ineq() {

		List<Inequality> res = new ArrayList<Inequality>();

		// para adelante
		for (int f = 0; f < filas; f++)
			for (int c = 0; c < columnas; c++) {
				if (f == filas - 1 && c == columnas - 1)
					continue;

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

		// para atrás
		for (int f = 0; f < filas; f++)
			for (int c = 0; c < columnas; c++) {
				if (f == 0 && c == 0)
					continue;

				// buscamos ahora el -1
				for (int f1 = f; f1 < filas; f1++)
					for (int c1 = 0; c1 < c; c1++) {

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

	private Inequality buildIneq(int f, int c, int f1, int c1, int f2, int c2) {

		Inequality ineq = new Inequality();
		Termino primer1 = ineq.new Termino(f, c, 1);
		ineq.rhs = 1;

		Termino menos1 = ineq.new Termino(f1, c1, -1);

		Termino segundo1 = ineq.new Termino(f2, c2, 1);

		ineq.terminos.add(primer1);
		ineq.terminos.add(menos1);
		ineq.terminos.add(segundo1);

		return ineq;
	}

	public List<Inequality> buildIneqThm3() {

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
