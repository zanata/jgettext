/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.antgettext;

import java.io.File;

import org.apache.tools.ant.BuildException;

class DirUtil
{

   static void checkDir(File dir, String attrName, boolean ignoreNonexistent) throws DirMissingException
   {
      if (dir == null)
      {
         throw new BuildException(attrName+" attribute must be set!");
      }
      if (dir.exists()) {
         if (!dir.isDirectory())
         {
             throw new BuildException(attrName+" \""+dir+"\" is not a directory!");
         }
      } else {
         if (!ignoreNonexistent)
         {
        	 throw new DirMissingException(attrName+" \""+dir+"\" does not exist!");
         }
      }
   }
}
