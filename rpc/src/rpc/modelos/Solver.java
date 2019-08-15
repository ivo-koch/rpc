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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rpc.branch.and.price.Matriz;
import rpc.branch.and.price.MatrizComprimida;
import rpg.img.ImportadorImagenes;

public class Solver {

	private static Map<String, Integer> archivos = new HashMap<String, Integer>();

	private static Writer out = null;

	public static void main(String[] args) throws IOException {

		String fileName = "instanceDesc.txt";

		out = new FileWriter("salida");
		out.write("Dir, Nombre, Filas, Columnas, Cant. unos, Heur st, Heur inv, Heur inv2, Heur inv3, Heur sh, Theur, TbuilM, Tsolve, Model \n");
		//Heur: out.write("Dir, Nombre, Filas, Columnas, Cant. unos, Heur st, Heur inv, Heur inv2, Heur inv3, Heur sh, Theur, TbuilM, Tsolve, Model \n");
		Files.walk(Paths.get("/home/ik/git/rpc/rpc/instancias/short")).filter(Files::isRegularFile)
				.forEach(Solver::resolverConHeur);
		
		if (out != null)
			out.close();
	}

	private static void resolverConZoom(Path path) {

		String absPath = path.toAbsolutePath().toString();
		try {
			Matriz m = ImportadorImagenes.importar(path);

			Matriz mat = m.zoom(100);
			int tamSol = mat.coverStandard().size();
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

	private static void resolverConHeur(Path path) {
		
		try {
			StringBuilder output = new StringBuilder();
			
			Matriz m = ImportadorImagenes.importar(path);
			
			output.append(path.getParent() + ", ");
			output.append(path.getFileName().toString() + ", ");
			output.append(m.filas() + ", " + m.columnas() + ", " + m.cantUnos() + ", ");
			long tiempoInicial = System.currentTimeMillis();
			
			List<Rectangle> sol = m.coverStandard();
			//Solucion nueva = new Solucion(m, sol);
			//if (!nueva.esValida())
			//	throw new RuntimeException("solucion no valida");						
			
			MatrizComprimida mc = new MatrizComprimida(sol);
			Set<Rectangle> conjuntoExpandido = new HashSet<Rectangle>(sol);
			output.append(conjuntoExpandido.size() + ", ");
			
			conjuntoExpandido.addAll(m.coverInv());
			output.append(conjuntoExpandido.size() + ", ");
			
			conjuntoExpandido.addAll(m.coverInv2());
			output.append(conjuntoExpandido.size() + ", ");
			
			conjuntoExpandido.addAll(m.coverInv3());
			output.append(conjuntoExpandido.size() + ", ");
			
			conjuntoExpandido.addAll(m.coverShuffle());
			output.append(conjuntoExpandido.size() + ", ");
			
			output.append((System.currentTimeMillis() - tiempoInicial) + ", ");
			
			mc = new MatrizComprimida(new ArrayList<Rectangle>(conjuntoExpandido));
			ModeloR modelo = new ModeloR(mc, 0.01);
			
			double tiempoInicialMod = System.currentTimeMillis();
			
			modelo.buildModel();
			
			output.append((System.currentTimeMillis() - tiempoInicialMod) + ", ");
			
			double tiempoInicialSolve = System.currentTimeMillis();
			if (!modelo.solve())
				throw new RuntimeException("No pudo mergear");
			
			output.append((System.currentTimeMillis() - tiempoInicialSolve) + ", ");
			
			Solucion s = new Solucion (m, modelo.getSolution().getRectangulos());
			
			//if (!s.esValida())
			//	throw new RuntimeException("solucion no valida");
			modelo.close();				
			
			output.append(s.getRectangulos().size());
			System.out.println("Resolvimos " + path.getFileName().toString());
			out.write (output.toString() + "\n");
			out.flush();
			return;
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private static void resolverConDesc(Path path) {
		
		try {
			StringBuilder output = new StringBuilder();
			
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
						else {
							acumuladaFila = acumuladaFila.mergeADerecha(s, info);
							tiempoMerge+= info.tiempoRes;									
						}					
						continue;
					}

					if (matriz.cantUnos() == 0) {
						itCount++;
						Solucion s = new Solucion(matriz, new ArrayList<Rectangle>());
						
						if (acumuladaFila == null)
							acumuladaFila = s;
						else {
							acumuladaFila = acumuladaFila.mergeADerecha(s, info);
							tiempoMerge+= info.tiempoRes;
						}
												
						continue;
					}

					int tamSol = matriz.coverStandard().size();
					Modelo modelo = new ModeloXY(matriz, tamSol);							
					//,new FileOutputStream("./out/" + path.getFileName().toString() + ".txt"));
					modelo.buildModel();

					//System.out.println("Modelo construido " + itCount++ + " .");
					boolean res = modelo.solve();								
					
					//if (!res || !modelo.getSolution().esValida())
					//	throw new RuntimeException("Solución inválida");

					Solucion s = modelo.getSolution();					

					if (acumuladaFila == null)
						acumuladaFila = s;
					else
						acumuladaFila = acumuladaFila.mergeADerecha(s, info);

					//if (!acumuladaFila.esValida())
					//	throw new RuntimeException("Solución acumulada inválida");
					
					tiempoModelos+= modelo.info().tiempoRes;
					tiempoMerge+= info.tiempoRes;
					//System.out.println("Matriz resuelta");					
					modelo.close();
					
				}
				if (acumulada == null)
					acumulada = acumuladaFila;
				else 
					acumulada = acumulada.mergeAbajo(acumuladaFila, info);
				
				//if (!acumulada.esValida())
				//	throw new RuntimeException("Solución acumulada inválida");
				
				tiempoMerge+= info.tiempoRes;				
			}

			tiempoTotal = System.currentTimeMillis() - tiempoTotal;
			out.write(path.getFileName().toString() + ", " + m.columnas() + ", " + m.filas() + ", " + m.cantUnos() + ", " + acumulada.getRectangulos().size() + ", " + tiempoTotal + ", " + tiempoModelos + ", " + tiempoMerge + ", " + itCount + "\n");
			out.flush();
			if (!acumulada.esValida())
				throw new RuntimeException("Solución acumulada inválida");
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
