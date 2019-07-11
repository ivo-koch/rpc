package rpc.modelos;

import java.awt.Rectangle;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.jorlib.frameworks.columnGeneration.io.TimeLimitExceededException;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;
import rpc.branch.and.price.Matriz;

public final class ModeloRC extends Modelo {

	private IloNumVar[][] wr;
	private IloNumVar[][] wc;
	private IloNumVar[][][] x;
	private IloNumVar[] y;

	public ModeloRC(Matriz matrix, int k) throws Exception {
		super(matrix, k, 0.0, null);
	}

	public ModeloRC(Matriz matrix, int k, OutputStream out) throws Exception {
		super(matrix, k, 0.0, out);
	}
	
	@Override
	public void buildModel() throws Exception {

		// inicializo las variables.
		wr = new IloNumVar[matriz.filas()][k];
		wc = new IloNumVar[matriz.columnas()][k];
		x = new IloNumVar[matriz.filas()][matriz.columnas()][k];
		y = new IloNumVar[k];
		
		forall((f, c, r) -> {			
			x[f][c][r] = cplex.boolVar("x[" + f + "," + c + ", " + r + "]");
			if (f == 0)
				wc[c][r] = cplex.boolVar("wc[" +  c + ", " + r + "]");
			if (c == 0)
				wr[f][r] = cplex.boolVar("wf[" +  c + ", " + r + "]");
			if (c == 0 && f == 0)
				y[r] = cplex.boolVar("y[" +  r + "]");
		});

		// función objetivo1 ok
		cplex.addMinimize(sum((r) -> {
			return cplex.prod(1, y[r]);
		}));

		// restricciones
		//# Los unos en cada fila deben ser consecutivos
		//subto consecutivosfila: forall <j,t,k> in C*C*K with j>2 and t<j-1:
		//    wc[k,j] <= wc[k,j-1] + (1 - wc[k,t]);
		forall((j, t, r) -> {
			if (j > 1 && t < j - 1)
			{
				addLe(-1.0, sum(cplex.prod(-1.0, wc[j][r]),
					cplex.prod(1.0, wc[j - 1][r]),
					cplex.prod(-1.0, wc[t][r])));
			}
		}, 0, matriz.columnas(), 0, matriz.columnas());

		//		# Los unos en cada columna deben ser consecutivos
		//		subto consecutivoscolumna: forall <i,t,k> in R*R*K with i>2 and t<i-1:
		//		    wr[k,i] <= wr[k,i-1] + (1 - wr[k,t]);
		forall((i, t, r) -> {
			if (i > 1 && t < i - 1)
			{
				addLe(-1.0, sum(cplex.prod(-1.0, wr[i][r]),
					cplex.prod(1.0, wr[i - 1][r]),
					cplex.prod(-1.0, wr[t][r])));
			}
		}, 0, matriz.filas(), 0, matriz.filas());
		
//		# x[k,i,j] = 1 solamente si el rectangulo k incluye la fila i y la columna j
//				subto defx: forall <i,j,k> in R*C*K:
//				    x[k,i,j] >= wr[k,i] + wc[k,j] - 1;
		forall((i, j, r) -> {			
			addLe(-1.0, sum(cplex.prod(1.0, x[i][j][r]),
				cplex.prod(-1.0, wr[i][r]),
				cplex.prod(-1.0, wc[j][r])));			
		});		
		
//		# x[k,i,j] = 0 si el rectangulo k no incluye la fila i
//				subto defxfila: forall <i,j,k> in R*C*K:
//				    x[k,i,j] <= wr[k,i];
		forall((i, j, r) -> {			
			addLe(0.0, sum(cplex.prod(-1.0, x[i][j][r]),
				cplex.prod(1.0, wr[i][r])));			
		});
		
//		# x[k,i,j] = 0 si el rectangulo k no incluye la columna j
//				subto defxcolumna: forall <i,j,k> in R*C*K:
//				    x[k,i,j] <= wc[k,j];
		forall((i, j, r) -> {			
			addLe(0.0, sum(cplex.prod(-1.0, x[i][j][r]),
				cplex.prod(1.0, wc[j][r])));			
		});	
		
//		# Cada punto con M[i,j] = 1 debe estar cubierto
//				subto cubiertos: forall <i,j> in R*C with M[i,j] == 1:
//				    sum <k> in K: x[k,i,j] >= 1;
		forall((i, j) -> {
			if (matriz.get(i, j))
				addLe(1.0, 
						sum(r -> {		
							return cplex.prod(1.0, x[i][j][r]);
					}));
		});

//# Ningun punto con M[i,j] = 0 debe estar cubierto
//subto nocubiertos: forall <i,j> in R*C with M[i,j] == 0:
//    sum <k> in K: x[k,i,j] == 0;
		forall((i, j) -> {
			if (!matriz.get(i, j))
				addEq(0.0, 
						sum(r -> {		
							return cplex.prod(1.0, x[i][j][r]);
					}));
		});
		
//		# Definition de las variables y en funcion de wr
//		subto defy: forall <i,k> in R*K:
//		    wr[k,i] <= y[k];		
		forall((i, r) -> {			
			addLe(0.0, sum(cplex.prod(1.0, y[r]),
					cplex.prod(-1.0, wr[i][r])));
		}, 0, matriz.filas(), 0, this.k);	
		
		//cplex.exportModel("modelorc.lp");

//		subto simetria:forall <k> in K with k > 1:
//		y[k] <= y[k-1];
		forall((r) -> {	
			if (r > 0)
				addLe(0.0, "simetria", sum(cplex.prod(-1.0, y[r]),
					cplex.prod(1.0, y[r - 1])));
		});			

	}
	
	/**
	 * Método principal que resuelve el problema de pricing.
	 * 
	 * @return List of columns (independent sets) with negative reduced cost.
	 * @throws TimeLimitExceededException
	 *             TimeLimitExceededException
	 */
	public boolean solve() throws TimeLimitExceededException {

		try {
			// apago el presolve. Si no lo hacemos, el puto hace algo que termina en unbounded.
			cplex.setParam(IloCplex.BooleanParam.PreInd, false);
			return super.solve();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
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
