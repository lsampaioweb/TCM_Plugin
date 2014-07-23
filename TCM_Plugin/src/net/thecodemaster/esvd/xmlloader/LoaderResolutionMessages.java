package net.thecodemaster.esvd.xmlloader;

import java.util.Map;

import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.helper.Creator;
import net.thecodemaster.esvd.logger.PluginLogger;
import net.thecodemaster.esvd.marker.resolution.ResolutionMessage;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Luciano Sampaio
 */
public class LoaderResolutionMessages extends LoaderXML {

	@Override
	protected String getFilePath() {
		return Constant.File.FILE_RESOLUTION;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<Integer, ResolutionMessage> load() {
		return (Map<Integer, ResolutionMessage>) super.load();
	}

	@Override
	protected Map<Integer, ResolutionMessage> load(String file) {
		Map<Integer, ResolutionMessage> resolutionMessages = Creator.newMap();

		// It gets the object that knows how to handle XML files.
		Document document = getDocument(file);

		if (null != document) {
			// It gets the list of element by the type of "resolution".
			NodeList nodeList = document.getElementsByTagName(Constant.XMLLoader.TAG_RESOLUTION);

			// It iterates over all the "resolution" elements in the xml file.
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {

					Element element = (Element) node;

					int type2 = getType(getAttributeValueFromElement(element, Constant.XMLLoader.TAG_RESOLUTION_TYPE));
					String label = getCDataFromElement(element, Constant.XMLLoader.TAG_RESOLUTION_LABEL);
					String description = getCDataFromElement(element, Constant.XMLLoader.TAG_RESOLUTION_DESCRIPTION);

					// Adds the new object to the list.
					resolutionMessages.put(type2, new ResolutionMessage(type2, label, description));
				}
			}
		}

		return resolutionMessages;
	}

	private int getType(String type) {
		try {
			return Integer.valueOf(type);
		} catch (NumberFormatException e) {
			PluginLogger.logError(e);
		}

		return -1;
	}

}
