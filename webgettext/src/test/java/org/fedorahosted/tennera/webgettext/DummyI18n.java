/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.webgettext;

import java.text.MessageFormat;

import org.fedorahosted.tennera.webgettext.I18nUtil;

/**
 * Convenience class for accessing the JMX console's resource bundle.
 * @author <a href="mailto:sflaniga@redhat.com">Sean Flanigan</a>
 * @version $$Revision: $$
 */
public class DummyI18n
{
   public static final String BUNDLE = "org.fedorahosted.tennera.webgettext.testmessages"; //$NON-NLS-1$
   public static String gettext(String englishKey)
   {
      return I18nUtil.gettext(englishKey, BUNDLE);
   }

   public static String gettext(String englishKey, Object... arg)
   {
      return MessageFormat.format(gettext(englishKey), arg);
   }


}
