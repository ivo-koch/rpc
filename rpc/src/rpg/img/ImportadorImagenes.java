package rpg.img;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.ImageProcessor;
import rpc.branch.and.price.Matriz;

public class ImportadorImagenes {

	public static void binarizar(Path path) {		
		ImagePlus imp = IJ.openImage(path.toAbsolutePath().toString());
		ImageProcessor imageProcessor = imp.getProcessor();
		
		if (!imageProcessor.isBinary()) {
			IJ.setRawThreshold(imp, 50, 255, null);
			//Prefs.blackBackground = true;
			IJ.run(imp, "Convert to Mask", "");
		}
		
		FileSaver fs = new FileSaver(imp);
		fs.saveAsTiff("/home/ik/git/rpc/rpc/instancias/bin/iconos/10x10/" + path.getFileName().toString());
		System.out.println("Binarizamos" + path.getFileName().toString());
	}
	
	
	public static void binarizarDir(String dir) throws IOException {
		Files.walk(Paths.get(dir))
        .filter(Files::isRegularFile)
        .forEach(ImportadorImagenes::binarizar);
	}
	
	private static StringBuilder builder;
		
	public static void buildFileDesc() throws IOException {
		builder = new StringBuilder();
		
		Files.walk(Paths.get("/home/ik/git/rpc/rpc/instancias/nasa/nuevas"))
        .filter(Files::isRegularFile)
        .forEach(ImportadorImagenes::printDesc);
		
		Files.write(Paths.get("instanceDesc.txt"), builder.toString().getBytes());
	}
	
	private static void printDesc(Path path) {
			
		Matriz m = ImportadorImagenes.importar(path);
		String linea = path.toAbsolutePath().toString() + ", " + m.filas() + ", " + m.columnas() + ", " + m.cantUnos() + "\n";//+ ", " + m.allMaximals().size() + "\n";
		System.out.println(linea);
		//path | width | height | cant unos | max. rect. maximales.
		builder.append(linea);
	}
	
	public static void main(String[] args) throws IOException {
		binarizarDir("/home/ik/git/rpc/rpc/instancias/iconos/10x10");
	}
	
	public static Matriz importar(Path path) {

		ImagePlus imp = IJ.openImage(path.toAbsolutePath().toString());
		ImageProcessor imageProcessor = imp.getProcessor();
	
		// width and height of image
		int width = imageProcessor.getWidth();
		int height = imageProcessor.getHeight();

		boolean[][] matriz = new boolean[height][width];

		// iterate through width and then through height
		for (int u = 0; u < width; u++) {
			for (int v = 0; v < height; v++) {
				int valuePixel = imageProcessor.getPixel(u, v);				
					matriz[v][u] = valuePixel > 0;
			}
		}
		
		return new Matriz(matriz);		
	}
}
