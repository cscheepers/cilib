/*
 * CoevolutionIterationStrategy.java
 * 
 * Created on 2007/04/27
 *
 * Copyright (C) 2003, 2007 - CIRG@UP 
 * Computational Intelligence Research Group (CIRG@UP)
 * Department of Computer Science 
 * University of Pretoria
 * South Africa
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
 * 
 */
package net.sourceforge.cilib.coevolution;

import net.sourceforge.cilib.algorithm.population.IterationStrategy;
import net.sourceforge.cilib.algorithm.population.PopulationBasedAlgorithm;//I

/**
 * Parent class of CompetitiveCoevolutionIterationStrategy and CooperativeCoevolutionIterationStrategy
 * @author Julien Duhain
 * 
 */
public abstract class CoevolutionIterationStrategy extends IterationStrategy<CoevolutionAlgorithm>{
	
	
	public CoevolutionIterationStrategy() {
		super();
	}
	
	public CoevolutionIterationStrategy(CoevolutionIterationStrategy copy) {
		
	}
	
	public abstract CoevolutionIterationStrategy clone();
		
	public abstract void performIteration(CoevolutionAlgorithm ca);
	
	public abstract void setEntityType(PopulationBasedAlgorithm pba, int populationID);
}