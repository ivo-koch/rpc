package rpc.polyhedral.computations;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import rpc.branch.and.price.Matriz;
import rpg.img.ImportadorImagenes;

public class DualGenerator {

	private static Map<String, Integer> archivos = new HashMap<String, Integer>();

	private static Writer out = null;

	public static void main(String[] args) throws IOException {

		String fileName = "instanceDesc.txt";

		out = new FileWriter("salida");
		out.write(
				"Dir, Nombre, Filas, Columnas, Cant. unos, Heur st, Heur inv, Heur inv2, Heur inv3, Heur sh, Theur, TbuilM, Tsolve, Model \n");
		// Heur: out.write("Dir, Nombre, Filas, Columnas, Cant. unos, Heur st, Heur inv,
		// Heur inv2, Heur inv3, Heur sh, Theur, TbuilM, Tsolve, Model \n");
		Files.walk(Paths.get("/home/ikoch/git/rpc/rpc/instancias/bin/caltech/image_0119.jpg")).filter(Files::isRegularFile)
				.forEach(DualGenerator::resolver);

		if (out != null)
			out.close();
	}

	private static void resolver(Path path) {

		// String absPath = path.toAbsolutePath().toString();
		try {

			Matriz m = ImportadorImagenes.importar(path);

			ModeloMaster mm = new ModeloMaster(m, null);
			mm.buildModel();

			System.out.println("Modelo construido");
			boolean res = mm.solve();
			if (!res)
				throw new RuntimeException("Master no resuelto");

			FileOutputStream fos = new FileOutputStream("/home/ikoch/git/rpc/rpc/out/" + path.getFileName() + ".dual");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			double[][] duals = mm.getDualMatrix();			

			for (int f = 0; f < duals.length; f++) {
				for (int c = 0; c < duals[0].length; c++)
					System.out.print(duals[f][c] + " ");
				System.out.println();
			}
			
			DualMask.mask(m, duals);

			oos.writeObject(duals);

			fos.close();
			mm.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
