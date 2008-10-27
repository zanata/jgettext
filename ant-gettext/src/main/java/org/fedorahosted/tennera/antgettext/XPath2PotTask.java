/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.antgettext;


/*public*/ class XPath2PotTask extends MatchExtractingTask {
    
    private String xpath;
    
    public void setXpath(String xpath) {
	this.xpath = xpath;
    }
    
    @Override
    void recordMatches(String filename, CharSequence contents,
            Integer[] lineStarts) {
	// TODO iterate through contents, recording xpath matches
//	       recordMatch(filename, key, lineNo);
    }

}
