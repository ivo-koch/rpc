package rpc.polyhedral.computations;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import rpc.branch.and.price.Matriz;
import rpg.img.ImportadorImagenes;

public class MainClass {

	private static Map<String, Integer> archivos = new HashMap<String, Integer>();

	private static Writer out = null;

	public static void main(String[] args) throws IOException {

		String fileName = "instanceDesc.txt";

		out = new FileWriter("salida");
		out.write(
				"Dir, Nombre, Filas, Columnas, Cant. unos, Heur st, Heur inv, Heur inv2, Heur inv3, Heur sh, Theur, TbuilM, Tsolve, Model \n");
		// Heur: out.write("Dir, Nombre, Filas, Columnas, Cant. unos, Heur st, Heur inv,
		// Heur inv2, Heur inv3, Heur sh, Theur, TbuilM, Tsolve, Model \n");
		Files.walk(Paths.get("/home/ikoch/git/rpc/rpc/out")).filter(Files::isRegularFile).forEach(MainClass::resolver);

		if (out != null)
			out.close();
	}

	private static void resolver(Path path) {

		String absPath = path.toAbsolutePath().toString();
		try {

			FileInputStream fis = new FileInputStream(path.toString());
			ObjectInputStream iis = new ObjectInputStream(fis);
			double[][] duales = (double[][]) iis.readObject();

			ModeloSlave ms = new ModeloSlave(duales);

			ms.buildModel();

			if (!ms.solve())
				throw new RuntimeException("cagada");

			System.out.println("Resolviendo matriz");
			
			ms.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
