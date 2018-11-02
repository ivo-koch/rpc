package rpc.branch.and.price;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jorlib.frameworks.columnGeneration.branchAndPrice.branchingDecisions.BranchingDecision;
import org.jorlib.frameworks.columnGeneration.io.TimeLimitExceededException;
import org.jorlib.frameworks.columnGeneration.pricing.AbstractPricingProblemSolver;

import ilog.concert.IloException;

/***
 * Implementación de solución exacta para el problema de pricing para un V0 de
 * varios vértices.
 */
public final class RPCMIPPricingSolver
		extends AbstractPricingProblemSolver<RPCDataModel, RPCColumn, RPCPricingProblem> {

	ExactMIPPricingSolverModel1 exactSolverModel1;

	/***
	 * Crea un nuevo solver de pricing.
	 * 
	 * @param dataModel
	 * @param pricingProblem
	 */
	public RPCMIPPricingSolver(RPCDataModel dataModel, RPCPricingProblem pricingProblem) {
		super(dataModel, pricingProblem);
		this.name = "ExactPricingProblemSolver";
		this.exactSolverModel1 = new ExactMIPPricingSolverModel1(dataModel.matriz, logger, config.PRECISION);
	}


	/**
	 * Método principal que resuelve el problema de pricing.
	 * 
	 * @return List of columns (independent sets) with negative reduced cost.
	 * @throws TimeLimitExceededException
	 *             TimeLimitExceededException
	 */
	@Override
	public List<RPCColumn> generateNewColumns() throws TimeLimitExceededException {
		List<RPCColumn> newPatterns = new ArrayList<>();
		try {

			logger.debug("Resolviendo exacto...");
			Estadisticas.llamadasExacto++;
			
			logger.debug("Resolviendo pricing...");

			if (!this.exactSolverModel1.solve()) {
				pricingProblemInfeasible = true;
				this.objective = Double.MIN_VALUE;
				throw new RuntimeException("Pricing problem infeasible");				
			}

			// Encontramos un óptimo
			logger.debug("Pricing resuelto");
			this.pricingProblemInfeasible = false;
			this.objective = this.exactSolverModel1.getObjective();

			// podemos agregar el resultado a la base?
			if (objective - config.PRECISION > 1) { //TODO: Ver con Javi- config.PRECISION) { // - config.PRECISION) {
				// SI
				// si es así, agregamos una nueva columna representada por este rectángulo
				// a la base
				Rectangle encontrado = this.exactSolverModel1.getColumn();

				logger.debug(encontrado.toString());
				logger.debug("Obj: " + objective);
				// logger.debug(dumpModel());

				RPCColumn columna = new RPCColumn(pricingProblem, false, this.getName(),
				dataModel.matriz.buildMaximal(encontrado));

				//RPCColumn columna = new RPCColumn(pricingProblem, false, this.getName(), encontrado);
				newPatterns.add(columna);
				Estadisticas.columnasExacto++;

			}
		} catch (IloException e) {
			e.printStackTrace();
		}
		return newPatterns;
	}

	/**
	 * Actualizamos la función objetivo del problema con la nueva solución dual que
	 * viene del master.
	 */
	@Override
	public void setObjective() {

		try {

			// maximize fobj: sum <i, j> in R*C: (start[i, j] + diag[i, j] + up[i, j] +
			// left[i, j])*X0[i, j];
			logger.debug("Duals: " + Arrays.toString(pricingProblem.dualCosts));
			
			this.exactSolverModel1.setObjective(pricingProblem.dualCosts);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Cerrar el problema de pricing.
	 */
	@Override
	public void close() {
		this.exactSolverModel1.close();
	}

	/**
	 * Aplicamos esta decisión de branching al problema
	 * 
	 * 
	 * @param bd
	 *            BranchingDecision
	 */
	@Override
	public void branchingDecisionPerformed(@SuppressWarnings("rawtypes") BranchingDecision bd) {
		this.close();
		this.exactSolverModel1.buildModel();
	}

	/**
	 * Volvemos atrás la decisión de branchear (cuando hacemos el backtracking)
	 * 
	 * 
	 * @param bd
	 *            BranchingDecision
	 */
	@Override
	public void branchingDecisionReversed(@SuppressWarnings("rawtypes") BranchingDecision bd) {
		this.close();
		this.exactSolverModel1.buildModel();
	}
}
