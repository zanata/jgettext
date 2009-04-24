package org.fedorahosted.tennera.jgettext;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestMessage {
	
	@Test
	public void testIsFuzzyWhenEmptyMsgstr(){
		Message msg = new Message();
		msg.setMsgid("hello world!");
		msg.setMsgstr("");
		assertFalse(msg.isFuzzy());
		
		msg.markFuzzy();
		assertFalse("Empty msgstr should not produce fuzzy", msg.isFuzzy());
	}

	public void testIsFuzzyWhenMarkedAsFuzzy(){
		Message msg = new Message();
		msg.setMsgid("hello world!");
		msg.setMsgstr("hei verden");
		msg.markFuzzy();
		assertTrue(msg.isFuzzy());
		msg.setFuzzy(false);
		assertFalse(msg.isFuzzy());
	}

}
