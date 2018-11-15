/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ogema.recordeddata.slotsdb;

//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.text.FieldPosition;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.NamedNodeMap;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//import org.xml.sax.SAXException;

/**
 * @deprecated this utility class does not seem to be used any more 
 */
// TODO remove
@Deprecated
public class XMLUtils {

//	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
//
//	public static Document getDocumentFromFile(File xmlFile) {
//		Document doc = null;
//		DocumentBuilderFactory docBFac;
//		DocumentBuilder docBuild;
//
//		docBFac = DocumentBuilderFactory.newInstance();
//		docBFac.setIgnoringComments(true);
//
//		try {
//			docBuild = docBFac.newDocumentBuilder();
//			doc = docBuild.parse(xmlFile);
//		} catch (ParserConfigurationException e) {
//			e.printStackTrace();
//		} catch (SAXException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		return doc;
//	}
//
//	public static Document getDocumentFromStream(InputStream xmlStream) {
//		Document doc = null;
//		DocumentBuilderFactory docBFac;
//		DocumentBuilder docBuild;
//
//		docBFac = DocumentBuilderFactory.newInstance();
//		docBFac.setIgnoringComments(true);
//
//		try {
//			docBuild = docBFac.newDocumentBuilder();
//			doc = docBuild.parse(xmlStream);
//		} catch (ParserConfigurationException e) {
//			e.printStackTrace();
//		} catch (SAXException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		return doc;
//	}
//
//	public static void updateTagWithValue(Element parent, Document document, String tag, String value) {
//		if (value != null) {
//			Node node = hasTagAsChild(parent, tag);
//
//			if (node == null) {
//				addTagWithValue(parent, document, tag, value);
//			}
//			else {
//				node.setTextContent(value);
//			}
//		}
//	}
//
//	public static void addTagWithValue(Element parent, Document document, String tag, String value) {
//		Element child = document.createElement(tag);
//		child.setTextContent(value);
//		parent.appendChild(child);
//	}
//
//	public static Node hasTagAsChild(Element parent, String tag) {
//		Node node = null;
//
//		NodeList nodeList = parent.getElementsByTagName(tag);
//		if (nodeList != null) {
//			node = nodeList.item(0);
//		}
//
//		return node;
//	}
//
//	public static Node hasTagAsChildWithAttributeValue(Element parent, String tag, String attribute, String value) {
//		Node node = null;
//
//		NodeList nodeList = parent.getElementsByTagName(tag);
//		if (nodeList != null) {
//			for (int i = 0; i < nodeList.getLength(); i++) {
//				node = nodeList.item(i);
//
//				if (hasAttributeValue(node, attribute, value)) {
//					return node;
//				}
//			}
//
//		}
//
//		return null;
//	}
//
//	/**
//	 * Delete an element with a given attribute value from an XML DOM
//	 * 
//	 * @param doc
//	 *            the XML document
//	 * @param elementName
//	 *            the name of the element to delete
//	 * @param attributeName
//	 *            the name of the elements attribute
//	 * @param value
//	 *            the attribute value
//	 */
//	public static void deleteElementWithAttributeValue(Document doc, String elementName, String attributeName,
//			String value) {
//		NodeList nodes = doc.getElementsByTagName(elementName);
//
//		for (int i = 0; i < nodes.getLength(); i++) {
//			Node node = nodes.item(i);
//
//			if (hasAttributeValue(node, attributeName, value)) {
//				Node parent = node.getParentNode();
//				parent.removeChild(node);
//				break;
//			}
//		}
//	}
//
//	/**
//	 * Delete an element with a given subelement text value form an XML DOM
//	 * 
//	 * @param doc
//	 *            the XML document
//	 * @param elementName
//	 *            the name of the element to delete
//	 * @param subElementName
//	 *            he name of the elements subelement
//	 * @param value
//	 *            the value of the sub element
//	 */
//	public static void deleteElementWithSubElementValue(Document doc, String elementName, String subElementName,
//			String value) {
//		NodeList elements = doc.getElementsByTagName(elementName);
//
//		for (int i = 0; i < elements.getLength(); i++) {
//			Node element = elements.item(i);
//
//			NodeList subElements = element.getChildNodes();
//
//			for (int j = 0; j < subElements.getLength(); j++) {
//				Node subElement = subElements.item(j);
//
//				if (subElement.getTextContent().equals(value)) {
//					Node parent = element.getParentNode();
//					parent.removeChild(element);
//					return;
//				}
//			}
//		}
//	}
//
//	/**
//	 * Test if the given element has the given attribute and attribute value
//	 * 
//	 * @param element
//	 *            element to test
//	 * @param attributeName
//	 *            attribute name
//	 * @param attributeValue
//	 *            attribute value
//	 */
//	public static boolean hasAttributeValue(Node element, String attributeName, String attributeValue) {
//		NamedNodeMap attributes = element.getAttributes();
//
//		Node nameAttribute = attributes.getNamedItem(attributeName);
//
//		if (nameAttribute != null) {
//			String content = nameAttribute.getTextContent();
//
//			if ((content != null) && (content.equals(attributeValue))) {
//				return true;
//			}
//		}
//
//		return false;
//	}
//
//	public static void updateAttributeValue(Node element, String attributeName, String attributeValue) {
//		NamedNodeMap attributes = element.getAttributes();
//
//		Node nameAttribute = attributes.getNamedItem(attributeName);
//
//		if (nameAttribute != null) {
//			nameAttribute.setTextContent(attributeValue);
//		}
//	}
//
//	public static String getAttributeValue(Node element, String attributeName) {
//		NamedNodeMap attributes = element.getAttributes();
//
//		Node nameAttribute = attributes.getNamedItem(attributeName);
//
//		if (nameAttribute != null) {
//			return nameAttribute.getTextContent();
//		}
//		else {
//			return null;
//		}
//	}
//
//	public static String dateToString(Date date) {
//		StringBuffer retBuffer = dateFormat.format(date, new StringBuffer(), new FieldPosition(1));
//
//		return retBuffer.toString();
//	}
//
//	public static long timeToMilliSec(String time) throws NumberFormatException {
//		if ((time.length() > 1) && (time.endsWith("ms"))) {
//			return Long.parseLong(time.substring(0, time.length() - 2));
//		}
//		else {
//			long msec;
//
//			if (Character.isDigit(time.charAt(time.length() - 1))) {
//				msec = Long.parseLong(time);
//			}
//			else {
//				msec = Long.parseLong(time.substring(0, time.length() - 1));
//			}
//
//			switch (time.charAt(time.length() - 1)) {
//			case 's':
//				msec *= 1000;
//				break;
//			case 'm':
//				msec *= 60 * 1000;
//				break;
//			case 'h':
//				msec *= 60 * 60 * 1000;
//				break;
//			case 'd':
//				msec *= 60 * 60 * 24 * 1000;
//				break;
//			case 'w':
//				msec *= 60 * 60 * 24 * 7 * 1000;
//				break;
//			case 'M':
//				msec *= 60 * 60 * 24 * 30 * 1000;
//				break;
//			case 'y':
//				msec *= 60 * 60 * 24 * 365 * 1000;
//				break;
//			default:
//				/* default no change */
//			}
//			return msec;
//		}
//
//	}
}
