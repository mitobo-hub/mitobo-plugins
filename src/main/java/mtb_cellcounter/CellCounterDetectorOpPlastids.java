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

package mtb_cellcounter;

import java.util.Vector;

import loci.common.StatusEvent;
import loci.common.StatusListener;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.ALDDerivedClass;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDProcessingDAGException;
import de.unihalle.informatik.MiToBo.apps.plastids.PlastidDetector2D;

/**
 * Cell counter detector for detecting plastids.
 *  
 * @author Birgit Moeller
 */
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.NONE)
@ALDDerivedClass
public class CellCounterDetectorOpPlastids 
	extends CellCounterDetectorOp {
	
	/**
	 * Identifier for outputs in verbose mode.
	 */
	private final static String opIdentifier =
			"[CellCounterDetectorOpPlastids] ";

	/**
	 * Constructor.	
	 * @throws ALDOperatorException Thrown in case of initialization error.
	 */
	public CellCounterDetectorOpPlastids() 
			throws ALDOperatorException {
		this.m_statusListeners = new Vector<StatusListener>(1);
	}

	@Override
  protected void operate() 
  		throws ALDOperatorException, ALDProcessingDAGException {
		
		// post ImageJ status
		String msg = opIdentifier + "running plastid detection...";	
		this.notifyListeners(new StatusEvent(msg));

		if (this.verbose.booleanValue())
			System.out.println(opIdentifier 
				+ "running plastid detection...");

		PlastidDetector2D pd = new PlastidDetector2D();
		pd.setInputImage(this.inputImage);
		if (this.particleOp != null) {
			pd.setDetector(this.particleOp);
		}
		pd.runOp();
		this.resultPlastidRegions = pd.getPlastidRegions();
		this.resultPlastidCount = this.resultPlastidRegions.size();

		if (this.verbose.booleanValue())
			System.out.println(opIdentifier + 
				"\t -> Number of detected plastids: " + this.resultPlastidCount);

		if (this.verbose.booleanValue())
			System.out.println(opIdentifier + "Operations finished!");
  }

}
