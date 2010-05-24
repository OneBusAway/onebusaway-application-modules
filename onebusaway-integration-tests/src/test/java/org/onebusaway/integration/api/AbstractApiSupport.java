package org.onebusaway.integration.api;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractApiSupport {

  protected String getBaseUrl() {
    String port = System.getProperty("org.onebusaway.api_webapp.port","9910");
    return "http://localhost:" + port + "/onebusaway-api-webapp";
  }

  protected URL getUrl(String request) {

    String url = getBaseUrl() + request;

    if (url.contains("?"))
      url += "&key=TEST";
    else
      url += "?key=TEST";

    try {
      return new URL(url);
    } catch (MalformedURLException e) {
      throw new IllegalStateException("bad url: " + url, e);
    }
  }

  protected Document getXml(String request) {

    try {

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();

      URL url = getUrl(request);
      return builder.parse(url.openStream());

    } catch (Throwable ex) {
      throw new IllegalStateException(ex);
    }
  }

  protected Element verifyResponseWrapper(Document document, int version,
      int code) {
    return verifyResponseWrapper(document, Integer.toString(version), code);
  }

  protected Element verifyResponseWrapper(Document document, String version,
      int code) {

    Element response = getElement(document, "response");

    assertEquals("response code comparison", Integer.toString(code), getText(
        response, "code"));

    String text = getText(response, "text");

    if (code == 200)
      assertEquals("text comparison", "OK", text);

    assertEquals("version comparison", version, getText(response, "version"));

    return getElement(response, "data");
  }

  protected static Element getElement(Node parent, String expression) {
    return (Element) compileExpression(parent, expression, Node.class);
  }

  protected static List<Element> getElements(Node parent, String expression) {
    NodeList list = compileExpression(parent, expression, NodeList.class);
    List<Element> elements = new ArrayList<Element>();
    for (int i = 0; i < list.getLength(); i++)
      elements.add((Element) list.item(i));
    return elements;
  }

  protected static double getDouble(Node parent, String expression) {
    return Double.parseDouble(getText(parent, expression));
  }
  
  protected static long getLong(Node parent, String expression) {
    return Long.parseLong(getText(parent, expression));
  }

  protected static String getText(Node parent, String expression) {
    return compileExpression(parent, expression, String.class);
  }

  @SuppressWarnings("unchecked")
  private static <T> T compileExpression(Object node, String expression,
      Class<T> returnType) {

    try {
      XPathFactory factory = XPathFactory.newInstance();
      XPath xpath = factory.newXPath();
      XPathExpression expr = xpath.compile(expression);

      QName type = null;
      if (returnType == String.class)
        type = XPathConstants.STRING;
      else if (returnType == NodeList.class)
        type = XPathConstants.NODESET;
      else if (returnType == Node.class)
        type = XPathConstants.NODE;
      else
        throw new IllegalStateException("unknown return type " + returnType);
      return (T) expr.evaluate(node, type);
    } catch (XPathExpressionException e) {
      throw new IllegalStateException(e);
    }
  }
}
