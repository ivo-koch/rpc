package rpc.branch.and.price;

public class MatrizConBorde extends Matriz{

	private final Matriz matrix;
	

	public MatrizConBorde(Matriz matriz) {
		this.matrix = matriz;
	}
	
	@Override
	public boolean get(int i, int j) {
		if (i == 0 || j == 0 || j == this.matrix.columnas() + 1 || i == this.matrix.filas() + 1)
			return false;
		
		return matrix.get(i - 1, j - 1);
	}
	
	@Override
	public int filas() {
		return matrix.filas() + 2;
	}

	@Override
	public int columnas() {
		return matrix.columnas() + 2;
	}
	
	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		for (int f = 0; f < filas(); f++) {
			for (int c = 0; c < columnas(); c++)
				if (get(f, c))
					sb.append("1 ");
				else
					sb.append("0 ");
			sb.append("\n");
		}
		return sb.toString();
	}

}
