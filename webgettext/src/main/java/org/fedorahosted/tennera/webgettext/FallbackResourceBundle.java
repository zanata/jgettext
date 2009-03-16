/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.webgettext;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This decorator ResourceBundle quietly falls back on the key when 
 * a key is missing from the underlying resource bundle, since English
 * strings may appear in the resource bundle, but otherwise should come 
 * from the source code.
 * @author <a href="mailto:sflaniga@redhat.com">Sean Flanigan</a>
 * @version $Revision: $
 */
class FallbackResourceBundle extends ResourceBundle
{

   private final ResourceBundle bundle;

   public FallbackResourceBundle(ResourceBundle bundle)
   {
      this.bundle = bundle;
      
   }

   @Override
   public Enumeration<String> getKeys()
   {
      return bundle.getKeys();
   }

   @Override
   protected Object handleGetObject(String key)
   {
      try
      {
         return bundle.getObject(key);
      }
      catch (MissingResourceException e)
      {
         return key;
      }
   }

}
