/*
OME Bio-Formats package for reading and converting biological file formats.
Copyright (C) 2005-@year@ UW-Madison LOCI and Glencoe Software, Inc.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
/*
LOCI Common package: utilities for I/O, reflection and miscellaneous tasks.
Copyright (C) 2005-@year@ Melissa Linkert, Curtis Rueden and Chris Allan.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

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

/*
 * This class uses the Bio-Formats and LOCI-commons packages/libraries (see the two licenses at the top)
 */

package mtb_io;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Vector;

import ij.IJ;
import ij.ImagePlus;
import ij.io.OpenDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import loci.common.StatusEvent;
import loci.common.StatusListener;
import loci.formats.FormatException;
import loci.formats.ImageWriter;
import loci.formats.gui.ExtensionFileFilter;
import loci.formats.gui.GUITools;

import de.unihalle.informatik.Alida.admin.annotations.ALDMetaInfo;
import de.unihalle.informatik.Alida.admin.annotations.ALDMetaInfo.ExportPolicy;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDProcessingDAGException;
import de.unihalle.informatik.Alida.helpers.ALDEnvironmentConfig;
import de.unihalle.informatik.MiToBo.core.datatypes.images.MTBImage;
import de.unihalle.informatik.MiToBo.gui.ImageWriterOptionsPane;
import de.unihalle.informatik.MiToBo.io.images.ImageWriterMTB;
import de.unihalle.informatik.MiToBo.io.tools.ImageIOUtils;

/** 
 * Plugin for image save dialog
 * @author Oliver Gress
 *
 */
@ALDMetaInfo(export=ExportPolicy.MANDATORY)
public class Save_Image_MTB implements PlugInFilter, PropertyChangeListener, StatusListener {

	// input image
	protected MTBImage img = null;
	protected ImagePlus imp = null;
	
	/** a panel for image writer options (set as accessory of a JFileChooser) */
	protected ImageWriterOptionsPane iwop;
	
	/** compression option if available */
	protected String compression = null;
	
	/** frames per second option if available */
	protected int fps = -1;
	
	/** quality option if available */
	protected int quality = -1;
	
	/** codec option if available */
	protected int codec = -1;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2325931378330699276L;
	

	@Override
	public void run(ImageProcessor arg0) {
		
		JFileChooser fc = this.createFileChooser();
		
		// set image title (without extension) as default file name
		String origtitle = null;
		if (this.img != null)
			origtitle = this.img.getTitle();
		else if (this.imp != null)
			origtitle = this.imp.getTitle();
		if (origtitle != null) {
			int dotID = origtitle.indexOf(".");
			if (dotID != -1) {
				String name = origtitle.substring(0,dotID);
				fc.setSelectedFile(new File(name + ".ome.tiff"));
			}
		}
		
		while (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			
			File outfile = fc.getSelectedFile();
			
			if (outfile == null) {
				IJ.error("File save dialog returned 'null'");
				return;
			}
			
			// if a specific format file filter was chosen, but the selected filename does not
			// match the formats extension, add the extension
			FileFilter ff = fc.getFileFilter();
			if (ff instanceof ExtensionFileFilter) {
				ExtensionFileFilter eff = (ExtensionFileFilter)ff;
				
				// #TODO: bio-formats release 4.2.2 contains a bug in the ExtensionFileFilter.accept(.) method,
				//  therefore the extensions are matched manually. Substitute the for-loop with accept()-method when
				//  using a new release
				String[] exts = eff.getExtensions();
				boolean extmatch = false;
				for (String ext : exts) {
					if (outfile.getName().endsWith(ext)) {
						extmatch = true;
						break;
					}
				}
				
				if (!extmatch) {
					outfile = new File(outfile.getAbsolutePath() + "." + eff.getExtension());
				}
			}
			
			// set initial directory of the file chooser for future call
			if (outfile.getParent() != null) {
				OpenDialog.setLastDirectory(outfile.getParent());
				OpenDialog.setDefaultDirectory(outfile.getParent());
			}
			
			// determine overwrite flag if file exists
			boolean overwrite = false;
			if (outfile.exists()) {
				int ret = JOptionPane.showConfirmDialog(null, "Do you want to overwrite existing file '" + outfile.getName() + "'?", 
						"File exists", JOptionPane.YES_NO_OPTION);
				
				if (ret == JOptionPane.YES_OPTION)
					overwrite = true;
				else
					continue;
			}
			
			// create a MiToBo image writer instance
			ImageWriterMTB writer = null;
			
			try {
				if (this.img != null) {
					writer = new ImageWriterMTB(this.img, outfile.getAbsolutePath());
				}
				else {
					writer = new ImageWriterMTB(this.imp, outfile.getAbsolutePath());
				}
			} catch (ALDOperatorException e) {
				IJ.error("Image writer instantiation failed: " + e.getMessage());
			}
			
			if (writer != null) {
				
				// listener for status and progress bar update
				writer.addStatusListener(this);
				
				// configure and run the writer
				try {
					writer.setOverwrite(overwrite);
					
					if (this.compression != null) {
						writer.setCompression(this.compression);
					}
					if (this.fps > 0) {
						writer.setFps(this.fps);
					}
					if (this.quality != -1) {
						writer.setQuality(this.quality);
					}
					if (this.codec != -1) {
						writer.setCodec(this.codec);
					}
					
					writer.runOp(null);
						
				} catch (ALDOperatorException e) {
					IJ.error("Writing of file '" + outfile.getName() + "' failed: " + e.getMessage());
					e.printStackTrace();
				} catch (FormatException e) {
					IJ.error("Writing of file '" + outfile.getName() + "' failed: " + e.getMessage());
					e.printStackTrace();
				} catch (ALDProcessingDAGException e) {
					IJ.error("Writing of file '" + outfile.getName() + "' failed: " + e.getMessage());
					e.printStackTrace();
				}	
			}	
			
			return;
		}
	}

