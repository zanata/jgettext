package org.fedorahosted.tennera.antgettext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;

import antlr.RecognitionException;
import antlr.TokenStreamException;

public abstract class AbstractPropReadingTask extends MatchingTask {

	protected static final String START_MARKER = "# START NON-TRANSLATABLE";
	protected static final String END_MARKER = "# END NON-TRANSLATABLE";
	protected File srcDir;
	protected File dstDir;
	protected boolean includeAll;
	EmptyStringPolicy emptyStringPolicy = EmptyStringPolicy.WARNANDSKIP;
	protected static final String NEWLINE_REGEX = "(\r\n|\r|\n)";

	public AbstractPropReadingTask() {
		super();
	}

	public void setSrcDir(File srcDir) {
		this.srcDir = srcDir;
	}

	public void setDstDir(File dstDir) {
		this.dstDir = dstDir;
	}

	public void setIncludeAll(boolean includeAll) {
		this.includeAll = includeAll;
	}

	public void setWhenEmptyString(String policy) {
		emptyStringPolicy = EmptyStringPolicy.valueOf(policy.toUpperCase());
	}

	protected void initialise() throws FileNotFoundException,
	RecognitionException, TokenStreamException, IOException {
		DirUtil.checkDir(srcDir, "srcDir", false); //$NON-NLS-1$
		DirUtil.checkDir(dstDir, "dstDir", true); //$NON-NLS-1$
	}

	protected DirectoryScanner getPropDirectoryScanner() {
		return this.getDirectoryScanner(srcDir);
	}

	protected boolean shouldSkip(String englishFile, String key, String englishString) {
			boolean skip = false;
			   if (englishString.length() == 0)
			   {
				   String message = "Empty value for key "+key+" in file "+englishFile;
				   switch (emptyStringPolicy) {
				     case SKIP:
				       log(message, Project.MSG_DEBUG);
					   skip = true;
					   break;
				     case WARNANDSKIP:
				       log(message, Project.MSG_WARN);
					   skip = true;
					   break;
				     case INCLUDE:
				       log(message, Project.MSG_DEBUG);
				       break;
				     case WARNANDINCLUDE:
				       log(message, Project.MSG_WARN);
				       break;
				       // if anyone ever implements this, don't forget to 
				       // handle @@EMPTY@@ in pot2en and po2prop.  And
				       // *please* come up with a better sentinel value,
				       // but remember, translators may need to enter it as msgstr.
	//        		     case REPLACE:
	//        		       englishString = "@@EMPTY@@";
	//        		       break;
				     case FAIL:
				    	 throw new BuildException(message);
					 default:
					   throw new RuntimeException("unhandled switch case "+emptyStringPolicy);
				   }
			   }
			return skip;
		}

}