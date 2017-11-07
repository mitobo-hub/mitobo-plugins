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

import java.awt.event.ActionEvent;
import java.util.Vector;

import loci.common.StatusEvent;
import loci.common.StatusListener;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.ALDDerivedClass;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeEvent;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeListener;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDProcessingDAGException;
import de.unihalle.informatik.Alida.gui.ALDOperatorConfigurationFrame;
import de.unihalle.informatik.MiToBo.apps.particles2D.ParticleDetectorUWT2D;
import de.unihalle.informatik.MiToBo.apps.plantCells.plastids.PlastidDetector2DParticles;

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
	 * Particle detector object.
	 */
	protected ParticleDetectorUWT2D particleOp;

	/**
	 * Configuration frame for particle detector.
	 * <p>
	 * This is basically an {@link ALDOperatorConfigurationFrame}, however,
	 * restricted to the elements relevant in the context of the 
	 * {@link MTB_CellCounter}. E.g., options for parameter validation and 
	 * the 'Actions' menu are missing.
	 */
	protected PlastidDetector2DParticlesUWTConfigFrame particleConfigureFrame;

	/**
	 * Constructor.	
	 * @throws ALDOperatorException Thrown in case of initialization error.
	 */
	public CellCounterDetectorOpPlastids() 
			throws ALDOperatorException {
		this.m_statusListeners = new Vector<StatusListener>(1);
		// configure the particle detector, except for the input image
		// which we do not know yet
	  this.particleOp = new ParticleDetectorUWT2D();
	  this.particleOp.setJmin(3);
	  this.particleOp.setJmax(4);
	  this.particleOp.setScaleIntervalSize(1);
	  this.particleOp.setMinRegionSize(1);
	  this.particleOp.setCorrelationThreshold(1.5);
	  this.particleConfigureFrame = 
	  	new PlastidDetector2DParticlesUWTConfigFrame(this.particleOp);
	}

	@Override
  protected void operate() 
  		throws ALDOperatorException, ALDProcessingDAGException {
		
		// if plastid detection is disabled, do nothing
		if (!this.detectPlastids)
			return;
		
		// post ImageJ status
		String msg = opIdentifier + "running plastid detection...";	
		this.notifyListeners(new StatusEvent(msg));

		if (this.verbose.booleanValue())
			System.out.println(opIdentifier 
				+ "running plastid detection...");

		// clean-up, reset variables
		this.resultPlastidCount = 0;
		this.resultPlastidRegions = null;
		this.resultStomataRegions = null;
		this.resultStromuliCount = 0;
		this.resultStromuliRegions = null;
		
		PlastidDetector2DParticles pd = new PlastidDetector2DParticles();
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

	@Override
	public void dispose() {
		this.particleConfigureFrame.setVisible(false);
	}
	
	@Override
	public void addValueChangeEventListener(
	    ALDSwingValueChangeListener listener) {
		// just hand the listener over to the configuration frame
		this.particleConfigureFrame.addValueChangeEventListener(listener);
	}

	@Override
	public void handleValueChangeEvent(ALDSwingValueChangeEvent event) {
		// just propagate the event to the configuration frame
		this.particleConfigureFrame.handleValueChangeEvent(event);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		
		// configure button has been clicked
		if (command.compareTo(CellCounter.CONFIGURE) == 0) {
			this.particleConfigureFrame.setVisible(true);
		}
	}

}
