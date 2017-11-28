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

import javax.swing.*;

import de.unihalle.informatik.Alida.operator.*;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.*;
import de.unihalle.informatik.Alida.exceptions.ALDException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.MiToBo.apps.particles2D.ParticleDetectorUWT2D;

/**
 * Frame to configure a {@link ParticleDetectorUWT2D} operator in context of 
 * {@link MTB_CellCounter}.
 * 
 * @author Birgit Moeller
 */
public class CellCounterDetectorOpConfigFramePlastidsParticlesUWT 
		extends CellCounterDetectorOpConfigFrame {

	/** 
	 * Constructs a control frame for an operator object.
	 * @param _op Operator to be associated with this frame object.
	 * @throws ALDOperatorException Thrown in case of failure.
	 */
	public CellCounterDetectorOpConfigFramePlastidsParticlesUWT(
			ParticleDetectorUWT2D _op) throws ALDOperatorException {
		super(_op);
		titleString = "Configure Plastid Detector Parameters...";		
	}
	
	/**
	 * Adds the input fields for all relevant parameters.
	 */
	@Override
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
}
