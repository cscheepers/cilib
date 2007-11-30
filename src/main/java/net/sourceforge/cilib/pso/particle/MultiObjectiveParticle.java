package net.sourceforge.cilib.pso.particle;

import net.sourceforge.cilib.type.types.Type;

public class MultiObjectiveParticle extends StandardParticle {
	private static final long serialVersionUID = 2449622504036301616L;

	public void setBestPosition(Type bestPosition) {
		this.properties.put("bestPosition", bestPosition.getClone());
//		bestFitness = fitnessCalculator.getFitness(bestPosition, false);
		this.properties.put("bestFitness", fitnessCalculator.getFitness(bestPosition, false));
	}

}
