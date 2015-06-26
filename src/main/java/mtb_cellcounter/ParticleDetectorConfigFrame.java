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

package mtb_cellcounter;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

import javax.swing.*;

import de.unihalle.informatik.Alida.operator.*;
import de.unihalle.informatik.Alida.version.ALDVersionProviderFactory;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerXmlbeans;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.*;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.*;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOException;
import de.unihalle.informatik.Alida.exceptions.ALDException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException.OperatorExceptionType;
import de.unihalle.informatik.Alida.gui.OnlineHelpDisplayer;
import de.unihalle.informatik.MiToBo.apps.particles2D.ParticleDetectorUWT2D;
import de.unihalle.informatik.MiToBo.core.helpers.MTBIcon;

/**
 * Frame to configure an {@link ParticleDetectorUWT2D} in context of 
 * {@link MTB_CellCounter}.
 * 
 * @author Birgit Moeller
 */
public class ParticleDetectorConfigFrame extends JFrame 
	implements ActionListener, ALDSwingValueChangeListener {

	/**
	 * Local flag for debug output.
	 */
	@SuppressWarnings("unused")
	private boolean debug = false;

	/**
	 * Width of the frame.
	 */
	private static final int windowWidth = 475;
	
	/**
	 * Height of the frame.
	 */
	private static final int windowHeight = 350;

	/**
	 * The operator associated with this frame.
	 */
	protected ParticleDetectorUWT2D op = null;

	/**
	 * The top level panel of this frame.
	 */
	protected JPanel mainPanel;

	/**
	 * Title string of window.
	 */
	protected final static String titleString = 
		"Configure Particle Detector Parameters...";

	/**
	 * Last directory visited, initially it's user's home.
	 */
	protected String lastDirectory = System.getProperty("user.home");

	/**
	 * Last selected file.
	 */
	protected File lastFile = new File("operatorParams.xml");

	/**
	 * Ok label to be used on button of Ok message boxes.
	 */
	protected final Object[] okOption = { "OK" };

	/**
	 * Labels to be used on buttons of Yes/No message boxes.
	 */
	protected final Object[] yesnoOption = { "YES", "NO" };

	protected HashMap<String, ALDSwingComponent> guiElements;
	
	/** 
	 * Constructs a control frame for an operator object.
	 * @param _op Operator to be associated with this frame object.
	 * @throws ALDOperatorException
	 */
	public ParticleDetectorConfigFrame(ParticleDetectorUWT2D _op) 
			throws ALDOperatorException {
		if (_op == null)
			throw new ALDOperatorException(OperatorExceptionType.INSTANTIATION_ERROR,
				"[ParticleDetectorConfigFrame] no operator given, object null!");
		this.op = _op;

		// init the window
		this.guiElements = new HashMap<String, ALDSwingComponent>();
		this.setupWindow();
	}
	
	/**
	 * Construct the frame to configure an operator.
	 */
	protected void setupWindow() {

		// set up the main panel containing input panel and status bar
		this.mainPanel = new JPanel();
		this.mainPanel.setLayout(new BorderLayout());
		
		// set up the parameter input panel
		JPanel inputPanel = new JPanel();
		BoxLayout ylayout = new BoxLayout(inputPanel, BoxLayout.Y_AXIS);
		inputPanel.setLayout(ylayout);
		
		// add fields for input parameters
		this.addParameterInputFields(inputPanel);
		
		// wrap input panel into scroll pane
		JScrollPane scrollPane = new JScrollPane(inputPanel);
		this.mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		this.mainPanel.add(Box.createVerticalGlue());
		this.mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		this.mainPanel.add(scrollPane);
		this.mainPanel.add(this.addCloseButtonPanel(), BorderLayout.SOUTH);
		
		// add pane to this window
		this.add(this.mainPanel);
		
		// add a nice menubar
		JMenuBar mainWindowMenu = new JMenuBar();
		JMenu fileM = new JMenu("File");
		JMenuItem itemSave = new JMenuItem("Save Settings");
		itemSave.setActionCommand("fileM_save");
		itemSave.addActionListener(this);
		JMenuItem itemLoad = new JMenuItem("Load Settings");
		itemLoad.setActionCommand("fileM_load");
		itemLoad.addActionListener(this);
		fileM.add(itemSave);
		fileM.add(itemLoad);
		mainWindowMenu.add(fileM);

		// generate help menu
		JMenu helpM = this.generateHelpMenu();
		mainWindowMenu.add(Box.createHorizontalGlue());
		mainWindowMenu.add(helpM);

		// and go ..
		this.setTitle(titleString);
		this.setJMenuBar(mainWindowMenu);
		this.setSize(new Dimension(windowWidth, windowHeight));

	}

	/**
	 * Adds the input fields for all relevant parameters.
	 */
	protected void addParameterInputFields(JPanel parentPanel) {
		try {
			// JMin
			JPanel paramPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
			JLabel nameLabel = new JLabel("Minimal Scale ( JMin ): ");
			nameLabel.setToolTipText("Smallest scale on which to detect particles," 
				+ " must be >= 1.");
			paramPanel.add(nameLabel);
			ALDOpParameterDescriptor descr = this.op.getParameterDescriptor("Jmin");
			Object value = this.op.getParameter("Jmin");
			ALDSwingComponent aldElement = 
				ALDDataIOManagerSwing.getInstance().createGUIElement(
					descr.getField(),	descr.getMyclass(),	value, descr);
			aldElement.addValueChangeEventListener(this);
			this.guiElements.put("Jmin", aldElement);
			paramPanel.add(aldElement.getJComponent());
			parentPanel.add(paramPanel);

			// JMax
			paramPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
			nameLabel = new JLabel("Maximal Scale ( JMax ): ");
			nameLabel.setToolTipText("Largest scale on which to detect particles," 
				+ " must be >= JMin.");
			paramPanel.add(nameLabel);
			descr = this.op.getParameterDescriptor("Jmax");
			value = this.op.getParameter("Jmax");
			aldElement = ALDDataIOManagerSwing.getInstance().createGUIElement(
				descr.getField(),	descr.getMyclass(),	value, descr);
			aldElement.addValueChangeEventListener(this);
			this.guiElements.put("Jmax", aldElement);
			paramPanel.add(aldElement.getJComponent());
			parentPanel.add(paramPanel);

			// scale interval size
			paramPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
			nameLabel = new JLabel("Scale Interval Size: ");
			nameLabel.setToolTipText("Number of scales to consider for each " 
				+ "correlation image, must be <= (JMax - JMin + 1).");
			paramPanel.add(nameLabel);
			descr = this.op.getParameterDescriptor("scaleIntervalSize");
			value = this.op.getParameter("scaleIntervalSize");
			aldElement = ALDDataIOManagerSwing.getInstance().createGUIElement(
				descr.getField(),	descr.getMyclass(),	value, descr);
			aldElement.addValueChangeEventListener(this);
			this.guiElements.put("scaleIntervalSize", aldElement);
			paramPanel.add(aldElement.getJComponent());
			parentPanel.add(paramPanel);

			// correlation threshold
			paramPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
			nameLabel = new JLabel("Correlation Threshold: ");
			nameLabel.setToolTipText("Threshold for correlation images, the smaller "
				+ "the more particles will be detected.");
			paramPanel.add(nameLabel);
			descr = this.op.getParameterDescriptor("corrThreshold");
			value = this.op.getParameter("corrThreshold");
			aldElement = ALDDataIOManagerSwing.getInstance().createGUIElement(
				descr.getField(),	descr.getMyclass(),	value, descr);
			aldElement.addValueChangeEventListener(this);
			this.guiElements.put("corrThreshold", aldElement);
			paramPanel.add(aldElement.getJComponent());
			parentPanel.add(paramPanel);

			// minimal region size
			paramPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
			nameLabel = new JLabel("Minimum Region Size: ");
			nameLabel.setToolTipText("Regions smaller than given threshold on " 
				+ "the size will be ignored.");
			paramPanel.add(nameLabel);
			descr = this.op.getParameterDescriptor("minRegionSize");
			value = this.op.getParameter("minRegionSize");
			aldElement = ALDDataIOManagerSwing.getInstance().createGUIElement(
				descr.getField(),	descr.getMyclass(),	value, descr);
			aldElement.addValueChangeEventListener(this);
			this.guiElements.put("minRegionSize", aldElement);
			paramPanel.add(aldElement.getJComponent());
			parentPanel.add(paramPanel);
		} catch (ALDException exp) {
			exp.printStackTrace();
		}
	}
	
	/**
	 * Adds set of control buttons to the main panel.
	 */
	protected JPanel addCloseButtonPanel() {

		// init panel
		JPanel runPanel = new JPanel();
		runPanel.setLayout(new GridLayout(1, 1));

		// close button
		JButton quitButton = new JButton("Close");
		quitButton.setActionCommand("close");
		quitButton.addActionListener(this);

		// now set up a panel to hold the button
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.LINE_AXIS));
		controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 0));
		controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		controlPanel.add(Box.createHorizontalGlue());
		controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		controlPanel.add(quitButton);
		controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		controlPanel.add(Box.createHorizontalGlue());
		controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		runPanel.add(controlPanel);
		return runPanel;
	}

	/**
	 * Set up the help menu.
	 * 
	 * @return Generated help menu.
	 */
	protected JMenu generateHelpMenu() {
		JMenu helpM = new JMenu("Help");
		JMenuItem itemHelp = new JMenuItem("Online Help");
		itemHelp.addActionListener(OnlineHelpDisplayer.getHelpActionListener(
				itemHelp, "welcome", this));
		JMenuItem itemAbout = new JMenuItem("About MiToBo");
		itemAbout.setActionCommand("helpM_about");
		itemAbout.addActionListener(this);
		helpM.add(itemHelp);
		helpM.add(itemAbout);
		return helpM;
	}

	protected void updateGUI() throws ALDOperatorException, ALDDataIOException {
		Set<String> keys = this.guiElements.keySet();
		for (String k: keys) {
			Object value = this.op.getParameter(k);
			ALDDataIOManagerSwing.getInstance().setValue(null, value.getClass(), 
				this.guiElements.get(k), value);
		}
		this.mainPanel.updateUI();
	}
	
	/**
	 * Clean-up on termination.
	 * @return	True if window was closed.
	 */
	public boolean quit() {
		// dispose all resources, i.e. sub-windows
		this.dispose();
		return true;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		// local variables
		String command = e.getActionCommand();

		// close the frame
		if (   command.equals("frame_close")
				|| command.equals("close") 
				|| command.equals("fileM_quit")) {
			this.quit();
		}

		// handle menu item commands

		else if (command.equals("fileM_save")) {
			// open file chooser
			JFileChooser getFileDialog = new JFileChooser();
			getFileDialog.setApproveButtonText("Save");
			getFileDialog.setCurrentDirectory(new File(this.lastDirectory));
			getFileDialog.setSelectedFile(this.lastFile);
			getFileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnVal = getFileDialog.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				// check if file exists already, if so ask what to do
				File file = getFileDialog.getSelectedFile();
				if (file.exists()) {
					if ( JOptionPane.showOptionDialog(null, 
							"File " + file.getAbsolutePath() + " exists, override?",
							"file exists",
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
							null, null, null) != 0 ) {
						return;
					}
				}
				this.lastFile = file;
				try {
					ALDDataIOManagerXmlbeans.writeXml(file.getAbsolutePath(), this.op);
				} catch (Exception e1) {
					System.out.println(e1.getMessage());
					e1.printStackTrace();
				}
				this.lastDirectory = file.getAbsolutePath();
			}
		} else if (command.equals("fileM_load")) {
			// open file chooser
			JFileChooser getFileDialog = new JFileChooser();
			getFileDialog.setCurrentDirectory(new File(this.lastDirectory));
			getFileDialog.setSelectedFile(this.lastFile);
			getFileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnVal = getFileDialog.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = getFileDialog.getSelectedFile();
				this.lastDirectory = file.getAbsolutePath();
				this.lastFile = file;
				try {
					ALDOperator loadedOp = (ALDOperator)ALDDataIOManagerXmlbeans.readXml(
						file.getAbsolutePath(), ALDOperator.class);
					if (!(loadedOp instanceof ParticleDetectorUWT2D)) {
						JOptionPane.showMessageDialog(this.getFocusOwner(), 
							"This is not a configuration file for the particle detector!");
					}
					else {
						// set the parameter values
						this.op.setParameter("Jmin", loadedOp.getParameter("Jmin"));
						this.op.setParameter("Jmax", loadedOp.getParameter("Jmax"));
						this.op.setParameter("scaleIntervalSize", 
							loadedOp.getParameter("scaleIntervalSize"));
						this.op.setParameter("corrThreshold", 
							loadedOp.getParameter("corrThreshold"));
						this.op.setParameter("minRegionSize", 
							loadedOp.getParameter("minRegionSize"));
					}
					// show new parameters in GUI
					this.updateGUI();
				} catch (Exception e1) {
					System.out.println(e1.getMessage());
				}
			}
		}
		else if (command.equals("helpM_about")) {
			Object[] options = { "OK" };
			String year = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
			String rev = ALDVersionProviderFactory.getProviderInstance().getVersion();
			if (rev.contains("=")) {
				int equalSign = rev.indexOf("=");
				int closingBracket = rev.lastIndexOf("]");
				rev = rev.substring(0, equalSign + 9) + rev.substring(closingBracket);
			}
			String msg = "<html>MiToBo - A Microscope Image Analysis Toolbox, <p>" 
		    + "Release " + rev + "<p>" + "\u00a9 2010 - " + year + "   "
		    + "Martin Luther University Halle-Wittenberg<p>"
		    + "Institute of Computer Science, Faculty of Natural Sciences III<p><p>"
		    + "Email: mitobo@informatik.uni-halle.de<p>"
		    + "Internet: <i>www.informatik.uni-halle.de/mitobo</i><p>"
		    + "License: GPL 3.0, <i>http://www.gnu.org/licenses/gpl.html</i></html>";

			JOptionPane.showOptionDialog(null, new JLabel(msg),
				"Information about MiToBo", JOptionPane.DEFAULT_OPTION,
			    JOptionPane.INFORMATION_MESSAGE, MTBIcon.getInstance().getIcon(), 
			    	options, options[0]);
		}
	}

	@Override
	public void handleValueChangeEvent(ALDSwingValueChangeEvent event) {
		Set<String> keys = this.guiElements.keySet();
		for (String k: keys) {
			Class<?> cl;
      try {
	      cl = this.op.getParameter(k).getClass();
	      Object value = ALDDataIOManagerSwing.getInstance().readData(null, 
		      	cl, this.guiElements.get(k));
	      this.op.setParameter(k, value);
      } catch (ALDException e) {
	      e.printStackTrace();
      }
		}
	}
}
