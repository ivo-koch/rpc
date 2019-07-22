package rpc.branch.and.price;

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
					if (!unosEnRectangulos.containsKey(nuevoUno))
						unosEnRectangulos.put(nuevoUno, new HashSet<Rectangle>());

					unosEnRectangulos.get(nuevoUno).add(r);

					iX++;
				}
				iY++;
			}
		}
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
