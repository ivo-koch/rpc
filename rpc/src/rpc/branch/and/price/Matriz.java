package rpc.branch.and.price;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Matriz {

	private final boolean[][] matrix;

	private List<Point> unos;

	private final Rectangle limite;

	protected Matriz() {

		matrix = null;
		limite = null;
	}

	public Matriz(boolean[][] matriz) {
		matrix = matriz;
		limite = new Rectangle(0, 0, matrix[0].length, matrix.length);
	}

	// Devuelve la matriz a partir de una submatriz válida.
	public Matriz(Matriz m, Rectangle r) {
		this.matrix = m.matrix;
		if (r.x < 0 || r.x >= matrix[0].length || r.width > matrix[0].length || r.height > matrix.length)
			throw new RuntimeException("Rango del rectángulo no válido");

		this.limite = r;
	}

	public Matriz(String string) {

		String[] lines = string.split("\n");
		int filas = lines.length;

		matrix = new boolean[filas][];
		unos = new ArrayList<Point>();

		for (int f = 0; f < filas; f++) {
			String[] positions = lines[f].split(" ");
			matrix[f] = new boolean[positions.length];
			for (int c = 0; c < positions.length; c++) {
				matrix[f][c] = positions[c].equals("1") ? true : false;
				if (matrix[f][c])
					unos.add(new Point(c, f));
			}
		}

		limite = new Rectangle(0, 0, matrix[0].length, matrix.length);
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		for (int f = 0; f < filas(); f++) {
			for (int c = 0; c < columnas(); c++)
				if (get(f, c))
					sb.append("1 ");
				else
					sb.append("0 ");
			sb.append("\n");
		}
		return sb.toString();
	}

	public boolean enRango(int i, int j) {
		return (i >= 0 && i < filas() && j >= 0 && j < columnas());
	}

	public boolean get(int i, int j) {
		if (!enRango(i, j))
			throw new RuntimeException("(" + i + "," + j + ") no es válida");

		if (limite.y + i >= matrix.length || limite.x + j >= matrix[0].length)
			throw new RuntimeException("(" + limite.y + " + " + i + "," + limite.x + " + " + j + ") no es válida");

		return matrix[limite.y + i][limite.x + j];
	}

	public List<Point> unos() {
		if (unos == null) {
			unos = new ArrayList<Point>();
			for (int f = 0; f < filas(); f++)
				for (int c = 0; c < columnas(); c++)
					if (get(f, c))
						unos.add(new Point(c, f));
		}
		return Collections.unmodifiableList(unos);
	}

	public int filas() {
		return limite.height;
	}

	public int columnas() {
		return limite.width;
	}

	public int cantUnos() {
		return unos().size();
	}

	public boolean todosUnos(Rectangle r) {
		return todosUnos(r.y, r.x, r.width, r.height);
	}

	public Set<Rectangle> allMaximals() {

		Set<Rectangle> maximals = new HashSet<Rectangle>();

		// genero todos los subrectángulos
		for (int f = 0; f < filas(); f++)
			for (int c = 0; c < columnas(); c++)
				for (int f1 = f; f1 < filas(); f1++)
					for (int c1 = c; c1 < columnas(); c1++) {
						Rectangle r = new Rectangle(c, f, c1 - c + 1, f1 - f + 1);
						if (todosUnos(r) && isMaximal(r))
							maximals.add(r);
					}
		return maximals;
	}

	public double weight(Rectangle r, double[] coef) {

		double weight = 0;
		int i = 0;
		for (Point p : unos()) {
			if (r.contains(p))
				weight += coef[i];
			i++;
		}

		return weight;
	}

	/***
	 * Construye un rectangulo maximal hecho de unos que incluya la posición que nos
	 * indican.
	 * 
	 * @param matrix
	 * @param p
	 * @return
	 */
	public Rectangle buildMaximal(Point p) {

		return buildMaximal(new Rectangle(p.x, p.y, 1, 1));
	}

	public boolean todosUnos(int f, int c, int width, int height) {

		for (int f1 = f; f1 < f + height; f1++)
			for (int c1 = c; c1 < c + width; c1++)
				if (!get(f1, c1))
					return false;

		return true;
	}

	public Rectangle buildMaximal(Rectangle r) {

		int filas = filas();
		int columnas = columnas();

		int minY = r.y;
		int maxY = r.y + r.height;
		int minX = r.x;
		int maxX = r.x + r.width;

		for (int f = r.y - 1; f >= 0 && todosUnos(f, r.x, r.width, 1); f--)
			minY--;

		for (int f = r.y + r.height; f < filas && todosUnos(f, r.x, r.width, 1); f++)
			maxY++;

		for (int c = r.x + r.width; c < columnas && todosUnos(minY, c, 1, maxY - minY); c++)
			maxX++;

		for (int c = r.x - 1; c >= 0 && todosUnos(minY, c, 1, maxY - minY); c--)
			minX--;

		return new Rectangle(minX, minY, maxX - minX, maxY - minY);

	}

	public boolean isMaximal(Rectangle r) {

		// un rectángulo no es maximal si una cara entera del mismo tiene unos
		// adyacentes.

		int filas = filas();
		int columnas = columnas();

		// miramos la cara de arriba.
		if (r.y - 1 >= 0 && todosUnos(r.y - 1, r.x, r.width, 1))
			return false;

		// miramos la cara de abajo
		if (r.y + r.height < filas && todosUnos(r.y + r.height, r.x, r.width, 1))
			return false;

		// miramos la cara de la izquierda
		if (r.x - 1 >= 0 && todosUnos(r.y, r.x - 1, 1, r.height))
			return false;

		// miramos la cara de la derecha
		if (r.x + r.width < columnas && todosUnos(r.y, r.x + r.width, 1, r.height))
			return false;

		return true;
	}

	public List<List<Matriz>> descomponer(int tamanio) {

		List<List<Matriz>> res = new ArrayList<List<Matriz>>();

		for (int f = 0; f < filas(); f += tamanio) {
			List<Matriz> fila = new ArrayList<Matriz>();
			res.add(fila);
			int height = f + tamanio > filas() ? filas() - f : tamanio;
			for (int c = 0; c < columnas(); c += tamanio) {
				int width = c + tamanio > columnas() ? columnas() - c : tamanio;
				Rectangle r = new Rectangle(c, f, width, height);
				fila.add(new Matriz(this, r));
				// System.out.println(r);
			}
		}
		return res;
	}

	public Matriz zoom(int tamanio) {

		List<List<Matriz>> res = descomponer(10);
		boolean[][] mat = new boolean[res.size()][res.get(0).size()];

		int f = 0;
		int c = 0;
		for (List<Matriz> fila : res) {
			for (Matriz elem : fila)
				mat[f][c++] = elem.cantUnos() >= elem.filas() * elem.columnas() / 2.0;
			f++;
			c = 0;
		}

		return new Matriz(mat);
	}

	public List<Rectangle> coverStandard() {
		Integer[] filas = new Integer[filas()];
		Integer[] columnas = new Integer[columnas()];

		for (int f = 0; f < filas(); f++)
			filas[f] = f;

		for (int c = 0; c < columnas(); c++)
			columnas[c] = c;

		return cover(filas, columnas);
	}

	public List<Rectangle> coverInv() {
		Integer[] filas = new Integer[filas()];
		Integer[] columnas = new Integer[columnas()];

		for (int f = 0; f < filas(); f++)
			filas[f] = filas() - 1 - f;

		for (int c = 0; c < columnas(); c++)
			columnas[c] = columnas() - 1 - c;

		return cover(filas, columnas);
	}

	public List<Rectangle> coverInv2() {
		Integer[] filas = new Integer[filas()];
		Integer[] columnas = new Integer[columnas()];

		for (int f = 0; f < filas(); f++)
			filas[f] = f;

		for (int c = 0; c < columnas(); c++)
			columnas[c] = columnas() - 1 - c;

		return cover(filas, columnas);
	}
	
	public List<Rectangle> coverInv3() {
		Integer[] filas = new Integer[filas()];
		Integer[] columnas = new Integer[columnas()];

		for (int f = 0; f < filas(); f++)
			filas[f] = filas() - 1 - f;

		for (int c = 0; c < columnas(); c++)
			columnas[c] = c;

		return cover(filas, columnas);
	}
	public List<Rectangle> coverShuffle() {

		Integer[] filas = new Integer[filas()];
		Integer[] columnas = new Integer[columnas()];

		for (int f = 0; f < filas(); f++)
			filas[f] = f;

		for (int c = 0; c < columnas(); c++)
			columnas[c] = c;

		List<Integer> fList = Arrays.asList(filas);
		Collections.shuffle(fList);

		List<Integer> cList = Arrays.asList(columnas);
		Collections.shuffle(cList);

		return cover(fList.toArray(filas), cList.toArray(columnas));

	}

	private List<Rectangle> cover(Integer[] filas, Integer[] columnas) {
		List<Rectangle> sol = new ArrayList<Rectangle>();

		boolean[][] unosCubiertos = new boolean[filas()][columnas()];

		int cantUnosCubiertos = 0;

		int[] unosPorFilas = new int[filas()];

		for (Point p : unos())
			unosPorFilas[p.y]++;

		while (cantUnosCubiertos < cantUnos()) {

			int fila = -1;
			for (int f = 0; f < filas(); f++)
				if (unosPorFilas[filas[f]] > 0) {
					fila = filas[f];
					break;
				}

			Point p = null;

			for (int c = 0; c < columnas(); c++)
				if (!unosCubiertos[fila][columnas[c]] && this.get(fila, columnas[c]))
				{
					p = new Point(columnas[c], fila);
					break;
				}

			Rectangle r = buildMaximal(p);

			for (int f = r.y; f < r.y + r.height; f++)
				for (int c = r.x; c < r.x + r.width; c++) {
					if (!unosCubiertos[f][c]) {
						cantUnosCubiertos++;
						unosPorFilas[f]--;
					}
					unosCubiertos[f][c] = true;
				}
			sol.add(r);
		}

		return sol;
	}

	// public List<Rectangle> heurCover() {
	// List<Rectangle> sol = new ArrayList<Rectangle>();
	//
	// boolean[][] unosCubiertos = new boolean[filas()][columnas()];
	//
	// int cantUnosCubiertos = 0;
	//
	// int[] unosPorFilas = new int[filas()];
	//
	// for (Point p : unos())
	// unosPorFilas[p.y]++;
	//
	// while (cantUnosCubiertos < cantUnos()) {
	//
	// int fila = -1;
	// for (int f = 0; f < filas(); f++)
	// if (unosPorFilas[f] > 0)
	// fila = f;
	//
	// Point p = null;
	//
	// for (int c = 0; c < columnas(); c++)
	// if (!unosCubiertos[fila][c] && this.get(fila, c))
	// p = new Point(c, fila);
	//
	// Rectangle r = buildMaximal(p);
	//
	// for (int f = r.y; f < r.y + r.height; f++)
	// for (int c = r.x; c < r.x + r.width; c++) {
	// if (!unosCubiertos[f][c]) {
	// cantUnosCubiertos++;
	// unosPorFilas[f]--;
	// }
	// unosCubiertos[f][c] = true;
	// }
	// sol.add(r);
	// }
	//
	// return sol;
	// }

	public Rectangle getLimite() {
		return limite;
	}
}
