package rpc.polyhedral.computations;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import rpc.branch.and.price.Matriz;

public class Solver {

	public void resolver(Matriz m) {

		try {

			ModeloMaster mm = new ModeloMaster(m, null);
			mm.buildModel();

			System.out.println("Modelo construido");
			boolean res = mm.solve();
			if (!res)
				throw new RuntimeException("Master no resuelto");
			
			
			FileOutputStream fos = new FileOutputStream("test.dat");
	        ObjectOutputStream oos = new ObjectOutputStream(fos);
	        oos.writeObject(mm.getDualMatrix());

	        fos.close();
	        
//	        FileInputStream fis = new FileInputStream("test.dat");
//	        ObjectInputStream iis = new ObjectInputStream(fis);
//	        newTwoD = (int[][]) iis.readObject();
	        
			// out.write(absPath + ", " + res + ", " + modeloXY.getObjective() + ", ");
			// out.flush();
			mm.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
