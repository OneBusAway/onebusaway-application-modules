package org.onebusaway.webapp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CopyCompiledGwtResourcesMain {
  public static void main(String[] args) throws Exception {

    if (args.length != 1) {
      System.err.println("usage: onebusaway-webapp-target-dir");
      System.exit(-1);
    }

    File pomFile = new File("pom.xml");
    File webappDirectory = new File(args[0]);

    if (!pomFile.exists()) {
      System.err.println("ERROR - didn't find the pom file where we expected it: "
          + pomFile.getAbsolutePath());
      System.exit(-1);
    }

    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = docFactory.newDocumentBuilder();

    Document doc = builder.parse(pomFile);

    String expression = "project/build/plugins/plugin[artifactId='maven-war-plugin']/configuration/webResources/resource";
    NodeList nodes = xpath(doc, expression, NodeList.class);

    for (int i = 0; i < nodes.getLength(); i++) {
      Node resourceNode = nodes.item(i);
      Node directoryNode = xpath(resourceNode, "directory", Node.class);
      Node targetPathNode = xpath(resourceNode, "targetPath", Node.class);
      String directory = directoryNode.getTextContent();
      String targetPath = targetPathNode.getTextContent();
      System.out.println("direectory=" + directory);
      System.out.println("targetPath=" + targetPath);
      copyFiles(new File(directory), new File(webappDirectory, targetPath));
    }
  }

  private static void copyFiles(File sourceDirectory, File targetDirectory)
      throws IOException {
    if (!(sourceDirectory.exists() && sourceDirectory.isDirectory()))
      return;
    File[] files = sourceDirectory.listFiles();
    if (files == null)
      return;
    for (File file : files)
      copyFile(file, new File(targetDirectory, file.getName()));
  }

  private static void copyFile(File srcFile, File dstFile) throws IOException {
    System.out.println("from=" + srcFile.getAbsolutePath());
    System.out.println("to=" + dstFile.getAbsolutePath());
    InputStream in = new BufferedInputStream(new FileInputStream(srcFile));
    OutputStream out = new BufferedOutputStream(new FileOutputStream(dstFile));
    byte[] buffer = new byte[1024];
    while (true) {
      int rc = in.read(buffer);
      if (rc == -1)
        break;
      if( rc > 0)
        out.write(buffer, 0, rc);
    }
    in.close();
    out.close();
  }

  @SuppressWarnings("unchecked")
  private static <T> T xpath(Node node, String expression, Class<T> type)
      throws XPathExpressionException {
    XPathFactory xpathFactory = XPathFactory.newInstance();
    XPath xpath = xpathFactory.newXPath();
    XPathExpression expr = xpath.compile(expression);

    QName returnType = null;
    if (type == NodeList.class)
      returnType = XPathConstants.NODESET;
    else if (type == Node.class)
      returnType = XPathConstants.NODE;
    else
      throw new IllegalStateException("unknown return type " + type);
    return (T) expr.evaluate(node, returnType);
  }
}
