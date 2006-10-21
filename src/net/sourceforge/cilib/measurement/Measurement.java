/*
 * Measurement.java
 *
 * Created on February 4, 2003, 4:04 PM
 *
 * 
 * Copyright (C) 2003 - 2006 
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

package net.sourceforge.cilib.measurement;

import java.io.Serializable;

import net.sourceforge.cilib.type.types.Type;


/**
 * All measurements must implement this interface. The measurment must return
 * the value of what it is measuring given the algorithm that it is measuring.
 *
 * @author Edwin Peer
 * @author Gary Pampara
 */
public interface Measurement extends Serializable {
	
	/**
	 * Get the domain string representing what this measurement's results will conform to.
	 * @return The Domain String representing the represenation of the measurement
	 */
	public String getDomain();

	
	/**
	 * Get the value of the measurement. The represenation of the measurement will be based
	 * on the domain string defined {@see Measurement#getDomain()}
	 * @return The <tt>Type</tt> representing the value of the measurement
	 */
	public Type getValue();
}
