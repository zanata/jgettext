/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.webgettext;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;

/**
 * A resolver for the unified JSP expression language (EL) which handles 
 * objects of type ResourceBundle and uses {@link java.text.MessageFormat} for 
 * string formatting.  
 * <p>
 * As with {@link javax.el.ResourceBundleELResolver},  
 * the "property" name is treated as a key of the ResourceBundle, but 
 * any subsequent properties are used as arguments to 
 * {@link java.text.MessageFormat#format(Object)}.  If there is only one 
 * property, it is used as a key to the ResourceBundle, but the string is 
 * <b>not</b> passed through MessageFormat.  Thus strings which 
 * don't require formatting aren't subject to MessageFormat's confusing 
 * quoting rules.  This is <i>intended</i> to make translator's jobs easier. 
 * <p>
 * Similar to {@link javax.el.ResourceBundleELResolver}, missing resources 
 * will be replaced by the resource key, surrounded by question marks, rather
 * than throwing an exception.
 * <p>
 * For example, assuming "messages" resolves to a ResourceBundle: 
 * <pre>${messages["There are ''{0}'' bytes free; total memory is ''{1}''"][freeMem][totalMem]}</pre>
 * will look up the key <code>"There are ''{0}'' bytes free; total memory is ''{1}''"</code> and format
 * the resulting string with the arguments <code>freeMem</code> and <code>totalMem</code>, ie:
 * <pre>MessageFormat.format(messages.getString("There are ''{0}'' bytes free; total memory is ''{1}''"), freeMem, totalMem)</pre>
 * <p>
 * <pre>${messages["I'm sorry Dave, I'm afraid I can't do that"]}</pre>
 * will look up the key in the resource bundle without requiring that the apostrophes be doubled:
 * <pre>messages.getString("I'm sorry Dave, I'm afraid I can't do that")</pre>
 * @see javax.el.ResourceBundleELResolver
 * @see java.text.MessageFormat
 * @author <a href="mailto:sflaniga@redhat.com">Sean Flanigan</a>
 * @version $$Revision: $$
 */
public class BundleResolver extends ELResolver
{
   private static final org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(BundleResolver.class);

   @Override
   public Class<?> getCommonPropertyType(ELContext elContext, Object base)
   {
      if (base instanceof ResourceBundle)
      {
         return String.class;
      }
      else if (base instanceof MessageFormatResolver)
      {
         return Object.class;
      }
      else
      {
         return null;
      }
   }

   @Override
   public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext elContext, Object base)
   {
      return null; // we accept any string as a ResourceBundle key, and any object as a MessageFormat arg.
   }

   @Override
   public Class<?> getType(ELContext elContext, Object base, Object property) throws NullPointerException,
         PropertyNotFoundException, ELException
   {
      getValue(elContext, base, property); // sets propertyResolved if appropriate
      if (elContext.isPropertyResolved())
      {
         return MessageFormatResolver.class;
      }
      else
      {
         return null;
      }
   }
   
   private static String describe(Object arg)
   {
      if(arg == null)
         return null;
      return "["+arg.getClass().getName()+"] '"+arg+"'";   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
   }

   @Override
   public Object getValue(ELContext elContext, Object base, Object property) throws NullPointerException,
         PropertyNotFoundException, ELException
   {
      log.debug("getValue("+describe(base)+", "+describe(property)+")");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
      if (base instanceof ResourceBundle)
      {
         elContext.setPropertyResolved(true);
         ResourceBundle bundle = (ResourceBundle) base;
         String s;
         try
         {
            s = bundle.getString(property.toString());
         }
         catch (MissingResourceException e)
         {
            s = I18nUtil.munge(property.toString(), I18nUtil.getRequestLocale());
//            s = I18nUtil.munge(property.toString(), bundle.getLocale());
         }
         return new MessageFormatResolver(s);
      }
      else if (base instanceof MessageFormatResolver)
      {
         elContext.setPropertyResolved(true);
         MessageFormatResolver res = (MessageFormatResolver) base;
         res.addArg(property);
         return res;
      }
      else
      {
         return null;
      }
   }

   @Override
   public boolean isReadOnly(ELContext elContext, Object base, Object property) throws NullPointerException,
         PropertyNotFoundException, ELException
   {
      getValue(elContext, base, property); // sets propertyResolved if appropriate
      return true;
   }

   @Override
   public void setValue(ELContext elContext, Object base, Object property, Object value) throws NullPointerException,
         PropertyNotFoundException, PropertyNotWritableException, ELException
   {
      getValue(elContext, base, property); // sets propertyResolved if appropriate
      if (elContext.isPropertyResolved())
      {
         throw new PropertyNotWritableException();
      }
   }

}
