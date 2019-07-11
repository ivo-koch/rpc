package rpc.modelos;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
				.forEach(Solver::resolver);

		if (out != null)
			out.close();
	}

	private static void resolver(Path path) {

		String absPath = path.toAbsolutePath().toString();		
		try {
			Matriz m = ImportadorImagenes.importar(path);

			List<Matriz> matrices = m.descomponer(8);
			
			
			for (Matriz mat : matrices) {
				
				if (mat.cantUnos() == 0 || mat.cantUnos() == mat.filas() * mat.columnas())
					continue;
					
				Modelo modeloXY = new ModeloXY(mat, mat.allMaximals().size());
						//new FileOutputStream("./out/" + path.getFileName().toString() + ".txt"));
				modeloXY.buildModel();
				boolean res = modeloXY.solve();
				if (!res)
					throw new RuntimeException("cagada");
				System.out.println("Resolviendo matriz");
				//out.write(absPath + ", " + res + ", " + modeloXY.getObjective() + ", ");
				//out.flush();
				modeloXY.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
