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

package mtb_segmentation;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.awt.AWTEvent;
import java.awt.Choice;
import java.util.Vector;

import de.unihalle.informatik.Alida.admin.annotations.ALDMetaInfo;
import de.unihalle.informatik.Alida.admin.annotations.ALDMetaInfo.ExportPolicy;
import de.unihalle.informatik.MiToBo.core.datatypes.images.MTBImage;
import de.unihalle.informatik.MiToBo.core.datatypes.images.MTBImage.MTBImageType;
import de.unihalle.informatik.MiToBo.segmentation.thresholds.ImgThresh;

/**
 * Plugin for thresholding plain images, stacks and hyperstacks.
 * The output image is of the datatype of the input image.
 * The output might contain values different from 0 depending on the image's datatype:<br>
 * Byte images: Output values are 0 for black and 255 for white, if the 'original pixel values' option was not chosen.<br>
 * Short images: Output values are 0 for black and the maximum gray value of the input image for white.<br>
 * Floating point images: Output values are the minimum gray value of the input image for black and the maximum gray value for white.
 * 
 * @author gress
 *
 */
@ALDMetaInfo(export=ExportPolicy.MANDATORY)
public class Threshold_Image implements PlugInFilter, DialogListener {


	/** image to threshold */
	private MTBImage m_img;
	
	/** thresholded image */
	private MTBImage m_threshImg;
	
	/** original ImagePlus */
	private ImagePlus m_imgPlus;
	
	/** threshold */
	private double m_thresh;
	
	/** foreground pixel value */
	private double m_fg;
	
	/** background pixel value */
	private double m_bg;
	
	/** black value for current image*/
	private double black;
	
	/** white value for current image*/
	private double white;
	
	/** min and max gray value of the current image */
	private double[] minmax_img;

	
	/** flag if only current slice is to be thresholded */
	private boolean currentSliceOnly;
	

	@Override
  public int setup(String arg, ImagePlus imp) {
		
		if (imp == null)
			return DOES_32 + DOES_16 + DOES_8G;
		
		this.m_imgPlus = imp;
		this.m_img = MTBImage.createMTBImage(imp);
		
		this.minmax_img = m_img.getMinMaxDouble();
		
		if (this.m_img.getType() == MTBImageType.MTB_BYTE) {
			this.white = 255;
			this.black = 0;
		}
		else if (this.m_img.getType() == MTBImageType.MTB_SHORT) {
			this.white = this.minmax_img[1];
			this.black = 0;
		}
		else {
			this.white = this.minmax_img[1];
			this.black = this.minmax_img[0];
		}

		return DOES_32 + DOES_16 + DOES_8G;
	}


	/*
	 * (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	@Override
  public void run(ImageProcessor ip) {
		this.m_img.setCurrentSliceIndex(this.m_imgPlus.getCurrentSlice()-1);
		
		// create thresholded image object
		this.m_threshImg = this.m_img.duplicate();

		this.m_threshImg.setCurrentSliceIndex(this.m_imgPlus.getCurrentSlice()-1);
		this.m_threshImg.show();
		
		// set ImagePlus parameters
		this.m_threshImg.getImagePlus().setDisplayRange(this.black, this.white);
		this.m_threshImg.getImagePlus().setPosition(this.m_imgPlus.getCurrentSlice());
		
		this.m_threshImg.updateAndRepaintWindow();
		
		// thresholding
		if (thresholdSetupDialog()) {

			try {
				ImgThresh th = new ImgThresh( this.m_img, this.m_thresh, this.m_fg, this.m_bg);
				
				this.m_threshImg.close();
				
				// create thresholded image object
				this.m_threshImg = this.m_img.duplicate();
				
				// set ImagePlus parameters
				this.m_threshImg.getImagePlus().setDisplayRange(this.black, this.white);
				this.m_threshImg.getImagePlus().setPosition(this.m_imgPlus.getCurrentSlice());

				//this.m_threshImg.show();
				th.setDestinationImage( this.m_threshImg);
				
				if (this.currentSliceOnly)
					th.setActualSliceOnly();
			
				th.runOp(null);

				// this is probably superflous
				this.m_threshImg = th.getResultImage();
			} catch ( Exception e) {
				IJ.error(e.getMessage());
				e.printStackTrace();
				
				return;
			}

			this.m_threshImg.show();
		}
		else {
			this.m_threshImg.close();
		}

	}
	
	
	
	// ----- Plugin helper functions
	
	/**
	 * Dialog for choosing thresholding parameters
	 * @return true if the dialog was terminated by OK-button, false in any other case
	 */
	private boolean thresholdSetupDialog() {
		
		GenericDialog gd = new GenericDialog("Thresholding configuration");
		
		// threshold parameter
		gd.addMessage("Choose a threshold:");
		gd.addSlider("Threshold", this.black, this.white, (this.minmax_img[1]-this.minmax_img[0])/2.0 + this.minmax_img[0]);
		
		// fg/bg pixel values
		gd.addMessage("Pixel values:");
		String[] choices = {"White", "Black", "Original pixel value"};
		gd.addChoice("Foreground", choices, "White");
		gd.addChoice("Background", choices, "Black");
		
		gd.addCheckbox("Current slice only", false);

		gd.addDialogListener(this);
		
		gd.showDialog();
		
		if (gd.wasOKed()) {	
			return true;
		}
		else {
			return false;
		}
	}
	
	
	// ----- functions to be implemented for DialogListener interface
	
	/*
	 * (non-Javadoc)
	 * @see ij.gui.DialogListener#dialogItemChanged(ij.gui.GenericDialog,java.awt.AWTEvent)
	 */
	@Override
  public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
	
		// get threshold
		this.m_thresh = (int)Math.round(gd.getNextNumber());
		
		// get fg/bg pixel values
		@SuppressWarnings("rawtypes")
		Vector vc = gd.getChoices();
		Choice c = (Choice)vc.get(0);
		int ci = c.getSelectedIndex();
		
		this.currentSliceOnly = gd.getNextBoolean();
		

		
		// fg
		if (ci == 0) 
			this.m_fg = this.white;
		else if (ci == 1) 
			this.m_fg = this.black;
		else
			this.m_fg = Double.POSITIVE_INFINITY;
		
		c = (Choice)vc.get(1);
		ci = c.getSelectedIndex();
		
		// bg
		if (ci == 0) {
			this.m_bg = this.white;
		}
		else if (ci == 1)
			this.m_bg = this.black;
		else
			this.m_bg = Double.POSITIVE_INFINITY;		
		
		
		try {
			ImgThresh thresOp = new ImgThresh(this.m_img, this.m_thresh, this.m_fg, this.m_bg);
			thresOp.setDestinationImage(this.m_threshImg);
			thresOp.setActualSliceOnly();
			
			thresOp.runOp(null);
			thresOp.getResultImage().updateAndRepaintWindow();

		} catch ( Exception ee) {
			IJ.error(ee.getMessage());
			ee.printStackTrace();
		}
		
		
		return true;
	}
}
