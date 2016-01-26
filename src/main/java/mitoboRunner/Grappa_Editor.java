package mitoboRunner;

import java.util.Collection;

import ij.IJ;
import ij.plugin.*;
import de.unihalle.informatik.Alida.admin.annotations.ALDMetaInfo;
import de.unihalle.informatik.Alida.admin.annotations.ALDMetaInfo.ExportPolicy;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.indexing.SezPozAdapter;
import de.unihalle.informatik.Alida.gui.OnlineHelpDisplayer;
import de.unihalle.informatik.Alida.helpers.ALDClassInfo;
import de.unihalle.informatik.Alida.operator.ALDOperatorLocation;
import de.unihalle.informatik.Alida.version.ALDVersionProviderFactory;
import de.unihalle.informatik.MiToBo.core.grappa.MTBGrappaFrame;
import de.unihalle.informatik.MiToBo.core.operator.MTBVersionProviderReleaseFile;

/**
 * ImageJ plugin for running the Alida/MiToBo graphical editor "Grappa".
 * 
 * @author moeller
 */
@ALDMetaInfo(export=ExportPolicy.MANDATORY)
public class Grappa_Editor implements PlugIn {

	@Override
  public void run(String arg0) {
		// init the SezPoz adapter properly
		SezPozAdapter.initAdapter(IJ.getClassLoader());

		// configure online help to use MiToBo help set
		OnlineHelpDisplayer.initHelpset("mitobo");

		// configure version management
		ALDVersionProviderFactory.setProviderClass("de.unihalle.informatik." 
				+	"MiToBo.core.operator.MTBVersionProviderReleaseFile");
		MTBVersionProviderReleaseFile.setRevisionFile(
				"revision-mitobo-plugins.txt");

		// search for available operators
		Collection<ALDOperatorLocation> standardOps = 
				configureCollectionStandardOps();
		Collection<ALDOperatorLocation> applicationOps = 
				configureCollectionApplicationOps();

		// open editor window
		MTBGrappaFrame grappaWin = new MTBGrappaFrame(standardOps, applicationOps);
		grappaWin.setVisible(true);
	}
	
	
	/**
	 * Configure collection of operators belonging to the standard set.
	 * @return	List of standard operators.
	 */
	protected static Collection<ALDOperatorLocation> 
																						configureCollectionStandardOps() {
		return ALDClassInfo.lookupOperators(ALDAOperator.Level.STANDARD,
				ALDAOperator.ExecutionMode.SWING);
	}
	
	/**
	 * Configure collection of operators belonging to the application set.
	 * @return	List of application operators.
	 */
	protected static Collection<ALDOperatorLocation> 
																				configureCollectionApplicationOps() {
		return ALDClassInfo.lookupOperators(ALDAOperator.Level.APPLICATION,
				ALDAOperator.ExecutionMode.SWING);
	}
}
