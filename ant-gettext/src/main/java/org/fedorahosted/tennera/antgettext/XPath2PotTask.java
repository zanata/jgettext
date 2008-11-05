/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.antgettext;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.ResourceLocation;
import org.apache.tools.ant.types.XMLCatalog;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class XPath2PotTask extends MatchExtractingTask {
    
    private String xpath;
    // This is to support <xmlcatalog> and <dtd> subelements, just like XMLValidate
    private XMLCatalog xmlCatalog = new XMLCatalog();

    public void setXpath(String xpath) {
	this.xpath = xpath;
    }
    
    // untested
    public void addConfiguredXMLCatalog(XMLCatalog xmlCatalog)
    {
	xmlCatalog.addConfiguredXMLCatalog(xmlCatalog);
    }

    public void addDTD(ResourceLocation dtd) throws BuildException {
	xmlCatalog.addDTD(dtd);
    }

    /*
     * Not sure if this would make sense
     */
//    public void addEntity(ResourceLocation entity) throws BuildException {
//	xmlCatalog.addEntity(entity);
//    }

    public void init() throws BuildException
    {
      super.init();
      xmlCatalog.setProject(getProject());
    }

    @Override
    // iterates through contents, recording xpath matches
    protected void processFile(String filename, File f) throws SAXException, IOException {
	XPath xpath = XPathFactory.newInstance().newXPath(); 
	String expression = this.xpath; 
	InputSource inputSource = new InputSource(f.getPath());
	try {
	    // all of this is to let us use the XMLCatalog: 
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setValidating(false);
            DocumentBuilder parser = dbf.newDocumentBuilder();
            // see http://www.javalobby.org/java/forums/m91839205.html
            // for a way of skipping DTDs
            parser.setEntityResolver(xmlCatalog);
            
            Document document = parser.parse(inputSource);
	    NodeList nodes = (NodeList) xpath.evaluate(expression, document, 
		    XPathConstants.NODESET);
	    for (int i = 0; i < nodes.getLength(); i++) {
		Node node = nodes.item(i);
		String key = node.getTextContent();
		String position = getXPathForElement(node, node.getOwnerDocument());
		recordMatch(filename, key, position);
	    }
	} catch (XPathExpressionException e) {
	    throw new BuildException(e);
	} catch (ParserConfigurationException e) {
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
