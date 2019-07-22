package rpc.modelos;

import java.awt.Rectangle;
import java.util.List;

import rpc.branch.and.price.MatrizComprimida;

public class SolucionModeloR {

	private List<Rectangle> rectangulos;
	private MatrizComprimida matriz;

	public SolucionModeloR(MatrizComprimida matriz, List<Rectangle> solucion) {
		this.matriz = matriz;
		this.rectangulos = solucion;
	}

	public boolean esValida() {

		for (Rectangle pixel : matriz.getUnos()) {
			boolean incluido = false;
			for (Rectangle r : rectangulos)
				if (r.contains(pixel)) {
					incluido = true;
					break;
				}
			if (!incluido)
				return false;
		}
		return true;
	}

	public List<Rectangle> getRectangulos() {
		return rectangulos;
	}
}
