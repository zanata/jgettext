package org.fedorahosted.tennera.jgettext;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Assert {

	public static void assertNoDiff(File file1, File file2){
		
		ProcessBuilder pb = new ProcessBuilder("diff", file1.getAbsolutePath(), file2.getAbsolutePath());
		Process p = null;
		try {
			p = pb.start();
			p.waitFor();
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
		String diff =  convertStreamToString(p.getInputStream());
		
		if(!diff.trim().isEmpty())
			fail("Files have differences:\n"+diff);
	}
	
    private static String convertStreamToString(InputStream is) {

    	BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
 
        return sb.toString();
    }

}
