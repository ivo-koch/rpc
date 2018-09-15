package rpc.branch.and.price;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

	/***
	 * Construye un rectangulo maximal hecho de unos que incluya la posición que nos
	 * indican.
	 * 
	 * @param matrix
	 * @param p
	 * @return
	 */
	public Rectangle buildMaximal(Point p) {

		int yMin = Integer.MIN_VALUE;
		int yMax = Integer.MAX_VALUE;

		int xMax = Integer.MAX_VALUE;
		int xMin = p.x;

		int filas = matrix.length;
		int columnas = matrix[0].length;

		int c = p.x;
		// nos movemos a derecha todo lo que podemos, hasta encontrar un 0.
		while (c < columnas && matrix[p.y][c]) {

			// procesamos ahora la columna donde estamos.
			// vamos hacia arriba todo lo que podemos, hasta encontrar un 0
			int f = p.y;

			while (f >= 0 && matrix[f][c])
				f--;

			// es este valor la última columna con un 1, hacia arriba.
			int yCol = f + 1;

			// nos vamos quedando con el maximo, de todas las columnas que miramos.
			yMin = Math.max(yMin, yCol);

			// vamos ahora hacia abajo todo lo que podemos, hasta encontrar un 0
			f = p.y;
			while (f < filas && matrix[f][c])
				f++;

			// es este valor la última columna con un 1, hacia abajo.
			yCol = f - 1;

			// nos vamos quedando con el minimo hacia abajo, de todas las columnas que
			// miramos.
			yMax = Math.min(yMax, yCol);

			c++;
		}

		xMax = c - 1;
		// ahora hacemos lo mismo, pero moviéndonos a izquierda
		c = p.x - 1;
		while (c >= 0 && matrix[p.y][c]) {
			// procesamos ahora la columna donde estamos.
			// vamos hacia arriba todo lo que podemos, hasta encontrar un 0
			int f = p.y;

			while (f >= 0 && matrix[f][c])
				f--;

			// es este valor la última columna con un 1, hacia arriba.
			int yCol = f + 1;

			// nos vamos quedando con el minimo, de todas las columnas que miramos.
			yMin = Math.max(yMin, yCol);

			// vamos ahora hacia abajo todo lo que podemos, hasta encontrar un 0
			f = p.y;
			while (f < filas && matrix[f][c])
				f++;

			// es este valor la última columna con un 1, hacia abajo.
			yCol = f - 1;

			// nos vamos quedando con el minimo hacia abajo, de todas las columnas que
			// miramos.
			yMax = Math.min(yMax, yCol);

			c--;
		}

		xMin = c + 1;

		return new Rectangle(xMin, yMin, (xMax - xMin + 1), (yMax - yMin + 1));
	}

	public Rectangle buildMaximal(Rectangle r) {

		int yMin = Integer.MIN_VALUE;
		int yMax = Integer.MAX_VALUE;

		int xMax = Integer.MAX_VALUE;
		int xMin = r.x;

		int filas = matrix.length;
		int columnas = matrix[0].length;

		int c = r.x;
		// nos movemos a derecha todo lo que podemos, hasta encontrar un 0.
		while (c < columnas && matrix[r.y][c]) {

			// procesamos ahora la columna donde estamos.
			// vamos hacia arriba todo lo que podemos, hasta encontrar un 0
			int f = r.y;

			while (f >= 0 && matrix[f][c])
				f--;

			// es este valor la última columna con un 1, hacia arriba.
			int yCol = f + 1;

			// nos vamos quedando con el minimo, de todas las columnas que miramos.
			yMin = Math.max(yMin, yCol);

			// vamos ahora hacia abajo todo lo que podemos, hasta encontrar un 0
			f = r.y + r.height;
			while (f < filas && matrix[f][c])
				f++;

			// es este valor la última columna con un 1, hacia abajo.
			yCol = f - 1;

			// nos vamos quedando con el minimo hacia abajo, de todas las columnas que
			// miramos.
			yMax = Math.min(yMax, yCol);

			c++;
		}

		xMax = c - 1;
		// ahora hacemos lo mismo, pero moviéndonos a izquierda
		c = r.x - 1;
		while (c >= 0 && matrix[r.y][c]) {
			// procesamos ahora la columna donde estamos.
			// vamos hacia arriba todo lo que podemos, hasta encontrar un 0
			int f = r.y;

			while (f >= 0 && matrix[f][c])
				f--;

			// es este valor la última columna con un 1, hacia arriba.
			int yCol = f + 1;

			// nos vamos quedando con el minimo, de todas las columnas que miramos.
			yMin = Math.max(yMin, yCol);

			// vamos ahora hacia abajo todo lo que podemos, hasta encontrar un 0
			f = r.y + r.height;
			while (f < filas && matrix[f][c])
				f++;

			// es este valor la última columna con un 1, hacia abajo.
			yCol = f - 1;

			// nos vamos quedando con el minimo hacia abajo, de todas las columnas que
			// miramos.
			yMax = Math.min(yMax, yCol);

			c--;
		}

		xMin = c + 1;

		return new Rectangle(xMin, yMin, (xMax - xMin + 1), (yMax - yMin + 1));
	}

	public boolean isMaximal(Rectangle r) {

		// un rectángulo no es maximal si una cara entera del mismo tiene unos
		// adyacentes.

		int filas = matrix.length;
		int columnas = matrix[0].length;

		// miramos la cara de arriba.
		int fila = r.y - 1;

		if (fila >= 0) {
			boolean todosUnos = true;
			for (int c = r.x; c < r.x + r.width; c++)
				if (!matrix[fila][c]) {
					todosUnos = false;
					break;
				}
			if (todosUnos)
				return false;
		}

		fila = r.y + r.height;

		// miramos la cara de abajo
		if (fila < filas) {
			boolean todosUnos = true;
			for (int c = r.x; c < r.x + r.width; c++)
				if (!matrix[fila][c]) {
					todosUnos = false;
					break;
				}
			if (todosUnos)
				return false;
		}

		// miramos la cara de la izquierda
		int columna = r.x - 1;
		if (columna >= 0) {
			boolean todosUnos = true;
			for (int f = r.y; f < r.y + r.height; f++)
				if (!matrix[f][columna]) {
					todosUnos = false;
					break;
				}
			if (todosUnos)
				return false;
		}

		// miramos la cara de la derecha
		columna = r.x + r.width;
		if (columna < columnas) {
			boolean todosUnos = true;
			for (int f = r.y; f < r.y + r.height; f++)
				if (!matrix[f][columna]) {
					todosUnos = false;
					break;
				}
			if (todosUnos)
				return false;
		}

		return true;
	}
}
