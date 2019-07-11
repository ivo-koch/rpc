package rpc.modelos;

import java.awt.Rectangle;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex.UnknownObjectException;
import rpc.branch.and.price.Matriz;

public final class ModeloXY extends Modelo {

	private IloNumVar[][][] x;
	private IloNumVar[] y;
	

	public ModeloXY(Matriz matrix, int k) throws Exception {
		super(matrix, k, 0.0, null);
	}
	
	public ModeloXY(Matriz matrix, int k, OutputStream out) throws Exception {
		super(matrix, k, 0.0, out);
	}

	@Override
	public void buildModel() throws Exception {

		int filas = matriz.filas();
		int cols = matriz.columnas();
		
		// inicializo las variables.
		x = new IloNumVar[filas][cols][k];
		y = new IloNumVar[k];		
		
		
//		for (int f = 0; f < filas; f++)
//			for (int c = 0; c < cols; c++)
//				for (int r = 0; r < k; r++){
//					x[f][c][r] = cplex.boolVar("x[" + f + "," + c + ", " + r + "]");
//					if (f == 0 && c == 0)
//						y[r] = cplex.boolVar("y[" + r + "]");					
//				};
		
		
		forall((f, c, r) -> {			
			x[f][c][r] = cplex.boolVar("x[" + f + "," + c + ", " + r + "]");
			if (f == 0 && c == 0)
				y[r] = cplex.boolVar("y[" + r + "]");
		});	


		//# Minimizar la cantidad de rectangulos usados
		//minimize fobj: sum <k> in K: y[k];
		// función objetivo1 ok
		cplex.addMinimize(sum((k) -> {
			return cplex.prod(1.0, y[k]);
		}, 0, k));

		//		subto rect1: forall <i, j,j1, k> in R*C*C*K with j1 < j-1:
		//			 x[i, j, k]  + x[i, j1, k] <= 1 + x[i, j-1, k];
		forall((i, j, j1, k) -> {
			if (j1 < j -1 )
				addLe(-1, sum(cplex.prod(-1.0, x[i][j][k]), cplex.prod(-1.0, x[i][j1][k]), cplex.prod(1.0, x[i][j - 1][k])));
		}, 0, filas, 1, cols, 0, cols, 0, k);	
		
//		subto rect2: forall <i, i1, j, k> in R*R*C*K with i1 < i-1:
//			 x[i, j, k]  + x[i1, j, k] <= 1 + x[i-1, j, k];
		forall((i, i1, j, k) -> {	
			if (i1 < i - 1)
				addLe(-1, sum(cplex.prod(-1.0, x[i][j][k]), cplex.prod(-1.0, x[i1][j][k]), cplex.prod(1.0, x[i-1][j][k])));
		}, 1, filas, 0, filas, 0, cols, 0, k);	
		
//		subto rect3: forall <i, i1, j, j1, k> in R*R*C*C*K with i < i1 and j != j1:
//			 x[i, j, k]  + x[i1, j1, k] <= 1 + (x[i, j1, k] + x[i1, j, k]) * 0.5;
		forall((i, i1, j, j1, k) -> {			
			if (i < i1 && j != j1)
				addLe(-1, sum(cplex.prod(0.5, x[i][j1][k]), cplex.prod(0.5, x[i1][j][k]), cplex.prod(-1.0, x[i][j][k]), cplex.prod(-1.0, x[i1][j1][k])));
		}, 0, filas, 0, filas, 0, cols, 0, cols,0, k);	
		

//		subto todosLos1sCubiertos: forall <i,j> in R*C with M[i, j] == 1:	
//			1 <= sum<k> in K: x[i, j, k];
		forall((i,j) -> {			
			if (matriz.get(i, j))
				addLe(1, sum((r) -> {
					return cplex.prod(1.0, x[i][j][r]);
				}));
		}, 0, filas, 0, cols);
		

//		subto ningun0Cubierto: forall <i,j> in R*C with M[i, j] == 0:	
//			0 == sum<k> in K: x[i, j, k];
		forall((i,j) -> {			
			if (!matriz.get(i, j))
				addEq(0, sum((r) -> {
					return cplex.prod(1.0, x[i][j][r]);
				}));
		}, 0, filas, 0, cols);
		
//		subto r6:
//			forall <i, j, k> in R*C*K:
//				x[i, j, k] <= y[k];

		forall((i,j,k) -> {
			addLe(0, sum(cplex.prod(1.0, y[k])), cplex.prod(-1.0, x[i][j][k]));
		}, 0, filas, 0, cols, 0, k);
		//cplex.exportModel("modelofc.lp");
		
		//subto simetria:forall <k> in K with k > 1:
		//    y[k] <= y[k-1];
		forall((k) -> {
			if (k > 0)
				addLe(0, sum(cplex.prod(-1.0, y[k])), cplex.prod(1.0, y[k - 1]));
		});
		
	}
	@Override
	public Solucion getSolution() throws UnknownObjectException, IloException {
		
		List<Rectangle> rects = new ArrayList<Rectangle>();
		
		for(int k = 0; k < this.k; k++)
			if (cplex.getValue(y[k]) > 0)
			{
				int minX = Integer.MAX_VALUE;
				int minY = Integer.MAX_VALUE;
				int maxX = Integer.MIN_VALUE;
				int maxY = Integer.MIN_VALUE;
				for (int f = 0; f < matriz.filas(); f++)
					for (int c = 0; c < matriz.columnas(); c++)
						if (cplex.getValue(x[f][c][k]) > 0) {
							minX = Integer.min(minX, c);
							minY = Integer.min(minY, f);
							maxX = Integer.max(maxX, c);
							maxY = Integer.max(maxY, f);
						}
				
				if (minX == Integer.MAX_VALUE || minY == Integer.MAX_VALUE || maxX == Integer.MIN_VALUE || maxY == Integer.MIN_VALUE)
					throw new RuntimeException("Error en la solución");
				
				rects.add(new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1));
			}
		
		return new Solucion(matriz, rects);		
	}
}
