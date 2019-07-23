package rpc.modelos;

import java.awt.Rectangle;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rpc.branch.and.price.Matriz;
import rpg.img.ImportadorImagenes;

public class Solver {

	private static Map<String, Integer> archivos = new HashMap<String, Integer>();

	private static Writer out = null;

	public static void main(String[] args) throws IOException {

		String fileName = "instanceDesc.txt";

		out = new FileWriter("salida");
		Files.walk(Paths.get("/home/ik/git/rpc/rpc/instancias/bin-2/image_0220.jpg")).filter(Files::isRegularFile)
				.forEach(Solver::resolverConDesc);

		if (out != null)
			out.close();
	}

	private static void resolverConZoom(Path path) {

		String absPath = path.toAbsolutePath().toString();
		try {
			Matriz m = ImportadorImagenes.importar(path);

			Matriz mat = m.zoom(100);
			int tamSol = mat.heurCover().size();
			ModeloRC modelo = new ModeloRC(mat, tamSol);
			// Modelo modeloXY = new ModeloXY(mat, tamSol);
			// new FileOutputStream("./out/" + path.getFileName().toString() + ".txt"));
			modelo.buildModel();

			System.out.println("Modelo construido");
			boolean res = modelo.solve();
			if (!res)
				throw new RuntimeException("cagada");
			System.out.println("Resolviendo matriz");
			// out.write(absPath + ", " + res + ", " + modeloXY.getObjective() + ", ");
			// out.flush();
			modelo.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void resolverConDesc(Path path) {
		
		try {
			Matriz m = ImportadorImagenes.importar(path);

			List<List<Matriz>> mat = m.descomponer(8);
			int itCount = 0;
			long tiempoModelos = 0;
			long tiempoMerge = 0;
			long tiempoTotal = System.currentTimeMillis();
			InfoResolucion info = new InfoResolucion();
			Solucion acumulada = null;
			for (List<Matriz> fila : mat) {
				
				Solucion acumuladaFila = null;
				for (Matriz matriz : fila) {
					info.tiempoRes = 0;
					itCount++;	
					if (matriz.cantUnos() == matriz.filas() * matriz.columnas()) {						
						List<Rectangle> rects = new ArrayList<Rectangle>();
						rects.add(new Rectangle(0, 0, matriz.columnas(), matriz.filas()));
						Solucion s = new Solucion(matriz, rects);
						
						if (acumuladaFila == null)
							acumuladaFila = s;
						else
							acumuladaFila = acumuladaFila.mergeADerecha(s, info);
												
						continue;
					}

					if (matriz.cantUnos() == 0) {
						itCount++;
						Solucion s = new Solucion(matriz, new ArrayList<Rectangle>());
						
						if (acumuladaFila == null)
							acumuladaFila = s;
						else
							acumuladaFila = acumuladaFila.mergeADerecha(s, info);
												
						continue;
					}

					int tamSol = matriz.heurCover().size();
					Modelo modelo = new ModeloXY(matriz, tamSol);							
					//,new FileOutputStream("./out/" + path.getFileName().toString() + ".txt"));
					modelo.buildModel();

					//System.out.println("Modelo construido " + itCount++ + " .");
					boolean res = modelo.solve();								
					
					if (!res || !modelo.getSolution().esValida())
						throw new RuntimeException("Solución inválida");

					Solucion s = modelo.getSolution();					

					if (acumuladaFila == null)
						acumuladaFila = s;
					else
						acumuladaFila = acumuladaFila.mergeADerecha(s, info);

					tiempoModelos+= modelo.info().tiempoRes;
					tiempoMerge+= info.tiempoRes;
					//System.out.println("Matriz resuelta");					
					modelo.close();
					
				}
				if (acumulada == null)
					acumulada = acumuladaFila;
				else 
					acumulada = acumulada.mergeAbajo(acumuladaFila, info);
				
				tiempoMerge+= info.tiempoRes;
				
			}

			tiempoTotal = System.currentTimeMillis() - tiempoTotal;
			out.write(path.getFileName().toString() + ", " + m.columnas() + ", " + m.filas() + ", " + m.cantUnos() + ", " + acumulada.getRectangulos().size() + ", " + tiempoTotal + ", " + tiempoModelos + ", " + tiempoMerge + ", " + itCount);
			out.flush();
			if (!acumulada.esValida())
				throw new RuntimeException("Solución acumulada inválida");
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
