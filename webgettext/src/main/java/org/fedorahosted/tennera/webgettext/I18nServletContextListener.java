/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.webgettext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;

import org.jboss.logging.Logger;

/**
 * This servlet filter registers the BundleResolver with the JSP context.
 * @see BundleResolver
 * @author <a href="mailto:sflaniga@redhat.com">Sean Flanigan</a>
 * @version $$Revision: $$
 */
public class I18nServletContextListener implements ServletContextListener
{
   
   private static final Logger log = Logger.getLogger(I18nServletContextListener.class);

   public void contextDestroyed(ServletContextEvent arg0)
   {
      // nop
   }

   // may not be needed???
//   private static ELResolver createELResolver()
//   {
//      CompositeELResolver resolver = new CompositeELResolver();
//      resolver.add( new BundleResolver() );
//      resolver.add( new MapELResolver() );
//      resolver.add( new ListELResolver() );
//      resolver.add( new ResourceBundleELResolver() );
//      resolver.add( new BeanELResolver() );
//      return resolver;
//   }


   public void contextInitialized(ServletContextEvent servletContextEvent)
   {
      ServletContext servletContext = servletContextEvent.getServletContext();
      JspApplicationContext jspContext = JspFactory.getDefaultFactory().getJspApplicationContext(servletContext);
      jspContext.addELResolver(new BundleResolver());
//      jspContext.addELResolver(createELResolver());
      log.info("BundleResolver registered: "+servletContext.getContextPath()); //$NON-NLS-1$
   }

}
