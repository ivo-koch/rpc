package rpc.branch.and.price;

public class Interval {

	public final int lower;
	public final int upper;

	public Interval(int lower, int upper) {		
		if (upper < lower)
			throw new RuntimeException("Intervalo mal formado. Lower: " + lower + " Upper: " + upper); 
		this.lower = lower;
		this.upper = upper;
		
	}
	
	public Interval clonar()
	{
		return new Interval (this.lower, this.upper);
	}

	@Override
	public String toString() {
		return "[" + this.lower + "," + this.upper + "]";
	}

}
