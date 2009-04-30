package org.fedorahosted.tennera.webgettext;

import java.util.Locale;

import junit.framework.TestCase;

public class I18nUtilTest extends TestCase
{

   public I18nUtilTest(String name)
   {
      super(name);
   }

   public void testToLocale()
   {
      assertEquals(Locale.ENGLISH, I18nUtil.toLocale("en")); //$NON-NLS-1$
      assertEquals(new Locale("en", "AU", "LINUX"), I18nUtil.toLocale("en_AU_LINUX"));    //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
      assertEquals(new Locale("en", "AU"), I18nUtil.toLocale("en_AU"));  //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
      assertEquals(new Locale("sr", "LATN"), I18nUtil.toLocale("sr_LATN"));  //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
   }

   private static final String key = "I''m sorry {0}, I''m afraid I can''t do that. '{'{1}'}'";  //$NON-NLS-1$
   public void testBundleLookup() throws Exception
   {
	   I18nUtil.setRequestLocale(new Locale("en", "AA"));  //$NON-NLS-1$//$NON-NLS-2$
       String resultB = DummyI18n.gettext(key, "Dave", "HAL");   //$NON-NLS-1$//$NON-NLS-2$
       assertEquals("No way, Dave! -HAL", resultB); //$NON-NLS-1$
   }

   public void testMissingBundle() throws Exception
   {
	   I18nUtil.setRequestLocale(Locale.GERMAN);
	   String resultA = DummyI18n.gettext(key, "Dave", "HAL");   //$NON-NLS-1$//$NON-NLS-2$
	   assertEquals("???[de]I'm sorry Dave, I'm afraid I can't do that. {HAL}???", resultA); //$NON-NLS-1$
   }

   public void testEnglishFallback() throws Exception
   {
	   I18nUtil.setRequestLocale(new Locale("en", "ZZ"));  //$NON-NLS-1$//$NON-NLS-2$
	   String resultA = DummyI18n.gettext(key, "Dave", "HAL");   //$NON-NLS-1$//$NON-NLS-2$
	   assertEquals("I'm sorry Dave, I'm afraid I can't do that. {HAL}", resultA); //$NON-NLS-1$
   }
}
