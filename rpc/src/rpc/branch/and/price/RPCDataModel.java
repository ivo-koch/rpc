package rpc.branch.and.price;

import java.awt.Point;
import java.util.Collections;
import java.util.List;

import org.jorlib.frameworks.columnGeneration.model.ModelInterface;

/***
 * Esta clase contiene información del problema, que se comparte entre el master
 * y el problema de pricing.
 * 
 * @author ik
 *
 */
public class RPCDataModel implements ModelInterface {

	/*** La matriz input ***/
	public final Matriz matriz;

	public Point MijConCubrimientoFrac;
	public double valorCubrimientoFrac; 
	public Interval intActual;
	
	public RPCDataModel(Matriz matriz) {
		this.matriz = matriz;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}	

}
