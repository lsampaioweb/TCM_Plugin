package net.thecodemaster.evd.xmlloader;

import java.util.List;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.point.EntryPoint;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Luciano Sampaio
 */
public class EntryPointLoader extends XMLLoader {

	@Override
	protected String getFilePath() {
		return Constant.File.FILE_ENTRY_POINT;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected List<EntryPoint> load(String file) {
		List<EntryPoint> entryPoints = Creator.newList();

		// It gets the object that knows how to handle XML files.
		Document document = getDocument(file);

		if (null != document) {
			// It gets the list of element by the type of "entrypoint".
			NodeList nodeList = document.getElementsByTagName(Constant.XMLLoader.TAG_ENTRY_POINT);

			// It iterates over all the "entrypoint" elements in the xml file.
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {

					Element element = (Element) node;

					String qualifiedName = getTagValueFromElement(element, Constant.XMLLoader.TAG_QUALIFIED_NAME);
					String methodName = getTagValueFromElement(element, Constant.XMLLoader.TAG_METHOD_NAME);
					EntryPoint entryPoint = new EntryPoint(qualifiedName, methodName);

					List<String> params = Creator.newList();

					// It gets the list of element by the type of "entrypoints".
					NodeList nodeListParameters = element.getElementsByTagName(Constant.XMLLoader.TAG_PARAMETERS);

					// It iterates over all the "parameter" elements in the xml file.
					for (int j = 0; j < nodeListParameters.getLength(); j++) {
						Node nodeParameter = nodeListParameters.item(j);
						if (nodeParameter.getNodeType() == Node.ELEMENT_NODE) {
							Element elementParameter = (Element) nodeParameter;

							String type = getAttributeValueFromElement(elementParameter, Constant.XMLLoader.TAG_PARAMETERS_TYPE);

							params.add(type);
						}
					}
					entryPoint.setParameters(params);

					// It adds the new object to the list.
					if (!entryPoints.contains(entryPoint)) {
						entryPoints.add(entryPoint);
					}
				}
			}
		}

		return entryPoints;
	}

}
