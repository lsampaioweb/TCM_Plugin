package net.thecodemaster.sap.xmlloaders;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.thecodemaster.sap.Activator;
import net.thecodemaster.sap.loggers.PluginLogger;
import net.thecodemaster.sap.points.ExitPoint;
import net.thecodemaster.sap.ui.l10n.Messages;
import net.thecodemaster.sap.utils.Creator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author Luciano Sampaio
 */
public abstract class XMLLoader {

  public List<ExitPoint> load(int verifierId) {
    String file = getFilePath(verifierId);
    if (fileExists(file)) {
      return load(file);
    }
    else {
      PluginLogger.logError(new FileNotFoundException(String.format(Messages.Error.FILE_NOT_FOUND, file)));
    }

    return Creator.newList();
  }

  protected abstract String getFilePath(int verifierId);

  protected abstract List<ExitPoint> load(String file);

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
    }
    catch (ParserConfigurationException e) {
      realException = e;
      errorMessage = Messages.Error.PARSING_XML_FILE;
    }
    catch (SAXException e) {
      realException = e;
      errorMessage = Messages.Error.PARSING_XML_FILE;
    }
    catch (IOException e) {
      realException = e;
      errorMessage = Messages.Error.READING_XML_FILE;
    }

    if (errorMessage != null) {
      PluginLogger.logError(String.format(errorMessage, file), realException);
    }

    return document;
  }

  private InputStream getInputStream(String file) {
    Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
    Path path = new Path(file);
    try {
      URL fileURL = FileLocator.find(bundle, path, null);
      if (null != fileURL) {
        return fileURL.openStream();
      }
    }
    catch (IOException e) {
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

  private Node getNodeByTagNameAndIndex(Element element, String sTag, int index) {
    Node child = element.getElementsByTagName(sTag).item(0);

    return (null == child) ? null : child.getChildNodes().item(index);
  }

}