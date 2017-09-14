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

/* === Original File Header === */

/*
 * ODReadXMLODD.java
 *
 * Created on 27 November 2004, 10:47
 */

package mtb_cellcounter;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reads markers of an image from file in XML format.
 *
 * @author  kurt
 * @author Birgit Moeller
 */
public class ReadXML {
	private boolean verbose;
	private DocumentBuilderFactory dbf;
	private DocumentBuilder db;
	private Document doc;
	private String str;
	public static final int IMAGE_FILE_PATH = 0;
	public static final int CURRENT_TYPE = 1;
	/**
	 * Creates a new instance of ODReadXMLODD
	 */
	public ReadXML(String XMLFilePath) 
			throws SAXException, IOException, ParserConfigurationException {
		setVerbose(this.verbose);
		this.dbf = DocumentBuilderFactory.newInstance();
		this.db = this.dbf.newDocumentBuilder();
		this.doc = this.db.parse(new File(XMLFilePath));
		this.doc.getDocumentElement().normalize();
	}

	public String readImgProperties(int valueID){ //as URL
		switch(valueID){
		case(IMAGE_FILE_PATH):
			this.str = readSingleValue(this.doc,"Image_Filename");
		break;
		case(CURRENT_TYPE):
			this.str = readSingleValue(this.doc,"Current_Type");
		break;
		}
		if (this.str !=null){
			return this.str;
		}
		return null;
	}

	public Vector readMarkerData(){
		Vector typeVector = new Vector();

		NodeList markerTypeNodeList = getNodeListFromTag(this.doc,"Marker_Type");
		for (int i=0; i<markerTypeNodeList.getLength(); i++){
			Element markerTypeElement = getElement(markerTypeNodeList, i);
			NodeList typeNodeList = markerTypeElement.getElementsByTagName("Type");
			CellCntrMarkerVector markerVector = new CellCntrMarkerVector(Integer.parseInt(readValue(typeNodeList , 0)));
			// get color
			NodeList colorNodeList = markerTypeElement.getElementsByTagName("Color");
			Color c;
			if (colorNodeList.getLength() != 0) {
				String markerColor = readValue(colorNodeList , 0);
				String colors[] = markerColor.split(",");
				c = new Color(Integer.parseInt(colors[0]),
												Integer.parseInt(colors[1]),
													Integer.parseInt(colors[2]));
			}	
			else {
				c = CellCntrMarkerVector.createColor(i+1);
			}
			markerVector.setColor(c);
			
			NodeList markerNodeList = markerTypeElement.getElementsByTagName("Marker");
			for(int j=0; j<markerNodeList.getLength(); j++){
				Element markerElement = getElement(markerNodeList, j);
				NodeList markerXNodeList = markerElement.getElementsByTagName("MarkerX");
				NodeList markerYNodeList = markerElement.getElementsByTagName("MarkerY");
				NodeList markerZNodeList = markerElement.getElementsByTagName("MarkerZ");
				CellCntrMarker marker = new CellCntrMarker(
					Integer.parseInt(readValue(markerXNodeList,0)),
						Integer.parseInt(readValue(markerYNodeList,0)),
							Integer.parseInt(readValue(markerZNodeList,0)), null);
				markerVector.addMarker(marker);
			}
			typeVector.add(markerVector);
		}
		return typeVector;
	}

	private String readValue(NodeList nodeList, int index) throws NullPointerException{
		Element element = getElement(nodeList, index);
		debugReport("Element = "+element.getNodeName());
		NodeList elementNodeList = getChildNodes(element);
		String str = getValue(elementNodeList, 0);
		return str;
	}
	private String[] readMarker(NodeList nodeList, int index) throws NullPointerException{
		Element element = getElement(nodeList, index);
		debugReport("Element = "+element.getNodeName());
		NodeList elementNodeList = getChildNodes(element);
		String str[] = {getValue(elementNodeList, 0),getValue(elementNodeList, 1),getValue(elementNodeList, 2)};
		return str;
	}
	private String readSingleValue(Document doc, String elementName){
		NodeList nodeList = getNodeListFromTag(doc,elementName);
		Element element = getElement(nodeList, 0);
		nodeList = getChildNodes(element);
		String str = getValue(nodeList, 0);
		return str;
	}
	private NodeList getNodeListFromTag(Document doc, String elementName){
		NodeList nodeList = doc.getElementsByTagName(elementName);
		return nodeList;
	}
	private NodeList getChildNodes(Element element){
		NodeList nodeList = element.getChildNodes();
		return nodeList;
	}
	private Element getElement(NodeList nodeList, int index){
		Element element = (Element)nodeList.item(index);
		return element;
	}
	private String getValue(NodeList nodeList, int index){
		String str = ((Node)nodeList.item(index)).getNodeValue().trim();
		return str;
	}


	public void debugReport(String report){
		if (this.verbose)
			System.out.println(report);
	}
	public void setVerbose(boolean verbose){
		this.verbose = verbose;
	}
	public boolean isVerbose(){
		return this.verbose;
	}
}
