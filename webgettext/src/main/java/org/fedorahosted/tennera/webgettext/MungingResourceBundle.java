/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.webgettext;

import java.util.Enumeration;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

/**
 * Decorates an underlying ResourceBundle so that missing keys are handled 
 * by munging the (English) key instead of throwing MissingResourceException. 
 * @author <a href="mailto:sflaniga@redhat.com">Sean Flanigan</a>
 * @version $$Revision: $$
 */
class MungingResourceBundle extends ResourceBundle
{

   private final Locale locale;

   public MungingResourceBundle(Locale locale)
   {
      this.locale = locale;
   }

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
      return I18nUtil.munge(key, locale);
   }

}
