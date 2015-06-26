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

import java.awt.geom.Point2D;
import java.util.Vector;

import loci.common.StatusEvent;
import loci.common.StatusListener;
import loci.common.StatusReporter;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.ALDAOperator.Level;
import de.unihalle.informatik.Alida.annotations.ALDDerivedClass;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.annotations.Parameter.ExpertMode;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDProcessingDAGException;
import de.unihalle.informatik.MiToBo.apps.particles2D.ParticleDetectorUWT2D;
//import de.unihalle.informatik.MiToBo.apps.plastids.StromuliDetector;
import de.unihalle.informatik.MiToBo.core.datatypes.MTBRegion2D;
import de.unihalle.informatik.MiToBo.core.datatypes.MTBRegion2DSet;
import de.unihalle.informatik.MiToBo.core.datatypes.images.MTBImage;
import de.unihalle.informatik.MiToBo.core.datatypes.images.MTBImage.MTBImageType;
import de.unihalle.informatik.MiToBo.core.datatypes.images.MTBImageByte;
import de.unihalle.informatik.MiToBo.core.operator.MTBOperator;

/**
 * Wrapper operator detecting plastids and stromuli.
 *  
 * @author Birgit Moeller
 */
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL,
		level=Level.STANDARD, allowBatchMode=false)
@ALDDerivedClass
public class PlastidStromuliDetectorWrapper extends MTBOperator
	implements StatusReporter {
	
	/**
	 * Identifier for outputs in verbose mode.
	 */
	private final static String opIdentifier = 
			"[ParticleStromuliDetectorWrapper] ";
	
	/**
	 * Input image to process.
	 */
	@Parameter( label = "Input image", required = true, 
		direction = Parameter.Direction.IN,	mode = ExpertMode.STANDARD, 
		dataIOOrder = 1, description = "Input image.")
	private transient MTBImage inputImage = null;

	/**
	 * Particle detector object.
	 */
	@Parameter( label = "Particle detector", required = true, 
		direction = Parameter.Direction.IN,	mode = ExpertMode.STANDARD, 
		dataIOOrder = 2, description = "Detector.")
	protected ParticleDetectorUWT2D particleOp;

	/**
	 * Set of detected plastid regions.
	 */
	@Parameter( label = "Resulting plastid regions", 
		direction = Parameter.Direction.OUT, mode = ExpertMode.STANDARD, 
		dataIOOrder = 1, description = "Detected plastid regions.")
	private transient MTBRegion2DSet resultPlastidRegions = null;

	/**
	 * Number of detected plastid regions.
	 */
	@Parameter( label = "#PlastidRegions", dataIOOrder = 2,
		direction = Parameter.Direction.OUT, mode = ExpertMode.STANDARD,	
		description = "Number of detected plastid regions.")
	private transient int resultPlastidCount = 0;
	
	/**
	 * Set of detected stromuli regions.
	 */
	@Parameter( label = "Resulting stromuli regions", 
		direction = Parameter.Direction.OUT, mode = ExpertMode.STANDARD, 
		dataIOOrder = 3, description = "Detected plastid regions.")
	private transient MTBRegion2DSet resultStromuliRegions = null;

	/**
	 * Number of detected stromuli regions.
	 */
	@Parameter( label = "#StromuliRegions", dataIOOrder = 4,
		direction = Parameter.Direction.OUT, mode = ExpertMode.STANDARD,	
		description = "Number of detected stromuli regions.")
	private transient int resultStromuliCount = 0;
	
	/** 
	 * Vector of installed {@link StatusListener} objects.
	 */
	protected Vector<StatusListener> m_statusListeners;
	
	/**
	 * Constructor.	
	 * @throws ALDOperatorException Thrown in case of initialization error.
	 */
	public PlastidStromuliDetectorWrapper() throws ALDOperatorException {
		this.m_statusListeners = new Vector<StatusListener>(1);
	}

	@Override
	protected void operate() throws ALDOperatorException,
			ALDProcessingDAGException {

		// post ImageJ status
		String msg = opIdentifier + "running plastid/stromuli detection...";	
		this.notifyListeners(new StatusEvent(msg));

		if (this.verbose.booleanValue())
			System.out.println(opIdentifier 
				+ "running plastid/stromuli detection...");

		int xSize = this.inputImage.getSizeX();
		int ySize = this.inputImage.getSizeY();
		int zSize = this.inputImage.getSizeZ();
		int tSize = this.inputImage.getSizeT();
		int cSize = this.inputImage.getSizeC();

		this.particleOp.setInputImage(this.inputImage);
		this.particleOp.runOp();
		this.resultPlastidRegions = this.particleOp.getResults();
		this.resultPlastidCount = this.resultPlastidRegions.size();

		if (this.verbose.booleanValue())
			System.out.println(opIdentifier + 
				"\t -> Number of detected plastids: " + this.resultPlastidCount);

		// create binary mask of plastid regions
		MTBImageByte plastidMask = (MTBImageByte)MTBImage.createMTBImage(
				xSize, ySize, zSize, tSize, cSize, MTBImageType.MTB_BYTE);
		for (MTBRegion2D reg: this.resultPlastidRegions) {
			for (Point2D.Double p: reg.getPoints()) {
				plastidMask.putValueInt((int)p.x, (int)p.y, 255);
			}
		}
		
		// run the stromuli detector
//		StromuliDetector stromuliOp = new StromuliDetector();
//		stromuliOp.setParameter("inImg", this.inputImage);
//		stromuliOp.setParameter("plastidMask", plastidMask);
//		stromuliOp.runOp();
//		this.resultStromuliRegions = 
//				(MTBRegion2DSet)stromuliOp.getParameter("stromuliRegions");
//		this.resultStromuliCount = this.resultPlastidRegions.size();
//
//		if (this.verbose.booleanValue())
//			System.out.println(opIdentifier + 
//				"\t -> Number of detected stromuli: "+this.resultStromuliCount);

		if (this.verbose.booleanValue())
			System.out.println(opIdentifier + "Operations finished!");
	}
	
	// ----- StatusReporter interface
	
	@Override
	public void addStatusListener(StatusListener statuslistener) {	
		this.m_statusListeners.add(statuslistener);	
	}

	@Override
	public void notifyListeners(StatusEvent e) {
		for (int i = 0; i < this.m_statusListeners.size(); i++) {
			this.m_statusListeners.get(i).statusUpdated(e);
		}
	}

	@Override
	public void removeStatusListener(StatusListener statuslistener) {
		this.m_statusListeners.remove(statuslistener);
	}	
	
	/**
	 * Set input image.
	 * @param img	Input image to process.
	 */
	public void setInputImage(MTBImage img) {
		this.inputImage = img;
	}	
	
	/**
	 * Set particle detector operator object.
	 * @param pOp	Detector object.
	 */
	public void setParticleDetector(ParticleDetectorUWT2D pOp) {
		this.particleOp = pOp;
	}
	
	/**
	 * Get resulting plastid regions.
	 * @return 	Set of detected plastid regions.
	 */
	public MTBRegion2DSet getResultPlastidRegions() {
		return this.resultPlastidRegions;
	}
	
	/**
	 * Get resulting stromuli regions.
	 * @return 	Set of detected stromuli regions.
	 */
	public MTBRegion2DSet getResultStromuliRegions() {
		return this.resultStromuliRegions;
	}
}
