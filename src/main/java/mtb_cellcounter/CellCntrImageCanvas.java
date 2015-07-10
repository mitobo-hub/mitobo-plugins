/* IMPORTANT NOTICE:
 * This file has originally been part of the Cell_Counter plugin written by
 * Kurt De Vos, see http://rsb.info.nih.gov/ij/plugins/cell-counter.html.
 * We extended the plugin functionality to fit to the specific needs of MiToBo. 
 * You can find the original license and file header below following the 
 * MiToBo license header.
 */

/*
 * Copyright (C) 2010 - @YEAR@  by the MiToBo development team
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
 * CellCntrImageCanvas.java
 *
 * Created on November 22, 2005, 5:58 PM
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

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.process.ImageProcessor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import de.unihalle.informatik.MiToBo.core.datatypes.MTBBorder2D;

/**
 * @author Kurt De Vos
 * @author Birgit Moeller
 */
public class CellCntrImageCanvas extends ImageCanvas 
	implements KeyListener {
		
	/**
	 * Step size for keyboard scrolling.
	 */
	private static final int SCROLL_STEP = 10; 
	/**
	 * Maximal distance of first and last point of freehand selection polygon.
	 */
	private static final int CLOSED_POLY_DIST = 20; 

	/**
	 * Size of cursor image.
	 * <p>
	 * Windows only supports cursors of size 32x32!
	 */
	private static final int cursorImageSize = 32;

	private Vector<CellCntrMarkerVector> typeVector;
	private CellCntrMarkerVector currentMarkerVector;
	private CellCounter cc;
	private ImagePlus img;
	private boolean showMarkers = true;
	private boolean showNumbers = true;
	private boolean showAll = false;
	private boolean showBorders = false;
	private boolean editsAllowed = true;
	private Font font = new Font("SansSerif", Font.PLAIN, 10);

	private String osName = System.getProperty("os.name");
	
	/**
	 * List of points while selecting region where to delete markers.
	 */
	private Vector<Point2D> dragList;
	/**
	 * Flag indicating if drag mode is active or not.
	 */
	private boolean dragMode = false;

	/**
	 * Status bar to display current settings in image window.
	 */
	private Panel statusBar;
	private Label markerColor = new Label("   ");
	private JLabel markerID = 	new JLabel(" Marker-ID: ");
	private JLabel markerType = new JLabel("     ");
	private JLabel lContours = new JLabel("  Contours:");
	private JCheckBox bContours = new JCheckBox();
	private JLabel lNumbers = new JLabel("  Numbers:");
	private JCheckBox bNumbers = new JCheckBox();
	private JLabel lMarkers = new JLabel("  Markers:");
	private JCheckBox bMarkers = new JCheckBox();
	
	/** Creates a new instance of CellCntrImageCanvas */
	public CellCntrImageCanvas(ImagePlus img, Vector<CellCntrMarkerVector> typeVector, CellCounter cc, Vector displayList) {
		super(img);
		this.img=img;
		this.typeVector = typeVector;
		this.cc = cc;
		if (displayList!=null)
			this.setDisplayList(displayList);
	}

	public void setImage(ImagePlus newImg, Vector newDisplayList) {
		this.img = newImg;
		this.imp = newImg;
		this.setDisplayList(newDisplayList);
	}

	/**
	 * Initialize the key handler on the current image.
	 */
	public void initKeyHandler() {
		// add key listener
		ImageWindow win = this.img.getWindow();
		if (win == null)
			return;
		// remove IJ default key listener, add ours to window and canvas
		win.removeKeyListener(IJ.getInstance());
		win.addKeyListener(this);
		this.removeKeyListener(IJ.getInstance());
		this.addKeyListener(this);
	}

	public void addStatusBar() {
		// if there is no window, no need for a status bar
		ImageWindow win = this.img.getWindow(); 
		if (win == null)
			return;
		
		this.bContours.setEnabled(false);
		this.bMarkers.setEnabled(false);
		this.bNumbers.setEnabled(false);
		this.statusBar = new Panel();
		JLabel status = new JLabel("Status - ");
		this.statusBar.add(status);
		this.statusBar.add(this.markerID);
		this.statusBar.add(this.markerType);
		this.statusBar.add(this.markerColor);
		this.statusBar.add(this.lContours);
		this.statusBar.add(this.bContours);
		this.statusBar.add(this.lNumbers);
		this.statusBar.add(this.bNumbers);
		this.statusBar.add(this.lMarkers);
		this.statusBar.add(this.bMarkers);
		this.updateStatusBar();
		win.add(this.statusBar);
		win.pack();
		win.repaint();
	}
	
	public void updateStatusBar() {
		int currentMarkerType = -1;
		Color currentMarkerColor = null;
		if (this.currentMarkerVector != null) {
			currentMarkerType = this.currentMarkerVector.getType();
			currentMarkerColor = this.currentMarkerVector.getColor();
			this.markerColor.setBackground(currentMarkerColor);
		}
		this.markerType.setText(" " + currentMarkerType + " ");
		if (this.showBorders)
			this.bContours.setSelected(true);
		else
			this.bContours.setSelected(false);
		if (this.showNumbers) 
			this.bNumbers.setSelected(true);
		else
			this.bNumbers.setSelected(false);
		if (this.showMarkers) 
			this.bMarkers.setSelected(true);
		else
			this.bMarkers.setSelected(false);

		// repaint window
		this.img.getWindow().pack();
		this.img.getWindow().repaint();
	}

	public void closeImageCanvas() {
		if (this.img.getWindow() != null)
			this.img.getWindow().removeKeyListener(this);
		this.removeKeyListener(this);
	}

	@Override
  public void mousePressed(MouseEvent e) {
		if (   IJ.spaceBarDown() || Toolbar.getToolId()==Toolbar.MAGNIFIER 
				|| Toolbar.getToolId()==Toolbar.HAND) {
			super.mousePressed(e);
			return;
		}

		// get mouse position
		int x = super.offScreenX(e.getX());
		int y = super.offScreenY(e.getY());
		
		// if shift key is down and we are in drag mode, save position from now on
		if (IJ.shiftKeyDown()) {
			if (!this.dragMode && this.editsAllowed) {
				this.dragList = new Vector<Point2D>();
				this.dragList.add(new Point2D.Double(x,y));
				this.dragMode = true;
			}
			return;
		}

		if (this.currentMarkerVector==null){
			IJ.error("Select a counter type first!");
			return;
		}

		if (!this.editsAllowed) {
			IJ.error("Please select your markers before editing!");
			return;
		}
		
		// left mouse button pressed -> add marker
		if (e.getButton() == 1 && !this.dragMode){
			// relabel mode
			if (IJ.controlKeyDown()) {
				Point mousePos = new Point(x,y);
				double dist;
				double markerDist = Double.MAX_VALUE;
				CellCntrMarker targetMarker = null;
				CellCntrMarkerVector targetVector = null;
				// search marker closest to mouse position
				for (CellCntrMarkerVector v : this.typeVector) {
					if (v == this.currentMarkerVector)
						continue;
					if (v.size() > 0) {
						CellCntrMarker m = v.getMarkerFromPosition(
							mousePos, this.img.getCurrentSlice());
						dist = mousePos.distance(m.getX(), m.getY());
						if (dist < markerDist) {
							markerDist = dist;
							targetMarker = m;
							targetVector = v;
						}
					}
				}
				// add new marker to current list (if found, otherwise skip)
				if (targetMarker != null && targetVector != null) {
					CellCntrMarker m = new CellCntrMarker(
							targetMarker.getX(), targetMarker.getY(), 
								this.img.getCurrentSlice());
					this.currentMarkerVector.addMarker(m);				
					// remove old marker from its list
					targetVector.removeMarker(
							targetVector.getVectorIndex(targetMarker));
				}
			}
			// add mode
			else {
				CellCntrMarker m = new CellCntrMarker(x, y, this.img.getCurrentSlice());
				this.currentMarkerVector.addMarker(m);				
			}
		}
		// right mouse button pressed -> delete marker
		else if (e.getButton() == 3 && !this.dragMode) {
			if (this.currentMarkerVector.size() > 0) {
				CellCntrMarker m = this.currentMarkerVector.getMarkerFromPosition(
						new Point(x,y) ,this.img.getCurrentSlice());
				this.currentMarkerVector.removeMarker(
						this.currentMarkerVector.getVectorIndex(m));
			}
		}
		repaint();
		this.cc.populateTxtFields();
	}
	
	@Override
  public void mouseReleased(MouseEvent e) {
		if (this.dragMode) {
			
			// check if a closed polygon was drawn, if not, just cancel
			Point2D first = this.dragList.get(0);
			Point2D last = this.dragList.get(this.dragList.size()-1);
			if (first.distance(last) < CellCntrImageCanvas.CLOSED_POLY_DIST) {
				int [] xpoints = new int[this.dragList.size()];
				int [] ypoints = new int[this.dragList.size()];
				for (int i=0;i<this.dragList.size();++i) {
					xpoints[i] = (int)this.dragList.elementAt(i).getX();
					ypoints[i] = (int)this.dragList.elementAt(i).getY();
				}
				
				// find all markers of active type located inside the polygon
				Polygon poly = new Polygon(xpoints, ypoints, this.dragList.size());
				CellCntrMarker m;
				Vector<CellCntrMarker> toDelete = new Vector<CellCntrMarker>();
				for (int i=0;i<this.currentMarkerVector.size();++i) {
					m = this.currentMarkerVector.get(i);
					if (poly.contains(m.getX(), m.getY()))
						toDelete.add(m);
				}
				// delete markers inside polygon
				for (CellCntrMarker d: toDelete)
					this.currentMarkerVector.removeMarker(
							this.currentMarkerVector.getVectorIndex(d));
			}
			// reset the drag list and disable drag mode
			this.dragList = null;
			this.dragMode = false;
			repaint();
			this.cc.populateTxtFields();
		}
		else {
			super.mouseReleased(e);
		}
	}
	
	@Override
  public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);
	}
	
	@Override
  public void mouseExited(MouseEvent e) {
		super.mouseExited(e);
	}
	
	@Override
  public void mouseEntered(MouseEvent e) {
		super.mouseEntered(e);
		if (   !IJ.spaceBarDown() || Toolbar.getToolId()!=Toolbar.MAGNIFIER 
				||  Toolbar.getToolId()!=Toolbar.HAND)
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}
	
	@Override
  public void mouseDragged(MouseEvent e) {
		// add current position to drag list
		if (this.dragMode) {
			int x = super.offScreenX(e.getX());
			int y = super.offScreenY(e.getY());
			this.dragList.add(new Point2D.Double(x,y));
			repaint();
		}
		else {
			super.mouseDragged(e);
		}
	}
	
	@Override
  public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		Rectangle clip;
		int newx, newy;

		int keyCode = e.getKeyCode();
		switch(keyCode)
		{
		// codes 49 to 57 switch the marker type
		case 49: // 1
		case 50: // 2
		case 51: // 3
		case 52: // 4
		case 53: // 5
		case 54: // 6
		case 55: // 7
		case 56: // 8
		case 57: // 9
			CellCounter.setType(keyCode-48);
			break;
		case 37:
		case 65: // a = scroll left
			clip = getSrcRect();
			newx = clip.x - SCROLL_STEP;
			newy = clip.y;
			if (newx<0) 
				newx = 0;
			if ((newx+this.srcRect.width)>this.imageWidth) 
				newx = this.imageWidth-this.srcRect.width;
			this.srcRect.x = newx;
			this.srcRect.y = newy;
			this.imp.draw();
			break;
		case 39:
		case 68: // d = scroll right
			clip = getSrcRect();
			newx = clip.x + SCROLL_STEP;
			newy = clip.y;
			if (newx<0) 
				newx = 0;
			if ((newx+this.srcRect.width)>this.imageWidth) 
				newx = this.imageWidth-this.srcRect.width;
			this.srcRect.x = newx;
			this.srcRect.y = newy;
			this.imp.draw();
			break;
		case 69: // e = zoom out
			e.setKeyCode(45);
			e.setKeyChar('-');
			IJ.getInstance().keyPressed(e);
			break;
		case 81: // q = zoom in
			e.setKeyCode(521);
			e.setKeyChar('+');
			IJ.getInstance().keyPressed(e);
			break;
		case 40:
		case 83: // s = scroll down
			clip = getSrcRect();
			newx = clip.x;
			newy = clip.y + SCROLL_STEP;
			if (newy<0) 
				newy = 0;
			if ((newy+this.srcRect.height)>this.imageHeight) 
				newy = this.imageHeight-this.srcRect.height;
			this.srcRect.x = newx;
			this.srcRect.y = newy;
			this.imp.draw();
			break;
		case 86: // v
			this.cc.switchContours();
			break;
		case 38:
		case 87: // w = scroll up
			clip = getSrcRect();
			newx = clip.x;
			newy = clip.y - SCROLL_STEP;
			if (newy<0) 
				newy = 0;
			if ((newy+this.srcRect.height)>this.imageHeight) 
				newy = this.imageHeight-this.srcRect.height;
			this.srcRect.x = newx;
			this.srcRect.y = newy;
			this.imp.draw();
			break;
		case 88: // x
			this.cc.switchNumbers();
			break;      	
		case 89: // y
			this.cc.switchMarkers();
			break;
		case 67: // c
		default:
			IJ.getInstance().keyPressed(e);
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		IJ.getInstance().keyReleased(e);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		IJ.getInstance().keyTyped(e);
	}

	@Override
  public void paint(Graphics g){
		super.paint(g);
		this.srcRect = getSrcRect();
		Roi roi = this.img.getRoi();
		double xM=0;
		double yM=0;

		/*
        double magnification = super.getMagnification();

        try {
            if (imageUpdated) {
                imageUpdated = false;
                img.updateImage();
            }
            Image image = img.getImage();
            if (image!=null)
                g.drawImage(image, 0, 0, (int)(srcRect.width*magnification),
                        (int)(srcRect.height*magnification),
                        srcRect.x, srcRect.y, srcRect.x+srcRect.width,
                        srcRect.y+srcRect.height, null);
            if (roi != null)
                roi.draw(g);
        } catch(OutOfMemoryError e) {
            IJ.outOfMemory("Paint "+e.getMessage());
        }
		 */

		Graphics2D g2 = (Graphics2D)g;
		g2.setStroke(new BasicStroke(1f));
		g2.setFont(this.font);

		int i=0;
		ListIterator<CellCntrMarkerVector> it = this.typeVector.listIterator();
		while(it.hasNext()){
			CellCntrMarkerVector mv = it.next();

			int typeID = mv.getType();
			Color defColor = mv.getColor();
			ListIterator mit = mv.listIterator();
			
			CellCntrPresegmentationResult regs = mv.getSegmentationData();
			
			Vector<Boolean> mask = null;
			if (regs != null)
				mask = regs.getActivityArray();
			//            i=0;
			//            while(mit.hasNext()){
				//                CellCntrMarker m = (CellCntrMarker)mit.next();
			for (i=0;i<mv.size();++i) {
				CellCntrMarker m = mv.elementAt(i);

				// check if marker is active
				if (mask != null	&& i < mask.size() && !mask.get(i).booleanValue()) {
					g2.setColor(Color.LIGHT_GRAY);
				}
				else {
					g2.setColor(defColor);
				}

				boolean sameSlice = m.getZ()==this.img.getCurrentSlice();
				if (sameSlice || this.showAll){
					xM = ((m.getX()-this.srcRect.x)*this.magnification);
					yM = ((m.getY()-this.srcRect.y)*this.magnification);
					// draw markers
					if (this.showMarkers) {
						if (sameSlice)
							g2.fillOval((int)xM-2, (int)yM-2,4,4);
						else
							g2.drawOval((int)xM-2, (int)yM-2,4,4);
					}
					// draw numbers
					if (this.showNumbers)
						g2.drawString(Integer.toString(typeID), (int)xM+3, (int)yM-3);
				}
			}

			// draw contours, if requested
			if (   this.showBorders 
					&& regs != null && regs.getBorders() != null) {
				MTBBorder2D border;
				for (i=0; i< regs.getBorders().size(); ++i) {
					if (mask != null	&& !mask.get(i).booleanValue()) {
						g2.setColor(Color.LIGHT_GRAY);
					}
					else {
						g2.setColor(defColor);
					}
					border = regs.getBorders().elementAt(i);
					for (int j=0; j<border.getPointNum(); ++j) {
						Point2D.Double p = border.getPointAt(j);
						xM = ((p.getX()-this.srcRect.x)*this.magnification);
						yM = ((p.getY()-this.srcRect.y)*this.magnification);
						g2.drawOval((int)xM, (int)yM,0,0);
					}
				}
			}
		}
		// draw selection rectangle
		if (this.dragList != null && this.dragList.size() > 0) {
			g2.setColor(this.currentMarkerVector.getColor());
			Point2D prev = this.dragList.firstElement();
			for (int j=1; j<this.dragList.size(); ++j) {
				double xP = ((prev.getX()-this.srcRect.x)*this.magnification);
				double yP = ((prev.getY()-this.srcRect.y)*this.magnification);
				Point2D next = this.dragList.elementAt(j);
				double xN = ((next.getX()-this.srcRect.x)*this.magnification);
				double yN = ((next.getY()-this.srcRect.y)*this.magnification);
				g2.drawLine((int)xP, (int)yP, (int)xN, (int)yN);
				prev = next;
			}
		}
	}

	public void removeLastMarker(){
		// no markers set so far
		if (this.currentMarkerVector == null)
			return;
		this.currentMarkerVector.removeLastMarker();
		repaint();
		this.cc.populateTxtFields();
	}
	public ImagePlus imageWithMarkers(){
		Image image = this.createImage(this.img.getWidth(),this.img.getHeight());
		Graphics gr = image.getGraphics();

		double xM=0;
		double yM=0;

		try {
			if (this.imageUpdated) {
				this.imageUpdated = false;
				this.img.updateImage();
			}
			Image image2 = this.img.getImage();
			if (image!=null)
				gr.drawImage(image2, 0, 0, this.img.getWidth(),this.img.getHeight(),null);
		} catch(OutOfMemoryError e) {
			IJ.outOfMemory("Paint "+e.getMessage());
		}

		Graphics2D g2r = (Graphics2D)gr;
		g2r.setStroke(new BasicStroke(1f));

		ListIterator<CellCntrMarkerVector> it = this.typeVector.listIterator();
		while(it.hasNext()){
			CellCntrMarkerVector mv = it.next();
			int typeID = mv.getType();
			g2r.setColor(mv.getColor());
			ListIterator mit = mv.listIterator();
			while(mit.hasNext()){
				CellCntrMarker m = (CellCntrMarker)mit.next();
				if (m.getZ()==this.img.getCurrentSlice()){
					xM = m.getX();
					yM = m.getY();
					g2r.fillOval((int)xM-2, (int)yM-2,4,4);
					if (this.showNumbers)
						g2r.drawString(Integer.toString(typeID), (int)xM+3, (int)yM-3);
				}
			}
		}

		Vector displayList = getDisplayList();
		if (displayList!=null && displayList.size()==1) {
			Roi roi = (Roi)displayList.elementAt(0);
			if (roi.getType()==Roi.COMPOSITE)
				roi.draw(gr);
		}

		return new ImagePlus("Markers_"+this.img.getTitle(),image);
	}

	public void measure(){
		IJ.setColumnHeadings("Type\tSlice\tX\tY\tValue");
		for (int i=1; i<=this.img.getStackSize(); i++){
			this.img.setSlice(i);
			ImageProcessor ip = this.img.getProcessor();

			ListIterator<CellCntrMarkerVector> it = this.typeVector.listIterator();
			while(it.hasNext()){
				CellCntrMarkerVector mv = it.next();
				int typeID = mv.getType();
				ListIterator mit = mv.listIterator();
				while(mit.hasNext()){
					CellCntrMarker m = (CellCntrMarker)mit.next();
					if (m.getZ()==i){
						int xM = m.getX();
						int yM = m.getY();
						int zM = m.getZ();
						double value = ip.getPixelValue(xM,yM);
						IJ.write(typeID+"\t"+zM+"\t"+xM+"\t"+yM+"\t"+value);
					}
				}
			}
		}
	}

	public Vector getTypeVector() {
		return this.typeVector;
	}

	public void setTypeVector(Vector typeVector) {
		this.typeVector = typeVector;
	}

	public CellCntrMarkerVector getCurrentMarkerVector() {
		return this.currentMarkerVector;
	}

	public void setCurrentMarkerVector(CellCntrMarkerVector currentMarkerVector) {
		this.currentMarkerVector = currentMarkerVector;
		this.updateStatusBar();
		this.updateCursor();
	}

	/**
	 * Query flag for showing numbers.
	 * @return	True, if numbers are to be shown.
	 */
	public boolean isShowNumbers() {
		return this.showNumbers;
	}

	/**
	 * Enable/disable markers.
	 * @return	_showMarkers	If true, markers are drawn.
	 */
	public void setShowMarkers(boolean _showMarkers) {
		this.showMarkers = _showMarkers;
		this.updateStatusBar();
	}

	/**
	 * Enable/disable numbers.
	 * @return	_showNumbers	If true, numbers are drawn.
	 */
	public void setShowNumbers(boolean _showNumbers) {
		this.showNumbers = _showNumbers;
		this.updateStatusBar();
	}

	public void setShowAll(boolean _showAll) {
		this.showAll = _showAll;
	}

	/**
	 * Turns on or off display of borders.
	 * @param _flag	If true, borders are displayed.
	 */
	 public void setShowBorders(boolean _flag) {
		 this.showBorders = _flag;
		 this.updateStatusBar();
	 }

	 /**
	  * Enables or disables marker edit mode.
	  * @param _flag		If false, no markers can be set or deleted.
	  */
	 public void setEditable(boolean _flag) {
		 this.editsAllowed = _flag;
	 }

	 /**
	  * Updates the cursor color on changes of marker type.
	  */
	 protected void updateCursor() {
		 
		 if (this.currentMarkerVector == null) {
			 // reset crosshair cursor to default
			 crosshairCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
			 return;
		 }
 
		 Toolkit toolkit = Toolkit.getDefaultToolkit();
		 Color targetColor = this.currentMarkerVector.getColor();
		 // allocate am image with transparency
		 BufferedImage im = new BufferedImage(cursorImageSize, cursorImageSize, 
				BufferedImage.TYPE_INT_ARGB);
		 for (int y = 0; y < cursorImageSize; y++) {
			 for (int x = 0; x < cursorImageSize; x++) {
				 if (   ((x == 15 || x == 16) && ((y<14 && y>7) || (y>17 && y<24))) 
						 || ((y == 15 || y == 16) && ((x<14 && x>7) || (x>17 && x<24)))) {
					 im.setRGB(x, y, targetColor.getRGB());
				 }
			 }
		 }
		 // set the new cursor
		 crosshairCursor = toolkit.createCustomCursor(
				im, new Point(cursorImageSize/2,cursorImageSize/2), "colorCrosshair");
	 }
	 
	 /**
	  * Renders non-crosshair parts of cursor image transparent.
	  * <p>
	  * Code adapted from: {@link 
	  * http://www.java2s.com/Code/Java/2D-Graphics-GUI/MakeimageTransparency.htm}
	  * 
	  * @param im				Image to modify.
	  * @param color		Color to render transparent
	  * @return		Image with target color rendered transparent.
	  */
	 protected Image makeColorTransparent(Image im, final Color color) {
		 ImageFilter filter = new RGBImageFilter() {
			 // the color we are looking for... Alpha bits are set to opaque
			 public int markerRGB = color.getRGB() | 0xFF000000;

			 @Override
			 public final int filterRGB(int x, int y, int rgb) {
				 if ((rgb | 0xFF000000) == markerRGB) {
					 // Mark the alpha bits as zero - transparent
					 return 0x00FFFFFF & rgb;
				 } else {
					 // nothing to do
					 return rgb;
				 }
			 }
		 };
		 ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
		 return Toolkit.getDefaultToolkit().createImage(ip);
	 }
}
