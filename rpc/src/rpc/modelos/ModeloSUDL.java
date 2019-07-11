package rpc.modelos;

import java.awt.Rectangle;
import java.io.OutputStream;
import java.util.List;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex.UnknownObjectException;
import rpc.branch.and.price.Matriz;
import rpc.branch.and.price.MatrizConBorde;

public final class ModeloSUDL extends Modelo {

	private IloNumVar[][][] start;
	private IloNumVar[][][] diag;
	private IloNumVar[][][] up;
	private IloNumVar[][][] left;
	

	public ModeloSUDL(Matriz matrix, int k) throws Exception {		
		super(matrix, k, 0.0, null);				
		matriz = new MatrizConBorde(matrix);
	}
	
	public ModeloSUDL(Matriz matrix, int k, OutputStream out) throws Exception {		
		super(matrix, k, 0.0, out);				
		matriz = new MatrizConBorde(matrix);
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
		//
		//minimize fobj: sum <i, j, k> in R*C*K: start[i, j, k];
		cplex.addMinimize(sum((f, c, r) -> {
			return cplex.prod(1, start[f][c][r]);
		}, 1, filas - 1, 1, cols - 1, 0, k));		
	
		// restricciones
		//
		//subto rectCubierto: forall <i,j> in R*C:
		//   M[i,j] <= sum <k> in K : (start[i, j, k] + diag[i, j, k] + up[i, j, k] + left[i, j, k]);
		forall((f, c) -> {
			addLe(matriz.get(f, c) ? 1.0 : 0.0, "rectCubierto",

					sum((r) -> {
						return sum(cplex.prod(1.0, start[f][c][r]), 
								cplex.prod(1.0, diag[f][c][r]),
								cplex.prod(1.0, up[f][c][r]), 
								cplex.prod(1.0, left[f][c][r]));
					}));
		}, 1, filas - 1, 1, cols - 1);
		
//		subto startEn1: forall <i,j> in RAmp*CAmp:
//			  forall <k> in K:
//			     (start[i,j, k] + left[i, j, k] + up[i, j, k] + diag[i,j,k]) <= M[i, j];
		forall((f, c) -> {
			forall((r) -> {
			addLe(matriz.get(f, c) ? -1.0 : 0.0, "startEn1",					
						sum(cplex.prod(-1.0, start[f][c][r]), 
								cplex.prod(-1.0, diag[f][c][r]),
								cplex.prod(-1.0, up[f][c][r]), 
								cplex.prod(-1.0, left[f][c][r])));
					});
		});

//		subto siempreHayStart1: forall <i,j, k> in RAmp*CAmp*K:
//			left[i, j, k] + diag[i, j, k] + up[i, j, k]  <= sum <i1, j1> in RAmp*CAmp:start[i1, j1, k];	
		forall((f, c, r) -> {
			addLe(0.0, "siempreHayStart1",

					sum(cplex.prod(-1.0, left[f][c][r]), 
						cplex.prod(-1.0, diag[f][c][r]), 
						cplex.prod(-1.0, up[f][c][r]),
							sum((f1, c1) -> {
								return cplex.prod(1.0, start[f1][c1][r]);
							})));
		});
		
//		#ver si no se puede eliminar.
//		subto unStartPorRect: forall <k> in K:
//					(sum <i,j> in R*C: start[i, j, k]) <= 1;
		forall((r) -> {
			addLe(-1, "unStartPorRect", sum((f, c) -> {
				return cplex.prod(-1.0, start[f][c][r]);
			}, 1, filas - 1, 1, cols - 1));
		});

//		subto upYLeft: forall <k> in K:
//		    forall <i1, j1> in R*C:
//		        forall <i2, j2> in R*C:
//		            left[i1, j1, k] + up[i2, j2,k] <= diag[i2, j1, k] + 1;		
		forall((r) -> {
			forall((f1, c1) -> {
				forall((f2, c2) -> {
			
					addLe(-1.0, "upYLeft",
						sum(cplex.prod(-1.0, left[f1][c1][r]), 						
							cplex.prod(-1.0, up[f2][c2][r]),
							cplex.prod(1.0, diag[f2][c1][r])
							));					
				}, 1, filas - 1, 1, cols - 1);
			}, 1, filas - 1, 1,  cols - 1);
		});

//#
//# (12)
//subto restUp: 
//   forall <i,j, k> in R*CAmp*K:	
//	    up[i, j, k] <= up[i-1,j, k] + start[i-1, j, k];
		forall((f, c, r) -> { 
				addLe(0.0, "restUp",	
						sum(cplex.prod(-1.0, up[f][c][r]), 
							cplex.prod(1.0, up[f-1][c][r]), 
							cplex.prod(1.0, start[f-1][c][r])
							));
		}, 1, filas - 1, 0, cols, 0, k);
		


//# (13)
//subto restLeft:
//   forall <i,j, k> in RAmp*C*K:	
//	    left[i, j, k] <= left[i,j-1, k] + start[i, j-1, k];
		forall((f, c, r) -> {			
				addLe(0.0,	"restLeft",
						sum(cplex.prod(-1.0, left[f][c][r]), 
							cplex.prod(1.0, left[f][c - 1][r]), 
							cplex.prod(1.0, start[f][c - 1][r])
							));
		}, 0, filas, 1, cols - 1, 0, k);
			
//		# (14)
//		subto restDiag:
//		   forall <i,j, k> in R*CAmp*K:	
//			      diag[i, j, k] <= diag[i-1, j, k] + left[i-1, j, k];
		forall((f, c, r) -> {						
				addLe(0.0, "restDiag",	
						sum(cplex.prod(-1.0, diag[f][c][r]), 
							cplex.prod(1.0, diag[f - 1][c][r]), 
							cplex.prod(1.0, left[f - 1][c][r])
							));
		}, 1, filas - 1, 0, cols, 0, k);


//subto restDiag2:
//   forall <i,j, k> in R*C*K:	
//	      diag[i, j, k] <= diag[i-1, j-1, k] + left[i-1, j-1, k] + up[i-1, j-1, k] + start[i-1, j-1, k];
		forall((f, c, r) -> {
				addLe(0.0, "restDiag2",	
						sum(cplex.prod(-1.0, diag[f][c][r]), 
							cplex.prod(1.0, diag[f - 1][c - 1][r]), 
							cplex.prod(1.0, left[f - 1][c - 1][r]),
							cplex.prod(1.0, up[f - 1][c - 1][r]),
							cplex.prod(1.0, start[f - 1][c - 1][r])
							));
		}, 1, filas - 1, 1, cols - 1, 0, k);
		
//		subto restDiag3:
//			   forall <i,j, k> in RAmp*C*K:	
//				      diag[i, j, k] <= diag[i, j-1, k] + up[i, j -1, k];
		forall((f, c, r) -> {			
				addLe(0.0,	"restDiag3",
					sum(cplex.prod(-1.0, diag[f][c][r]), 
						cplex.prod(1.0, diag[f][c - 1][r]),						
						cplex.prod(1.0, up[f][c - 1][r])
						));
		}, 0, filas, 1, cols - 1, 0, k);
		
		//no puedo prender variables en la fila 0
		forall((c, r) -> {
			addEq(0.0,
			sum(cplex.prod(1.0, diag[0][c][r]), 
					cplex.prod(1.0, start[0][c][r]),						
					cplex.prod(1.0, up[0][c][r]),
					cplex.prod(1.0, left[0][c][r])
					));
		}, 0, cols, 0, k);
		
		//no puedo prender variables en la col 0
		forall((f,r) -> {
			addEq(0.0,
			sum(cplex.prod(1.0, diag[f][0][r]), 
					cplex.prod(1.0, start[f][0][r]),						
					cplex.prod(1.0, up[f][0][r]),
					cplex.prod(1.0, left[f][0][r])
					));
		}, 0, filas, 0, k);
		
		//no puedo prender variables en la fila filas - 1
		forall((c, r) -> {
			addEq(0.0,
			sum(cplex.prod(1.0, diag[filas - 1][c][r]), 
					cplex.prod(1.0, start[filas - 1][c][r]),						
					cplex.prod(1.0, up[filas - 1][c][r]),
					cplex.prod(1.0, left[filas - 1][c][r])
					));
		},0, cols, 0, k);
		
		//no puedo prender variables en la columna cols - 1
		forall((f, r) -> {
			addEq(0.0,
			sum(cplex.prod(1.0, diag[f][cols-1][r]), 
					cplex.prod(1.0, start[f][cols-1][r]),						
					cplex.prod(1.0, up[f][cols-1][r]),
					cplex.prod(1.0, left[f][cols-1][r])
					));
		}, 0, filas, 0, k);
		
		cplex.exportModel("modelosudl1.lp");
		//boolean t = cplex.solve();
		//Status s = cplex.getStatus();		
	}
	
	@Override
	public Solucion getSolution() throws UnknownObjectException, IloException {	
		throw new RuntimeException("Falta implementar");	
	}
}
