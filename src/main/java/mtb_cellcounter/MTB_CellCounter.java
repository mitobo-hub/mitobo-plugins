/* IMPORTANT NOTICE:
 * This file has originally been part of the Cell_Counter plugin written by
 * Kurt De Vos, see http://rsb.info.nih.gov/ij/plugins/cell-counter.html.
 * We extended the plugin functionality to fit to the specific needs of MiToBo. 
 * You can find the original license and file header below following the 
 * MiToBo license header.
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

/* === Original File Header below === */

/*
 * Cell_Counter.java
 *
 * Created on December 27, 2005, 4:56 PM
 *
 */
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

import java.util.Vector;

import loci.common.StatusEvent;
import loci.common.StatusListener;
import de.unihalle.informatik.Alida.annotations.indexing.SezPozAdapter;
import de.unihalle.informatik.Alida.version.ALDVersionProvider;
import de.unihalle.informatik.Alida.version.ALDVersionProviderFactory;
import de.unihalle.informatik.MiToBo.core.operator.MTBVersionProviderReleaseFile;
import ij.plugin.frame.PlugInFrame;
import ij.IJ;

/**
 * Main class of the plugin called from ImageJ.
 * 
 * @author Kurt De Vos
 * @author Birgit Moeller
 */
public class MTB_CellCounter extends PlugInFrame implements StatusListener {
    
	/** 
	 * Vector of installed {@link StatusListener} objects.
	 */
	protected Vector<StatusListener> m_statusListeners;

	/**
	 * Reference to actual cell counter object.
	 */
	protected CellCounter cellCounter;
	
	/** 
	 * Creates a new instance of MTB_CellCounter. 
	 */
	public MTB_CellCounter() {
		super("MTB Cell Counter");
		this.m_statusListeners = new Vector<StatusListener>();
		// init the SezPoz adapter properly
		SezPozAdapter.initAdapter(IJ.getClassLoader());
		// init version provider, but maybe overwritten by environment
		ALDVersionProviderFactory.setProviderClass("de.unihalle.informatik." 
				+	"MiToBo.core.operator.MTBVersionProviderReleaseFile");
		// specify revision file name, if revision file provider is in use
		ALDVersionProvider prov = 
				ALDVersionProviderFactory.getProviderInstance();
		if (prov instanceof MTBVersionProviderReleaseFile) {
			MTBVersionProviderReleaseFile.setRevisionFile(
					"revision-mitobo-plugins.txt");
		}
		// run the cell counter
		this.cellCounter = new CellCounter();
	}

	@Override
  public void run(String arg){
		// register plugin as listener for events
		this.cellCounter.addStatusListener(this);
	}

	/*
	 * Status listener interface.
	 */
	
	@Override
	public void statusUpdated(StatusEvent e) {
		IJ.showStatus("MTB_CellCounter: " + e.getStatusMessage());
		IJ.showProgress(e.getProgressValue(), e.getProgressMaximum());
	}

}
