/*
 * Copyright (C) 2010 - @YEAR@ by the MiToBo development team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Fore more information on MiToBo, visit
 *
 *    http://www.informatik.uni-halle.de/mitobo/
 *
 */

package mtb_cellcounter;

import de.unihalle.informatik.MiToBo.core.datatypes.MTBBorder2D;
import de.unihalle.informatik.MiToBo.core.datatypes.MTBRegion2D;

/**
 * Class using a region as shape for markers.
 *
 * @author Birgit Moeller
 */
public class CellCntrMarkerShapeRegion implements CellCntrMarkerShape {
	
	/**
	 * Region object representing the shape of the marker.
	 */
	protected MTBRegion2D mRegion = null;
	
	/**
	 * Average intensity of all region pixels.
	 */
	protected double avgIntensity = -1;
	
	/**
	 * Default constructor, it's protected to avoid constructing objects
	 * without region data.
	 */
	@SuppressWarnings("unused")
	private CellCntrMarkerShapeRegion() {
		// nothing to do here, should never be called explicitly
	}
	
	/**
	 * Default constructor with region object.
	 * @param r	Region object.
	 */
	public CellCntrMarkerShapeRegion(MTBRegion2D r) {
		this.mRegion = r;
	}
	
	/**
	 * Get region representing the shape.
	 * @return	Region object.
	 */
	public MTBRegion2D getRegion() {
		return this.mRegion;
	}

	/**
	 * Set average region intensity.
	 * @param ai	Average intensity.
	 */
	public void setAvgIntensity(double ai) {
		this.avgIntensity = ai;
	}
	
	/**
	 * Get average intensity.
	 * @return	Average intensity, -1 if not available.
	 */
	public double getAvgIntensity() {
		return this.avgIntensity;
	}
	
	@Override
	public MTBBorder2D getOutline() {
		try {
			return this.mRegion.getBorder();
		} catch (Exception e) {
			System.err.println("Something went wrong extracting the border...");
			return null;
		}
	}

}
