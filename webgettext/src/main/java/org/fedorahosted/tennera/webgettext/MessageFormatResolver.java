/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.webgettext;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolves strings by accumulating the format arguments and then formatting 
 * the pattern and its arguments with MessageFormat when toString() is called.  
 * If no arguments are supplied, the pattern string is simply returned verbatim.
 * @author <a href="mailto:sflaniga@redhat.com">Sean Flanigan</a>
 * @version $$Revision: $$
 */
public class MessageFormatResolver
{
   private static final org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(MessageFormatResolver.class);
   
   private final String pattern;

   private final List<Object> args = new ArrayList<Object>();

   public MessageFormatResolver(String pattern)
   {
      log.trace("<init>('"+pattern+"')"); //$NON-NLS-1$ //$NON-NLS-2$
      this.pattern = pattern;
   }

   public void addArg(Object arg)
   {
      log.trace("addArg(["+arg.getClass().getName()+"] '"+arg+"')"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      args.add(arg);
   }

   public String toString()
   {
      if (args.size() == 0)
         return pattern;
      return MessageFormat.format(pattern, args.toArray());
   }

}