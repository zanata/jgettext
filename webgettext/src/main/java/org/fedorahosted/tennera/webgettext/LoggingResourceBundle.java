/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.webgettext;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Simply decorates a ResourceBundle to add debug logging of contained keys and lookups.
 * @author <a href="mailto:sflaniga@redhat.com">Sean Flanigan</a>
 * @version $$Revision: $$
 */
public class LoggingResourceBundle extends ResourceBundle
{
   private static final org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(LoggingResourceBundle.class);

   private final ResourceBundle original;

   private String locale;

   public static ResourceBundle wrap(ResourceBundle original, String bundleName, Locale locale)
   {
      if (log.isTraceEnabled())
         return new LoggingResourceBundle(original, bundleName, locale);
      else
         return original;
   }

   private LoggingResourceBundle(ResourceBundle original, String bundleName, Locale locale)
   {
      this.original = original;
      this.locale = locale.toString();
      List<String> keys = new ArrayList<String>();
      for (Enumeration<String> e = original.getKeys(); e.hasMoreElements();)
      {
         String key = e.nextElement();
         keys.add(key);
      }
      log.trace(bundleName+" keys: "+keys); //$NON-NLS-1$
   }

   @Override
   public Enumeration<String> getKeys()
   {
      return original.getKeys();
   }

   @Override
   protected Object handleGetObject(String key)
   {
      log.trace("get: \"" + key + '"'); //$NON-NLS-1$
      try
      {
         return original.getObject(key);
      }
      catch (MissingResourceException e)
      {
         log.warn("get FAILED in [" + locale + "]: \"" + key + '"'); //$NON-NLS-1$ //$NON-NLS-2$
         throw e;
      }
   }

}
