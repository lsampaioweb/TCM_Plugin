package net.thecodemaster.evd.xmlloader;

import java.util.List;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.point.SanitizationPoint;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Luciano Sampaio
 */
public class LoaderSanitizationPoint extends LoaderXML {

	@Override
	protected String getFilePath() {
		return Constant.File.FILE_SANITIZATION_POINT;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SanitizationPoint> load() {
		return (List<SanitizationPoint>) super.load();
	}

	@Override
	protected List<SanitizationPoint> load(String file) {
		List<SanitizationPoint> sanitizers = Creator.newList();

		// It gets the object that knows how to handle XML files.
		Document document = getDocument(file);

		if (null != document) {
			// It gets the list of element by the type of "sanitizer".
			NodeList nodeList = document.getElementsByTagName(Constant.XMLLoader.TAG_SANITIZATION_POINT);

			// It iterates over all the "sanitizer" elements in the xml file.
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {

					Element element = (Element) node;

					String qualifiedName = getTagValueFromElement(element, Constant.XMLLoader.TAG_QUALIFIED_NAME);
					String methodName = getTagValueFromElement(element, Constant.XMLLoader.TAG_METHOD_NAME);
					SanitizationPoint sanitizer = new SanitizationPoint(qualifiedName, methodName);

					List<String> params = Creator.newList();

					// It gets the list of element by the type of "sanitizers".
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
					sanitizer.setParameters(params);

					// It adds the new object to the list.
					if (!sanitizers.contains(sanitizer)) {
						sanitizers.add(sanitizer);
					}
				}
			}
		}

		return sanitizers;
	}

}
