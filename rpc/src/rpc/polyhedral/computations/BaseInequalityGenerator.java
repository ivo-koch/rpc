package rpc.polyhedral.computations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rpc.polyhedral.computations.Inequality.Termino;

public abstract class BaseInequalityGenerator {

	protected int filas;
	protected int columnas;


	public BaseInequalityGenerator(int filas, int columnas) {

		this.filas = filas;
		this.columnas = columnas;
	}	

	public abstract List<Inequality> build();
	
	protected Inequality buildIneq(int f, int c, int f1, int c1, int f2, int c2) {

		Inequality ineq = new Inequality();
		Termino primer1 = ineq.new Termino(f, c, 1);
		ineq.rhs = 1;

		Termino menos1 = ineq.new Termino(f1, c1, -1);

		Termino segundo1 = ineq.new Termino(f2, c2, 1);

		ineq.terminos.add(primer1);
		ineq.terminos.add(menos1);
		ineq.terminos.add(segundo1);

		return ineq;
	}

	
}
