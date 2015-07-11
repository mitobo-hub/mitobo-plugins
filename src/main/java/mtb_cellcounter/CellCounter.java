/* IMPORTANT NOTICE:
 * This file has originally been part of the Cell_Counter plugin written 
 * by Kurt De Vos, http://rsb.info.nih.gov/ij/plugins/cell-counter.html.
 * We extended the plugin functionality to fit to the specific needs of 
 * MiToBo. You can find the original license and file header below 
 * following the MiToBo license header.
 */

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

/* === Original File Header === */

/*
 *
 * @author Kurt De Vos 2005
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation (http://www.gnu.org/licenses/gpl.txt )
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package mtb_cellcounter;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.gui.StackWindow;
import ij.process.ImageProcessor;
import ij.CompositeImage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.ListIterator;
import java.util.Vector;
import java.util.concurrent.BlockingDeque;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.xml.parsers.ParserConfigurationException;

import loci.common.StatusEvent;
import loci.common.StatusListener;
import loci.common.StatusReporter;

import org.xml.sax.SAXException;

import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing;
import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwing;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeEvent;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeListener;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDWorkflowException;
import de.unihalle.informatik.Alida.gui.ALDChooseOpNameFrame;
import de.unihalle.informatik.Alida.gui.ALDOperatorConfigurationFrame;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.operator.events.ALDControlEvent;
import de.unihalle.informatik.Alida.operator.events.ALDControlEvent.ALDControlEventType;
import de.unihalle.informatik.Alida.version.ALDVersionProviderFactory;
import de.unihalle.informatik.Alida.workflows.ALDWorkflow;
import de.unihalle.informatik.Alida.workflows.ALDWorkflowNode.ALDWorkflowNodeState;
import de.unihalle.informatik.Alida.workflows.ALDWorkflowNodeID;
import de.unihalle.informatik.Alida.workflows.ALDWorkflow.ALDWorkflowContextType;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEvent;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEventListener;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEvent.ALDWorkflowEventType;
import de.unihalle.informatik.MiToBo.apps.particles2D.ParticleDetectorUWT2D;
import de.unihalle.informatik.MiToBo.core.dataio.provider.swing.AwtColorDataIOSwing.ColorChooserPanel;
import de.unihalle.informatik.MiToBo.core.datatypes.MTBBorder2DSet;
import de.unihalle.informatik.MiToBo.core.datatypes.MTBQuadraticCurve2D;
import de.unihalle.informatik.MiToBo.core.datatypes.MTBRegion2D;
import de.unihalle.informatik.MiToBo.core.datatypes.MTBRegion2DSet;
import de.unihalle.informatik.MiToBo.core.datatypes.images.MTBImage;
import de.unihalle.informatik.MiToBo.core.datatypes.images.MTBImage.MTBImageType;

/**
 * Main class of (MiToBo's) cell counter plugin.
 * 
 * @author Kurt De Vos
 * @author Birgit Moeller
 */
