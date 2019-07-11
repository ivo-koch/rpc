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

public final class ModeloSUDL1 extends Modelo {

	private IloNumVar[][][] start;
	private IloNumVar[][][] diag;
	private IloNumVar[][][] up;
	private IloNumVar[][][] left;
	

	public ModeloSUDL1(Matriz matrix, int k) throws Exception {		
		super(matrix, k, 0.0, null);
	}
	
	public ModeloSUDL1(Matriz matrix, int k, OutputStream out) throws Exception {		
		super(matrix, k, 0.0, out);
	}

	@Override
	public void buildModel() throws Exception {

		System.out.println(matriz.toString());
		
		int filas = matriz.filas();
		int cols = matriz.columnas();
		
		// inicializo las variables.
		start = new IloNumVar[filas][cols][k];
		diag = new IloNumVar[filas][cols][k];
		up = new IloNumVar[filas][cols][k];
		left = new IloNumVar[filas][cols][k];

	
		forall((f, co, r) -> {
			start[f][co][r] = cplex.boolVar("start[" + f + "," + co + ", " + r + "]");
			diag[f][co][r] = cplex.boolVar("diag[" + f + "," + co + ", " + r + "]");
			up[f][co][r] = cplex.boolVar("up[" + f + "," + co + ", " + r + "]");
			left[f][co][r] = cplex.boolVar("left[" + f + "," + co + ", " + r + "]");
		});

		// función objetivo1
//		# Minimizar la cantidad de rectangulos usados
//		minimize fobj: sum <i, j, k> in R*C*K: start[i, j, k];
		cplex.addMinimize(sum((f, c, r) -> {
			return cplex.prod(1, start[f][c][r]);
		}));		
	
		// restricciones
//		subto rectCubierto: forall <i,j> in R*C:
//			   M[i,j] <= sum <k> in K : (start[i, j, k] + diag[i, j, k] + up[i, j, k] + left[i, j, k]);
		forall((f, c) -> {
			addLe(matriz.get(f, c) ? 1.0 : 0.0, "rectCubierto",

					sum((r) -> {
						return sum(cplex.prod(1.0, start[f][c][r]), 
								cplex.prod(1.0, diag[f][c][r]),
								cplex.prod(1.0, up[f][c][r]), 
								cplex.prod(1.0, left[f][c][r]));
					}));
		});
		

//		subto startEn1: forall <i,j,k> in R*C*K:
//		     (start[i,j, k] + left[i, j, k] + up[i, j, k] + diag[i,j,k]) <= M[i, j];	
		forall((f, c, r) -> {			
			addLe(matriz.get(f, c) ? -1.0 : 0.0, "startEn1",					
						sum(cplex.prod(-1.0, start[f][c][r]), 
								cplex.prod(-1.0, diag[f][c][r]),
								cplex.prod(-1.0, up[f][c][r]), 
								cplex.prod(-1.0, left[f][c][r])));
		});

//		subto siempreHayStart1: forall <i,j, k> in R*C*K:
//			left[i, j, k] + diag[i, j, k] + up[i, j, k]  <= sum <i1, j1> in R*C:start[i1, j1, k];
		forall((f, c, r) -> {
			addLe(0.0, "siempreHayStart1",

					sum(cplex.prod(-1.0, left[f][c][r]), 
						cplex.prod(-1.0, diag[f][c][r]), 
						cplex.prod(-1.0, up[f][c][r]),
							sum((f1, c1) -> {
								return cplex.prod(1.0, start[f1][c1][r]);
							})));
		});
		
//		subto unStartPorRect: forall <k> in K:
//			(sum <i,j> in R*C: start[i, j, k]) <= 1;
		forall((r) -> {
			addLe(-1, "unStartPorRect", sum((f, c) -> {
				return cplex.prod(-1.0, start[f][c][r]);
			}));
		});

//		subto upYLeft: forall <k> in K:
//		    forall <i1, j1, i2, j2> in R*C*R*C:
//		            left[i1, j1, k] + up[i2, j2,k] <= diag[i2, j1, k] + 1;
		forall((r) -> {
			forall((f1, c1, f2, c2) -> {			
					addLe(-1.0, "upYLeft",
						sum(cplex.prod(-1.0, left[f1][c1][r]), 						
							cplex.prod(-1.0, up[f2][c2][r]),
							cplex.prod(1.0, diag[f2][c1][r])
							));					
				}, 0, filas, 0, cols, 0, filas, 0, cols);
			});		

//		subto restUp: 
//			   forall <i,j, k> in R*C*K with i > 1:	
//				    up[i, j, k] <= up[i-1,j, k] + start[i-1, j, k];
		forall((f, c, r) -> { 
			if (f > 0)	
				addLe(0.0, "restUp",	
						sum(cplex.prod(-1.0, up[f][c][r]), 
							cplex.prod(1.0, up[f-1][c][r]), 
							cplex.prod(1.0, start[f-1][c][r])
							));
		});
		
//subto restUp2: 
//   forall <j, k> in C*K:	
//	    up[1, j, k] == 0;
		forall((c, r) -> { 		
				addEq(0.0, "restUp2", cplex.prod(1.0, up[0][c][r]));
		}, 0, cols, 0, k);		
		
		
//		subto restLeft:
//			   forall <i,j, k> in R*C*K with j > 1:	
//				    left[i, j, k] <= left[i,j-1, k] + start[i, j-1, k];
		forall((f, c, r) -> {	
			if (c > 0)
				addLe(0.0,	"restLeft",
						sum(cplex.prod(-1.0, left[f][c][r]), 
							cplex.prod(1.0, left[f][c - 1][r]), 
							cplex.prod(1.0, start[f][c - 1][r])
							));
		});
		
//		subto restLeft2:
//			   forall <i, k> in R*K:	
//				    left[i, 1, k] == 0;
		forall((f, r) -> { 		
			addEq(0.0, "restLeft2", cplex.prod(1.0, left[f][0][r]));
		}, 0, filas, 0, k);		
			
		
//		subto restDiag:
//			   forall <i,j, k> in R*C*K with i > 1:	
//				      diag[i, j, k] <= diag[i-1, j, k] + left[i-1, j, k];			   
		forall((f, c, r) -> {	
			if (f > 0)
				addLe(0.0, "restDiag",	
						sum(cplex.prod(-1.0, diag[f][c][r]), 
							cplex.prod(1.0, diag[f - 1][c][r]), 
							cplex.prod(1.0, left[f - 1][c][r])
							));
		});

//		subto restDiag2:
//			   forall <i,j, k> in R*C*K with i > 1 and j > 1:	
//				      diag[i, j, k] <= diag[i-1, j-1, k] + left[i-1, j-1, k] + up[i-1, j-1, k] + start[i-1, j-1, k];			   
		forall((f, c, r) -> {
			if (f > 0 && c > 0)
				addLe(0.0, "restDiag2",	
						sum(cplex.prod(-1.0, diag[f][c][r]), 
							cplex.prod(1.0, diag[f - 1][c - 1][r]), 
							cplex.prod(1.0, left[f - 1][c - 1][r]),
							cplex.prod(1.0, up[f - 1][c - 1][r]),
							cplex.prod(1.0, start[f - 1][c - 1][r])
							));
		});
		
	//subto restDiag3:
	//   forall <i,j, k> in R*C*K with j > 1:	
	//	      diag[i, j, k] <= diag[i, j-1, k] + up[i, j -1, k];
		forall((f, c, r) -> {	
			if (c > 0)
				addLe(0.0,	"restDiag3",
					sum(cplex.prod(-1.0, diag[f][c][r]), 
						cplex.prod(1.0, diag[f][c - 1][r]),						
						cplex.prod(1.0, up[f][c - 1][r])
						));
		});
		
	//		subto restDiag4:
	//			   forall <i, k> in R*K:	
	//				    diag[i, 1, k] == 0;
		forall((f, r) -> { 		
			addEq(0.0, "restDiag4", cplex.prod(1.0, diag[f][0][r]));
		}, 0, filas, 0, k);	
		
//		subto restDiag5: 
//			   forall <j, k> in C*K:	
//				    diag[1, j, k] == 0;
		forall((c, r) -> { 		
			addEq(0.0, "restDiag5", cplex.prod(1.0, diag[0][c][r]));
		}, 0, cols, 0, k);			
		
		//cplex.exportModel("modelosudl1.lp");
		

		//subto simetria:
		//forall <i, j, k> in R*C*K with k > 1:
		//	start[i,j, k] <= sum <i1, j1> in R*C: start[i1, j1, k - 1];
		forall((f, c, r) -> {	
			if (r > 0)
				addLe(0.0,	"simetria",
					sum(cplex.prod(-1.0, start[f][c][r]), 
							sum((f1, c1) -> {
								return cplex.prod(1.0, start[f1][c1][r - 1]);
							})
						));
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
	
	private void printCplexVars() throws UnknownObjectException, IloException {
		
		for(int k = 0; k < this.k; k++) 
			for (int f = 0; f < matriz.filas(); f++)
				for (int col = 0; col < matriz.columnas(); col++) {
					if (cplex.getValue(start[f][col][k]) > 0)
						System.out.println("start["+ f + ", " + col + ", " + k + "]");
					if (cplex.getValue(left[f][col][k]) > 0)
						System.out.println("left["+ f + ", " + col + ", " + k + "]");
					if (cplex.getValue(up[f][col][k]) > 0)
						System.out.println("up["+ f + ", " + col + ", " + k + "]");
					if (cplex.getValue(diag[f][col][k]) > 0)
						System.out.println("diag["+ f + ", " + col + ", " + k + "]");
				}

	}
	
	@Override
	public Solucion getSolution() throws UnknownObjectException, IloException {
		
		List<Rectangle> rects = new ArrayList<Rectangle>();
		printCplexVars();
		for(int k = 0; k < this.k; k++) {
				int minX = Integer.MAX_VALUE;
				int minY = Integer.MAX_VALUE;
				int maxX = Integer.MIN_VALUE;
				int maxY = Integer.MIN_VALUE;
				for (int f = 0; f < matriz.filas(); f++)
					for (int col = 0; col < matriz.columnas(); col++)
						if (cplex.getValue(start[f][col][k]) > 0 || 
							cplex.getValue(up[f][col][k]) > 0 ||
							cplex.getValue(left[f][col][k]) > 0 ||
							cplex.getValue(diag[f][col][k]) > 0) {
							
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
