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

import de.unihalle.informatik.MiToBo.core.datatypes.MTBQuadraticCurve2D;

/**
 * Class using a parametric curve as shape for markers.
 *
 * @author Birgit Moeller
 */
public class CellCntrMarkerShapeCurve implements CellCntrMarkerShape {
	
	/**
	 * Curve object representing the shape of the marker.
	 */
	protected MTBQuadraticCurve2D mCurve = null;
	
	/**
	 * Default constructor, it's protected to avoid constructing objects
	 * without curve data.
	 */
	protected CellCntrMarkerShapeCurve() {
		// nothing to do here, should never be called explicitly
	}
	
	/**
	 * Default constructor with parametric curve object.
	 * @param c	Curve object.
	 */
	public CellCntrMarkerShapeCurve(MTBQuadraticCurve2D c) {
		this.mCurve = c;
	}
}
