/*
 * This file is part of MiToBo, the Microscope Image Analysis Toolbox.
 *
 * Copyright (C) 2010 - @YEAR@
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
/* 
 * Most recent change(s):
 * 
 * $Rev$
 * $Date$
 * $Author$
 * 
 */

package mitoboRunner;

import ij.IJ;
import ij.plugin.*;
import de.unihalle.informatik.Alida.annotations.indexing.SezPozAdapter;
import de.unihalle.informatik.Alida.gui.ALDOperatorGUIExecutionProxy;
import de.unihalle.informatik.Alida.gui.OnlineHelpDisplayer;
import de.unihalle.informatik.Alida.operator.ALDOperatorLocation;

/**
 * ImageJ plugin to directly start the snake optimizer operator.
 * 
 * @author Stefan Posch 
 * @author Birgit Moeller
 */
public class Snake_Optimizer implements PlugIn {

	@Override
	public void run(String arg0) {
		// init the SezPoz adapter properly
		SezPozAdapter.initAdapter(IJ.getClassLoader());
		
		// configure online help to use MiToBo help set
		OnlineHelpDisplayer.initHelpset("mitobo");

		// open the control frame
		final String name = 
			"de.unihalle.informatik.MiToBo.segmentation.snakes.optimize." + 
				"SnakeOptimizerSingleVarCalc";
		ALDOperatorLocation opLoc = ALDOperatorLocation.createClassLocation(name);
		ALDOperatorGUIExecutionProxy execManager = 
																			new ALDOperatorGUIExecutionProxy(opLoc);
		execManager.showGUI();
	}
}
