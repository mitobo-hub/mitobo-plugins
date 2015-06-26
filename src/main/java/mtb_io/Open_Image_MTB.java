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

import ij.IJ;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JFileChooser;

import loci.common.StatusEvent;
import loci.common.StatusListener;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import loci.formats.ImageReader;
import loci.formats.gui.GUITools;
import de.unihalle.informatik.Alida.admin.annotations.ALDMetaInfo;
import de.unihalle.informatik.Alida.admin.annotations.ALDMetaInfo.ExportPolicy;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDProcessingDAGException;
import de.unihalle.informatik.Alida.helpers.ALDEnvironmentConfig;
import de.unihalle.informatik.MiToBo.core.datatypes.images.MTBImage;
import de.unihalle.informatik.MiToBo.gui.ImageReaderOptionsPane;
import de.unihalle.informatik.MiToBo.io.images.ImageReaderMTB;

/**
 * Plugin for image open dialog
 * @author Oliver Gress
 *
 */
@ALDMetaInfo(export=ExportPolicy.MANDATORY)
public class Open_Image_MTB implements PlugIn, StatusListener, PropertyChangeListener {

	private String currentfile = "";
	private int currentfileN = 1;
	private int currentidx = 0;
	
	protected ImageReaderOptionsPane irop;
	
	protected HashMap<String, int[]> seriesIndices = null;
	
	@Override
	public void run(String arg0) {

		JFileChooser jfc = GUITools.buildFileChooser(new ImageReader());
		
		// allow multiple files to be selected
		jfc.setMultiSelectionEnabled(true);
		
		// set initial directory of the file chooser
		String idir = ALDEnvironmentConfig.getConfigValue("mitobo", null, "opendir");
		if (idir == null)
			idir = ALDEnvironmentConfig.getConfigValue("mitobo", null, "imagedir");
		if (idir == null)
			idir = OpenDialog.getLastDirectory();
		if (idir == null)
			idir = OpenDialog.getDefaultDirectory();
		
		if (idir != null)
			jfc.setCurrentDirectory(new File(idir));
		
		this.irop = new ImageReaderOptionsPane(jfc);
		this.irop.addPropertyChangeListener(this);
		
		jfc.setPreferredSize(new Dimension(700,600));
		
		if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

			File dir = jfc.getCurrentDirectory();
			// set initial directory of the file chooser for future call
			if (dir.getAbsolutePath() != null) {
				OpenDialog.setLastDirectory(dir.getAbsolutePath());
				OpenDialog.setDefaultDirectory(dir.getAbsolutePath());
			}
			
			File[] files = jfc.getSelectedFiles();
			
			for (File f : files) {

				ImageReaderMTB reader = null;
				
				try {
					reader = new ImageReaderMTB(f.getAbsolutePath());
				} catch (ALDOperatorException e) {
					IJ.error("Failed to create image reader instance for file " + f.getName() + ": " + e.getMessage());
				} catch (FormatException e) {
					IJ.error("Failed to create image reader instance for file " + f.getName() + ": " + e.getMessage());
				} catch (IOException e) {
					IJ.error("Failed to create image reader instance for file " + f.getName() + ": " + e.getMessage());
				} catch (DependencyException e) {
					IJ.error("Failed to create image reader instance for file " + f.getName() + ": " + e.getMessage());
				} catch (ServiceException e) {
					IJ.error("Failed to create image reader instance for file " + f.getName() + ": " + e.getMessage());
				}
				
				if (reader != null) {

					this.currentfile = f.getName();
					this.currentfileN = reader.getImageCount();
					
					reader.addStatusListener(this);
					
					
					if (this.seriesIndices != null && this.seriesIndices.containsKey(f.getAbsolutePath())) {
					// read and open only selected images of the file
						
						int[] indices = this.seriesIndices.get(f.getAbsolutePath());
						
						for (int i = 0; i < indices.length; i++) {
							this.currentidx = indices[i];
							
							try {
								reader.setIndexOfImageToRead(this.currentidx);
								reader.runOp(null);
								
								MTBImage resultImg = reader.getResultMTBImage();
								
								if (resultImg != null) {
									resultImg.show();
								}
								else {
									if (this.currentfileN > 1)
										IJ.error("Failed to open image " + this.currentidx + " of " + this.currentfile);
									else	
										IJ.error("Failed to open " + this.currentfile);
								}
								
							} catch (ALDOperatorException e) {
								if (this.currentfileN > 1)
									IJ.error("Failed to open image " + this.currentidx + " of " + this.currentfile + ": " + e.getMessage());
								else	
									IJ.error("Failed to open " + this.currentfile + ": " + e.getMessage());
							} catch (ALDProcessingDAGException e) {
								if (this.currentfileN > 1)
									IJ.error("Failed to open image " + this.currentidx + " of " + this.currentfile + ": " + e.getMessage());
								else	
									IJ.error("Failed to open " + this.currentfile + ": " + e.getMessage());
							}
						}
					}
					else {
					// read and open all images in the file
						
						for (this.currentidx = 0; this.currentidx < this.currentfileN; this.currentidx++) {
							
							try {
								reader.setIndexOfImageToRead(this.currentidx);
								reader.runOp(null);
								
								MTBImage resultImg = reader.getResultMTBImage();
								
								if (resultImg != null) {
									resultImg.show();
								}
								else {
									if (this.currentfileN > 1)
										IJ.error("Failed to open image " + this.currentidx + " of " + this.currentfile);
									else	
										IJ.error("Failed to open " + this.currentfile);
								}
								
							} catch (ALDOperatorException e) {
								if (this.currentfileN > 1)
									IJ.error("Failed to open image " + this.currentidx + " of " + this.currentfile + ": " + e.getMessage());
								else	
									IJ.error("Failed to open " + this.currentfile + ": " + e.getMessage());
							} catch (ALDProcessingDAGException e) {
								if (this.currentfileN > 1)
									IJ.error("Failed to open image " + this.currentidx + " of " + this.currentfile + ": " + e.getMessage());
								else	
									IJ.error("Failed to open " + this.currentfile + ": " + e.getMessage());
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void statusUpdated(StatusEvent evt) {
		if (this.currentfileN > 1)
			IJ.showStatus("Opening image " + this.currentidx + " of " + this.currentfile + ": " + evt.getStatusMessage());
		else
			IJ.showStatus("Opening " + this.currentfile + ": " + evt.getStatusMessage());
		IJ.showProgress(evt.getProgressValue(), evt.getProgressMaximum());	
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		
		if (prop.equals(ImageReaderOptionsPane.READER_OPTIONS_APPROVED_PROPERTY)) {
			
			if (this.irop != null)
				this.seriesIndices = this.irop.getSelections();
		}
		
	}


	
	
	

}
