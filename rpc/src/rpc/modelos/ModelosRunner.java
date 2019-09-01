package rpc.modelos;

import java.awt.Rectangle;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rpc.branch.and.price.Matriz;
import rpc.branch.and.price.MatrizComprimida;
import rpg.img.ImportadorImagenes;

public class ModelosRunner {

	private static Writer out = null;
	// private static StringBuilder builder;

	public static void main(String[] args) throws Exception {

		out = new FileWriter("salidaModelos");
		out.write(
				"Filas, Columnas, Cant. unos, ObjHeurSt, ObjHeurInv, ObjHeurInv2, ObjHeurInv3, ObjHeurSh, ModABCStatus, ModABCGap, ModABCNodos, ModABCTRes, ModABCMSolEnt, ModABCBBound, ModABCNodosCallback, ModABCObj, ModSUDLStatus, ModSUDLGap, ModSUDLNodos, ModSUDLTRes, ModSUDLSolEnt, ModSUDLBBound, ModSUDLNodosCallback, ModSUDLObj, ModRCStatus, ModRCGap, ModRCNodos, ModRCTRes, ModRCMSolEnt, ModRCBBound, ModRCNodosCallback, ModRCObj, ModXYStatus, ModXYGap, ModXYNodos, ModXYTRes, ModXYMSolEnt, ModXYBBound, ModXYNodosCallback, ModXYObj\n");
		Files.walk(Paths.get("/home/ik/git/rpc/rpc/instancias/bin/iconos/16x16")).filter(Files::isRegularFile)
				.forEach(t -> {
					try {
						printDesc(t);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});

		if (out != null)
			out.close();
		// Files.write(Paths.get("resultado.txt"), builder.toString().getBytes());
	}

	private static void printDesc(Path path) throws Exception {

		double timeLimit = 900;
		Matriz matrix = ImportadorImagenes.importar(path);

		List<Rectangle> sol = matrix.coverStandard();

		int solH = sol.size();

		MatrizComprimida mc = new MatrizComprimida(sol);
		Set<Rectangle> conjuntoExpandido = new HashSet<Rectangle>(sol);
		conjuntoExpandido.addAll(matrix.coverInv());
		conjuntoExpandido.addAll(matrix.coverInv2());
		conjuntoExpandido.addAll(matrix.coverInv3());
		conjuntoExpandido.addAll(matrix.coverShuffle());
		mc = new MatrizComprimida(new ArrayList<Rectangle>(conjuntoExpandido));

		ModeloR modelo = new ModeloR(mc, 0.01);
		modelo.buildModel();
		if (!modelo.solve())
			throw new RuntimeException("No pudo resolver");

		
		out.write(matrix.filas() + ", " + matrix.columnas() + ", " + matrix.cantUnos() + ", "
				+ modelo.getSolution().getRectangulos().size() + ", " + 0 + ", " + 0 + ", " + 0 + ", " + 0 + ", ");
		out.flush();

		modelo.close();
		
		ModeloABC modeloABC = new ModeloABC(matrix, solH, null);
		modeloABC.buildModel(timeLimit);
		modeloABC.solve();
		InfoResolucion info = modeloABC.info();
		out.write(info.toString() + ", " + modeloABC.getObjective() + ", ");
		out.flush();
		modeloABC.close();

		ModeloSUDL1 modeloSUDL1 = new ModeloSUDL1(matrix, solH, null);
		modeloSUDL1.buildModel(timeLimit);

		modeloSUDL1.solve();
		info = modeloSUDL1.info();
		out.write(info.toString() + ", " + modeloSUDL1.getObjective() + ", ");
		out.flush();
		modeloSUDL1.close();

		ModeloRC modeloRC = new ModeloRC(matrix, solH, null);
		modeloRC.buildModel(timeLimit);

		modeloRC.solve();
		info = modeloRC.info();
		out.write(info.toString() + ", " + modeloRC.getObjective() + ", ");
		out.flush();
		modeloRC.close();

		ModeloXY modeloXY = new ModeloXY(matrix, solH, null);
		modeloXY.buildModel(timeLimit);

		modeloXY.solve();
		info = modeloXY.info();
		out.write(info.toString() + ", " + modeloXY.getObjective() + "\n");
		out.flush();
		modeloXY.close();
	}

	// public static void main(String[] args) throws Exception {
	//
	// double timeLimit = 1800;
	//
	// out = new FileWriter("salidaModelos");
	// out.write(
	// "Filas, Columnas, Cant. unos, ObjHeurSt, ObjHeurInv, ObjHeurInv2,
	// ObjHeurInv3, ObjHeurSh, ModABCStatus, ModABCGap, ModABCNodos, ModABCTRes,
	// ModABCMSolEnt, ModABCBBound, ModABCNodosCallback, ModABCObj, ModSUDLStatus,
	// ModSUDLGap, ModSUDLNodos, ModSUDLTRes, ModSUDLSolEnt, ModSUDLBBound,
	// ModSUDLNodosCallback, ModSUDLObj, ModRCStatus, ModRCGap, ModRCNodos,
	// ModRCTRes, ModRCMSolEnt, ModRCBBound, ModRCNodosCallback, ModRCObj,
	// ModXYStatus, ModXYGap, ModXYNodos, ModXYTRes, ModXYMSolEnt, ModXYBBound,
	// ModXYNodosCallback, ModXYObj\n");
	//
	// for (int f = 14; f < 31; f++)
	// for (int density = 20; density < 100; density += 20) {
	//
	// Matriz matrix = MatrixGenerator.generateRandomMatrix(f, f, density);
	//
	// List<Rectangle> sol = matrix.coverStandard();
	//
	// int solH = sol.size();
	//
	// MatrizComprimida mc = new MatrizComprimida(sol);
	// Set<Rectangle> conjuntoExpandido = new HashSet<Rectangle>(sol);
	// conjuntoExpandido.addAll(matrix.coverInv());
	// conjuntoExpandido.addAll(matrix.coverInv2());
	// conjuntoExpandido.addAll(matrix.coverInv3());
	// conjuntoExpandido.addAll(matrix.coverShuffle());
	// mc = new MatrizComprimida(new ArrayList<Rectangle>(conjuntoExpandido));
	// ModeloR modelo = new ModeloR(mc, 0.01);
	// modelo.buildModel();
	// if (!modelo.solve())
	// throw new RuntimeException("No pudo resolver");
	//
	// out.write(matrix.filas() + ", " + matrix.columnas() + ", " +
	// matrix.cantUnos() + ", " + modelo.getSolution().getRectangulos().size() + ",
	// "
	// + 0 + ", " + 0 + ", " + 0 + ", " + 0 + ", ");
	// out.flush();
	//
	// ModeloABC modeloABC = new ModeloABC(matrix, solH, null);
	// modeloABC.buildModel(timeLimit);
	// modeloABC.solve();
	// InfoResolucion info = modeloABC.info();
	// out.write(info.toString() + ", " + modeloABC.getObjective() + ", ");
	// out.flush();
	// modeloABC.close();
	//
	// ModeloSUDL1 modeloSUDL1 = new ModeloSUDL1(matrix, solH, null);
	// modeloSUDL1.buildModel(timeLimit);
	//
	// modeloSUDL1.solve();
	// info = modeloSUDL1.info();
	// out.write(info.toString() + ", " + modeloSUDL1.getObjective() + ", ");
	// out.flush();
	// modeloSUDL1.close();
	//
	// ModeloRC modeloRC = new ModeloRC(matrix, solH, null);
	// modeloRC.buildModel(timeLimit);
	//
	// modeloRC.solve();
	// info = modeloRC.info();
	// out.write(info.toString() + ", " + modeloRC.getObjective() + ", ");
	// out.flush();
	// modeloRC.close();
	//
	// ModeloXY modeloXY = new ModeloXY(matrix, solH, null);
	// modeloXY.buildModel(timeLimit);
	//
	// modeloXY.solve();
	// info = modeloXY.info();
	// out.write(info.toString() + ", " + modeloXY.getObjective() + "\n");
	// out.flush();
	// modeloXY.close();
	// }
	//
	// if (out != null)
	// out.close();
	// }
}
