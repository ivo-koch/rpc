package rpc.modelos;

import java.awt.Rectangle;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex.UnknownObjectException;
import rpc.branch.and.price.Matriz;

public final class ModeloABC extends Modelo {

	private IloNumVar[][][] a;
	private IloNumVar[][][] b;
	private IloNumVar[][][] c;

	public ModeloABC(Matriz matrix, int k) throws Exception {
		super(matrix, k, 0.01, null);
	}
	
	public ModeloABC(Matriz matrix, int k, OutputStream out) throws Exception {
		super(matrix, k, 0.01, out);
	}

	@Override
	public void buildModel() throws Exception {

		// inicializo las variables.
		a = new IloNumVar[matriz.filas()][matriz.columnas()][k];
		b = new IloNumVar[matriz.filas()][matriz.columnas()][k];
		c = new IloNumVar[matriz.filas()][matriz.columnas()][k];

		forall((f, co, r) -> {
			a[f][co][r] = cplex.boolVar("a[" + f + "," + co + ", " + r + "]");
			b[f][co][r] = cplex.boolVar("b[" + f + "," + co + ", " + r + "]");
			c[f][co][r] = cplex.boolVar("c[" + f + "," + co + ", " + r + "]");
		});

		// función objetivo1 ok
		cplex.addMinimize(sum((f, c, r) -> {
			return cplex.prod(1, a[f][c][r]);
		}));

		// restricciones

		// # (1)
		// subto posicionDeTopVertex: forall <i,j,k> in R*C*K:
		// 2*c[i,j,k] <= sum <i1, j1> in R*C with i1 <= i and j1 <= j: a[i1, j1, k] +
		// sum <i1, j1> in R*C with i1 >= i and j1 >= j : b[i1, j1, k];

		forall((f, co, r) -> {
			addLe(0.0, cplex.prod(-2.0, c[f][co][r]),

					sum((f1, c1) -> {
						return cplex.prod(1.0, a[f1][c1][r]);
					}, (f1, c1) -> { return f1 <= f && c1 <= co;}),

					sum((f1, c1) -> {
						return cplex.prod(1.0, b[f1][c1][r]);
					}, (f1, c1) -> { return f1 >= f && c1 >= co;}));
		});

		// # (2)
		// subto cerosEnRectangulos: forall <i,j,k> in R*C*K:
		// 1 - c[i,j,k] <= 2 - (sum <i1, j1> in R*C with i1 <= i and j1 <= j: a[i1, j1,
		// k]) - (sum <i1, j1> in R*C with i1 >= i and j1 >= j : b[i1, j1, k]);

		forall((f, co, r) -> {
			addLe(-1.0, cplex.prod(1.0, c[f][co][r]),

					sum((f1, c1) -> {
						return cplex.prod(-1.0, a[f1][c1][r]);
					}, (f1, c1) -> { return f1 <= f && c1 <= co;}),

					sum((f1, c1) -> {
						return cplex.prod(-1.0, b[f1][c1][r]);
					}, (f1, c1) -> { return f1 >= f && c1 >= co;}));
		});
		// # (3)
		// subto ceros: forall <i,j> in R*C:
		// M[i,j] <= sum <k> in K : c[i, j, k];
		forall((f, co) -> {
			addLe(matriz.get(f, co) ? 1.0 : 0.0,

					sum((r) -> {
						return cplex.prod(1.0, c[f][co][r]);
					}));
		});

		// # (4)
		// subto unos: forall <i,j,k> in R*C*K:
		// c[i,j, k] <= M[i,j];
		forall((f, co, r) -> {
			addLe(matriz.get(f, co) ? -1.0 : 0.0, cplex.prod(-1.0, c[f][co][r]));
		});

		// # (5)
		// subto unInicioRectanguloPorPosicion: forall <k> in K:
		// sum <i, j> in R*C : a[i, j, k] <= 1;
		forall((r) -> {
			addLe(-1.0, sum((f, c) -> {
				return cplex.prod(-1.0, a[f][c][r]);
			}));
		});

		// # (6)
		// subto unFinRectanguloPorPosicion: forall <k> in K:
		// sum <i, j> in R*C : b[i, j, k] <= 1;
		forall((r) -> {
			addLe(-1.0, sum((f, c) -> {
				return cplex.prod(-1.0, b[f][c][r]);
			}));
		});

		
		
//		subto simetria:
//			forall <i, j, k> in R*C*K with k > 1:
//				a[i,j, k] <= sum <i1, j1> in R*C: a[i1, j1, k - 1];
		forall((f, co, r) -> {
			if (r > 0)
				addLe(0.0, "simetria", cplex.prod(-1.0, a[f][co][r]),						
					sum((f1, c1) -> {
						return cplex.prod(1.0, a[f1][c1][r - 1]);
					}));				
		});
		
		//cplex.exportModel("model1.lp");	
	}
	
	@Override
	public Solucion getSolution() throws UnknownObjectException, IloException {
		
		List<Rectangle> rects = new ArrayList<Rectangle>();
		
		for(int k = 0; k < this.k; k++) {
				int minX = Integer.MAX_VALUE;
				int minY = Integer.MAX_VALUE;
				int maxX = Integer.MIN_VALUE;
				int maxY = Integer.MIN_VALUE;
				for (int f = 0; f < matriz.filas(); f++)
					for (int col = 0; col < matriz.columnas(); col++)
						if (cplex.getValue(c[f][col][k]) > precision) {
							minX = Integer.min(minX, col);
							minY = Integer.min(minY, f);
							maxX = Integer.max(maxX, col);
							maxY = Integer.max(maxY, f);
						}
				
				if (minX < Integer.MAX_VALUE || minY < Integer.MAX_VALUE || maxX > Integer.MIN_VALUE || maxY > Integer.MIN_VALUE)								
					rects.add(new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1));
			}
		
		return new Solucion(matriz, rects);
		
	}
	

}
