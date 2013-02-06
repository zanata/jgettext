Tennera
=======

**Tennera** is an umbrella project for various Java tools to do with internationalisation, initially tools for processing the Gettext and Properties formats.


JGettext
--------

JGettext includes an ANTLR-based parser for GNU Gettext PO/POT files and a PO/POT generator as well.


Ant-Gettext (unmaintained)
-----------

Ant-Gettext includes various Ant tasks which can convert Java Properties files to and from Gettext PO/POT files.  Ant-Gettext makes use of [OpenProps](https://github.com/zanata/openprops) and JGettext (below) to do its work.

Some of these tasks have similarities to commands in the [GNU Gettext tools](http://www.gnu.org/software/gettext/) and the [Translate Toolkit](http://translate.sourceforge.net/wiki/toolkit/index) (but generally with fewer options):
 * Gettext2PropTask (gettext2prop) is like 'po2prop' or 'msgcat --properties-output', except that it generates a directory tree of .properties from each PO file (one per locale).  It copes with renamed properties files and directories because it uses the English properties files as a template (similar to po2prop -t).  The generated properties files will go into the same directory as the corresponding English properties files.
 * Prop2GettextTask (prop2gettext) is like 'prop2po --duplicates=msgctxt' or 'msgcat --properties-input', except that it generates one POT for a directory tree of .properties (and/or one PO per locale)
 * Regex2PotTask (regex2pot) is like 'xgettext', but uses regexes to identify strings which should be extracted, rather than parsers for each supported language.  Works in conjunction with WebGettext (see below)
 * !VerifyPropTask compares the contents of two .properties files and breaks the build if the names or values are different (used for testing Ant-Gettext)

Some tasks which might go away unless someone shows interest in them:
 * Po2EnTask is like 'poen' (generates English .po file from .pot)
 * Po2PropTask is like 'po2prop' or  'msgcat --properties-output' (generates .properties from .po)
 * Prop2PotTask is like 'prop2po --duplicates=msgctxt' or 'msgcat --properties-input' (generates .pot from default locale .properties, eg messages.properties -> messages.pot)
 * Prop2PoTask is like 'prop2po --duplicates=msgctxt' or 'msgcat --properties-input' (generates .po from translate .properties, eg messages_es.properties -> messages_es.po)
 * XPath2PotTask (xpath2pot) is like Regex2PotTask, but extracts values matching an [XPath](http://www.w3.org/TR/xpath) expression.


WebGettext (unmaintained)
----------

Originated as a patch for the old JBoss consoles, and hasn't been properly documented. 

Features:
 a. a method for localising strings in JSPs in the Gettext style (keeping English in the source JSP)
 b. an Ant task which can find such strings in JSPs and generate a Gettext POT file.

(a) is provided by WebGettext, which provides an EL resolver for expressions such as `${messages["Hello {0}"][username]}`.  The resolver looks up a translation, for the requesting user's locale, and substitutes any parameters using MessageFormat.

(b) is provided by Regex2PotTask (actually part of Ant-Gettext, not WebGettext), which is a bit like xgettext but rather than finding C, Java, Perl, etc, function calls such as gettext("my string"), it finds a configurable regex expression.

[Example JSP project](https://github.com/zanata/tennera/tree/master/webgettext-example) - see RUNNING.txt


History
-------

Ant-Gettext was created by Red Hat's Internationalisation team in Brisbane, for use in JBoss projects, by allowing i18n tasks to be added to pure-Java build processes.

JGettext was created by Steve Ebersole as part of the Maven jDocBook plugin in the JBoss project.  


Maintainer
----------

 * Sean Flanigan <sflaniga@redhat.com>


Download
--------

Tennera is made up of several jars, arranged in a Maven2 repository here: http://svn.fedorahosted.org/svn/tennera/m2repo/ 

Tennera's jars are all under: http://svn.fedorahosted.org/svn/tennera/m2repo/org/fedorahosted/tennera/ 

Irregular snapshots go here occasionally: http://seanf.fedorapeople.org/maven2/snapshot/

It's best if you can use something like Maven to pick up your dependencies (see below), but otherwise you can download the individual jars from http://svn.fedorahosted.org/svn/tennera/m2repo/org/fedorahosted/tennera/
You will also need [ANTLR](http://repo1.maven.org/maven2/antlr/antlr/2.7.6/antlr-2.7.6.jar) .  If you're using Ant, Maven Ant Tasks can help with the dependencies.

Maven dependency details for pom.xml
------------------------------------

    <repositories>
        <repository>
            <name>Tennera release repository on fedorahosted.org</name>
            <id>fhosted.tennera.svn</id>
            <url>http://svn.fedorahosted.org/svn/tennera/m2repo/</url>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
        </repository>
    <!--
        <repository>
          <name>Tennera snapshot repository on fedorapeople.org</name>
          <id>fpeople.tennera.snap</id>
          <url>http://seanf.fedorapeople.org/maven2/snapshot/</url>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
        </repository>
    -->
    </repositories>
    
    <dependencies>
        <dependency>
            <groupId>org.fedorahosted.tennera</groupId>
            <artifactId>tennera</artifactId>
            <version>0.6</version>
        </dependency>
    </dependencies>

NB: You might need to update the version numbers.  And don't forget to uncomment the snapshot repo if you use snapshot versions!


Task definitions for Ant
------------------------

Set up a classpath called "dependency.classpath" with the jars above, then:

    <taskdef resource="org/fedorahosted/tennera/antgettext/antlib.xml" 
     classpathref="dependency.classpath" />


Licence
-------

LGPL 2.1
