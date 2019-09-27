package rpc.branch.and.price;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MaximumRectangleFinder {

	private Matriz matriz;
	private Point p;
	public int minY;
	public int minX;
	public int maxX;
	public int maxY;
	public Set<Point> c1P = new HashSet<Point>();
	public Set<Point> c2P = new HashSet<Point>();
	public Set<Point> c3P = new HashSet<Point>();
	public Set<Point> c4P = new HashSet<Point>();

	// dada una fila entre minY y maxY, nos dice en qué columna está la silueta, en
	// cada cuadrante.
	public int[] c1X;
	public int[] c2X;
	public int[] c3X;
	public int[] c4X;

	// dada una fila entre minX y maxX, nos dice en qué fila está la silueta, en
	// cada cuadrante.
	public int[] c1Y;
	public int[] c2Y;
	public int[] c3Y;
	public int[] c4Y;

	public MaximumRectangleFinder(Matriz m) {
		this.matriz = m;

	}

	public Set<Rectangle> rectsPto = new HashSet<Rectangle>();
	public Set<Rectangle> rectsAcum = new HashSet<Rectangle>();

	public Rectangle maximumRectangle(Point p) {

		c1P.clear();
		c2P.clear();
		c3P.clear();
		c4P.clear();

		minX = 0;
		minY = 0;
		maxX = 0;
		maxY = 0;

		maximoEncontrado = null;
		maxAreaEncontrada = 0;

		minimoEncontrado = null;
		minAreaEncontrada = 0;
		
		rectsAcum.addAll(rectsPto);
		rectsPto.clear();

		this.p = p;

		trazarCruz();
		vertC1();
		vertC2();
		vertC3();
		vertC4();

		buscarRC1();
		buscarDC1();

		buscarLC2();
		buscarDC2();

		buscarLC3();
		buscarUC3();

		buscarRC4();
		buscarUC4();
		return maximoEncontrado;
	}

	// trazamos la cruz de unos con centro en p.
	public void trazarCruz() {

		int filas = matriz.filas();
		int columnas = matriz.columnas();

		int f = p.y;
		int c = p.x;

		while (f >= 0 && matriz.get(f, c))
			f--;
		minY = f + 1;

		f = p.y;

		while (f < filas && matriz.get(f, c))
			f++;
		maxY = f - 1;

		f = p.y;
		c = p.x;

		while (c >= 0 && matriz.get(f, c))
			c--;
		minX = c + 1;

		c = p.x;

		while (c < columnas && matriz.get(f, c))
			c++;
		maxX = c - 1;
	}

	// actualizamos las estructuras con los puntos del primer cuadrante.
	public void vertC1() {

		c1P.add(new Point(minX, p.y));

		c1X = new int[matriz.filas()];
		c1Y = new int[matriz.columnas()];

		/*Arrays.fill(c1X, -1);
		Arrays.fill(c1Y, -1);*/

		c1X[p.y] = minX;
		c1Y[p.x] = minY;

		for (int x = p.x - 1; x >= minX; x--)
			c1Y[x] = p.y;

		int colRectAnt = minX;
		for (int fi = p.y - 1; fi >= minY; fi--) {
			int minXActual = p.x;
			while (minXActual >= colRectAnt && matriz.get(fi, minXActual)) {
				c1Y[minXActual] = fi;
				minXActual--;
			}

			minXActual++;

			c1X[fi] = minXActual;

			if (colRectAnt != minXActual)
				c1P.add(new Point(colRectAnt, fi + 1));
			if (fi == minY)
				c1P.add(new Point(minXActual, fi));
			colRectAnt = minXActual;
		}
	}

	public void vertC2() {

		c2P.add(new Point(maxX, p.y));
		c2X = new int[matriz.filas()];
		c2Y = new int[matriz.columnas()];

		/*Arrays.fill(c2X, -1);
		Arrays.fill(c2Y, -1);*/

		c2X[p.y] = maxX;
		c2Y[p.x] = minY;

		for (int x = p.x + 1; x <= maxX; x++)
			c2Y[x] = p.y;

		int colRectAnt = maxX;

		for (int fi = p.y - 1; fi >= minY; fi--) {
			int maxXActual = p.x;
			while (maxXActual <= colRectAnt && matriz.get(fi, maxXActual)) {
				c2Y[maxXActual] = fi;
				maxXActual++;
			}
			maxXActual--;
			c2X[fi] = maxXActual;

			if (colRectAnt != maxXActual)
				c2P.add(new Point(colRectAnt, fi + 1));
			if (fi == minY)
				c2P.add(new Point(maxXActual, fi));
			colRectAnt = maxXActual;
		}
	}

	public void vertC4() {

		c4P.add(new Point(minX, p.y));
		c4X = new int[matriz.filas()];
		c4Y = new int[matriz.columnas()];

		/*Arrays.fill(c4X, -1);
		Arrays.fill(c4Y, -1);*/

		c4X[p.y] = minX;
		c4Y[p.x] = maxY;

		for (int x = p.x - 1; x >= minX; x--)
			c4Y[x] = p.y;

		int colRectAnt = minX;
		for (int fi = p.y + 1; fi <= maxY; fi++) {
			int minXActual = p.x;
			while (minXActual >= colRectAnt && matriz.get(fi, minXActual)) {
				c4Y[minXActual] = fi;
				minXActual--;
			}
			minXActual++;
			c4X[fi] = minXActual;
			if (colRectAnt != minXActual)
				c4P.add(new Point(colRectAnt, fi - 1));
			if (fi == maxY)
				c4P.add(new Point(minXActual, fi));

			colRectAnt = minXActual;
		}
	}

	public void vertC3() {
		// buscamos ahora los rect maximales que empiezan en el primer cuadrante

		c3P.add(new Point(maxX, p.y));
		c3X = new int[matriz.filas()];
		c3Y = new int[matriz.columnas()];

		/*Arrays.fill(c3X, -1);
		Arrays.fill(c3Y, -1);*/

		c3X[p.y] = maxX;
		c3Y[p.x] = maxY;

		for (int x = p.x + 1; x <= maxX; x++)
			c3Y[x] = p.y;

		int colRectAnt = maxX;
		for (int fi = p.y + 1; fi <= maxY; fi++) {
			int minXActual = p.x;
			while (minXActual <= colRectAnt && matriz.get(fi, minXActual)) {
				c3Y[minXActual] = fi;
				minXActual++;
			}

			minXActual--;
			c3X[fi] = minXActual;
			if (colRectAnt != minXActual)
				c3P.add(new Point(colRectAnt, fi - 1));
			if (fi == maxY)
				c3P.add(new Point(minXActual, fi));
			colRectAnt = minXActual;
		}
	}

	private final int limiteRectsPorPto = 1;

	public void buscarRC1() {
		

		for (Point p1 : c1P) {
			int topX = p1.x;
			int topY = p1.y;
			int width = c2X(p1.y) - p1.x + 1;
			int height = Math.min(c4Y(p1.x), c3Y(c2X(p1.y))) - p1.y + 1;
			//int nuevaArea = width * height;
			agregar(topX, topY, width, height);
//			if (nuevaArea > maxAreaEncontrada) {
//				maxAreaEncontrada = nuevaArea;
//				maximoEncontrado = new Rectangle(topX, topY, width, height);
//				// if (topX < minX || topX > maxX || topY < minY || topY > maxY ||
//				// !matriz.todosUnos(maximoEncontrado))
//				// throw new RuntimeException("Cagada en puerta");
//			}
//			if (agregados < limiteRectsPorPto)
//				rectsPto.add(new Rectangle(topX, topY, width, height));
//			agregados++;
		}
	}

	private void agregar(int x, int y, int width, int height) {
				
		int nuevaArea = width * height;
		
		if (nuevaArea < minAreaEncontrada)
			return;
		
		Rectangle nuevo =  new Rectangle(x, y, width, height);
		if (nuevaArea > maxAreaEncontrada) {
			maxAreaEncontrada = nuevaArea;
			maximoEncontrado = nuevo;
		}
			
		if (rectsPto.size() < limiteRectsPorPto)
			rectsPto.add(nuevo);
		else 
		{
			//eliminamos el mínimo
			rectsPto.remove(minimoEncontrado);
			
			//buscamos el nuevo mínimo
			minAreaEncontrada = Integer.MAX_VALUE;
			for (Rectangle r : rectsPto) {
				int area = r.height * r.width;
				if (area < minAreaEncontrada) {
					minimoEncontrado = r;
					minAreaEncontrada = area;
				}
			}
			
			//y agregamos el nuevo elemento
			rectsPto.add(nuevo);
		}	
	}

	public void buscarDC1() {		
		for (Point p1 : c1P) {
			int topX = p1.x;
			int topY = p1.y;
			int width = Math.min(c2X(topY), c3X(c4Y(p1.x))) - p1.x + 1;
			int height = c4Y(p1.x) - p1.y + 1;
			agregar(topX, topY, width, height);
//			int nuevaArea = width * height;
//			if (nuevaArea > maxAreaEncontrada) {
//				maxAreaEncontrada = nuevaArea;
//				maximoEncontrado = new Rectangle(topX, topY, width, height);
//				// if (topX < minX || topX > maxX || topY < minY || topY > maxY ||
//				// !matriz.todosUnos(maximoEncontrado))
//				// throw new RuntimeException("Cagada en puerta");
//			}
//			if (agregados < limiteRectsPorPto)
//				rectsPto.add(new Rectangle(topX, topY, width, height));
//			agregados++;
		}
	}

	public void buscarLC2() {

		for (Point p2 : c2P) {
			int topX = c1X(p2.y);
			int topY = p2.y;
			int width = p2.x - topX + 1;
			int height = Math.min(c4Y(topX), c3Y(p2.x)) - p2.y + 1;
			agregar(topX, topY, width, height);
//			int nuevaArea = width * height;			
//			if (nuevaArea > maxAreaEncontrada) {
//				maxAreaEncontrada = nuevaArea;
//				maximoEncontrado = new Rectangle(topX, topY, width, height);
//				// if (topX < minX || topX > maxX || topY < minY || topY > maxY ||
//				// !matriz.todosUnos(maximoEncontrado))
//				// throw new RuntimeException("Cagada en puerta");
//			}
//			if (agregados < limiteRectsPorPto)
//				rectsPto.add(new Rectangle(topX, topY, width, height));
//			agregados++;
		}
	}

	public void buscarDC2() {		
		for (Point p2 : c2P) {
			int topX = Math.max(c1X(p2.y), c4X(c3Y(p2.x)));
			int topY = p2.y;
			int width = p2.x - topX + 1;
			int height = c3Y(p2.x) - p2.y + 1;
			agregar(topX, topY, width, height);
//			int nuevaArea = width * height;
//			if (nuevaArea > maxAreaEncontrada) {
//				maxAreaEncontrada = nuevaArea;
//				maximoEncontrado = new Rectangle(topX, topY, width, height);
//				// if (topX < minX || topX > maxX || topY < minY || topY > maxY ||
//				// !matriz.todosUnos(maximoEncontrado))
//				// throw new RuntimeException("Cagada en puerta");
//			}
//			if (agregados < limiteRectsPorPto)
//				rectsPto.add(new Rectangle(topX, topY, width, height));
//			agregados++;
		}
	}

	public void buscarLC3() {
		
		for (Point p3 : c3P) {
			int topX = c4X(p3.y);
			int topY = Math.max(c1Y(topX), c2Y(p3.x));

			int width = p3.x - topX + 1;
			int height = p3.y - topY + 1;
			agregar(topX, topY, width, height);
//			int nuevaArea = width * height;
//			if (nuevaArea > maxAreaEncontrada) {
//				maxAreaEncontrada = nuevaArea;
//				maximoEncontrado = new Rectangle(topX, topY, width, height);
//				// if (topX < minX || topX > maxX || topY < minY || topY > maxY ||
//				// !matriz.todosUnos(maximoEncontrado))
//				// throw new RuntimeException("Cagada en puerta");
//			}
//			if (agregados < limiteRectsPorPto)
//				rectsPto.add(new Rectangle(topX, topY, width, height));
//			agregados++;
		}
	}

	public void buscarUC3() {
		
		for (Point p3 : c3P) {
			int topY = c2Y(p3.x);
			int topX = Math.max(c1X(topY), c4X(p3.y));

			int width = p3.x - topX + 1;
			int height = p3.y - topY + 1;
			agregar(topX, topY, width, height);
//			int nuevaArea = width * height;
//			if (nuevaArea > maxAreaEncontrada) {
//				maxAreaEncontrada = nuevaArea;
//				maximoEncontrado = new Rectangle(topX, topY, width, height);
//				// if (topX < minX || topX > maxX || topY < minY || topY > maxY ||
//				// !matriz.todosUnos(maximoEncontrado))
//				// throw new RuntimeException("Cagada en puerta");
//			}
//			if (agregados < limiteRectsPorPto)
//				rectsPto.add(new Rectangle(topX, topY, width, height));
//			agregados++;
		}
	}

	public void buscarUC4() {
		
		for (Point p4 : c4P) {
			int topX = p4.x;
			int topY = c1Y(topX);

			int width = Math.min(c2X(topY), c3X(p4.y)) - p4.x + 1;
			int height = p4.y - topY + 1;
			agregar(topX, topY, width, height);
//			int nuevaArea = width * height;
//			if (nuevaArea > maxAreaEncontrada) {
//				maxAreaEncontrada = nuevaArea;
//				maximoEncontrado = new Rectangle(topX, topY, width, height);
//				// if (topX < minX || topX > maxX || topY < minY || topY > maxY ||
//				// !matriz.todosUnos(maximoEncontrado))
//				// throw new RuntimeException("Cagada en puerta");
//			}
//			if (agregados < limiteRectsPorPto)
//				rectsPto.add(new Rectangle(topX, topY, width, height));
//			agregados++;
		}
	}

	public void buscarRC4() {		
		for (Point p4 : c4P) {
			int topX = p4.x;
			int topY = Math.max(c1Y(topX), c2Y(c3X(p4.y)));

			int width = p4.x - topX + 1;
			int height = p4.y - topY + 1;
			agregar(topX, topY, width, height);
//			int nuevaArea = width * height;
//			if (nuevaArea > maxAreaEncontrada) {
//				maxAreaEncontrada = nuevaArea;
//				maximoEncontrado = new Rectangle(topX, topY, width, height);
//				// if (topX < minX || topX > maxX || topY < minY || topY > maxY ||
//				// !matriz.todosUnos(maximoEncontrado))
//				// throw new RuntimeException("Cagada en puerta");
//			}
//			if (agregados < limiteRectsPorPto)
//				rectsPto.add(new Rectangle(topX, topY, width, height));
//			agregados++;
		}
	}

	private Rectangle maximoEncontrado = null;
	private int maxAreaEncontrada;

	private Rectangle minimoEncontrado = null;
	private int minAreaEncontrada;

	private int c1X(int y) {
		if (y < 0)
			throw new RuntimeException("Coordenadas inválidas");
		return c1X[y];
	}

	private int c2X(int y) {
		if (y < 0)
			throw new RuntimeException("Coordenadas inválidas");
		return c2X[y];
	}

	private int c3X(int y) {
		if (y < 0)
			throw new RuntimeException("Coordenadas inválidas");
		return c3X[y];
	}

	private int c4X(int y) {
		if (y < 0)
			throw new RuntimeException("Coordenadas inválidas");
		return c4X[y];
	}

	private int c1Y(int x) {
		if (x < 0)
			throw new RuntimeException("Coordenadas inválidas");
		return c1Y[x];
	}

	private int c2Y(int x) {
		if (x < 0)
			throw new RuntimeException("Coordenadas inválidas");
		return c2Y[x];
	}

	private int c3Y(int x) {
		if (x < 0)
			throw new RuntimeException("Coordenadas inválidas");
		return c3Y[x];
	}

	private int c4Y(int x) {
		if (x < 0)
			throw new RuntimeException("Coordenadas inválidas");
		return c4Y[x];
	}
}
