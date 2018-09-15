package tests;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import rpc.branch.and.price.Matriz;

public class MatrixGenerator {

	public static Matriz generateRandomMatrix(int filas, int columnas, int density) {
		boolean[][] res = new boolean[filas][columnas];

		List<Point> puntos = new ArrayList<Point>();
		for (int f = 0; f < filas; f++)
			for (int c = 0; c < columnas; c++)
				puntos.add(new Point(c, f));

		int cantUnos = (int) Math.floor(filas * columnas * density / 100.0);

		Random r = new Random();

		for (int i = 1; i <= cantUnos; i++) {
			int pos = r.nextInt(puntos.size());
			Point p = puntos.get(pos);
			res[p.y][p.x] = true;
			puntos.remove(pos);
		}

		return new Matriz(res);
	}
}
