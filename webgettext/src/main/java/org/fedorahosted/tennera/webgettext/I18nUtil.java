/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.webgettext;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.jboss.logging.Logger;

/**
 * 
 * @author <a href="mailto:sflaniga@redhat.com">Sean Flanigan</a>
 * @version $$Revision: $$
 */
@SuppressWarnings("nls")
public class I18nUtil
{
   private static final Logger log = Logger.getLogger(I18nUtil.class);
   // see reloadBundlesHack().
   private static boolean warnedAboutReload = false;
   private static Locale defaultLocale;

   public static String munge(String englishKey, Locale locale)
   {
      return "???[" + locale + "]" + englishKey + "???";
   }
   
   public static Locale toLocale(String name)
   {
      int firstUnderscore = name.indexOf('_');
      if (firstUnderscore < 0)
      {
         return new Locale(name);
      }
      String lang = name.substring(0, firstUnderscore);
      int secondUnderscore = name.indexOf('_', firstUnderscore+1);
      if (secondUnderscore < 0)
      {
         String country = name.substring(firstUnderscore+1);
         return new Locale(lang, country);
      }
      String country = name.substring(firstUnderscore+1, secondUnderscore);
      String variant = name.substring(secondUnderscore+1);
      
      return new Locale(lang, country, variant);
   }
   
   public static Locale getRequestLocale() 
   {
      Locale locale = requestLocale.get();
      if (locale == null)
      {
         // This could happen if we are running in the applet, not the web server.
         // Just fall back on the JVM's default locale.
         log.debug("unknown locale for request; using default");
         if (defaultLocale != null)
            locale = defaultLocale;
         else
            locale = Locale.getDefault();
      }
      return locale;
   }

   public static String gettext(String englishKey, String baseName)
   {
      Locale locale = getRequestLocale();
      
      try
      {
         ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
         if (bundle != null)
         {
            try
            {
               return bundle.getString(englishKey);
            }
            catch (MissingResourceException e)
            {
               if (locale.getLanguage().equals(Locale.ENGLISH.getLanguage()))
               {
                  return englishKey;
               }
               else
               {
//                  return munge(englishKey, bundle.getLocale());
                  return munge(englishKey, locale);
               }
            }
         }
         return munge(englishKey, locale);
      }
      catch (MissingResourceException e)
      {
          if (locale.getLanguage().equals(Locale.ENGLISH.getLanguage()))
          {
             return englishKey;
          }
          return munge(englishKey, locale);
      }
   }

   // A hack to force all ResourceBundles to be reloaded at dev-time
   // from here: http://saloon.javaranch.com/cgi-bin/ubb/ultimatebb.cgi?ubb=get_topic&f=7&t=019653
   static void reloadBundlesHack()
   {
      if (!warnedAboutReload)
      {
         log.warn("Clearing ResourceBundles: this hurts performance, and should not be enabled in production");
         warnedAboutReload = true;
      }
      try
      {
         clearMap(ResourceBundle.class, null, "cacheList");
         clearClassLoaderCache();
      }
      catch (Exception e)
      {
         log.warn("Could not reload resource bundles", e);
      }
   }

   private static void clearClassLoaderCache()
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      // no need for compilation here.
      Class<?> cl = loader.getClass();

      try
      {
         if ("org.jboss.web.tomcat.service.WebAppClassLoader".equals(cl.getName()))
         {
            cl = cl.getSuperclass();
         }
         if ("org.apache.catalina.loader.WebappClassLoader".equals(cl.getName()))
         {
            clearMap(cl, loader, "resourceEntries"); 
         }
         else if ("org.jboss.classloader.spi.base.BaseClassLoader".equals( 
        		 loader.getParent().getClass().getName()))
         {
        	 // apparently not needed
//        	 cl = loader.getParent().getClass();
//             clearMap(cl, loader.getParent(), "resourceCache"); 
         }
         else
         {
            log.info("couldn't clear classloader cache: classloader " + 
            	cl.getName() + " is unknown. Parent is " + 
            	loader.getParent().getClass().getName());
         }
      }
      catch (Exception e)
      {
         log.warn("couldn't clear classloader cache", e);
      }
   }

   private static void clearMap(Class<?> cl, Object obj, String name) throws NoSuchFieldException, IllegalAccessException,
         NoSuchMethodException, InvocationTargetException
   {
      Field field = cl.getDeclaredField(name);
      field.setAccessible(true);

      Object cache = field.get(obj);
      Class<?> ccl = cache.getClass();
      Method clearMethod = ccl.getMethod("clear", (Class[]) null); 
      clearMethod.invoke(cache, (Object[]) null);
   }

   static final ThreadLocal<Locale> requestLocale = new ThreadLocal<Locale>();

   static void clearRequestLocale()
   {
      I18nUtil.requestLocale.remove();
   }

   public static void setRequestLocale(final Locale locale)
   {
      I18nUtil.requestLocale.set(locale);
   }

   public static void setLocale(Locale locale)
   {
      // not allowed in unsigned applet, so we simulate it:
//      Locale.setDefault(locale);
      defaultLocale = locale;
   }
}
