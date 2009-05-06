/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.antgettext;

import java.io.File;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


public class XPath2PotTestRun {
    
    public static void main(String[] args) throws XPathExpressionException {
	File f = new File("src/test/resources/taskdefs/meta/studio_eclipse_option.meta");
	XPath xpath = XPathFactory.newInstance().newXPath(); 
	String expression = "//attribute::*[namespace-uri()='http://test.example.org/'] | //@displayName | //XModelAttribute/@name | //XModelAttribute/@default | //Constraint/value/@name"; 
	InputSource inputSource = new InputSource(f.getPath());
	NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource, 
		XPathConstants.NODESET);
	for (int i = 0; i < nodes.getLength(); i++) {
	    Node node = nodes.item(i);
	    String key = node.getTextContent();
	    Document doc = node.getOwnerDocument();
	    String path = XPath2PotTask.getXPathForElement(node, doc);
	    System.out.println(key+": "+path);
	}
    } 
}
