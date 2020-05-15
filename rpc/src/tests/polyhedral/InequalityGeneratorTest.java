package tests.polyhedral;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import rpc.polyhedral.computations.Inequality;
import rpc.polyhedral.computations.InequalityGenerator;

class InequalityGeneratorTest {

	@Test
	void test() {
		InequalityGenerator ig = new InequalityGenerator(5, 5);
		
		List<Inequality> ineq = ig.buildIneqThm3();
		
		System.out.println(ineq.size());
		for (Inequality i: ineq)
			System.out.println(i);
	
	}

}
