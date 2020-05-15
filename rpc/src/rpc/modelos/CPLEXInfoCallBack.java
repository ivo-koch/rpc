package rpc.modelos;

import ilog.concert.IloException;
import ilog.cplex.IloCplex.MIPInfoCallback;

public class CPLEXInfoCallBack extends MIPInfoCallback {

	private int mejorSolEntera;
	
	private InfoResolucion info;

	private boolean minimizando = true;
	public CPLEXInfoCallBack(InfoResolucion info) {
		this.info = info;
	}
	
	public CPLEXInfoCallBack(InfoResolucion info, boolean minimizando) {
		this.info = info;
		this.minimizando = minimizando;
		if (minimizando)
			mejorSolEntera = Integer.MAX_VALUE;
		else 
			mejorSolEntera = Integer.MIN_VALUE;
	}
	
	@Override
	protected void main() throws IloException {
		
		if (minimizando)
			mejorSolEntera =(int) Math.min(mejorSolEntera, getIncumbentObjValue());
		else 
			mejorSolEntera =(int) Math.max(mejorSolEntera, getIncumbentObjValue());
		info.mejorSolEntera = mejorSolEntera;
		info.bestBound = getBestObjValue();
		info.nodosCallback = getNnodes64();
		
	}

	public int getMejorSolEntera() {
		return mejorSolEntera;
	}
}
