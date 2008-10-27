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

import org.apache.tools.ant.BuildException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


public class XPath2PotTask extends MatchExtractingTask {
    
    private String xpath;
    
    public void setXpath(String xpath) {
	this.xpath = xpath;
    }
    
    @Override
    // iterates through contents, recording xpath matches
    protected void processFile(String filename, File f) {
	XPath xpath = XPathFactory.newInstance().newXPath(); 
	String expression = this.xpath; 
	InputSource inputSource = new InputSource(f.getPath());
	try {
	    NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource, 
		    XPathConstants.NODESET);
	    for (int i = 0; i < nodes.getLength(); i++) {
		Node node = nodes.item(i);
		String key = node.getTextContent();
		// TODO use https://svn.apache.org/repos/asf/xerces/java/branches/schemawork/samples/dom/DOMAddLines.java
		// or https://jaxb2-commons.dev.java.net/xpath-tracker/
		int lineNo = -1; 
		recordMatch(filename, key, lineNo);
	    }
	} catch (XPathExpressionException e) {
	    throw new BuildException(e);
	} 
    }
    
    public static void main(String[] args) throws XPathExpressionException {
	File f = new File("src/test/data/taskdefs/meta/studio_eclipse_option.meta");
	XPath xpath = XPathFactory.newInstance().newXPath(); 
	String expression = "//@displayName | //XModelAttribute/@name | //XModelAttribute/@default | //Constraint/value/@name"; 
	InputSource inputSource = new InputSource(f.getPath());
	NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource, 
		XPathConstants.NODESET);
	for (int i = 0; i < nodes.getLength(); i++) {
	    Node node = nodes.item(i);
	    String key = node.getTextContent();
	    System.out.println(key);
	}
    } 
}
