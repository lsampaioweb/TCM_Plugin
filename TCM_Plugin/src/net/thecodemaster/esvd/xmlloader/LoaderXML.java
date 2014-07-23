package net.thecodemaster.esvd.xmlloader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.thecodemaster.esvd.Activator;
import net.thecodemaster.esvd.helper.Creator;
import net.thecodemaster.esvd.logger.PluginLogger;
import net.thecodemaster.esvd.ui.l10n.Message;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Luciano Sampaio
 */
public abstract class LoaderXML {

	protected Object load() {
		String file = getFilePath();
		if (fileExists(file)) {
			return load(file);
		} else {
			PluginLogger.logError(new FileNotFoundException(String.format(Message.Error.FILE_NOT_FOUND, file)));
		}

		return Creator.newList();
	}

	protected abstract String getFilePath();

	protected abstract Object load(String file);

	protected boolean fileExists(String file) {
		return (getInputStream(file) != null);
	}

	protected Document getDocument(String file) {
		Document document = null;
		Exception realException = null;
		String errorMessage = null;
		try {
			// Create a DocumentBuilderFactory
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			// It is the DocumentBuilder instance that is the DOM parser.
			// Using this DOM parser you can parse XML files into DOM objects
			DocumentBuilder db = dbf.newDocumentBuilder();

			// Parse the input file to get a Document object
			document = db.parse(getInputStream(file));

			// Puts all Text nodes in the full depth of the sub-tree underneath this Node.
			document.getDocumentElement().normalize();
		} catch (ParserConfigurationException e) {
			realException = e;
			errorMessage = Message.Error.FILE_XML_PARSING_FAIL;
		} catch (SAXException e) {
			realException = e;
			errorMessage = Message.Error.FILE_XML_PARSING_FAIL;
		} catch (IOException e) {
			realException = e;
			errorMessage = Message.Error.FILE_XML_READING_FAIL;
		}

		if (errorMessage != null) {
			PluginLogger.logError(String.format(errorMessage, file), realException);
		}

		return document;
	}

	private InputStream getInputStream(String file) {
		try {
			Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
			Path path = new Path(file);
			URL fileURL = FileLocator.find(bundle, path, null);
			if (null != fileURL) {
				return fileURL.openStream();
			}
		} catch (IOException e) {
			PluginLogger.logError(e);
		}

		return null;
	}

	protected String getAttributeValueFromElement(Element element, String AttributeName) {
		String attr = element.getAttribute(AttributeName);

		return ("".equals(attr)) ? null : attr;
	}

	protected String getTagValueFromElement(Element element, String sTag) {
		Node node = getNodeByTagNameAndIndex(element, sTag, 0);

		return (null == node) ? null : node.getNodeValue();
	}

	protected String getCDataFromElement(Element element, String sTag) {
		Node node = element.getElementsByTagName(sTag).item(0);

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node child = nodeList.item(i);

			if (child.getNodeType() == Node.CDATA_SECTION_NODE) {
				CharacterData cd = (CharacterData) child;
				return cd.getData();
			}
		}

		return null;
	}

	private Node getNodeByTagNameAndIndex(Element element, String sTag, int index) {
		Node child = element.getElementsByTagName(sTag).item(0);

		return (null == child) ? null : child.getChildNodes().item(index);
	}

}