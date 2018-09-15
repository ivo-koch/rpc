package rpc.branch.and.price;

public class Estadisticas {
	
	public static int llamadasGreedy = 0;
	public static int llamadasExitosasAGreedy = 0;
	public static int columnasGreedy = 0;
	public static int llamadasDist = 0;
	public static int llamadasExitosasDist = 0;
	public static int columnasDist = 0;
	public static int llamadasExacto = 0;
	public static int columnasExacto = 0;
	public static int llamadasBacktracking = 0;
	public static int columnasBacktracking = 0;
	public static long startTime = 0;
	public static long stopTime = 0;
	
	public static void print()
	{
		System.out.println("Llamados heur. greedy: " + llamadasGreedy);
		System.out.println("Llamados exitosos heur. greedy: " + llamadasExitosasAGreedy);
		System.out.println("Columnas greedy: " + columnasGreedy);
		System.out.println("Llamados heur. dist: " + llamadasDist);
		System.out.println("Llamados exitosos heur. dist: " + llamadasExitosasDist);
		System.out.println("Columnas heur.dist: " + columnasDist);
		System.out.println("Llamados exacto: " + llamadasExacto);
		System.out.println("Columnas exacto: " + columnasExacto);
		System.out.println("Llamados backtracking: " + llamadasBacktracking);
		System.out.println("Columnas backtracking: " + columnasBacktracking);
		System.out.println("Tiempo: " + (stopTime - startTime));
	}
	
	
}
