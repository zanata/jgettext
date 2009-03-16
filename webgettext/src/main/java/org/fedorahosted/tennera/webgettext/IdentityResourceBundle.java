/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.webgettext;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

/**
 * This ResourceBundle simply maps keys to themselves.
 * @author <a href="mailto:sflaniga@redhat.com">Sean Flanigan</a>
 * @version $$Revision: $$
 */
class IdentityResourceBundle extends ResourceBundle
{

   @Override
   public Enumeration<String> getKeys()
   {
      return new Enumeration<String>()
      {
         public boolean hasMoreElements()
         {
            return false;
         }
         public String nextElement()
         {
            throw new NoSuchElementException();
         }
      };
   }

   @Override
   protected Object handleGetObject(String key)
   {
      return key;
   }

}
