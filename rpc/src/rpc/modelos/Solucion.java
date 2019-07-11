package rpc.modelos;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import rpc.branch.and.price.Matriz;

public class Solucion {

	private List<Rectangle> rectangulos;

	private Matriz matriz;

	public Solucion(Matriz m, List<Rectangle> rects) {
		this.matriz = m;
		this.rectangulos = rects;
	}

	public boolean esValida() {

		for (Rectangle r : rectangulos)
			if (!matriz.todosUnos(r))
				return false;

		for (Point p : matriz.unos()) {
			boolean incluido = false;
			for (Rectangle r : rectangulos)
				if (r.contains(p)) {
					incluido = true;
					break;
				}

			if (!incluido)
				return false;
		}
		return true;
	}

}
