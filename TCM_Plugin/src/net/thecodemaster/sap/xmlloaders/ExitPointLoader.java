package net.thecodemaster.sap.xmlloaders;

import java.util.List;
import java.util.Map;

import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.graph.Parameter;
import net.thecodemaster.sap.points.ExitPoint;
import net.thecodemaster.sap.utils.Creator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Luciano Sampaio
 */
public class ExitPointLoader extends XMLLoader {

  @Override
  protected String getFilePath(int verifierId) {
    switch (verifierId) {
      case Constants.Plugin.COOKIE_POISONING_VERIFIER_ID:
        return Constants.Plugin.COOKIE_POISONING_VERIFIER_EXIT_POINT_FILE;
      case Constants.Plugin.SECURITY_MISCONFIGURATION_VERIFIER_ID:
        return Constants.Plugin.SECURITY_MISCONFIGURATION_VERIFIER_EXIT_POINT_FILE;
      case Constants.Plugin.SQL_INJECTION_VERIFIER_ID:
        return Constants.Plugin.SQL_INJECTION_VERIFIER_EXIT_POINT_FILE;
      case Constants.Plugin.XSS_VERIFIER_ID:
        return Constants.Plugin.XSS_VERIFIER_EXIT_POINT_FILE;
      default:
        return null;
    }
  }

  @Override
  protected List<ExitPoint> load(String file) {
    List<ExitPoint> exitPoints = Creator.newList();

    // It gets the object that knows how to handle XML files.
    Document document = getDocument(file);

    if (null != document) {
      // It gets the list of element by the type of "exitpoint".
      NodeList nodeList = document.getElementsByTagName(Constants.Plugin.SM_TAG_EXIT_POINT);

      // It iterates over all the "exitpoint" elements in the xml file.
      for (int i = 0; i < nodeList.getLength(); i++) {
        Node node = nodeList.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE) {

          Element element = (Element) node;

          String qualifiedName = getTagValueFromElement(element, Constants.Plugin.SM_TAG_QUALIFIED_NAME);
          String methodName = getTagValueFromElement(element, Constants.Plugin.SM_TAG_METHOD_NAME);
          ExitPoint exitPoint = new ExitPoint(qualifiedName, methodName);

          Map<Parameter, List<Integer>> params = Creator.newMap();

          // It gets the list of element by the type of "exitpoints".
          NodeList nodeListParameters = element.getElementsByTagName(Constants.Plugin.SM_TAG_PARAMETERS);

          // It iterates over all the "parameter" elements in the xml file.
          for (int j = 0; j < nodeListParameters.getLength(); j++) {
            Node nodeParameter = nodeListParameters.item(j);
            if (nodeParameter.getNodeType() == Node.ELEMENT_NODE) {
              Element elementParameter = (Element) nodeParameter;

              String type = getAttributeValueFromElement(elementParameter, Constants.Plugin.SM_TAG_PARAMETERS_TYPE);
              String rules = getAttributeValueFromElement(elementParameter, Constants.Plugin.SM_TAG_PARAMETERS_RULES);

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
      String[] elements = rules.split(Constants.SEPARATOR);

      for (String element : elements) {
        int intValue = Integer.valueOf(element);

        if (intValue == -1) {
          return null; // Anything is valid.
        }
        else if (intValue == 0) {
          return Creator.newList(); // Only sanitized values are valid.
        }
        else {
          if (null == listRules) {
            listRules = Creator.newList();
          }
          listRules.add(intValue); // These are the accepted values.
        }
      }
    }

    return listRules;
  }

}