	@Override
	public int setup(String arg, ImagePlus imP) {
		
		this.img = null;
		this.imp = null;
		
		try {
			this.img = MTBImage.createMTBImage(imP);
		} catch(IllegalArgumentException e) {
			this.imp = imP;
		} catch(NullPointerException e) {
			// do nothing, ImageJ will handle the case of no input image
		}
		
		return DOES_ALL + NO_CHANGES;
	}
	
		/**
		 * Create a file chooser configured for image writing, adding a panel for
		 * image writer options
		 */
	public JFileChooser createFileChooser() {
		
		// use bio-formats to obtain file filters for available formats
		FileFilter[] ffs = GUITools.buildFileFilters(new ImageWriter());
		
		Vector<FileFilter> ffv = new Vector<FileFilter>(ffs.length - 1);
		
		// remove non-image writers
		if (ffs != null) {
			for (int i = 0; i < ffs.length; i++) {
				if (ffs[i] instanceof ExtensionFileFilter) {
					if (!((ExtensionFileFilter)ffs[i]).getExtension().equals("java")) {
						ffv.add(ffs[i]);
					}
				}
				else {
					ffv.add(ffs[i]);
				}
			}
		}
		ffs = new FileFilter[ffv.size()];
		ffs = ffv.toArray(ffs);
		
		// use bio-formats to create a file chooser with a preview panel
		JFileChooser jfc = GUITools.buildFileChooser(ffs, true);
		
		// set initial directory of the file chooser
		String idir = ALDEnvironmentConfig.getConfigValue("mitobo", null, "savedir");
		if (idir == null)
			idir = ALDEnvironmentConfig.getConfigValue("mitobo", null, "imagedir");
		if (idir == null)
			idir = OpenDialog.getLastDirectory();
		if (idir == null)
			idir = OpenDialog.getDefaultDirectory();
		
		if (idir != null)
			jfc.setCurrentDirectory(new File(idir));
		
		// remove file filter that accepts any file extension
		jfc.setAcceptAllFileFilterUsed(false);


		// install the image writer options panel in the file chooser
		this.iwop = new ImageWriterOptionsPane(jfc);
		if (this.img != null) {
			double dt = ImageIOUtils.toSeconds(this.img.getStepsizeT(), this.img.getUnitT());
				
			if (dt > 0)
				this.iwop.setDefaultFps((int)Math.round(1.0/dt));
		}
		else if (this.imp != null) {
			if (this.imp.getCalibration() != null) {
				int fps = (int)Math.round(this.imp.getCalibration().fps);
				if (fps > 0)
					this.iwop.setDefaultFps(fps);
			}
		}
		
		// listen to changed property of the writer options panel
		this.iwop.addPropertyChangeListener(this);
		

		// set default/initial file filter to OME-TIFF if possible
		if (ffs != null) {
			for (int i = 0; i < ffs.length; i++) {
				if (ffs[i] instanceof ExtensionFileFilter) {
					if (((ExtensionFileFilter)ffs[i]).getExtension().equals("ome.tif")) {
						jfc.setFileFilter(ffs[i]);
					}
				}
			}
		}
		
		return jfc;
	}

	/**
	 * Process the <code>WRITER_OPTIONS_APPROVED_PROPERTY</code> event of the ImageWriterOptionsPane
	 * to assign the writer options when the file chooser closed by approving the file selection (and thereby writer options).
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		
		if (prop.equals(ImageWriterOptionsPane.WRITER_OPTIONS_APPROVED_PROPERTY)) {
			this.compression = this.iwop.getCompression();
			this.fps = this.iwop.getFramesPerSecond();
			this.quality = this.iwop.getQuality();
			this.codec = this.iwop.getCodec();
		}
	}

	@Override
	public void statusUpdated(StatusEvent evt) {
		IJ.showStatus(evt.getStatusMessage());
		IJ.showProgress(evt.getProgressValue(), evt.getProgressMaximum());
	}
	
	

}
