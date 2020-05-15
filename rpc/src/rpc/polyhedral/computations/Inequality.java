package rpc.polyhedral.computations;

import java.util.ArrayList;
import java.util.List;

public class Inequality {

	public List<Termino> terminos = new ArrayList<Termino>();

	public double rhs;

	public class Termino {

		public int f;
		public int c;
		public double coef;

		public Termino(int f, int c, double coef) {
			super();
			this.f = f;
			this.c = c;
			this.coef = coef;
		}

		@Override
		public String toString() {
			return "[f=" + f + ", c=" + c + ", coef=" + coef + "]";
		}
		
		
	}

	public Inequality() {

	}

	public Inequality clonar() {

		Inequality nueva = new Inequality();
		nueva.rhs = this.rhs;
		for (Termino t : this.terminos)
			nueva.terminos.add(new Termino(t.f, t.c, t.coef));

		return nueva;
	}

	public void addTermino(int f, int c, double coef) {

		this.terminos.add(new Termino(f, c, coef));
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(rhs);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((terminos == null) ? 0 : terminos.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Inequality other = (Inequality) obj;
		if (Double.doubleToLongBits(rhs) != Double.doubleToLongBits(other.rhs))
			return false;
		if (terminos == null) {
			if (other.terminos != null)
				return false;
		} else if (!terminos.equals(other.terminos))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[" + terminos + "]";
	}
	
	
	
	
}
