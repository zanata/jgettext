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
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
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
		String position = getXPathForElement(node, node.getOwnerDocument());
		recordMatch(filename, key, position);
	    }
	} catch (XPathExpressionException e) {
	    throw new BuildException(e);
	} 
    }
    
    /**
     * Returns an xpath to a given node
     * 
     * https://developer.mozilla.org/en/Using_XPath#getXPathForElement
     * @param el
     * @param doc
     * @return
     */
    public static String getXPathForElement(Node el, Document doc) {
	String xpath = "";
	int pos;
	Node tempitem2;
	if (el.getNodeType() == Node.ATTRIBUTE_NODE) {
	    Attr at = (Attr) el;
	   el = at.getOwnerElement();
	   
	   if (at.getNamespaceURI() == null)
	       xpath = '@'+at.getName();
	   else
	       xpath = "attribute::*[local-name()='"+at.getLocalName()+"' and namespace-uri()='"+at.getNamespaceURI()+"']";
	}
	
	while (el != null && el != doc.getDocumentElement()) {		
		pos = 0;
		tempitem2 = el;
		while (tempitem2 != null) {
			if (tempitem2.getNodeType() == Node.ELEMENT_NODE && tempitem2.getNodeName().equals(el.getNodeName())) {
			    // If it is ELEMENT_NODE of the same name
				++pos;
			}
			tempitem2 = tempitem2.getPreviousSibling();
		}
		
		xpath = nodeToName(el)+'['+pos+']'+'/'+xpath;
		el = el.getParentNode();
	}
	if (el != null) {
	    xpath = '/'+nodeToName(el)+'/'+xpath;
	}
	if (xpath.endsWith("/"))
	    xpath = xpath.substring(0, xpath.length()-1);
	return xpath;
    }
    
    private static String nodeToName(Node el) {
	if (el.getNamespaceURI()== null)
	    return el.getNodeName();
	return "*[local-name()='"+el.getLocalName()+"' and namespace-uri()=\'"+el.getNamespaceURI()+"']";
    }
    
    public static void main(String[] args) throws XPathExpressionException {
	File f = new File("src/test/data/taskdefs/meta/studio_eclipse_option.meta");
	XPath xpath = XPathFactory.newInstance().newXPath(); 
	String expression = "//attribute::*[namespace-uri()='http://test.example.org/'] | //@displayName | //XModelAttribute/@name | //XModelAttribute/@default | //Constraint/value/@name"; 
	InputSource inputSource = new InputSource(f.getPath());
	NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource, 
		XPathConstants.NODESET);
	for (int i = 0; i < nodes.getLength(); i++) {
	    Node node = nodes.item(i);
	    String key = node.getTextContent();
	    Document doc = node.getOwnerDocument();
	    String path = getXPathForElement(node, doc);
	    System.out.println(key+": "+path);
	}
    } 
}
