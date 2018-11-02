package rpc.branch.and.price;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Matriz {

	private boolean[][] matrix;

	private List<Point> unos = new ArrayList<Point>();

	public Point MijConCubrimientoFrac;
	public double valorCubrimientoFrac;
	public Interval intActual;

	public Matriz(boolean[][] matriz) {
		this.matrix = matriz;
		for (int f = 0; f < filas(); f++)
			for (int c = 0; c < columnas(); c++)
				if (matriz[f][c])
					unos.add(new Point(c, f));
	}

	public Matriz(String string) {

		String[] lines = string.split("\n");
		int filas = lines.length;

		matrix = new boolean[filas][];

		for (int f = 0; f < filas; f++) {
			String[] positions = lines[f].split(" ");
			matrix[f] = new boolean[positions.length];
			for (int c = 0; c < positions.length; c++) {
				matrix[f][c] = positions[c].equals("1") ? true : false;
				if (matrix[f][c])
					unos.add(new Point(c, f));
			}
		}
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		for (int f = 0; f < filas(); f++) {
			for (int c = 0; c < columnas(); c++)
				if (matrix[f][c])
					sb.append("1 ");
				else
					sb.append("0 ");
			sb.append("\n");
		}
		return sb.toString();
	}

	public boolean get(int i, int j) {
		return matrix[i][j];
	}

	public List<Point> unos() {
		return Collections.unmodifiableList(unos);
	}

	public int filas() {
		return matrix.length;
	}

	public int columnas() {
		return matrix[0].length;
	}

	public int cantUnos() {
		return unos.size();
	}

	private boolean todosUnos(Rectangle r) {
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
		for (Point p : unos) {
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

	private boolean todosUnos(int f, int c, int width, int height) {

		for (int f1 = f; f1 < f + height; f1++)
			for (int c1 = c; c1 < c + width; c1++)
				if (!matrix[f1][c1])
					return false;

		return true;
	}

	public Rectangle buildMaximal(Rectangle r) {

		int filas = matrix.length;
		int columnas = matrix[0].length;

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

		int filas = matrix.length;
		int columnas = matrix[0].length;

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
}
