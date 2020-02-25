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

package mitoboRunner;

import ij.IJ;
import ij.plugin.*;
import de.unihalle.informatik.Alida.annotations.indexing.SezPozAdapter;
import de.unihalle.informatik.Alida.gui.ALDOperatorGUIExecutionProxy;
import de.unihalle.informatik.Alida.operator.ALDOperatorLocation;
import de.unihalle.informatik.Alida.version.ALDVersionProviderFactory;
import de.unihalle.informatik.MiToBo.core.operator.MTBVersionProviderReleaseFile;

/**
 * ImageJ plugin to directly start the scratch assay analyzer.
 * 
 * @author Stefan Posch 
 * @author Birgit Moeller
 */
public class ScratchAssayAnalyzer_ implements PlugIn {

	@Override
	public void run(String arg0) {
		// init the SezPoz adapter properly
		SezPozAdapter.initAdapter(IJ.getClassLoader());

		// configure version management
		ALDVersionProviderFactory.setProviderClass("de.unihalle.informatik." 
				+	"MiToBo.core.operator.MTBVersionProviderReleaseFile");
		MTBVersionProviderReleaseFile.setRevisionFile(
				"revision-mitobo-plugins.txt");

		// open the control frame
		final String className = 
				"de.unihalle.informatik.MiToBo.apps.scratchAssay.ScratchAssayAnalyzer";
		ALDOperatorLocation opLoc = 
														ALDOperatorLocation.createClassLocation(className);
		ALDOperatorGUIExecutionProxy execManager = 
														new ALDOperatorGUIExecutionProxy(opLoc);
		execManager.showGUI();
	}
}
