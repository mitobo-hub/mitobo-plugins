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

/*
 * This file is derived from the ImageJ 1.0 file
 * http://imagej.nih.gov/ij/source/ij/plugin/frame/ThresholdAdjuster.java
 */

package mtb_cellcounter;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ij.*;
import ij.gui.*;
import ij.measure.*;
import de.unihalle.informatik.MiToBo.core.datatypes.MTBRegion2D;
import de.unihalle.informatik.MiToBo.core.datatypes.MTBRegion2DSet;
import de.unihalle.informatik.MiToBo.core.datatypes.images.MTBImage;

/** 
 * Adjusts the lower and upper thresholds for size and intensity of regions.
 *
 * @author ImageJ 1.0 Team (see notice on top)
 * @author Birgit Moeller
 */
public class ParticleFilterFrame extends JFrame implements Measurements,
	Runnable, ActionListener {

	/**
	 * Reference to running ImageJ instance.
	 */
	private ImageJ ij;

	/*
	 * Declarations needed for ImageJ interaction.
	 */
	private static final String LOC_KEY = "threshold.loc";
	@SuppressWarnings("unused")
	private static final String MODE_KEY = "threshold.mode";
	@SuppressWarnings("unused")
	private static final String DARK_BACKGROUND = "threshold.dark";

	/**
	 * The main thread of this application.
	 */
	private Thread thread;
	
	/**
	 * Main panel of the frame.
	 */
	private JPanel panel;
	
	/**
	 * Label and command of close button.
	 */
	private static final String CLOSE = "Close";

	/**
	 * Reference to the corresponding plugin object.
	 */
	private CellCounter cc;
	
	/*
	 * elements for filtering size
	 */
	
	/**
	 * Panel to filter regions according to size.
	 */
	private ParticleFilterAdjustPanel panelFilterSize;
	
	/**
	 * Panel to filter regions according to average intensity.
	 */
	private ParticleFilterAdjustPanel panelFilterIntensity;
	
	/**
	 * Window close button.
	 */
	private JButton closeButton;
	
	/**
	 * Current markers.
	 */
	private CellCntrMarkerVector currentMarkers;
	
	/**
	 * Z index of current slice in stack.
	 */
	private int currentStackZ;
	
	/**
	 * Size of biggest region in current segmentation.
	 */
	private int maxRegSize;
	
	/**
	 * Size of smallest region in current segmentation.
	 */
	private int minRegSize;

	/**
	 * Intensity of brightest region in current segmentation.
	 */
	private int maxRegIntensity;
	
	/**
	 * Intensity of darkest region in current segmentation.
	 */
	private int minRegIntensity;
	
	/**
	 * Histogram of region sizes with 256 bins.
	 */
	private int[] histogramRegionSizes;
	
	/**
	 * Histogram of region average intensities with 256 bins.
	 */
	private int[] histogramRegionIntensities;
	
	/**
	 * Default constructor.
	 * @param counter		Reference to associated cell counter.
	 * @param markers		Markers to filter.
	 * @param image			Image from which segmentation data originates.
	 * @param stackZ 		Coordinate of image in complete z-stack.
	 */
	public ParticleFilterFrame(CellCounter counter, 
			CellCntrMarkerVector markers,	MTBImage image, int stackZ) {
		super("Region Filter");
		this.cc = counter;
		this.ij = IJ.getInstance();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);
		this.setMarkers(markers, image, stackZ);
		
		int y = 0;
		this.panelFilterSize = 
			new ParticleFilterAdjustPanel(this, "region sizes:", 
				this.histogramRegionSizes, this.minRegSize, this.maxRegSize);
		c.gridx = 0;
		c.gridy = y++;
		c.gridwidth = 2;
		c.insets = new Insets(0, 5, 10, 5);
		add(this.panelFilterSize, c);
		this.panelFilterIntensity = 
			new ParticleFilterAdjustPanel(this, "region intensities:",
				this.histogramRegionIntensities, 
				this.minRegIntensity, this.maxRegIntensity);
		c.gridx = 0;
		c.gridy = y++;
		c.gridwidth = 2;
		c.insets = new Insets(0, 5, 10, 5);
		add(this.panelFilterIntensity, c);

		// button panel
		this.panel = new JPanel();
		this.closeButton = new JButton(CLOSE);
		this.closeButton.addActionListener(this);
		this.closeButton.addKeyListener(this.ij);
		this.panel.add(this.closeButton);
		c.gridx = 0;
		c.gridy = y++;
		c.gridwidth = 2;
		c.insets = new Insets(0, 5, 10, 5);
		add(this.panel, c);
		
 		addKeyListener(this.ij);  // ImageJ handles keyboard shortcuts
 		this.setSize(400, 100);
		pack();
		Point loc = Prefs.getLocation(LOC_KEY);
		if (loc!=null)
			setLocation(loc);
		else
			GUI.center(this);
		this.setResizable(false);
		this.thread = new Thread(this, "RegionFilter");
		this.thread.start();
	}

	/**
	 * Set new markers.
	 * <p>
	 * This methods initializes all internal member variables related to 
	 * marker data, e.g., minimal and maximal values of size and intensity
	 * ranges, and data histograms.
	 * 
	 * @param data		New data.
	 * @param image		Image from where segmentation data originates.
	 * @param stackZ 	Coordinate of image in complete z-stack.
	 */
	private void setMarkers(CellCntrMarkerVector data, 
			MTBImage image, int stackZ) {
		this.currentMarkers = data;
		this.currentStackZ = stackZ;
		
		MTBRegion2DSet markerRegions = new MTBRegion2DSet();
		Iterator<CellCntrMarker> it = this.currentMarkers.iterator();
		while (it.hasNext()) {
			CellCntrMarkerShape shape = it.next().getShape();
			if (!(shape.getClass().equals(CellCntrMarkerShapeRegion.class))) {
				System.err.println("Error! Found non-region shape!");
				return;
			}
			markerRegions.add(((CellCntrMarkerShapeRegion)shape).getRegion());
		}
		
		// minimal and maximal region size
		this.maxRegSize = markerRegions.calcMaxSize();
		this.minRegSize = markerRegions.calcMinSize();

		// calculate histogram of region sizes
		this.histogramRegionSizes = new int[256];
		for (int i=0;i<markerRegions.size();++i) {
			int s = markerRegions.get(i).getArea();
			int bin = (int)(0.5+(double)(s-this.minRegIntensity) / 
					(double)(this.maxRegSize-this.minRegSize)*256.0);
			if (bin >= 0 && bin < 256)
				this.histogramRegionSizes[bin]++;
		}
		
		// minimal and maximal region intensity
		Vector<Point2D.Double> points;
		MTBRegion2D reg;
		double intensitySum = 0, averageIntensity = 0;
		double minimalRegIntensity = Double.MAX_VALUE, maximalRegIntensity = 0;
		for (int j=0; j<markerRegions.size(); ++j) {
			reg = markerRegions.get(j);
			intensitySum = 0;
			points = reg.getPoints();
			for (Point2D.Double p: points) {
				intensitySum += image.getValueDouble((int)p.x, (int)p.y);
			}
			averageIntensity = intensitySum / points.size();
			((CellCntrMarkerShapeRegion)this.currentMarkers.get(j).getShape())
					.setAvgIntensity(averageIntensity);
			if (averageIntensity > maximalRegIntensity)
				maximalRegIntensity = averageIntensity;
			if (averageIntensity < minimalRegIntensity)
				minimalRegIntensity = averageIntensity;
		}
		this.maxRegIntensity = (int)(maximalRegIntensity+0.5);
		this.minRegIntensity = (int)(minimalRegIntensity+0.5);

		// calculate intensity histogram
		this.histogramRegionIntensities = new int[256];
		for (int i=0;i<this.currentMarkers.size();++i) {
			double s = ((CellCntrMarkerShapeRegion)this.currentMarkers.elementAt(i)
					.getShape()).getAvgIntensity();
			int bin = (int)(0.5 + (s-this.minRegIntensity) / 
					(this.maxRegIntensity - this.minRegIntensity)*256.0);
			if (bin >= 0 && bin < 256)
				this.histogramRegionIntensities[bin]++;
		}
		this.setTitle("Region Filter - Type " + this.currentMarkers.getType());
	}		

	/**
	 * Update marker data.
	 * @param data		New marker data.
	 * @param image		New image.
	 * @param stackZ 	Coordinate of image in complete z-stack.
	 */
	public void updateMarkerData(CellCntrMarkerVector data, 
			MTBImage image, int stackZ) {
		this.setMarkers(data, image, stackZ);
		this.panelFilterSize.setData(this.histogramRegionSizes, 
			this.minRegSize, this.maxRegSize);
		this.panelFilterSize.updateGUI();
		this.panelFilterIntensity.setData(this.histogramRegionIntensities, 
			this.minRegIntensity, this.maxRegIntensity);
		this.panelFilterIntensity.updateGUI();
	}
	
	/**
	 * Returns the lower threshold for the region size.
	 * @return	Minimal region size.
	 */
	public int getMinSizeValue() {
		return this.panelFilterSize.getMinSliderValue();
	}

	/**
	 * Returns the upper threshold for the region size.
	 * @return	Maximal region size.
	 */
	public int getMaxSizeValue() {
		return this.panelFilterSize.getMaxSliderValue();
	}
	
	/**
	 * Returns the lower threshold for the region intensity.
	 * @return	Minimal region intensity.
	 */
	public int getMinIntensityValue() {
		return this.panelFilterIntensity.getMinSliderValue();
	}

	/**
	 * Returns the upper threshold for the region intensity.
	 * @return	Maximal region intensity.
	 */
	public int getMaxIntensityValue() {
		return this.panelFilterIntensity.getMaxSliderValue();
	}

	/*
	 * Implementation of ActionListener interface.
	 */

	@Override
  public synchronized void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.compareTo(CLOSE) == 0) {
			// close the filter frame
			this.setVisible(false);
		}
		notify();
	}
	
	/*
	 * Implementation of Runnable interface.
	 */

	@Override
	public void run() {
		this.setVisible(true);
	}

	/**
	 * Updates the region filter.
	 * <p>
	 * This function is called in case of changes in threshold settings.
	 */
	public void updateRegionFiltering() {
		// don't do any filtering if not in detect mode
		if (!this.cc.detectMode)
			return;
		if (this.panelFilterSize != null && this.panelFilterIntensity != null) {
			int minSize = this.panelFilterSize.getMinSliderValue();
			int maxSize = this.panelFilterSize.getMaxSliderValue();
			int minIntensity = this.panelFilterIntensity.getMinSliderValue();
			int maxIntensity = this.panelFilterIntensity.getMaxSliderValue();
			
			filterMarkerRegions(this.currentMarkers, this.currentStackZ,
					minSize, maxSize, minIntensity, maxIntensity);
			
			this.cc.ic.repaint();
			this.cc.populateTxtFields();
		}
	}
	
	/**
	 * Function to filter given set of markers with region shape.
	 * <p>
	 * Markers not coinciding with the given criteria are set inactive 
	 * after filtering. Note that markers not located in given z-slice are 
	 * skipped.
	 * 
	 * @param markers				Set of markers to filter.
	 * @param z							Z-coordinate of image/slice in stack.
	 * @param minSize				Minimal size of valid markers.
	 * @param maxSize				Maximal size of valid markers.
	 * @param minIntensity	Minimal intensity of valid markers.
	 * @param maxIntensity	Maximal intensity of valid markers.
	 */
	public static void filterMarkerRegions(CellCntrMarkerVector markers, int z,
		int minSize, int maxSize, int minIntensity,	int maxIntensity) {
		int regionCount = markers.size();
		for (int i=0; i<regionCount; ++i) {
			if (markers.get(i).getZ() != z) {
				System.out.println("Skipping marker: " + z + " vs " + markers.get(i).getZ());
				continue;
			}
			markers.get(i).setActive();
			CellCntrMarkerShapeRegion sr = 
					(CellCntrMarkerShapeRegion)markers.get(i).getShape();
			if (sr != null) {
				if (   sr.getRegion().getArea() < minSize 
						|| sr.getRegion().getArea() > maxSize ) {
					markers.get(i).setInactive();
				}
				else if (   sr.getAvgIntensity() < minIntensity
						     || sr.getAvgIntensity() > maxIntensity) {
					markers.get(i).setInactive();
				}
			}
		}
	}
}
