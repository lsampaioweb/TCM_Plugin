package net.thecodemaster.esvd.xmlloader;

import java.util.Map;

import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.graph.Parameter;
import net.thecodemaster.esvd.helper.Creator;
import net.thecodemaster.esvd.point.EntryPointManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Luciano Sampaio
 */
public class LoaderEntryPoint extends LoaderXML {

	@Override
	protected String getFilePath() {
		return Constant.File.FILE_ENTRY_POINT;
	}

	@Override
	public EntryPointManager load() {
		return (EntryPointManager) super.load();
	}

	@Override
	protected EntryPointManager load(String file) {
		EntryPointManager manager = new EntryPointManager();

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

					// Get the package name.
					String packageName = getTagValueFromElement(element, Constant.XMLLoader.TAG_QUALIFIED_NAME);
					// Get the method name.
					String methodName = getTagValueFromElement(element, Constant.XMLLoader.TAG_METHOD_NAME);

					Map<Parameter, Integer> params = Creator.newMap();
					// It gets the list of element by the type of "entrypoints".
					NodeList nodeListParameters = element.getElementsByTagName(Constant.XMLLoader.TAG_PARAMETERS);

					// It iterates over all the "parameter" elements in the xml file.
					for (int j = 0; j < nodeListParameters.getLength(); j++) {
						Node nodeParameter = nodeListParameters.item(j);
						if (nodeParameter.getNodeType() == Node.ELEMENT_NODE) {
							Element elementParameter = (Element) nodeParameter;

							String type = getAttributeValueFromElement(elementParameter, Constant.XMLLoader.TAG_PARAMETERS_TYPE);

							params.put(new Parameter(type), null);
						}
					}
					manager.add(methodName, packageName, params);
				}
			}
		}

		return manager;
	}

}