public class CellCounter extends JFrame 
	implements ActionListener, ItemListener, DocumentListener, 
		StatusReporter, ALDSwingValueChangeListener {
	
	/*
	 * Global command definitions.
	 */
	private static final String ADD = "Add";
	private static final String REMOVE = "Remove";
	private static final String INITIALIZE = "Initialize";
	private static final String RESULTS = "Results";
	private static final String DELETE = "Delete";
	private static final String KEEPORIGINAL = "Keep Original";
	private static final String SHOWMARKERS = "Show Markers";
	private static final String SHOWNUMBERS = "Show Numbers";
	private static final String SHOWALL = "Show All";
	private static final String RESET = "Reset";
	private static final String EXPORTMARKERS = "Save Markers";
	private static final String LOADMARKERS = "Load Markers";
	private static final String EXPORTIMG = "Export Image";
	private static final String MEASURE = "Measure...";
	private static final String ABOUT = "About";
	private static final String QUIT = "Quit";
	
	// commands associated with particle/stromuli/stomata pre-segmentation
	private static final String DETECT = "Detect";
	private static final String CONFIGURE = "Configure Operator...";
	private static final String FILTER = "Filter Particles...";
	private static final String SELECT = "Select markers";
	private static final String DETECTPLASTIDS = "Plastids";
	private static final String DETECTSTROMULI = "Stromuli";
	private static final String DETECTSTOMATA = "Stomata";
    
	/**
	 * Default colors to be used for the first 8 markers.
	 */
	private static final Color[] defaultColors =
			new Color[]{Color.YELLOW, Color.RED, Color.CYAN, Color.ORANGE,
					Color.BLUE, Color.GREEN, Color.MAGENTA, Color.LIGHT_GRAY};
	
	/**
	 * Frame for filtering segmented regions in a user-friendly manner.
	 */
	protected ParticleFilterFrame pFilter;
	
	/**
	 * Keeps track of the marker vectors.
	 */
	protected Vector<CellCntrMarkerVector> typeVector;
	/**
	 * Keeps track of the radio buttons for type selection.
	 */
	protected Vector<JRadioButton> dynRadioVector;
	/**
	 * Keeps track of the color choosers.
	 */
	protected Vector<ALDSwingComponent> dynColorChooserVector;
	/**
	 * Keeps track of the text fields showing numbers of markers.
	 */
	private Vector<JTextField> txtFieldVector;
	
	private CellCntrMarkerVector markerVector;
	protected CellCntrMarkerVector currentMarkerVector;

	protected int plastidMarkerIndex = -1;
	protected int stromuliMarkerIndex = -1;
	protected int stomataMarkerIndex = -1;
	
	protected JPanel dynPanel;
	protected JPanel dynButtonPanel;
	protected JPanel statButtonPanel;
	protected JPanel dynTxtPanel;
	protected JPanel dynColorPanel;
	protected JCheckBox showBordersBox;
	protected JCheckBox newCheck;
	protected JCheckBox markersCheck;
	protected JCheckBox numbersCheck;
	protected JCheckBox showAllCheck;
	// checkbox to enable/disable plastid detection
	protected JCheckBox cbDetectPlastids;
	// checkbox to enable/disable stromuli detection
	protected JCheckBox cbDetectStromuli;
	// checkbox to enable/disable stomata detection
	protected JCheckBox cbDetectStomata;
	protected ButtonGroup radioGrp;
	protected ButtonGroup channelGrp;
	protected JRadioButton ch1Button;
	protected JRadioButton ch2Button;
	protected JRadioButton ch3Button;
	protected JRadioButton ch4Button;
	protected JSeparator separator;
	protected JButton addButton;
	protected JButton removeButton;
	protected JButton initializeButton;
	protected JButton detectButton;
	protected JButton filterButton;
	protected JButton selectButton;
	protected JButton resultsButton;
	protected JButton deleteButton;
	protected JButton resetButton;
	protected JButton exportButton;
	protected JButton loadButton;
	protected JButton exportimgButton;
	protected JButton measureButton;
	
	private boolean keepOriginal=false;
	/**
	 * Avoids editing markers during pre-segmentation phase.
	 */
	protected boolean detectMode=false;

	/**
	 * Reference to associated image window.
	 */
	protected CellCntrImageCanvas ic;

	private ImagePlus img;
	private ImagePlus counterImg;
	/**
	 * Copy of input image used for pre-segmentation of regions.
	 */
	protected MTBImage detectImg;

	private GridLayout dynGrid;
	
	/**
	 * Plastid and stromuli wrapper operator.
	 */
	protected CellCounterDetectorOp detectorOp;
	/**
	 * Particle detector operator.
	 */
	protected ParticleDetectorUWT2D particleOp;
	/**
	 * Configuration window associated with particle detector.
	 */
//	protected ALDSwingComponent particleConfButton;
	protected JButton particleConfigureButton;
	/**
	 * Configuration frame for particle detector.
	 * <p>
	 * This is basically an {@link ALDOperatorConfigurationFrame}, however,
	 * restricted to the elements relevant in the context of the 
	 * {@link MTB_CellCounter}. E.g., options for parameter validation and 
	 * the 'Actions' menu are missing.
	 */
	protected ParticleDetectorConfigFrame particleConfigureFrame;
	
	/**
	 * Proxy object to run particle detector in thread mode.
	 */
	private OperatorExecutionProxy opProxy;
	
	private boolean isJava14;

	/**
	 * MiToBo icon to be shown in about box.
	 */
	protected ImageIcon aboutIcon;

	/** 
	 * Vector of installed {@link StatusListener} objects.
	 */
	protected Vector<StatusListener> m_statusListeners;

	static CellCounter instance;

	public CellCounter(){
		super("MiToBo Cell Counter");
		this.m_statusListeners = new Vector<StatusListener>();
		this.isJava14 = IJ.isJava14(); 
		if(!this.isJava14){
			IJ.showMessage("You are using a pre 1.4 version of java, " + 
					"exporting and loading marker data is disabled");
		}
		setResizable(false);
		this.typeVector = new Vector<CellCntrMarkerVector>();
		this.txtFieldVector = new Vector<JTextField>();
		this.dynRadioVector = new Vector<JRadioButton>();
		this.dynColorChooserVector = new Vector<ALDSwingComponent>();
		try {
			// configure the particle detector, except for the input image
			// which we do not know yet
	    this.particleOp = new ParticleDetectorUWT2D();
	    this.particleOp.setJmin(3);
	    this.particleOp.setJmax(4);
	    this.particleOp.setScaleIntervalSize(1);
	    this.particleOp.setMinRegionSize(1);
	    this.particleOp.setCorrelationThreshold(1.5);
	    this.particleConfigureFrame = 
	    	new ParticleDetectorConfigFrame(this.particleOp);
	    this.particleConfigureButton = new JButton(CONFIGURE);
	    this.particleConfigureButton.setActionCommand(CONFIGURE);
	    this.particleConfigureButton.addActionListener(this);
//	    		ALDDataIOManagerSwing.getInstance().createGUIElement(null, 
//	    				ParticleDetectorUWT2D.class, this.particleOp, null);
//	    this.opProxy = new OperatorExecutionProxy(this.particleOp);
//	    this.opProxy.nodeParameterChanged();
	    
	    // init the detector container
	    CellCounterDetectorOp detectorContainer;
	    try {
	    	// try to use a concrete sub-class implementation...
	    	detectorContainer = (CellCounterDetectorOp)Class.forName(
	    			"mtb_cellcounter.CellCounterDetectorOpAll").newInstance();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	// ... if it cannot be found, fall-back to plastid-only detector
	    	detectorContainer = new CellCounterDetectorOpPlastids();
	    }
	    this.detectorOp = detectorContainer;
	    this.detectorOp.setParticleDetector(this.particleOp);
	    this.opProxy = new OperatorExecutionProxy(this.detectorOp);
	    this.opProxy.nodeParameterChanged();
    } catch (ALDOperatorException e) {
    	IJ.error("Cannot initialize particle detector, initial\n" 
     		+ "pre-segmentation will not be possible!");
    	this.particleOp = null;
    } 
//		catch (ALDDataIOException e) {
//    	IJ.error("Cannot initialize configuration window for \n" 
//   			+ "particle detector, changing configuration will not be possible!");
//    	this.particleConfButton = null;
//    }
		initGUI();
		populateTxtFields();
		instance = this;
	}

	/** Show the GUI threadsafe*/
	private static class GUIShower implements Runnable {
		final JFrame jFrame;
		public GUIShower(JFrame _jFrame) {
			this.jFrame = _jFrame;
		}
		@Override
    public void run() {
			this.jFrame.pack();
			this.jFrame.setLocation(1000, 200);
			this.jFrame.setVisible(true);
		}
	}

	private void initGUI(){
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		GridBagLayout gb = new GridBagLayout();
		getContentPane().setLayout(new BorderLayout());
		
		this.radioGrp = new ButtonGroup();//to group the radiobuttons

		this.dynGrid = new GridLayout(8,1);
		this.dynGrid.setVgap(2);

		//this panel will keep the dynamic GUI parts
		this.dynPanel = new JPanel();
		this.dynPanel.setBorder(BorderFactory.createTitledBorder("Counters"));
		this.dynPanel.setLayout(gb);

		//this panel keeps the radiobuttons
		this.dynButtonPanel = new JPanel();
		this.dynButtonPanel.setLayout(this.dynGrid);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx=5;
		gb.setConstraints(this.dynButtonPanel,gbc);
		this.dynPanel.add(this.dynButtonPanel);

		//this panel allows to select the colors
		this.dynColorPanel=new JPanel();
		this.dynColorPanel.setLayout(this.dynGrid);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx=5;
		gb.setConstraints(this.dynColorPanel,gbc);
		this.dynPanel.add(this.dynColorPanel);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.ipadx=5;
		gb.setConstraints(this.dynPanel,gbc);
		getContentPane().add(this.dynPanel,BorderLayout.WEST);

		//this panel keeps the score
		this.dynTxtPanel=new JPanel();
		this.dynTxtPanel.setLayout(this.dynGrid);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx=5;
		gb.setConstraints(this.dynTxtPanel,gbc);
		this.dynPanel.add(this.dynTxtPanel);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.ipadx=5;
		gb.setConstraints(this.dynPanel,gbc);
		getContentPane().add(this.dynPanel);

		this.dynButtonPanel.add(makeDynRadioButton(1,null));
		this.dynButtonPanel.add(makeDynRadioButton(2,null));
		this.dynButtonPanel.add(makeDynRadioButton(3,null));
		this.dynButtonPanel.add(makeDynRadioButton(4,null));
		this.dynButtonPanel.add(makeDynRadioButton(5,null));
		this.dynButtonPanel.add(makeDynRadioButton(6,null));
		this.dynButtonPanel.add(makeDynRadioButton(7,null));
		this.dynButtonPanel.add(makeDynRadioButton(8,null));

		// create a "static" panel to hold control buttons
		this.statButtonPanel = new JPanel();
		this.statButtonPanel.setBorder(BorderFactory.createTitledBorder(
				"Actions"));
		this.statButtonPanel.setLayout(gb);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.newCheck = new JCheckBox(KEEPORIGINAL);
		this.newCheck.setToolTipText("Keep original");
		this.newCheck.setSelected(false);
		this.newCheck.addItemListener(this);
		gb.setConstraints(this.newCheck,gbc);
		this.statButtonPanel.add(this.newCheck);

		// add radio buttons to select channel to process
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		JLabel channelSelectLabel = new JLabel("Channel to process:");
		gb.setConstraints(channelSelectLabel, gbc);
		this.statButtonPanel.add(channelSelectLabel);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.ch1Button = new JRadioButton("1");
		this.ch1Button.setToolTipText("Process channel 1.");
		this.ch2Button = new JRadioButton("2");
		this.ch2Button.setToolTipText("Process channel 2.");
		this.ch3Button = new JRadioButton("3");
		this.ch3Button.setToolTipText("Process channel 3.");
		this.ch4Button = new JRadioButton("4");
		this.ch4Button.setToolTipText("Process channel 4.");
		this.ch1Button.setSelected(true);
		this.channelGrp = new ButtonGroup();
		this.channelGrp.add(this.ch1Button);
		this.channelGrp.add(this.ch2Button);
		this.channelGrp.add(this.ch3Button);
		this.channelGrp.add(this.ch4Button);
		JPanel channelSelectPanel = new JPanel();
		channelSelectPanel.setAlignmentX(LEFT_ALIGNMENT);
		channelSelectPanel.add(this.ch1Button);
		channelSelectPanel.add(this.ch2Button);
		channelSelectPanel.add(this.ch3Button);
		channelSelectPanel.add(this.ch4Button);
		gb.setConstraints(channelSelectPanel,gbc);
		this.statButtonPanel.add(channelSelectPanel);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.initializeButton = makeButton(INITIALIZE, "Initialize image to count");
		gb.setConstraints(this.initializeButton,gbc);
		this.statButtonPanel.add(this.initializeButton);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(3,0,3,0);
		this.separator = new JSeparator(SwingConstants.HORIZONTAL);
		this.separator.setPreferredSize(new Dimension(1,1));
		gb.setConstraints(this.separator,gbc);
		this.statButtonPanel.add(this.separator);

		/*
		 * Which objects to detect?
		 */
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		JLabel objectDetectLabel = new JLabel("Objects to detect:");
		gb.setConstraints(objectDetectLabel, gbc);
		this.statButtonPanel.add(objectDetectLabel);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.cbDetectPlastids = new JCheckBox(DETECTPLASTIDS);
		this.cbDetectPlastids.setToolTipText("Enable/disable plastid detection");
		this.cbDetectPlastids.setSelected(true);
		this.cbDetectPlastids.addItemListener(this);
		gb.setConstraints(this.cbDetectPlastids, gbc);
		this.statButtonPanel.add(this.cbDetectPlastids);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.cbDetectStromuli = new JCheckBox(DETECTSTROMULI);
		this.cbDetectStromuli.setToolTipText("Enable/disable stromuli detection");
		this.cbDetectStromuli.setSelected(false);
		this.cbDetectStromuli.addItemListener(this);
		gb.setConstraints(this.cbDetectStromuli, gbc);
		this.statButtonPanel.add(this.cbDetectStromuli);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.cbDetectStomata = new JCheckBox(DETECTSTOMATA);
		this.cbDetectStomata.setToolTipText("Enable/disable stomata detection");
		this.cbDetectStomata.setSelected(false);
		this.cbDetectStomata.addItemListener(this);
		gb.setConstraints(this.cbDetectStomata, gbc);
		this.statButtonPanel.add(this.cbDetectStomata);

		/*
		 * Initial particle detection.
		 */
		
		// detect particles
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		if (this.particleOp != null) {
			this.detectButton = makeButton(DETECT,
					"Perform pre-segmentation of particles.");
			gb.setConstraints(this.detectButton,gbc);
			this.statButtonPanel.add(this.detectButton);
		}
		else {
			JButton dummyButton = new JButton(DETECT);
			dummyButton.setEnabled(false);
			gb.setConstraints(dummyButton,gbc);
			this.statButtonPanel.add(dummyButton);
		}

		// configure detection
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		if (this.particleConfigureButton != null) {
//			gb.setConstraints(this.particleConfButton.getJComponent(),gbc);
//			this.statButtonPanel.add(this.particleConfButton.getJComponent());
			gb.setConstraints(this.particleConfigureButton,gbc);
			this.statButtonPanel.add(this.particleConfigureButton);
		}
		else {
			JButton dummyButton = new JButton("Configure...");
			dummyButton.setEnabled(false);
			gb.setConstraints(dummyButton,gbc);
			this.statButtonPanel.add(dummyButton);
		}

		// filter particles
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.filterButton = makeButton(FILTER, 
				"Filter particles according to size.");
		gb.setConstraints(this.filterButton,gbc);
		this.statButtonPanel.add(this.filterButton);

		// show contours or do not show them
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.showBordersBox = new JCheckBox("Show contours");
		this.showBordersBox.setToolTipText("Show detected contours, shortcut <v>.");
		this.showBordersBox.addItemListener(this);
		gb.setConstraints(this.showBordersBox,gbc);
		this.statButtonPanel.add(this.showBordersBox);

		// select markers
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.selectButton = makeButton(SELECT,
				"Selects active markers, deletes others.");
		this.selectButton.setEnabled(false);
		gb.setConstraints(this.selectButton,gbc);
		this.statButtonPanel.add(this.selectButton);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(3,0,3,0);
		this.separator = new JSeparator(SwingConstants.HORIZONTAL);
		this.separator.setPreferredSize(new Dimension(1,1));
		gb.setConstraints(this.separator,gbc);
		this.statButtonPanel.add(this.separator);

		/*
		 * Editing markers.
		 */

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.addButton = makeButton(ADD, "add a counter type");
		gb.setConstraints(this.addButton,gbc);
		this.statButtonPanel.add(this.addButton);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.removeButton = makeButton(REMOVE, "remove last counter type");
		gb.setConstraints(this.removeButton,gbc);
		this.statButtonPanel.add(this.removeButton);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.insets = new Insets(3,0,3,0);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.separator = new JSeparator(SwingConstants.HORIZONTAL);
		this.separator.setPreferredSize(new Dimension(1,1));
		gb.setConstraints(this.separator,gbc);
		this.statButtonPanel.add(this.separator);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.deleteButton = makeButton(DELETE, "delete last marker");
		this.deleteButton.setEnabled(false);
		gb.setConstraints(this.deleteButton,gbc);
		this.statButtonPanel.add(this.deleteButton);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(3,0,3,0);
		this.separator = new JSeparator(SwingConstants.HORIZONTAL);
		this.separator.setPreferredSize(new Dimension(1,1));
		gb.setConstraints(this.separator,gbc);
		this.statButtonPanel.add(this.separator);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.resultsButton = makeButton(RESULTS, "show results in results table");
		this.resultsButton.setEnabled(false);
		gb.setConstraints(this.resultsButton,gbc);
		this.statButtonPanel.add(this.resultsButton);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.resetButton=makeButton(RESET, "reset all counters");
		this.resetButton.setEnabled(false);
		gb.setConstraints(this.resetButton,gbc);
		this.statButtonPanel.add(this.resetButton);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(3,0,3,0);
		this.separator = new JSeparator(SwingConstants.HORIZONTAL);
		this.separator.setPreferredSize(new Dimension(1,1));
		gb.setConstraints(this.separator,gbc);
		this.statButtonPanel.add(this.separator);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.markersCheck = new JCheckBox(SHOWMARKERS);
		this.markersCheck.setToolTipText("Show markers, shortcut <y>.");
		this.markersCheck.setSelected(true);
		this.markersCheck.setEnabled(false);
		this.markersCheck.addItemListener(this);
		gb.setConstraints(this.markersCheck,gbc);
		this.statButtonPanel.add(this.markersCheck);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.numbersCheck = new JCheckBox(SHOWNUMBERS);
		this.numbersCheck.setToolTipText("Show numbers, shortcut <x>");
		this.numbersCheck.setSelected(true);
		this.numbersCheck.setEnabled(false);
		this.numbersCheck.addItemListener(this);
		gb.setConstraints(this.numbersCheck,gbc);
		this.statButtonPanel.add(this.numbersCheck);

		this.showAllCheck = new JCheckBox(SHOWALL);
		this.showAllCheck.setToolTipText("When selected, all stack markers are shown");
		this.showAllCheck.setSelected(false);
		this.showAllCheck.setEnabled(false);
		this.showAllCheck.addItemListener(this);
		gb.setConstraints(this.showAllCheck,gbc);
		this.statButtonPanel.add(this.showAllCheck);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.exportButton = makeButton(EXPORTMARKERS, "Save markers to file");
		this.exportButton.setEnabled(false);
		gb.setConstraints(this.exportButton,gbc);
		this.statButtonPanel.add(this.exportButton);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.loadButton = makeButton(LOADMARKERS, "Load markers from file");
		if (!this.isJava14) this.loadButton.setEnabled(false);
		gb.setConstraints(this.loadButton,gbc);
		this.statButtonPanel.add(this.loadButton);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.exportimgButton= makeButton(EXPORTIMG, "Export image with markers");
		this.exportimgButton.setEnabled(false);
		gb.setConstraints(this.exportimgButton,gbc);
		this.statButtonPanel.add(this.exportimgButton);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(3,0,3,0);
		this.separator = new JSeparator(SwingConstants.HORIZONTAL);
		this.separator.setPreferredSize(new Dimension(1,1));
		gb.setConstraints(this.separator,gbc);
		this.statButtonPanel.add(this.separator);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		this.measureButton = 
				makeButton(MEASURE, "Measure pixel intensity of marker points");
		this.measureButton.setEnabled(false);
		gb.setConstraints(this.measureButton,gbc);
		this.statButtonPanel.add(this.measureButton);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.ipadx=5;
		gb.setConstraints(this.statButtonPanel,gbc);
		getContentPane().add(this.statButtonPanel,BorderLayout.EAST);

		// add a menubar
		this.addMenuBar();

		// setup icon for about box
		this.setupAboutBoxIcon();
		
		Runnable runner = new GUIShower(this);
		EventQueue.invokeLater(runner);
	}

	/**
	 * Adds a menu bar to the frame.
	 */
	private void addMenuBar() {
		// 'File' menu on the left
		JMenuBar mainWindowMenu = new JMenuBar();
		JMenu fileM = new JMenu("File");
		JMenuItem quitItem = new JMenuItem(QUIT);
		quitItem.addActionListener(this);
		fileM.add(quitItem);
		
		// 'Help' menu on the right
		JMenu helpM = new JMenu("Help");
		JMenuItem itemAbout = new JMenuItem(ABOUT);
		itemAbout.addActionListener(this);
		helpM.add(itemAbout);

		// put it all together
		mainWindowMenu.add(fileM);
		mainWindowMenu.add(Box.createHorizontalGlue());
		mainWindowMenu.add(helpM);
		this.setJMenuBar(mainWindowMenu);
	}
	
	/**
	 * Initializes the icon for the about box.
	 */
	private void setupAboutBoxIcon() {
		String iconDataName = "/share/logo/MiToBo_logo.png";
		Image iconimg = null;
		BufferedImage bi = null;
		Graphics g = null;
		InputStream is = null;
		try {
			ImageIcon icon;
			File iconDataFile = new File("./" + iconDataName);
			if(iconDataFile.exists()) {
				icon = new ImageIcon("./" + iconDataName);
				iconimg = icon.getImage();
			}
			// try to find it inside a jar archive....
			else {
				is = ALDChooseOpNameFrame.class.getResourceAsStream(iconDataName);
				if (is == null) {
					System.err.println("Warning - cannot find icons...");
					iconimg = new BufferedImage(20,20,BufferedImage.TYPE_INT_ARGB);
				}
				else {
					iconimg = ImageIO.read(is);
				}
				bi= new BufferedImage(20,20,BufferedImage.TYPE_INT_ARGB);
				g = bi.createGraphics();
				g.drawImage(iconimg, 0, 0, 20, 20, null);
			}
		} catch (IOException ex) {
			System.err.println("MiToBo Cell Counter - problems loading icons...!");
			iconimg = new BufferedImage(20,20,BufferedImage.TYPE_INT_ARGB);
			bi= new BufferedImage(20,20,BufferedImage.TYPE_INT_ARGB);
			g = bi.createGraphics();
			g.drawImage(iconimg, 0, 0, 20, 20, null);
		}
		this.aboutIcon = new ImageIcon(iconimg);
	}
	
	private JTextField makeDynamicTextArea(){
		JTextField txtFld = new JTextField(6);
		txtFld.setHorizontalAlignment(SwingConstants.CENTER);
		txtFld.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		txtFld.setEditable(false);
		txtFld.setText("0");
		txtFld.getDocument().addDocumentListener(this);
		this.txtFieldVector.add(txtFld);
		return txtFld;
	}

	void populateTxtFields(){
		ListIterator<CellCntrMarkerVector> it = this.typeVector.listIterator();
		while (it.hasNext()){
			int index = it.nextIndex();
			CellCntrMarkerVector markVector = it.next();
			int count = markVector.size();
			JTextField tArea = this.txtFieldVector.get(index);
			tArea.setText(""+count);
		}
		validateLayout();
	}

	/**
	 * Initializes the radio buttons to select the marker type.
	 * @param id		Index of the marker (in the range of 1-8)
	 * @return	Created button.
	 */
	private JRadioButton makeDynRadioButton(int id, CellCntrMarkerVector mv){
		JRadioButton jrButton = new JRadioButton("Type "+ id);
		jrButton.addActionListener(this);
		this.dynRadioVector.add(jrButton);
		this.radioGrp.add(jrButton);
		if (mv == null) {
			this.markerVector = new CellCntrMarkerVector(id);
			if (id <= defaultColors.length)
				this.markerVector.setColor(defaultColors[id-1]);
			this.typeVector.add(this.markerVector);
		}
		this.dynTxtPanel.add(makeDynamicTextArea());
		// init color chooser component
		try {
			ALDDataIOSwing provider = 
					(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
							Color.class, ALDDataIOSwing.class);
			Color c;
			if (id <= defaultColors.length)
				c = defaultColors[id-1];
			else 
				c = this.markerVector.getColor();
			ALDSwingComponent colorChooser = 
				provider.createGUIElement(null,Color.class,c,null);
			colorChooser.addValueChangeEventListener(this);
			this.dynColorPanel.add(colorChooser.getJComponent());
			((ColorChooserPanel)colorChooser).addActionListenerToButtons(this);
			this.dynColorChooserVector.add(colorChooser);
		} catch (ALDDataIOException e) {
			IJ.error("Init of color chooser failed,\n changes will not be possible!");
		}
		return jrButton;
	}

	private JButton makeButton(String name, String tooltip){
		JButton jButton = new JButton(name);
		jButton.setToolTipText(tooltip);
		jButton.addActionListener(this);
		return jButton;
	}

	/**
	 * Method to initialize internal image data from focussed GUI image.
	 */
	private void initializeImage(){
		reset();
		this.img = WindowManager.getCurrentImage();
		this.img.setIgnoreFlush(true);
		// check number of channels and choose selected one
		MTBImage tmpImage = MTBImage.createMTBImage(this.img);
		int selectedChannel = 1;
		if (this.ch1Button.isSelected())
			selectedChannel = 1;
		else if (this.ch2Button.isSelected())
			selectedChannel = 2;
		else if (this.ch3Button.isSelected())
			selectedChannel = 3;
		else
			selectedChannel = 4;
		if (tmpImage.getSizeC() < selectedChannel)
			JOptionPane.showMessageDialog(CellCounter.this.getFocusOwner(), 
					"Input image has only " + tmpImage.getSizeC() + " channel(s),"
					+ "\n please select another one!");

		// initialize detection image with byte image
		this.detectImg = tmpImage.getImagePart(0, 0, 0, 0, 
				selectedChannel-1, tmpImage.getSizeX(), tmpImage.getSizeY(), 
				1, 1, 1);
		if (!this.detectImg.getType().equals(MTBImageType.MTB_BYTE)) {
			this.detectImg = 
					this.detectImg.convertType(MTBImageType.MTB_BYTE, true);
		}
		boolean v139t = IJ.getVersion().compareTo("1.39t")>=0;
		if (this.img==null){
			IJ.noImage();
			return;
		}else if (this.img.getStackSize() == 1) {
			ImageProcessor ip = this.img.getProcessor();
			ip.resetRoi();
			if (this.keepOriginal)
				ip = ip.crop();
			this.counterImg = new ImagePlus("MTB Cell Counter Window - "+this.img.getTitle(), ip);
			Vector displayList = v139t?this.img.getCanvas().getDisplayList():null;
			this.ic = new CellCntrImageCanvas(this.counterImg,this.typeVector,this,displayList);
			new ImageWindow(this.counterImg, this.ic);
		} else if (this.img.getStackSize() > 1){
			ImageStack stack = this.img.getStack();
			int size = stack.getSize();
			ImageStack counterStack = this.img.createEmptyStack();
			for (int i = 1; i <= size; i++){
				ImageProcessor ip = stack.getProcessor(i);
				if (this.keepOriginal)
					ip = ip.crop();
				counterStack.addSlice(stack.getSliceLabel(i), ip);
			}
			this.counterImg = new ImagePlus("Counter Window - "+this.img.getTitle(), counterStack);
			this.counterImg.setDimensions(this.img.getNChannels(), this.img.getNSlices(), this.img.getNFrames());
			if (this.img.isComposite()) {
				this.counterImg = new CompositeImage(this.counterImg, ((CompositeImage)this.img).getMode());
				((CompositeImage) this.counterImg).copyLuts(this.img);
			}
			this.counterImg.setOpenAsHyperStack(this.img.isHyperStack());
			Vector displayList = v139t?this.img.getCanvas().getDisplayList():null;
			this.ic = new CellCntrImageCanvas(this.counterImg,this.typeVector,this,displayList);
			new StackWindow(this.counterImg, this.ic);
		}
		if (!this.keepOriginal){
			this.img.changes = false;
			this.img.close();
		}
		this.markersCheck.setEnabled(true);
		this.numbersCheck.setEnabled(true);
		this.showAllCheck.setSelected(false);
		if (this.counterImg.getStackSize()>1)
			this.showAllCheck.setEnabled(true);
		this.addButton.setEnabled(true);
		this.removeButton.setEnabled(true);
		this.resultsButton.setEnabled(true);
		this.deleteButton.setEnabled(true);
		this.resetButton.setEnabled(true);
		if (this.isJava14) this.exportButton.setEnabled(true);
		this.exportimgButton.setEnabled(true);
		this.measureButton.setEnabled(true);
		this.ic.initKeyHandler();
		// add a status bar to the bottom of the image window
		this.ic.addStatusBar();
	}

	private void validateLayout(){
		this.dynPanel.validate();
		this.dynButtonPanel.validate();
		this.dynTxtPanel.validate();
		this.statButtonPanel.validate();
		validate();
		pack();
	}
	
	@Override
  public void dispose() {
		if (   this.ic != null 
				&& this.ic.getImage() != null
				&& this.ic.getImage().getWindow() != null)
			this.ic.getImage().getWindow().dispose();
		this.particleConfigureFrame.quit();
		if (this.pFilter != null)
			this.pFilter.dispose();
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
  public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();

		// add another type of marker
		if (command.compareTo(ADD) == 0) {
			int i = this.dynRadioVector.size() + 1;
			this.dynGrid.setRows(i);
			this.dynButtonPanel.add(makeDynRadioButton(i,null));
			validateLayout();
			
			if (this.ic != null)
				this.ic.setTypeVector(this.typeVector);
		}
		// remove last marker type (markers on image are deleted)
		else if (command.compareTo(REMOVE) == 0) {
			if (this.dynRadioVector.size() > 1) {
				JRadioButton rbutton = this.dynRadioVector.lastElement();
				this.dynButtonPanel.remove(rbutton);
				this.radioGrp.remove(rbutton);
				this.dynRadioVector.removeElementAt(this.dynRadioVector.size() - 1);
				this.dynGrid.setRows(this.dynRadioVector.size());
			}
			if (this.txtFieldVector.size() > 1) {
				JTextField field = this.txtFieldVector.lastElement();
				this.dynTxtPanel.remove(field);
				this.txtFieldVector.removeElementAt(this.txtFieldVector.size() - 1);
			}
			if (this.typeVector.size() > 1) {
				this.typeVector.removeElementAt(this.typeVector.size() - 1);
			}
			// remove color chooser button
			if (this.dynColorChooserVector.size() > 1) {
				JComponent c= this.dynColorChooserVector.lastElement().getJComponent();
				this.dynColorPanel.remove(c);
				this.dynColorChooserVector.removeElementAt(
						this.dynColorChooserVector.size()-1);
			}
			validateLayout();

			if (this.ic != null)
				this.ic.setTypeVector(this.typeVector);
		} else if (command.compareTo(INITIALIZE) == 0){
			initializeImage();
		} else if (command.startsWith("Type")){ //COUNT
			if (this.ic == null){
				IJ.error("You need to initialize first");
				return;
			}
			int index = Integer.parseInt(command.substring(command.indexOf(" ")+1, 
					command.length()));
			//ic.setDelmode(false); // just in case
			this.currentMarkerVector = this.typeVector.get(index-1);
			// set the current color if no markers with ID are present
      try {
      	int markerCount = 
      		Integer.parseInt(
      			((JTextField)this.dynTxtPanel.getComponent(index-1)).getText());
      	if (markerCount == 0) {
      		Color cc = (Color)ALDDataIOManagerSwing.getInstance().readData(null, 
	      		Color.class, this.dynColorChooserVector.get(index-1));
      		this.currentMarkerVector.setColor(cc);
      	}
      } catch (ALDDataIOException e) {
      	IJ.error("Problems changing color, something went wrong...");
      }
			this.ic.setCurrentMarkerVector(this.currentMarkerVector);
		}
		// button press on a color chooser
		else if (command.compareTo("openColorChooser") == 0) {
			// show an error message if marker count is not zero
			for (int i=0; i<this.dynColorChooserVector.size(); ++i) {
				ALDSwingComponent c = this.dynColorChooserVector.elementAt(i);
				if (event.getSource() == c.getJComponent()) {
					if (Integer.valueOf(this.txtFieldVector.get(i).getText()).
																													intValue() != 0) {
						JOptionPane.showMessageDialog(CellCounter.this.getFocusOwner(), 
								"Color cannot be changed if there still\n"
										+ "exist markers of this type in the image!");
						return;
					}
				}
			}
		}	else if (command.compareTo(DELETE) == 0){
			if (!this.detectMode)
				this.ic.removeLastMarker();
		} else if (command.compareTo(RESET) == 0){
			if (!this.detectMode)
				reset();
			this.opProxy.nodeParameterChanged();
		} else if (command.compareTo(RESULTS) == 0){
			report();
		}else if (command.compareTo(EXPORTMARKERS) == 0){
			exportMarkers();
		}else if (command.compareTo(LOADMARKERS) == 0){
			if (this.ic == null)
				initializeImage();
			loadMarkers();
			validateLayout();
		}else if (command.compareTo(EXPORTIMG) == 0){
			this.ic.imageWithMarkers().show();
		}else if (command.compareTo(MEASURE) == 0){
			measure();
		}else if (command.compareTo(QUIT) == 0) {
			this.dispose();			
		}else if (command.compareTo(ABOUT) == 0) {
			this.showAboutBox();
		}
		else if (command.compareTo(CONFIGURE) == 0) {
			this.particleConfigureFrame.setVisible(true);
		}
		// detect particles
		else if (command.compareTo(DETECT) == 0) {
			ALDWorkflowNodeState state = this.opProxy.getOpState();
			if (state == null) {
				IJ.error("An error occurred during detection!");
				return;
			}
			if (state.equals(ALDWorkflowNodeState.READY)) {
				JOptionPane.showMessageDialog(CellCounter.this.getFocusOwner(), 
						"Particles already detected with current configuration!");
				return;
			}
			if (this.ic == null){
				IJ.error("You need to initialize first");
				return;
			}
			// warning that markers of type 1 will be lost!
			if (this.typeVector.elementAt(0).size() > 0) {
				Object[] options = {"Continue", "Cancel"};
				int n = JOptionPane.showOptionDialog(null,
						"Attention, your markers of type 1 will be lost!",
						"Warning: Markers will be lost",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE,
						null,     //do not use a custom Icon
						options,  //the titles of buttons
						options[0]); //default button title
				if (n == JOptionPane.NO_OPTION)
					return;
			}
			this.detectMode = true;
			this.ic.setEditable(false);
			// delete markers in GUI
			this.typeVector.setElementAt(new CellCntrMarkerVector(1), 0);
			if (this.ic!=null)
				this.ic.repaint();	
			populateTxtFields();
			// run the workflow (automatically threaded)
			this.opProxy.runWorkflow();
			this.opProxy.processWorkflowEventQueue();
		}
		// filter particles
		else if (command.compareTo(FILTER) == 0) {
			// check if regions were already detected
			if (   this.currentMarkerVector == null 
					|| this.currentMarkerVector.getSegmentationData() == null) {
				Object[] options = {"Ok"};
				JOptionPane.showOptionDialog(null,
						"No regions detected! Please run detector first!",
						"Filter Warning: run detector first",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE,
						null, options, options[0]);
				return;				
			}				
			if (this.pFilter == null) {
				CellCntrMarkerVector pVec = CellCounter.this.typeVector.get(
						CellCounter.this.plastidMarkerIndex);
				this.pFilter = new ParticleFilterFrame(this, 
					(CellCntrSegResultRegions)pVec.getSegmentationData(),
						this.detectImg);
			}
//			else {
//				this.pFilter.setRegions(this.currentMarkerVector.getDetectedRegions());
//				this.pFilter.setup(this.currentMarkerVector.getDetectedRegions());
//			}
			this.pFilter.setVisible(true);
		}
		// update borders
//		else if (command.compareTo(UPDATE) == 0) {
//			this.filterRegions();
//		}
		// fix selected markers, i.e. remove filtered regions
		else if (command.compareTo(SELECT) == 0) {
			this.detectMode = false;
			this.ic.setEditable(true);
			CellCntrMarkerVector oldVec = this.typeVector.elementAt(0);
			CellCntrMarkerVector newVec = new CellCntrMarkerVector(1);
      try {
        Color cc = (Color)ALDDataIOManagerSwing.getInstance().readData(null, 
        		Color.class, this.dynColorChooserVector.get(0));
	      newVec.setColor(cc);
      } catch (ALDDataIOException e) {
      	// nothing to do here, simply skip color setting...
      }
			MTBRegion2DSet regs = new MTBRegion2DSet();
			MTBBorder2DSet borders = new MTBBorder2DSet();
			Vector<Double> avgIntensities = new Vector<Double>();
			CellCntrSegResultRegions oldSeg = 
					(CellCntrSegResultRegions)oldVec.getSegmentationData();
			for (int i=0; i<oldVec.size(); ++i) {
				if (oldSeg.getActivityArray().elementAt(i).booleanValue()) {
					newVec.add(oldVec.elementAt(i));
					regs.add(oldSeg.getRegions().elementAt(i));
					borders.add(oldSeg.getBorders().elementAt(i));
					avgIntensities.add(oldSeg.getAverageIntensities().elementAt(i));
				}
			}
			CellCntrSegResultRegions newRes = 
					new CellCntrSegResultRegions(
							this.detectImg, regs, borders, avgIntensities);
			newVec.setSegmentationData(newRes);
			this.typeVector.setElementAt(newVec, 0);
			this.dynRadioVector.elementAt(0).setSelected(true);
			this.currentMarkerVector = newVec;
			this.ic.setCurrentMarkerVector(newVec);
//			boolean v139t = IJ.getVersion().compareTo("1.39t")>=0;
//			Vector displayList = v139t?this.img.getCanvas().getDisplayList():null;
//			this.ic.setImage(this.detectImg.getImagePlus(),displayList);
			this.disableDetectMode();
		}
		if (this.ic!=null)
			this.ic.repaint();
		populateTxtFields();
	}

	@Override
  public void itemStateChanged(ItemEvent e){
		if (e.getItem().equals(this.newCheck)){
			if (e.getStateChange()==ItemEvent.SELECTED){
				this.keepOriginal=true;
			}else{
				this.keepOriginal=false;
			}
		}else if (e.getItem().equals(this.markersCheck)){
			if (e.getStateChange()==ItemEvent.SELECTED){
				this.ic.setShowMarkers(true);
			}else{
				this.ic.setShowMarkers(false);
			}
			this.ic.repaint();
		}else if (e.getItem().equals(this.numbersCheck)){
			if (e.getStateChange()==ItemEvent.SELECTED){
				this.ic.setShowNumbers(true);
			}else{
				this.ic.setShowNumbers(false);
			}
			this.ic.repaint();
		}else if (e.getItem().equals(this.showAllCheck)){
			if (e.getStateChange()==ItemEvent.SELECTED){
				this.ic.setShowAll(true);
			}else{
				this.ic.setShowAll(false);
			}
			this.ic.repaint();
		}else if (e.getItem().equals(this.showBordersBox)){
			if (e.getStateChange()==ItemEvent.SELECTED){
				this.ic.setShowBorders(true);
			}else{
				this.ic.setShowBorders(false);
			}
			this.ic.repaint();			
		}
		// options for object detection
		else if (e.getItem().equals(this.cbDetectPlastids)){
			if (e.getStateChange()==ItemEvent.SELECTED){
				this.cbDetectStromuli.setEnabled(true);
				this.cbDetectStomata.setEnabled(true);
			}else{
				// disable the other two checkboxes
				this.cbDetectStromuli.setEnabled(false);
				this.cbDetectStomata.setEnabled(false);
			}
		}
	}

	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeListener#handleValueChangeEvent(de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeEvent)
	 */
	@Override
  public void handleValueChangeEvent(ALDSwingValueChangeEvent event) {
		ALDSwingComponent colorChooser = (ALDSwingComponent)event.getSource();
		int index = this.dynColorChooserVector.indexOf(colorChooser);
    try {
    	int markerCount = 
    		Integer.parseInt(
    			((JTextField)this.dynTxtPanel.getComponent(index)).getText());
    	if (markerCount == 0) {
    		Color cc = (Color)ALDDataIOManagerSwing.getInstance().readData(null, 
      		Color.class, this.dynColorChooserVector.get(index));
    		this.typeVector.get(index).setColor(cc);
    	}
    	this.ic.updateStatusBar();
    	this.ic.updateCursor();
    } catch (ALDDataIOException e) {
    	IJ.error("Problems changing color, something went wrong...");
    }
	}
	
	/**
	 * Show MiToBo About Box window.
	 */
	protected void showAboutBox() {
		Object[] options = { "OK" };
		String year = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
		String rev = ALDVersionProviderFactory.getProviderInstance().getVersion();
		if (rev.contains("=")) {
			int equalSign = rev.indexOf("=");
			int closingBracket = rev.lastIndexOf("]");
			rev = rev.substring(0, equalSign + 9) + rev.substring(closingBracket);
		}
		String msg = "<html>This plugin is based on the Cell_Counter plugin "
				+ "originally written by<p> Kurt De Vos, website: "
				+ "<i>http://rsb.info.nih.gov/ij/plugins/cell-counter.html</i>"
				+ "<p>"
				+ "<p>This extended version is developed by the MiToBo team:"
				+ "<p>"
				+ "<p>MiToBo - A Microscope Image Analysis Toolbox, <p>" 
	    + "Release " + rev + "<p>" + "\u00a9 2010 - " + year + "   "
	    + "Martin Luther University Halle-Wittenberg<p>"
	    + "Institute of Computer Science, Faculty of Natural Sciences III<p><p>"
	    + "Email: mitobo@informatik.uni-halle.de<p>"
	    + "Internet: <i>www.informatik.uni-halle.de/mitobo</i><p>"
	    + "License: GPL 3.0, <i>http://www.gnu.org/licenses/gpl.html</i></html>";

		JOptionPane.showOptionDialog(null, new JLabel(msg),
		    "Information about MiToBo Cell Counter", JOptionPane.DEFAULT_OPTION,
		    JOptionPane.INFORMATION_MESSAGE, this.aboutIcon, options, options[0]);
	}
	
	/**
	 * Disables detect mode and deactivates buttons.
	 */
	protected void disableDetectMode() {
		this.detectMode = false;
		this.selectButton.setEnabled(false);
		// reset the workflow node
		this.opProxy.nodeParameterChanged();
		if (this.ic!=null) {
			this.ic.setEditable(true);
			this.ic.repaint();
		}
		populateTxtFields();
	}

	public void measure(){
		this.ic.measure();
	}

	public void reset(){
		if (this.typeVector.size()<1){
			return;
		}
		ListIterator<CellCntrMarkerVector> mit = this.typeVector.listIterator();
		while (mit.hasNext()){
			CellCntrMarkerVector mv = mit.next();
			mv.clear();
			// delete borders and regions
			mv.clearRegions();
		}
		if (this.ic!=null)
			this.ic.repaint();
	}

	public void report(){
		String labels = "Slice\t";
		boolean isStack = this.counterImg.getStackSize()>1;
		//add the types according to the button vector!!!!
		ListIterator<JRadioButton> it = this.dynRadioVector.listIterator();
		while (it.hasNext()){
			JRadioButton button = it.next();
			String str = button.getText(); //System.out.println(str);
			labels = labels.concat(str+"\t");
		}
		IJ.setColumnHeadings(labels);
		String results = "";
		if (isStack){
			for (int slice=1; slice<=this.counterImg.getStackSize(); slice++){
				results="";
				ListIterator<CellCntrMarkerVector> mit= this.typeVector.listIterator();
				int types = this.typeVector.size();
				int[] typeTotals = new int[types];
				while (mit.hasNext()){
					int type = mit.nextIndex();
					CellCntrMarkerVector mv = mit.next();
					ListIterator tit = mv.listIterator();
					while(tit.hasNext()){
						CellCntrMarker m = (CellCntrMarker)tit.next();
						if (m.getZ() == slice){
							typeTotals[type]++;
						}
					}
				}
				results=results.concat(slice+"\t");
				for(int i=0; i<typeTotals.length;i++){
					results = results.concat(typeTotals[i]+"\t");
				}
				IJ.write(results);
			}
			IJ.write("");
		}
		results = "Total\t";
		ListIterator<CellCntrMarkerVector> mit = this.typeVector.listIterator();
		while (mit.hasNext()){
			CellCntrMarkerVector mv = mit.next();
			int count = mv.size();
			results = results.concat(count+"\t");
		}
		IJ.write(results);
	}

	public void loadMarkers(){
		String filePath = getFilePath(new JFrame(), "Select Marker File", OPEN);
		if (filePath == null)
			return;
		ReadXML rxml;
		try {
			rxml = new ReadXML(filePath);
		} catch (SAXException e) {
			JOptionPane.showMessageDialog(this.getFocusOwner(), 
				"XML file <" + filePath + "> is not well-formed, exiting!");
			return;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this.getFocusOwner(), 
				"Error on loading the file, exiting!");
			return;
		} catch (ParserConfigurationException e){
			JOptionPane.showMessageDialog(this.getFocusOwner(), 
				"ParserConfigurationException, something went wrong on reading file!");
			return;
		}

		String storedfilename = rxml.readImgProperties(rxml.IMAGE_FILE_PATH);
		if(storedfilename.equals(this.img.getTitle())){
			Vector loadedvector = rxml.readMarkerData();
			this.typeVector = loadedvector;
			this.ic.setTypeVector(this.typeVector);
			int index = Integer.parseInt(rxml.readImgProperties(rxml.CURRENT_TYPE));
			this.currentMarkerVector = this.typeVector.get(index); 
			this.ic.setCurrentMarkerVector(this.currentMarkerVector);

			// if GUI shows less marker types than in file, add some
			int buttonNum = this.dynRadioVector.size();
			while (this.typeVector.size() > buttonNum) {
				int i = this.dynRadioVector.size() + 1;
				this.dynGrid.setRows(i);
				this.dynButtonPanel.add(makeDynRadioButton(i,this.typeVector.get(i-1)));
				buttonNum = this.dynRadioVector.size();
				validateLayout();
			}

			// update color choosers
			for (int i=0; i<this.typeVector.size(); ++i) {
				CellCntrMarkerVector marker = this.typeVector.get(i);
				Color c = marker.getColor();
				ALDSwingComponent comp = this.dynColorChooserVector.get(i);
				try {
	        ALDDataIOManagerSwing.getInstance().setValue(
	        		null, Color.class, comp, c);
        } catch (ALDDataIOException e) {
        	IJ.error("Problems setting color, something went wrong...");
        }
			}
			
			// if GUI shows more markers than in file, delete some
			while(this.dynRadioVector.size()>this.typeVector.size()){
				if (this.dynRadioVector.size() > 1) {
					JRadioButton rbutton = this.dynRadioVector.lastElement();
					this.dynButtonPanel.remove(rbutton);
					this.radioGrp.remove(rbutton);
					this.dynRadioVector.removeElementAt(this.dynRadioVector.size() - 1);
					this.dynGrid.setRows(this.dynRadioVector.size());
				}
				if (this.txtFieldVector.size() > 1) {
					JTextField field = this.txtFieldVector.lastElement();
					this.dynTxtPanel.remove(field);
					this.txtFieldVector.removeElementAt(this.txtFieldVector.size() - 1);
				}
				if (this.dynColorChooserVector.size() > 1) {
					ALDSwingComponent comp = this.dynColorChooserVector.lastElement();
					this.dynColorPanel.remove(comp.getJComponent());
					this.dynColorChooserVector.removeElementAt(
							this.dynColorChooserVector.size()-1);
				}
			}
			JRadioButton butt = (this.dynRadioVector.get(index));
			butt.setSelected(true);

		}else{
			IJ.error("These Markers do not belong to the current image");
		}
		// turn off the detect mode
		this.disableDetectMode();
	}

	public void exportMarkers(){
		String filePath = getFilePath(new JFrame(), "Save Marker File (.xml)", SAVE);
		if (!filePath.endsWith(".xml"))
			filePath+=".xml";
		try {
			WriteXML wxml = new WriteXML(filePath);
			wxml.writeXML(this.img.getTitle(), this.typeVector, this.typeVector.indexOf(this.currentMarkerVector));
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this.getFocusOwner(), 
				"File not found, please check output file name!");
			return;
		} catch (UnsupportedEncodingException e) {
			JOptionPane.showMessageDialog(this.getFocusOwner(), 
				"This VM does not support the UTF-8 character set.\n" 
					+ "Cannot save markers to file!");
			return;
		}
	}

	public static final int SAVE=FileDialog.SAVE, OPEN=FileDialog.LOAD;
	private String getFilePath(JFrame parent, String dialogMessage, int dialogType){
		switch(dialogType){
		case(SAVE):
			dialogMessage = "Save "+dialogMessage;
		break;
		case(OPEN):
			dialogMessage = "Open "+dialogMessage;
		break;
		}
		FileDialog fd ;
		String[] filePathComponents = new String[2];
		int PATH = 0;
		int FILE = 1;
		fd = new FileDialog(parent, dialogMessage, dialogType);
		switch(dialogType){
		case(SAVE):
			String filename = this.img.getTitle();
		fd.setFile("CellCounter_"+filename.substring(0,filename.lastIndexOf(".")+1)+"xml");
		break;
		}
		fd.setVisible(true);
		// choice was cancelled
		if (fd.getFile() == null) 
			return null;
		filePathComponents[PATH] = fd.getDirectory();
		filePathComponents[FILE] = fd.getFile();
		return filePathComponents[PATH]+filePathComponents[FILE];
	}

	public Vector<JRadioButton> getButtonVector() {
		return this.dynRadioVector;
	}

	public void setButtonVector(Vector<JRadioButton> buttonVector) {
		this.dynRadioVector = buttonVector;
	}

	public CellCntrMarkerVector getCurrentMarkerVector() {
		return this.currentMarkerVector;
	}

	public void setCurrentMarkerVector(CellCntrMarkerVector currMarkerVector) {
		this.currentMarkerVector = currMarkerVector;
	}

	public static void setType(int type) {
		if (instance==null || instance.ic==null)
			return;
		// do not allow type changes in detect mode
		if (instance.detectMode)
			return;
		int index = type-1;
		int buttons = instance.dynRadioVector.size();
		if (index<0 || index>=buttons)
			return;
		JRadioButton rbutton = instance.dynRadioVector.elementAt(index);
		instance.radioGrp.setSelected(rbutton.getModel(), true);
		instance.currentMarkerVector = instance.typeVector.get(index);
		instance.ic.setCurrentMarkerVector(instance.currentMarkerVector);
	}

	public void switchMarkers() {
		if (this.markersCheck.isSelected()){
			this.ic.setShowMarkers(false);
			this.markersCheck.setSelected(false);
		}else{
			this.ic.setShowMarkers(true);
			this.markersCheck.setSelected(true);
		}
		this.ic.repaint();
	}
	
	public void switchNumbers() {
		if (this.numbersCheck.isSelected()){
			this.ic.setShowNumbers(false);
			this.numbersCheck.setSelected(false);
		}else{
			this.ic.setShowNumbers(true);
			this.numbersCheck.setSelected(true);
		}
		this.ic.repaint();
	}

	public void switchContours() {
		if (this.showBordersBox.isSelected()){
			this.ic.setShowBorders(false);
			this.showBordersBox.setSelected(false);
		}else{
			this.ic.setShowBorders(true);
			this.showBordersBox.setSelected(true);
		}
		this.ic.repaint();
	}

	/*
	 * Methods implementing the DocumentListener interface. They are used for
	 * automatically activating or deactivating the color chooser buttons 
	 * according to the number of markers of a certain type currently present.
	 * The buttons are only active, i.e. allow for changing the color, if no
	 * markers are set. Otherwise one marker type might get multiple colors 
	 * which will most likely end-up in confusion.
	 */
	
	/* (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
	 */
	@Override
  public void insertUpdate(DocumentEvent e) {
		this.updateChooserButton(e);
  }

	/* (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
	 */
	@Override
  public void removeUpdate(DocumentEvent e) {
		this.updateChooserButton(e);
  }

	/* (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
	 */
	@Override
  public void changedUpdate(DocumentEvent e) {
		this.updateChooserButton(e);
  }
	
	/**
	 * Method to enable or disable the color chooser button linked with document.
	 * @param e		Document event to be handled.
	 */
	private void updateChooserButton(DocumentEvent e) {
		Document doc = e.getDocument();
		
		// search for the corresponding text field
		int index = -1;
		for (int i=0; i<this.txtFieldVector.size(); ++i) {
			JTextField comp = this.txtFieldVector.get(i);
			if (doc.equals(comp.getDocument())) {
				index = i;
				break;
			}
		}
		// if found, enable/disanle the corresponding chooser button
		if (index != -1) {
			String text= this.txtFieldVector.get(index).getText();
			if (text != null && !text.isEmpty()) {
				ColorChooserPanel b = 
						(ColorChooserPanel)this.dynColorChooserVector.get(index);
				int markerCount =	Integer.parseInt(text);
				if (markerCount == 0) {
					b.ignoreButtonPress(false);
				}
				else {
					b.ignoreButtonPress(true);
				}
			}
		}
	}
	
	/**
	 * Manager class for (threaded) execution of particle detector via workflow.
	 * 
	 * @author Birgit Moeller
	 */
	protected class OperatorExecutionProxy implements	
		ALDWorkflowEventListener {
		
		/**
		 * Reference to the underlying Alida workflow object.
		 */
		protected ALDWorkflow alidaWorkflow;

		/**
		 * Listener object attached to the operator configuration object.
		 */
		protected ValueChangeListener valueChangeListener;

		/**
		 * Corresponding configuration and control window.
		 */
		protected JDialog progressMessageWin;

		/**
		 * Reference ID of the operator node in the Alida workflow;
		 */
		public ALDWorkflowNodeID opNodeID;
		
		/**
		 * Default constructor.
		 * @param op	Operator object to be executed.
		 */
		public OperatorExecutionProxy(ALDOperator op) {
			try {
				this.alidaWorkflow = new ALDWorkflow(" ",ALDWorkflowContextType.OTHER);
				this.alidaWorkflow.addALDWorkflowEventListener(this);
			} catch (ALDOperatorException e) {
				IJ.error("Workflow initialization failed! Exiting!");
				System.exit(-1);
			}
			// init the operator and its workflow, i.e. add a single node to the flow
			try {
				this.opNodeID = this.alidaWorkflow.createNode(op);
				this.valueChangeListener = new ValueChangeListener(this.opNodeID);
//				CellCounter.this.particleConfButton.addValueChangeEventListener(
//						this.valueChangeListener);
			} catch (ALDWorkflowException ex) {
				JOptionPane.showMessageDialog(null, "Instantiation of operator \""
						+ op.getName() + "\" failed!\n", 
						"Error", JOptionPane.ERROR_MESSAGE);
			}
			// process workflow events
			this.processWorkflowEventQueue();
			
			// some additional initializations
			this.initProgressWin();
		}
		
		/**
		 * Notify workflow that operator object parameters changed.
		 */
		public void nodeParameterChanged() {
			try {
	      this.alidaWorkflow.nodeParameterChanged(this.opNodeID);
      } catch (ALDWorkflowException e) {
      	IJ.error("Workflow interaction failed!");
      }
		}
		
		/**
		 * Request the state of the operator workflow node.
		 * @return	State of the node.
		 */
		public ALDWorkflowNodeState getOpState() {
      try {
      	return this.alidaWorkflow.getState(this.opNodeID);
      } catch (ALDWorkflowException e) {
      	IJ.error("Workflow interaction failed!");
	      return null;
      }
		}
		
		/**
		 * Setup the window for informing the user about progress.
		 */
		protected void initProgressWin() {
			String message = 
					new String("The particle detector is running, this \n" 
										+ "may take some moments, please wait...");
			final JOptionPane optionPane = new JOptionPane(message, 
					JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, 
					new Object[]{}, null);
			this.progressMessageWin = new JDialog();
			this.progressMessageWin.setTitle("Message: pre-segmentation phase");
			this.progressMessageWin.setModal(true);
			this.progressMessageWin.setContentPane(optionPane);
			this.progressMessageWin.setDefaultCloseOperation(
					WindowConstants.DO_NOTHING_ON_CLOSE);
			this.progressMessageWin.pack();
			this.progressMessageWin.setVisible(false);
		}
		
		/**
		 * Displays progress window in the center of the image.
		 */
		protected void showProgressWin() {
			if (CellCounter.this.ic != null) {
				Point p = CellCounter.this.ic.getLocationOnScreen();
				Dimension d = CellCounter.this.ic.getSize();
				int x = p.x + (int)(d.width/2.0) - 
						(int)(this.progressMessageWin.getSize().width/2.0);
				int y = p.y + (int)(d.height/2.0) - 
						(int)(this.progressMessageWin.getSize().height/2.0);
				this.progressMessageWin.setLocation(x, y);
			}
			this.progressMessageWin.setVisible(true);			
		}

		/**
		 * Executes the workflow.
		 */
		public void runWorkflow() {
			try {
				// configure operator
				CellCounter.this.detectorOp.setInputImage(CellCounter.this.detectImg);
				CellCounter.this.detectorOp.setDoPlastidDetection(
						CellCounter.this.cbDetectPlastids.isSelected());
				CellCounter.this.detectorOp.setDoStromuliDetection(
						 CellCounter.this.cbDetectStromuli.isEnabled()
					&& CellCounter.this.cbDetectStromuli.isSelected());
				CellCounter.this.detectorOp.setDoStomataDetection(
					   CellCounter.this.cbDetectStomata.isEnabled()
					&& CellCounter.this.cbDetectStomata.isSelected());
				// verify configuration once again
				this.alidaWorkflow.nodeParameterChanged(this.opNodeID);
				// execute the node/workflow
				this.alidaWorkflow.handleALDControlEvent(
						new ALDControlEvent(this, ALDControlEventType.RUN_EVENT));
				this.alidaWorkflow.runWorkflow();
				this.showProgressWin();
			} catch (ALDWorkflowException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Executing operator failed!\n",
						"Error", JOptionPane.ERROR_MESSAGE);
			} 
			// post-process workflow events
			this.processWorkflowEventQueue();
		}
		
		/**
		 * Processes all events that were recently added to the queue.
		 * <p>
		 * Note that this function needs to be called after all actions on the 
		 * Alida workflow except calls to 'run' methods.
		 */
		protected synchronized void processWorkflowEventQueue() {
			BlockingDeque<ALDWorkflowEvent> queue = 
					this.alidaWorkflow.getEventQueue(this);
			ALDWorkflowEvent event = null; 	
			while (!queue.isEmpty()) {
				event = queue.pop();
				this.handleALDWorkflowEvent(event);
			}
		}
		
		/* (non-Javadoc)
		 * @see de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEventListener#handleALDWorkflowEvent(de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEvent)
		 */
		@Override
		public synchronized void handleALDWorkflowEvent(ALDWorkflowEvent event) {

			// extract event data
			ALDWorkflowEventType type = event.getEventType();
			
			// handle the event
			switch(type) 
			{
			// some events are simply ignored in this context...
			case ADD_NODE:
			case EXECUTION_FINISHED:
			case NODE_STATE_CHANGE:				
			case RENAME:
			case NODE_PARAMETER_CHANGE:
			case USER_INTERRUPT:
				break;
			case RUN_FAILURE:
				JOptionPane.showMessageDialog(null, "Detection failed!\n",
						"Error", JOptionPane.ERROR_MESSAGE);
				break;
			case SHOW_RESULTS:
				this.progressMessageWin.setVisible(false);
				MTBRegion2DSet particles = 
					CellCounter.this.detectorOp.getResultPlastidRegions();
				CellCntrSegResult res = null;
				if (particles != null) {
					res = new CellCntrSegResultRegions(
							CellCounter.this.detectImg,	particles);
				}
				MTBRegion2DSet stromuli = 
						CellCounter.this.detectorOp.getResultStromuliRegions();
				CellCntrSegResult resStromuli = null;
				if (stromuli != null) {
					resStromuli = new CellCntrSegResultRegions(
							CellCounter.this.detectImg,	stromuli);
				}
				Vector<MTBQuadraticCurve2D> stomata = 
						CellCounter.this.detectorOp.getResultStomataRegions();
				CellCntrSegResult resStomata = null;
				if (stomata != null) {
					resStomata = new CellCntrSegResultCurves(
							CellCounter.this.detectImg,	stomata);
				}

				int markerIndex = 0;
				
				// draw detected particles
				if (particles != null) {
					CellCounter.this.plastidMarkerIndex = markerIndex;
					CellCounter.this.currentMarkerVector =
							CellCounter.this.typeVector.get(markerIndex);
					CellCounter.this.currentMarkerVector.setSegmentationData(res);
					for (int i=0; i<particles.size(); ++i) {
						MTBRegion2D reg = particles.elementAt(i);
						CellCntrMarker marker = new CellCntrMarker();
						marker.setX((int)reg.getCenterOfMass_X());
						marker.setY((int)reg.getCenterOfMass_Y());
						marker.setZ(1);
						CellCounter.this.currentMarkerVector.add(marker);
					}
					if (CellCounter.this.pFilter != null) {
						// note: cast to correct segmentation data type necessary
						((CellCntrSegResultRegions)CellCounter.this.
								currentMarkerVector.getSegmentationData()).filterRegions(
								CellCounter.this.pFilter.getMinSizeValue(),
								CellCounter.this.pFilter.getMaxSizeValue(),
								CellCounter.this.pFilter.getMinIntensityValue(),
								CellCounter.this.pFilter.getMaxIntensityValue());
						CellCounter.this.pFilter.updateSegmentationData(
								(CellCntrSegResultRegions)CellCounter.this.
									currentMarkerVector.getSegmentationData(),
								CellCounter.this.detectImg);
					}
					try {
						Color cc = (Color)ALDDataIOManagerSwing.getInstance().readData(null, 
								Color.class, 
								CellCounter.this.dynColorChooserVector.get(markerIndex));
						CellCounter.this.currentMarkerVector.setColor(cc);
					} catch (ALDDataIOException e) {
						IJ.error("Setting color failed!");
					}
					CellCounter.this.ic.setCurrentMarkerVector(
							CellCounter.this.currentMarkerVector);
				}
				
				// draw detected stomata
				if (stomata != null) {
					++markerIndex;
					CellCounter.this.stomataMarkerIndex = markerIndex;
					CellCounter.this.currentMarkerVector =
							CellCounter.this.typeVector.get(markerIndex);
					CellCounter.this.currentMarkerVector.setSegmentationData(
							resStomata);
					for (int i=0; i<stomata.size(); ++i) {
						MTBQuadraticCurve2D reg = stomata.elementAt(i);
						CellCntrMarker marker = new CellCntrMarker();
						marker.setX((int)reg.getCenterX());
						marker.setY((int)reg.getCenterY());
						marker.setZ(1);
						CellCounter.this.currentMarkerVector.add(marker);
					}
					try {
						Color cc = (Color)ALDDataIOManagerSwing.getInstance().readData(
								null,	Color.class, 
								CellCounter.this.dynColorChooserVector.get(markerIndex));
						CellCounter.this.currentMarkerVector.setColor(cc);
					} catch (ALDDataIOException e) {
						IJ.error("Setting color failed!");
					}
					CellCounter.this.ic.setCurrentMarkerVector(
							CellCounter.this.currentMarkerVector);
				}

				// draw detected stromulis
				if (stromuli != null) {
					++markerIndex;
					CellCounter.this.stromuliMarkerIndex = markerIndex;
					CellCounter.this.currentMarkerVector =
							CellCounter.this.typeVector.get(markerIndex);
					CellCounter.this.currentMarkerVector.setSegmentationData(
							resStromuli);
					for (int i=0; i<stromuli.size(); ++i) {
						MTBRegion2D reg = stromuli.elementAt(i);
						CellCntrMarker marker = new CellCntrMarker();
						marker.setX((int)reg.getCenterOfMass_X());
						marker.setY((int)reg.getCenterOfMass_Y());
						marker.setZ(1);
						CellCounter.this.currentMarkerVector.add(marker);
					}
					try {
						Color cc = (Color)ALDDataIOManagerSwing.getInstance().readData(
								null,	Color.class, 
								CellCounter.this.dynColorChooserVector.get(markerIndex));
						CellCounter.this.currentMarkerVector.setColor(cc);
					} catch (ALDDataIOException e) {
						IJ.error("Setting color failed!");
					}
					CellCounter.this.ic.setCurrentMarkerVector(
							CellCounter.this.currentMarkerVector);
				}
				
				// update GUI
				CellCounter.this.dynRadioVector.elementAt(1).setSelected(true);
//				CellCounter.this.updateButton.setEnabled(true);
				CellCounter.this.selectButton.setEnabled(true);
				if (CellCounter.this.ic!=null)
					CellCounter.this.ic.repaint();
				populateTxtFields();
				break;
			default:
				System.err.println("Event type \'" + type + "\' not yet handled...");
			}
		}

		/**
		 * Listener class to react on parameter value changes in operator.
		 * @author moeller
		 */
		class ValueChangeListener implements ALDSwingValueChangeListener {

			/**
			 * Corresponding node ID.
			 */
			private ALDWorkflowNodeID id;

			/**
			 * Default constructor.
			 * @param nodeID	Alida workflow node ID of associated operator node.
			 */
			public ValueChangeListener(ALDWorkflowNodeID nodeID) {
				this.id = nodeID;
			}

			/**
			 * Updates the ID of the workflow node associated with this listener.
			 * @param nodeID	New node ID.
			 */
			public void updateNodeID(ALDWorkflowNodeID nodeID) {
				this.id = nodeID;
			}

			@Override
      public void handleValueChangeEvent(ALDSwingValueChangeEvent event) {
				try {
					// notify workflow of change in node parameters
					OperatorExecutionProxy.this.alidaWorkflow.nodeParameterChanged(
							this.id);
					// process event queue
					OperatorExecutionProxy.this.processWorkflowEventQueue();
				} catch (ALDWorkflowException ex) {
					System.err.println("[ValueChangeListener] Warning! " 
							+ "could not propagate parameter change event, node not found!");
				}	  
      }
		}
	}
	
	/*
	 * Status listener interface, takes care of also updating particle detector.
	 */
	
	@Override
  public void addStatusListener(StatusListener statListener) {
		this.m_statusListeners.add(statListener);	
		this.particleOp.addStatusListener(statListener);
  }

	@Override
  public void notifyListeners(StatusEvent e) {
		for (int i = 0; i < this.m_statusListeners.size(); i++) {
			this.m_statusListeners.get(i).statusUpdated(e);
		}
		this.particleOp.notifyListeners(e);
  }

	@Override
  public void removeStatusListener(StatusListener statListener) {
		this.m_statusListeners.remove(statListener);
		this.particleOp.removeStatusListener(statListener);
  }

}
