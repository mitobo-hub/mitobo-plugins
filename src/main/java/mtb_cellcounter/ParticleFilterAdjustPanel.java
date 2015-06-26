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
 * This file is partially originating from the ImageJ 1.0 file
 * http://imagej.nih.gov/ij/source/ij/plugin/frame/ThresholdAdjuster.java
 */

package mtb_cellcounter;

import java.awt.*;
import java.awt.event.*;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextField;

import ij.*;
import ij.measure.*;

/** 
 * GUI element to filter numerical values according to an interval.
 *
 * @author ImageJ 1.0 Team (see notice on top)
 * @author Birgit Moeller
 */
class ParticleFilterAdjustPanel extends JPanel implements Measurements,
	Runnable, ActionListener, AdjustmentListener, FocusListener {

	/**
	 * Instance of parent particle filter frame.
	 */
	private ParticleFilterFrame adjuster; 

	/**
	 * ImageJ instance.
	 */
	private ImageJ ij;
	
	/*
	 * GUI elements for adjusting thresholds.
	 */

	/**
	 * Slider for lower threshold.
	 */
	private JScrollBar minSlider;
	
	/**
	 * Slider for higher threshold.
	 */
	private JScrollBar maxSlider;
	
	/**
	 * Label for lower threshold slider.
	 */
	private JTextField minLabel;
	
	/**
	 * Label for higher threshold slider.
	 */	
	private JTextField maxLabel;
	
	/**
	 * Canvas for plotting the histogram.
	 */
	private ParticleSizePlot plot;
	
	/**
	 * Histogram of given data.
	 */
	private PlotHistogram plotData;

	/**
	 * Maximal value in given dataset.
	 */
	private int maxValue;
	
	/**
	 * Minimal value in given dataset.
	 */
	private int minValue;
	
	/**
	 * Size of data interval.
	 */
	private int dataRange;

	/**
	 * Current value of minimum slider.
	 */
	private int currentSliderMinValue = -1;

	/**
	 * Current value of maximum slider.
	 */
	private int currentSliderMaxValue = -1;

	/**
	 * Default constructor.
	 * @param psa					Reference to associated filter frame.
	 * @param titleLabel	Label for entity to be filtered.
	 * @param data				Data to be filtered.
	 * @param minVal			Minimal value in dataset.
	 * @param maxVal			Maximal value in dataset.
	 */
	public ParticleFilterAdjustPanel(ParticleFilterFrame psa, 
			String titleLabel, int [] data, int minVal, int maxVal) {
		super();
		this.adjuster = psa;
		this.ij = IJ.getInstance();
		
		// update internal status according to given data
		this.setData(data, minVal, maxVal);
		
		// setup panel
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);
		int y = 0;
		
		// title label
		c.gridx = 0;
		c.gridy = y++;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(10, 10, 0, 10);
		add(new JLabel("Interval of " + titleLabel), c);
		
		// plot
		c.gridx = 0;
		c.gridy = y++;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(10, 10, 0, 10);
		this.plot = new ParticleSizePlot();
		add(this.plot, c);
		this.plot.addKeyListener(this.ij);

		// slider for minimal threshold
		this.minSlider = new JScrollBar(Scrollbar.HORIZONTAL, 
			(int)(dataRange/3.0)+this.minValue,	 1, this.minValue, this.maxValue+1);
		c.gridx = 0;
		c.gridy = y++;
		c.gridwidth = 1;
		c.weightx = IJ.isMacintosh()?90:100;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 10, 0, 0);
		add(this.minSlider, c);
		this.minSlider.addAdjustmentListener(this);
		this.minSlider.addKeyListener(this.ij);
		this.minSlider.setUnitIncrement(1);
		this.minSlider.setFocusable(false);
		
		// label for minimal threshold
		c.gridx = 1;
		c.gridwidth = 1;
		c.weightx = IJ.isMacintosh()?10:0;
		c.insets = new Insets(5, 0, 0, 10);
		String text = IJ.isMacOSX()?"000000":"00000000";
		this.minLabel = new JTextField(text,5);
		this.minLabel.addFocusListener(this);
		this.minLabel.addActionListener(this);
		add(this.minLabel, c);
		
		// slider for maximal threshold
		this.maxSlider = new JScrollBar(Scrollbar.HORIZONTAL, 
			(int)(dataRange*2/3)+this.minValue,	1, this.minValue, this.maxValue+1);
		c.gridx = 0;
		c.gridy = y++;
		c.gridwidth = 1;
		c.weightx = 100;
		c.insets = new Insets(2, 10, 0, 0);
		add(this.maxSlider, c);
		this.maxSlider.addAdjustmentListener(this);
		this.maxSlider.addKeyListener(this.ij);
		this.maxSlider.setUnitIncrement(1);
		this.maxSlider.setFocusable(false);
		
		// label for maximal threshold
		c.gridx = 1;
		c.gridwidth = 1;
		c.weightx = 0;
		c.insets = new Insets(2, 0, 0, 10);
		this.maxLabel = new JTextField(text,5);
		this.maxLabel.addFocusListener(this);
		this.maxLabel.addActionListener(this);
		add(this.maxLabel, c);
		
		// global configuration
 		addKeyListener(this.ij);  // ImageJ handles keyboard shortcuts
 		this.setSize(400, 100);
 		this.updateGUI();
	}

	/**
	 * Updates internal configuration of panel
	 * @param data			New data.
	 * @param minVal		Minimal value in data.
	 * @param maxVal		Maximal value in data.
	 */
	public void setData(int[] data, int minVal, int maxVal) {
		this.minValue = minVal;
		this.maxValue = maxVal;
		this.dataRange = (int)(this.maxValue-this.minValue);		
		this.currentSliderMinValue = this.minValue;
		this.currentSliderMaxValue = this.maxValue;
		if (this.minSlider != null) {
			this.minSlider.setMinimum(this.minValue);
			this.minSlider.setMaximum(this.maxValue+1);
		}
		if (this.maxSlider != null) {
			this.maxSlider.setMinimum(this.minValue);
			this.maxSlider.setMaximum(this.maxValue+1);
		}
		this.plotData = 
			new PlotHistogram(data, minVal, maxVal);
	}

	/**
	 * Re-initializes the histogram and updates all graphical elements.
	 */
	public void updateGUI() {
		this.plot.setHistogram(this.plotData);
		updatePlot();
		updateScrollBars();
		updateLabels();
	}

	/**
	 * Triggers the update of the histogram plot.
	 */
	private void updatePlot() {
		this.plot.setMinThreshold(
			(double)(this.currentSliderMinValue-this.minValue)/(double)dataRange);
		this.plot.setMaxThreshold(
			(double)(this.currentSliderMaxValue-this.minValue)/(double)dataRange);
		this.plot.repaint();
	}

	/**
	 * Updates the labels.
	 */
	private void updateLabels() {
		this.minLabel.setText(Integer.toString(this.currentSliderMinValue));
		this.maxLabel.setText(Integer.toString(this.currentSliderMaxValue));
	}

	/**
	 * Updates the scrollbars.
	 */
	void updateScrollBars() {
		this.minSlider.setValue(this.currentSliderMinValue);
		this.maxSlider.setValue(this.currentSliderMaxValue);
	}

	/**
	 * Returns the current minimal threshold.
	 * @return	Value of minimal threshold.
	 */
	public int getMinSliderValue() {
		return this.currentSliderMinValue;
	}

	/**
	 * Returns the current maximal threshold.
	 * @return	Value of maximal threshold.
	 */
	public int getMaxSliderValue() {
		return this.currentSliderMaxValue;
	}
	
	/*
	 * Implementation of ActionListener interface.
	 */

	@Override
  public synchronized void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.maxLabel) {
			this.updateSlidersFromTextfield(new FocusEvent(this.maxLabel, 0));
			return;
		}
		else if (e.getSource() == this.minLabel) {
			this.updateSlidersFromTextfield(new FocusEvent(this.minLabel, 0));
			return;
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

	/*
	 * Implementation of FocusListener interface.
	 */
	
	@Override
	public void focusGained(FocusEvent e) {
		// nothing to do here
	}

	@Override
	public void focusLost(FocusEvent e) {
		this.updateSlidersFromTextfield(e);
	}
	
	/*
	 * Implementation of {@link AdjustmentListener} interface.
	 */
	
	@Override
  public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
		if (e.getSource()==this.minSlider) {
			this.currentSliderMinValue = this.minSlider.getValue();
		}
		else {
			this.currentSliderMaxValue = this.maxSlider.getValue();
		}
		this.updatePlot();
		this.updateLabels();
		this.adjuster.updateRegionFiltering();
		notify();
	}

	/**
	 * Called if user edits the text fields besides the slider labels.
	 * @param e		Event triggered on edit of text field.
	 */
	private void updateSlidersFromTextfield(FocusEvent e) {
		try {
			if (e.getSource() == this.minLabel) {
				if (   this.minLabel == null || this.minLabel.getText() == null
						|| this.minLabel.getText().isEmpty())
					return;
				int newValue = Integer.valueOf(this.minLabel.getText()).intValue();
				this.currentSliderMinValue = newValue; 
			}
			else if (e.getSource() == this.maxLabel) {
				if (   this.maxLabel == null || this.maxLabel.getText() == null
						|| this.maxLabel.getText().isEmpty())
					return;
				int newValue = Integer.valueOf(this.maxLabel.getText()).intValue();
				this.currentSliderMaxValue = newValue; 			
			}
			this.updateScrollBars();
			this.updatePlot();		
			this.adjuster.updateRegionFiltering();
		} catch(NumberFormatException ex) {
			Object[] options = {"Ok"};
			JOptionPane.showOptionDialog(null,
					"You entered non-numerical values, please correct your entry!",
					"Warning: error in text field!",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			return;
		}
	}

	/**
	 * Class to represent a histogram of numerical values.
	 * <p>
	 * Note that the bins are constrained to contain integer counts.
	 */
	protected class PlotHistogram {
		
		/**
		 * Actual data of the histogram, indexed per bin.
		 */
		private int[] histogramData;
		
		/**
		 * Numerical value referring to bin with minimal index, i.e. 0.
		 */
		private int minEntry;
		
		/**
		 * Numerical value referring to bin with maximal.
		 */
		private int maxEntry;
		
		/**
		 * Index of the bin with the histogram's maximal count.
		 */
		private int peakEntry;
		
		/**
		 * Default constructor.
		 * @param data		Histogram data.
		 * @param min			Smallest numerical value represented.
		 * @param max			Largest numerical value represented.
		 */
		public PlotHistogram(int[] data, int min, int max) {
			this.histogramData = data;
			this.minEntry = min;
			this.maxEntry = max;
			// find maximal entry in histogram
			this.peakEntry = 0;
			for (int i=0;i<256;++i) {
				if (this.histogramData[i]>this.peakEntry)
					this.peakEntry = this.histogramData[i];
			}
		}
		
		/**
		 * Get reference to histogram data.
		 * @return		Histogram data.
		 */
		public int[] getData() {
			return this.histogramData;
		}
		
		/**
		 * Get numerical value of bin with minimal index.
		 * @return		Value of bin with minimal index.
		 */
		public int getMinEntry() {
			return this.minEntry;
		}
		
		/**
		 * Get numerical value of bin with maximal index.
		 * @return		Value of bin with maximal index.
		 */
		public int getMaxEntry() {
			return this.maxEntry;
		}
		
		/**
		 * Get maximal count in histogram.
		 * @return
		 */
		public int getPeakEntry() {
			return this.peakEntry;
		}
	}
	
	/**
	 * Canvas to plot the histogram of particle sizes.
	 * 
	 * @author moeller
	 */
	protected class ParticleSizePlot extends Canvas 
		implements Measurements, MouseListener {
		
		/**
		 * Default width of the plot.
		 */
		protected static final int PLOTWIDTH = 256;
		/**
		 * Default height of the plot.
		 */
		protected static final int PLOTHEIGHT=48;

		/**
		 * Image object.
		 */
		protected Image os = null;
		/**
		 * Image's graphics context.
		 */
		protected Graphics osg = null;

		/**
		 * Current lower threshold, relative to data range.
		 */
		protected double currentMinThreshold;
		/**
		 * Current upper threshold, relative to data range.
		 */
		protected double currentMaxThreshold;

		/**
		 * Data histogram.
		 */
		protected int[] histogram;
		
		/**
		 * Maximal entry in histogram.
		 */
		protected int hmax;
		/**
		 * Minimal value in histogram.
		 */
		protected int minVal;
		
		/**
		 * Maximal value in histogram (used for normalization).
		 */
		protected int maxVal;
		
		/**
		 * Default constructor.
		 */
		public ParticleSizePlot() {
			addMouseListener(this);
			setSize(PLOTWIDTH+1, PLOTHEIGHT+1);
		}

		/**
		 * Overrides Component getPreferredSize().
		 * Added to work around a bug in Java 1.4.1 on Mac OS X.
		 */
		@Override
		public Dimension getPreferredSize() {
			return new Dimension(PLOTWIDTH+1, PLOTHEIGHT+1);
		}

		/**
		 * Update the histogram of data.
		 * @param histo 	New histogram data to be displayed.
		 */
		void setHistogram(PlotHistogram histo) {
			this.histogram = histo.getData();
			this.minVal = (int)histo.getMinEntry();
			this.maxVal = (int)histo.getMaxEntry();
			this.currentMinThreshold = 0.0;
			this.currentMaxThreshold = 1.0;
			this.hmax = histo.getPeakEntry();
			// reset the plot image
			this.os = null;
		}

		/**
		 * New minimal threshold
		 * @param minT	New relative threshold in interval [0,1].
		 */
		void setMinThreshold(double minT) {
			this.currentMinThreshold = minT;
		}
		
		/**
		 * New maximal threshold
		 * @param minT	New relative threshold in interval [0,1].
		 */
		void setMaxThreshold(double maxT) {
			this.currentMaxThreshold = maxT;
		}
		
		/* (non-Javadoc)
		 * @see java.awt.Canvas#update(java.awt.Graphics)
		 */
		@Override
		public void update(Graphics g) {
			paint(g);
		}

		/* (non-Javadoc)
		 * @see java.awt.Canvas#paint(java.awt.Graphics)
		 */
		@Override
		public void paint(Graphics g) {
			if (g==null) 
				return;
			if (this.histogram!=null) {
				if (this.os==null && this.hmax>0) {
					this.os = createImage(PLOTWIDTH,PLOTHEIGHT);
					this.osg = this.os.getGraphics();
					this.osg.setColor(Color.white);
					this.osg.fillRect(0, 0, PLOTWIDTH, PLOTHEIGHT);
					this.osg.setColor(Color.gray);
					for (int i = 0; i < PLOTWIDTH; i++) {
						this.osg.drawLine(i, PLOTHEIGHT, 
								i, PLOTHEIGHT - (PLOTHEIGHT * this.histogram[i]/this.hmax));
					}
					this.osg.dispose();
				}
				if (this.os==null) {
//					System.out.println("OS still null!");
					return;
				}
				g.drawImage(this.os, 0, 0, this);
			} else {
				System.err.println("Histogram is null");
				g.setColor(Color.white);
				g.fillRect(0, 0, PLOTWIDTH, PLOTHEIGHT);
			}
			g.setColor(Color.black);
			g.drawRect(0, 0, PLOTWIDTH, PLOTHEIGHT);
			g.setColor(Color.red);
			g.drawRect((int)(this.currentMinThreshold*256.0), 1, 
				(int)((this.currentMaxThreshold-this.currentMinThreshold)*256.0), 
					PLOTHEIGHT);
			g.drawLine((int)this.currentMinThreshold*256, 0, 
									(int)this.currentMaxThreshold*256, 0);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// nothing to do here
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			// nothing to do here
		}
		
		@Override
		public void mouseExited(MouseEvent e) {
			// nothing to do here
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			// nothing to do here
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {
			// nothing to do here			
		}
	} 
}
