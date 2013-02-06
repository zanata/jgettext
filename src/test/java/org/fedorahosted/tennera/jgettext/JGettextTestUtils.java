package org.fedorahosted.tennera.jgettext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import org.fedorahosted.tennera.jgettext.catalog.parse.MessageStreamParser;
import org.fedorahosted.tennera.jgettext.catalog.parse.ParseException;

public class JGettextTestUtils
{

   // NOTE: We assume all po files in test are using UTF-8 in their header. If
   // this is not true test will break
   public static final String CHARSET_NAME = "UTF-8";

   public static void testRoundTrip(String message, File f) throws FileNotFoundException, ParseException, IOException
   {
      System.out.println(f.getPath() + ":");
      String output = roundTrip(f);

      // System.out.println("(original):");
      // String origInput = readToString(f);
      // System.out.println(origInput);
      // System.out.println();
      String msgcatInput = readToStringFromMsgcat(f, false);
      // System.out.println("(msgcat):");
      // System.out.println(msgcatInput);
      // System.out.println();

      // System.out.println("(jgettext):");
      // System.out.println(output);
      // System.out.println();
      String msgcatOutput = readToStringFromMsgcat(output, false);
      // System.out.println("(jgettext+msgcat):");
      // System.out.println(msgcatOutput);
      // System.out.println();

      assertEquals(message, msgcatInput, msgcatOutput);
   }

   public static void testRoundTrip(File f) throws ParseException, IOException
   {
      testRoundTrip(null, f);
   }

   public static String roundTrip(File original) throws ParseException, IOException
   {
      PoWriter poWriter = new PoWriter();
      MessageStreamParser parser = new MessageStreamParser(original);
      StringWriter outputWriter = new StringWriter();
      while (parser.hasNext())
      {
         poWriter.write(parser.next(), outputWriter);
      }
      outputWriter.flush();
      return outputWriter.toString();
   }

   public static String readToStringFromMsgcat(File file, boolean ignoreErrors)
   {
      BufferedReader reader = null;
      StringBuilder sb = new StringBuilder();
      String[] cmd_elements = { "/usr/bin/msgcat", file.getAbsolutePath() };
      try
      {
         Process process = Runtime.getRuntime().exec(cmd_elements);
         InputStream cmd_output = process.getInputStream();
         reader = new BufferedReader(new InputStreamReader(cmd_output, CHARSET_NAME));
         String line;
         while ((line = reader.readLine()) != null)
         {
            sb.append(line).append("\n");
         }
         if (!ignoreErrors)
         {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorStr = new StringBuilder();
            while ((line = errorReader.readLine()) != null)
            {
               errorStr.append(line);
               errorStr.append("\n");
            }
            assertTrue("Error parsing input file:\n" + errorStr.toString(), errorStr.toString().isEmpty());
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      finally
      {
         closeQuietly(reader);
      }

      return sb.toString();
   }

   public static String readToStringFromMsgcat(String input, boolean ignoreErrors)
   {
      BufferedReader reader = null;
      StringBuilder sb = new StringBuilder();
      String[] cmd_elements = { "/usr/bin/msgcat", "-" };
      try
      {
         Process prcess = Runtime.getRuntime().exec(cmd_elements);
         prcess.getOutputStream().write(input.getBytes(CHARSET_NAME));
         prcess.getOutputStream().close();

         InputStream cmd_output = prcess.getInputStream();
         reader = new BufferedReader(new InputStreamReader(cmd_output, CHARSET_NAME));
         String line;
         while ((line = reader.readLine()) != null)
         {
            sb.append(line).append("\n");
         }

         if (!ignoreErrors)
         {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(prcess.getErrorStream()));
            StringBuilder errorStr = new StringBuilder();
            while ((line = errorReader.readLine()) != null)
            {
               errorStr.append(line);
               errorStr.append("\n");
            }
            assertTrue("Error parsing output file:\n" + errorStr.toString() + "\n\nOutput was:\n" + input, errorStr.toString().isEmpty());
         }

      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      finally
      {
         closeQuietly(reader);
      }

      return sb.toString();
   }

   private static void closeQuietly(Reader reader)
   {
      try
      {
         if (reader != null)
         {
            reader.close();
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

}
