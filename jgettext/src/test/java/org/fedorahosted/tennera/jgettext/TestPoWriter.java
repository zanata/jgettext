package org.fedorahosted.tennera.jgettext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.matchers.JUnitMatchers.containsString;

public class TestPoWriter {
   boolean encodeTabs = true;

	private String serializeMsg(Message msg) throws IOException {
		Catalog catalog = new Catalog(true);
		catalog.addMessage(msg);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Charset charset = Charset.forName("utf-8");
		new PoWriter(encodeTabs).write(catalog, bos, charset);
		bos.close();
		return new String(bos.toByteArray(), charset);
	}

	@Test
	public void testDefaultMsgStrShouldBeEmpty() throws IOException {
		Message msg = new Message();
		msg.setMsgid("id");
		
		String str = serializeMsg(msg);
		Pattern pattern = Pattern.compile("^msgstr\\s+\"\"$", Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(str);
		Assert.assertTrue(matcher.find());
		Assert.assertFalse(matcher.find());
	}

	@Test
	public void testDefaultPluralStrShouldBeEmpty() throws IOException {
		Message msg = new Message();
		msg.setMsgid("id");
		msg.setMsgidPlural("ids");

		String str = serializeMsg(msg);
		Pattern pattern = Pattern.compile("^msgstr\\[0\\]\\s+\"\"$", Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(str);
		Assert.assertTrue(matcher.find());
		Assert.assertFalse(matcher.find());
	}

   @Test
   public void tabShouldNotBeEncoded() throws IOException {
      Message msg = new Message();
      msg.setMsgid("msgid\tmsgid");
      msg.setMsgstr("msgstr\tmsgstr");

      encodeTabs = false;
      String str = serializeMsg(msg);
      Assert.assertThat(str, containsString("msgid \"msgid\tmsgid\""));
      Assert.assertThat(str, containsString("msgstr \"msgstr\tmsgstr\""));
   }

   @Test
   public void tabShouldBeEncoded() throws IOException {
      Message msg = new Message();
      msg.setMsgid("msgid\tmsgid");
      msg.setMsgstr("msgstr\tmsgstr");

      encodeTabs = true;
      String str = serializeMsg(msg);
      Assert.assertThat(str, containsString("msgid \"msgid\\tmsgid\""));
      Assert.assertThat(str, containsString("msgstr \"msgstr\\tmsgstr\""));
   }
}
