package rpc.modelos;

import ilog.concert.IloException;
import ilog.cplex.IloCplex.MIPInfoCallback;

public class CPLEXInfoCallBack extends MIPInfoCallback {

	private int mejorSolEntera = Integer.MAX_VALUE;
	
	private InfoResolucion info;

	public CPLEXInfoCallBack(InfoResolucion info) {
		this.info = info;
	}
	@Override
	protected void main() throws IloException {
		
		mejorSolEntera =(int) Math.min(mejorSolEntera, getIncumbentObjValue());
		info.mejorSolEntera = mejorSolEntera;
		info.bestBound = getBestObjValue();
		info.nodosCallback = getNnodes64();
		
	}

	public int getMejorSolEntera() {
		return mejorSolEntera;
	}
}
