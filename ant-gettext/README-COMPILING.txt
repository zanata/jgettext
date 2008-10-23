To compile this project, you will need Maven 2.0.9 and OpenProps.

OpenProps dependency:

OpenProps is not currently available in the Maven repositories, so you 
will need to compile it yourself.  Just download the source, run 'mvn 
install', and you should now be able to build tennera/ant-gettext. 

For Eclipse:

From the tennera directory, run 'mvn install eclipse:eclipse'.  

This will download required dependencies, compile jgettext and ant-gettext, 
and create Eclipse's project files.  Eclipse should now be able 
to compile the .java files, but you will need to run 'mvn install' again
if you change the jgettext grammar.
