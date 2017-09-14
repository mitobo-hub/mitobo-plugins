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

/**
 * Class to represent the geometric shape of a single marker in the image.
 * <p>
 * In the MTB Cell Counter in addition to a position a marker also owns 
 * a shape, e.g., a region contour. 
 *
 * @author Birgit Moeller
 */
public interface CellCntrMarkerShape {
	
	/*
	 * Interface class without functionality. 
	 */
	
	/**
	 * Get outline of shape.
	 * @return	Outline of shape.
	 */
	public abstract MTBBorder2D getOutline();
	
}
