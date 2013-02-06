JGettext includes an ANTLR-based parser for GNU Gettext PO/POT files and a PO/POT generator as well.

To compile this project, you will need Maven 2.0.9.

Eclipse:

From the jgettext directory, run 'mvn install eclipse:eclipse'.  

This will download required dependencies, run ANTLR against the jgettext 
grammar, and create Eclipse's project files.  Eclipse should now be able 
to compile the .java files, but you will need to run 'mvn install' again
if you change the grammar.

