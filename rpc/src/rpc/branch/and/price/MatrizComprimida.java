package rpc.branch.and.price;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MatrizComprimida {

	private List<Integer> xs = new ArrayList<Integer>();
	private List<Integer> ys = new ArrayList<Integer>();
	private Set<Rectangle> unos = new HashSet<Rectangle>();
	private Map<Rectangle, Set<Rectangle>> unosEnRectangulos = new HashMap<Rectangle, Set<Rectangle>>();
	private Map<Point, Rectangle> unosPorTopLeft = new HashMap<Point, Rectangle>();
	private List<Rectangle> rectangulosOriginales;

	public MatrizComprimida(List<Rectangle> rectangulos) {

		this.rectangulosOriginales = rectangulos;
		for (Rectangle r : rectangulos) {
			xs.add(r.x);
			xs.add(r.x + r.width);
			ys.add(r.y);
			ys.add(r.y + r.height);
		}

		// eliminamos los duplicados
		xs = xs.stream().distinct().collect(Collectors.toList());

		ys = ys.stream().distinct().collect(Collectors.toList());

		// ordenamos los puntos
		Collections.sort(xs);
		Collections.sort(ys);

		// y los unos de la matriz
		for (Rectangle r : rectangulos) {
			Set<Rectangle> unosEnRect = new HashSet<Rectangle>();
			boolean procesandoFilas = true;

			int iY = ys.indexOf(r.y);
			while (procesandoFilas) {

				int y = ys.get(iY);

				if (iY == ys.size() - 1 || ys.get(iY + 1) > r.y + r.height) {
					procesandoFilas = false;
					break;
				}

				int iX = xs.indexOf(r.x);
				boolean procesandoColumnas = true;
				while (procesandoColumnas) {
					int x = xs.get(iX);

					if (iX == xs.size() - 1 || xs.get(iX + 1) > r.x + r.width) {
						procesandoColumnas = false;
						break;
					}

					Rectangle nuevoUno = new Rectangle(x, y, xs.get(iX + 1) - x, ys.get(iY + 1) - y);
					unosEnRect.add(nuevoUno);
					unos.add(nuevoUno);
					unosPorTopLeft.put(new Point(x, y), nuevoUno);
					if (!unosEnRectangulos.containsKey(nuevoUno))
						unosEnRectangulos.put(nuevoUno, new HashSet<Rectangle>());

					unosEnRectangulos.get(nuevoUno).add(r);

					iX++;
				}
				iY++;
			}
		}
	}

	public Set<Rectangle> splitEachRectangle(int step) {

		Set<Rectangle> rects = new HashSet<Rectangle>();
		for (Rectangle r : rectangulosOriginales) {
			rects.add(r);
			int ixMin = xs.indexOf(r.x);
			int ixMax = xs.indexOf(r.x + r.width);
			int iyMin = ys.indexOf(r.y);
			int iyMax = ys.indexOf(r.y + r.height);
			int iX = ixMin;
			while(iX < ixMax) {
				int incrementoX = Math.min(step, ixMax - iX);
				int x = xs.get(iX);
				int xMax = xs.get(iX + incrementoX);
				int iY = iyMin;
				
				while(iY < iyMax) {
					int incrementoY = Math.min(step, iyMax - iY);
					int y = ys.get(iY);
					int yMax = ys.get(iY + incrementoY);
					
					rects.add(new Rectangle(x, y, xMax - x, yMax - y));
					
					iY+=incrementoY;
				}	
				iX+= incrementoX;
			}
		}

		return rects;

	}

	public Set<Rectangle> allRectangles() {

		Set<Rectangle> rects = new HashSet<Rectangle>();

		// genero todos los subrectángulos
		for (int iX = 0; iX < xs.size(); iX++)
			for (int iY = 0; iY < ys.size(); iY++)
				for (int iX1 = iX; iX1 < xs.size(); iX1++)
					for (int iY1 = iY; iY1 < ys.size(); iY1++) {
						int x = xs.get(iX);
						int x1 = xs.get(iX1);
						int y = ys.get(iY);
						int y1 = ys.get(iY1);
						Rectangle r = new Rectangle(x, y, x1 - x, y1 - x);
						if (todosUnos(r))
							rects.add(r);
					}
		return rects;
	}

	public boolean todosUnos(Rectangle r) {

		// int y = r.y;
		// int x = r.x;

		// boolean cubrimos = false;
		List<Rectangle> aProcesar = new ArrayList<Rectangle>();
		aProcesar.add(unosPorTopLeft.get(new Point(r.x, r.y)));

		while (!aProcesar.isEmpty()) {

			Rectangle uno = aProcesar.remove(0);

			if (uno.y + uno.height < r.height) {
				Rectangle unoDebajo = unosPorTopLeft.get(new Point(uno.x, uno.y + uno.height));

				if (unoDebajo == null)
					return false;

				if (unoDebajo.y + unoDebajo.height > r.height)
					throw new RuntimeException("No debería pasar");

				aProcesar.add(unoDebajo);
			}

			if (uno.x + uno.width < r.width) {
				Rectangle unoALaDerecha = unosPorTopLeft.get(new Point(uno.x + uno.width, uno.y));

				if (unoALaDerecha == null)
					return false;

				if (unoALaDerecha.x + unoALaDerecha.width > r.width)
					throw new RuntimeException("No debería pasar");

				aProcesar.add(unoALaDerecha);
			}
		}
		return true;
	}

	public Set<Rectangle> getUnos() {
		return unos;
	}

	public Map<Rectangle, Set<Rectangle>> getUnosEnRectangulos() {
		return unosEnRectangulos;
	}

	public List<Rectangle> getRectangulosOriginales() {
		return rectangulosOriginales;
	}
}
