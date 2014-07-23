package net.thecodemaster.evd.xmlloader;

import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.Parameter;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.helper.HelperVerifiers;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.point.ExitPoint;
import net.thecodemaster.evd.verifier.Verifier;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Luciano Sampaio
 */
public class LoaderExitPoint extends LoaderXML {

	private final int				fileId;
	private final Verifier	verifier;

	public LoaderExitPoint(Verifier verifier) {
		this.verifier = verifier;
		this.fileId = verifier.getId();
	}

	@Override
	protected String getFilePath() {
		return HelperVerifiers.getFilePath(fileId);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ExitPoint> load() {
		return (List<ExitPoint>) super.load();
	}

	@Override
	protected List<ExitPoint> load(String file) {
		List<ExitPoint> exitPoints = Creator.newList();

		// It gets the object that knows how to handle XML files.
		Document document = getDocument(file);

		if (null != document) {
			// It gets the list of element by the type of "exitpoint".
			NodeList nodeList = document.getElementsByTagName(Constant.XMLLoader.TAG_EXIT_POINT);

			// It iterates over all the "exitpoint" elements in the xml file.
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {

					Element element = (Element) node;

					// String qualifiedName = getTagValueFromElement(element, Constant.XMLLoader.TAG_QUALIFIED_NAME);
					String qualifiedName = getCDataFromElement(element, Constant.XMLLoader.TAG_QUALIFIED_NAME);

					String methodName = getTagValueFromElement(element, Constant.XMLLoader.TAG_METHOD_NAME);
					ExitPoint exitPoint = new ExitPoint(verifier, qualifiedName, methodName);

					Map<Parameter, List<Integer>> params = Creator.newMap();

					// It gets the list of element by the type of "exitpoints".
					NodeList nodeListParameters = element.getElementsByTagName(Constant.XMLLoader.TAG_PARAMETERS);

					// It iterates over all the "parameter" elements in the xml file.
					for (int j = 0; j < nodeListParameters.getLength(); j++) {
						Node nodeParameter = nodeListParameters.item(j);
						if (nodeParameter.getNodeType() == Node.ELEMENT_NODE) {
							Element elementParameter = (Element) nodeParameter;

							String type = getAttributeValueFromElement(elementParameter, Constant.XMLLoader.TAG_PARAMETERS_TYPE);
							String rules = getAttributeValueFromElement(elementParameter, Constant.XMLLoader.TAG_PARAMETERS_RULES);

							params.put(new Parameter(type), getListFromRules(rules));
						}
					}
					exitPoint.setParameters(params);

					// It adds the new object to the list.
					if (!exitPoints.contains(exitPoint)) {
						exitPoints.add(exitPoint);
					}
				}
			}
		}

		return exitPoints;
	}

	private List<Integer> getListFromRules(String rules) {
		List<Integer> listRules = null;

		if (null != rules) {
			String[] elements = rules.split(Constant.SEPARATOR_RESOURCES_TYPE);

			try {
				for (String element : elements) {
					int intValue = Integer.valueOf(element);

					if (intValue == -1) {
						return null; // Anything is valid.
					} else if (intValue == 0) {
						return Creator.newList(); // Only sanitized values are valid.
					} else {
						if (null == listRules) {
							listRules = Creator.newList();
						}
						listRules.add(intValue); // These are the accepted values.
					}
				}
			} catch (NumberFormatException e) {
				PluginLogger.logError(e);
			}

		}

		return listRules;
	}

}
