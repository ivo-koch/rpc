package rpc.modelos;

import java.awt.Rectangle;
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

		// List<String> arch = new ArrayList<String>();
		// //read file into stream, try-with-resources
		// try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
		//
		// arch = stream.collect(Collectors.toList());
		//
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		//
		// arch.forEach(s->
		// archivos.put(s.split(",")[0], Integer.parseInt(s.split(",")[4].trim())));

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

		String absPath = path.toAbsolutePath().toString();
		try {
			Matriz m = ImportadorImagenes.importar(path);

			List<List<Matriz>> mat = m.descomponer(8);
			int itCount = 1;
			int cantRectsEnBorde = 0;

			Solucion acumulada = null;
			for (List<Matriz> fila : mat) {
				
				Solucion acumuladaFila = null;
				for (Matriz matriz : fila) {
					if (matriz.cantUnos() == matriz.filas() * matriz.columnas()) {
						itCount++;
						List<Rectangle> rects = new ArrayList<Rectangle>();
						rects.add(new Rectangle(0, 0, matriz.columnas(), matriz.filas()));
						Solucion s = new Solucion(matriz, rects);
						
						if (acumuladaFila == null)
							acumuladaFila = s;
						else
							acumuladaFila.mergeADerecha(s);
						
						cantRectsEnBorde++;
						continue;
					}

					if (matriz.cantUnos() == 0) {
						itCount++;
						Solucion s = new Solucion(matriz, new ArrayList<Rectangle>());
						
						if (acumuladaFila == null)
							acumuladaFila = s;
						else
							acumuladaFila.mergeADerecha(s);
						
						cantRectsEnBorde++;
						continue;
					}

					int tamSol = matriz.heurCover().size();
					Modelo modeloXY = new ModeloXY(matriz, tamSol);
					// new FileOutputStream("./out/" + path.getFileName().toString() + ".txt"));
					modeloXY.buildModel();

					System.out.println("Modelo construido " + itCount++ + " .");
					boolean res = modeloXY.solve();
					if (!res || !modeloXY.getSolution().esValida())
						throw new RuntimeException("cagada");

					Solucion s = modeloXY.getSolution();
					modeloXY.close();

					if (acumuladaFila == null)
						acumuladaFila = s;
					else
						acumuladaFila.mergeADerecha(s);

					System.out.println("Matriz resuelta");
					// out.write(absPath + ", " + res + ", " + modeloXY.getObjective() + ", ");
					// out.flush();
				}
				if (acumulada == null)
					acumulada = acumuladaFila;
				else 
					acumulada.mergeAbajo(acumuladaFila);
			}

			System.out.println("Rectangulos a mergear: " + cantRectsEnBorde + " .");
			if (!acumulada.esValida())
				System.out.println("Ha ocurrido una cagada.");
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
