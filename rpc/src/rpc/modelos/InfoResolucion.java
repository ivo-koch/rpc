package rpc.modelos;

import ilog.cplex.IloCplex.Status;

public class InfoResolucion {

	public Status cplexStatus;
	public int nodos;
	public double gap;
	public long tiempoRes;

	public InfoResolucion() {
	}

	public InfoResolucion(Status status, int nodos, double gap, long tiempoRes) {
		super();
		this.cplexStatus = status;
		this.nodos = nodos;
		this.gap = gap;
		this.tiempoRes = tiempoRes;
	}

	@Override
	public String toString() {
		return cplexStatus.toString() + ", " + nodos + ", " + gap + ", " + tiempoRes;
	}
}
