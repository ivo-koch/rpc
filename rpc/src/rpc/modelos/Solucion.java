package rpc.modelos;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import rpc.branch.and.price.Matriz;
import rpc.branch.and.price.MatrizComprimida;

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

	public List<Rectangle> enBordeIzquierdo() {

		List<Rectangle> res = new ArrayList<Rectangle>();

		for (Rectangle r : rectangulos)
			if (matriz.todosUnos(new Rectangle(0, r.y, r.x, r.height)))
				res.add(r);

		return res;
	}

	public List<Rectangle> enBordeInferior() {

		List<Rectangle> res = new ArrayList<Rectangle>();

		for (Rectangle r : rectangulos)
			if (matriz.todosUnos(new Rectangle(r.x, r.y, r.width, matriz.columnas() - r.y)))
				res.add(r);


		return res;
	}
	
	public List<Rectangle> enBordeSuperior() {

		List<Rectangle> res = new ArrayList<Rectangle>();

		for (Rectangle r : rectangulos)
			if (matriz.todosUnos(new Rectangle(r.x, 0, r.width, r.y)))
				res.add(r);


		return res;
	}
	
	public List<Rectangle> enBordeDerecho() {

		List<Rectangle> res = new ArrayList<Rectangle>();

		for (Rectangle r : rectangulos)
			if (matriz.todosUnos(new Rectangle(r.x, r.y, matriz.columnas() - r.x, matriz.filas() - r.y)))
				res.add(r);

		return res;
	}

	
	public Solucion mergeADerecha(Solucion s1) throws Exception {

		List<Rectangle> res = new ArrayList<Rectangle>();
		List<Rectangle> der = enBordeDerecho();
		int offsetFilas = matriz.filas();		

		// agregamos los rectángulos de solucion que no están en el borde
		for (Rectangle r : rectangulos) {
			if (!der.contains(r))
				res.add(r);
		}

		// agregamos los rect de s1 que no están en el borde.
		List<Rectangle> izq = s1.enBordeIzquierdo();
		for (Rectangle r : s1.rectangulos)
			if (!izq.contains(r))
				res.add(new Rectangle(r.x + offsetFilas, r.y, r.width, r.height));

		Matriz ampliada = new Matriz(matriz,
				new Rectangle(0, 0, matriz.columnas() + s1.matriz.columnas(), matriz.filas() + s1.matriz.filas()));

		List<Rectangle> aMergear = new ArrayList<Rectangle>();

		// ahora, maximalizamos el rectángulo 'a derecha'
		for (Rectangle r : der)
			aMergear.add(ampliada.buildMaximal(r));

		// ahora, maximalizamos el rectángulo 'a izquierda'
		for (Rectangle r : izq) {
			//trasladamos las coordenadas a la matriz ampliada			
			aMergear.add(ampliada.buildMaximal(new Rectangle(r.x + offsetFilas, r.y, r.width, r.height)));
		}

		// ahora, mergeamos
		if (aMergear.isEmpty())
			return new Solucion(ampliada, res);
		
		MatrizComprimida mc = new MatrizComprimida(aMergear);
		
		ModeloR modelo = new ModeloR(mc, 0.01);
		modelo.buildModel();
		if (!modelo.solve())
			throw new RuntimeException("No pudo mergear");
		
		
		res.addAll(modelo.getSolution().getRectangulos());
		modelo.close();
		
		return new Solucion(ampliada, res);
	}
	

	
	public Solucion mergeAbajo(Solucion s1) throws Exception {

		List<Rectangle> res = new ArrayList<Rectangle>();
		List<Rectangle> inf = enBordeInferior();		
		int offsetColumnas = matriz.columnas();

		// agregamos los rectángulos de solucion que no están en el borde
		for (Rectangle r : rectangulos) {
			if (!inf.contains(r))
				res.add(r);
		}

		// agregamos los rect de s1 que no están en el borde.
		List<Rectangle> sup = s1.enBordeSuperior();
		for (Rectangle r : s1.rectangulos)
			if (!sup.contains(r))
				res.add(new Rectangle(r.x, r.y + offsetColumnas, r.width, r.height));

		Matriz ampliada = new Matriz(matriz,
				new Rectangle(0, 0, matriz.columnas() + s1.matriz.columnas(), matriz.filas() + s1.matriz.filas()));

		List<Rectangle> aMergear = new ArrayList<Rectangle>();

		// ahora, maximalizamos el rectángulo 'hacia abajo'
		for (Rectangle r : inf)
			aMergear.add(ampliada.buildMaximal(r));

		// ahora, maximalizamos el rectángulo 'hacia arriba'
		for (Rectangle r : sup) {
			//trasladamos las coordenadas a la matriz ampliada			
			aMergear.add(ampliada.buildMaximal(new Rectangle(r.x, r.y + offsetColumnas, r.width, r.height)));
		}

		// ahora, mergeamos
		MatrizComprimida mc = new MatrizComprimida(aMergear);
		
		ModeloR modelo = new ModeloR(mc, 0.01);
		modelo.buildModel();
		if (!modelo.solve())
			throw new RuntimeException("No pudo mergear");
		
		modelo.close();
		res.addAll(modelo.getSolution().getRectangulos());
		
		return new Solucion(ampliada, res);

	}

}
