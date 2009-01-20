/*
 Copyright 2006, 2007, 2008 Markus Goetz, Sebastian Breier
 Webpage on http://p300.eu/
 */
/*
 This file is part of p300.


 p300 is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 p300 is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with p300.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.guruz.p300.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.guruz.p300.Configuration;

public class DOMUtils {

	static DocumentBuilderFactory documentBuilderFactory;

	static {
		try {
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
		} catch (Exception e) {
			e.printStackTrace();
			
		}
	}

	public static Document documentFromInputStream(InputStream inputStream) {
		InputSource is = new InputSource(inputStream);
		return documentFromInputSource(is);			
	}
	
	/**
	 * Parse an InputSource object (which wraps around Streams & Readers) and return a DOM Document
	 * @param inputSource
	 * @return
	 */
	public static Document documentFromInputSource(InputSource inputSource) {
		try {
			inputSource.setEncoding(Configuration.getDefaultEncoding());
			Document doc = null;

			doc = parse(inputSource);

			doc.normalize();
			doc.normalizeDocument();
			return doc;

		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	static public Document documentFromByteArray(byte ba[]) {
		if ((ba == null) || (ba.length == 0)) {
			return null;
		}

		ByteArrayInputStream bais = new ByteArrayInputStream(ba);
		InputSource is = new InputSource(bais);

		Document doc = null;

		try {
			doc = parse(is);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return doc;
	}

	private static Document parse(InputSource is)
			throws SAXException, IOException, ParserConfigurationException {
		return documentBuilderFactory.newDocumentBuilder().parse(is);
	}


	public static Element getFirstNontextChild(Node d) {
		Node n = d.getFirstChild();

		while ((n != null) && (n.getNodeType() != Node.ELEMENT_NODE)) {
			n = n.getNextSibling();
		}

		return (Element) n;
	}

	/**
	 * Create a pretty String representation of a DOM Document
	 * @param node
	 * @return String of the given document
	 */
	public static String prettyPrintXMLDocument(Node node) {
		if (node == null) {
			return "";
		}
		DocumentBuilder builder = null;
		try {
			builder = getDocumentBuilder();
		} catch (ParserConfigurationException exception) {
			System.err.println(exception);
			return "";
		}
		DOMImplementation implementation = builder.getDOMImplementation();
		Object object = implementation.getFeature("LS", "3.0");
		if (!(object instanceof DOMImplementationLS)) {
			return "";
		}
		DOMImplementationLS loadSave = (DOMImplementationLS)object;
		LSSerializer serializer = loadSave.createLSSerializer();
		DOMConfiguration config = serializer.getDomConfig();
		if (config.canSetParameter("format-pretty-print", true)) {
			config.setParameter("format-pretty-print", true);
		}
		
		LSOutput loadSaveOut = loadSave.createLSOutput();
		StringWriter string = new StringWriter();
		loadSaveOut.setCharacterStream(string);
		loadSaveOut.setEncoding("UTF-8");
		serializer.write(node, loadSaveOut);
		String result = string.toString();
		
//		String result = serializer.writeToString(node);
		return result;
	}
	
	/**
	 * Return a fresh DocumentBuilder instance
	 * @return
	 * @throws ParserConfigurationException if there was a serious error
	 */
	private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder;
	}
	
	/**
	 * Return the first text child node of a given node
	 * Returns null if there is no text child node
	 * @param n
	 * @return
	 */
	public static Node getFirstTextChild(Node n) {
		NodeList children = n.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node currentChild = children.item(i);
			if (children.item(i).getNodeType() == Node.TEXT_NODE) {
				return currentChild;
			}
		}
		return null;
	}
	
	/**
	 * Return the first text child of a node that contains the given text
	 * Returns null if no such child exists
	 * @param n
	 * @param text
	 * @return
	 */
	public static Node getFirstTextChild(Node n, String text) {
		NodeList children = n.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node currentChild = children.item(i);
			String nodeValue = currentChild.getNodeValue();
			if (nodeValue != null && nodeValue.equalsIgnoreCase(text)) {
				return currentChild;
			}
		}
		return null;
	}

	/**
	 * Check if the given node has a child with the specified name
	 * Return the child of the specified name, or null if not found
	 * @param n The parent node
	 * @param namespace The namespace name
	 * @param name The node name
	 * @return
	 */
	public static Node getFirstNamedChildNS(Node n, String namespace, String name) {
		NodeList children = n.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node currentChild = children.item(i);
			String ns = currentChild.getNamespaceURI();
			String localName = currentChild.getLocalName();
			if (namespace != null && localName != null && ns.equalsIgnoreCase(namespace) && localName.equalsIgnoreCase(name)) {
				return currentChild;
			}
		}
		return null;
	}
	
	/**
	 * Check if the given node has a child with the specified name
	 * Return the child of the specified name, or null if not found
	 * @param n The parent node
	 * @param namespace The namespace name
	 * @param name The node name
	 * @return
	 */
	public static Node getFirstNamedChild(Node n, String name) {
		NodeList children = n.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node currentChild = children.item(i);
			String localName = currentChild.getNodeName();
			if (localName != null && localName.equalsIgnoreCase(name)) {
				return currentChild;
			}
		}
		return null;
	}

}
