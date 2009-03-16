/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.webgettext;

import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;


/**
 * This servlet filter is responsible for 
 * (a) keeping the request locale in a thread-local for later use 
 * by i18n classes, and 
 * (b) loading the localised resource bundle for the current request, 
 * and storing it as a request attribute 'messages' to allow EL lookups 
 * such as ${messages["my key"]}. 
 * 
 * @author <a href="mailto:sflaniga@redhat.com">Sean Flanigan</a>
 * @version $Revision: $
 */
public class I18nFilter implements Filter
{

   static final Logger log = Logger.getLogger(I18nFilter.class);
   private String bundleName = null;

   public void destroy()
   {
      // nop
   }

   
   public void init(FilterConfig config) throws ServletException
   {
      String bundle = config.getInitParameter("bundle"); //$NON-NLS-1$
      setBundleName(bundle);
      log.debug("Using bundle "+bundle+" for "+config.getServletContext().getContextPath()); //$NON-NLS-1$ //$NON-NLS-2$
   }
   
   private void setBundleName(String bundleName)
   {
      this.bundleName = bundleName;
   }

   public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
         ServletException
   {
      HttpServletRequest httpReq = (HttpServletRequest)req;
      HttpServletResponse httpResp = (HttpServletResponse) resp;
      String path = httpReq.getServletPath();
      String uri;
      if (httpReq.getQueryString() == null)
         uri = httpReq.getRequestURI();
      else
         uri = httpReq.getRequestURI()+"?"+httpReq.getQueryString(); //$NON-NLS-1$
      if (log.isTraceEnabled())
      {
         log.trace("doFilter called for "+uri); //$NON-NLS-1$
      }
      //I18nUtil.reloadBundlesHack();
      ResourceBundle bundle;
      Locale localeFromCookie = null;
      Cookie[] cookies = httpReq.getCookies();
      if (cookies != null)
      {
         for (int i = 0; i < cookies.length; i++)
         {
            Cookie cookie = cookies[i];
            if("locale".equals(cookie.getName())) //$NON-NLS-1$
            {
               localeFromCookie = I18nUtil.toLocale(cookie.getValue());
               if(log.isTraceEnabled())
               {
                  log.trace("Using locale from cookie: "+localeFromCookie); //$NON-NLS-1$
               }
            }
         }
      }
      final Locale locale;
      if (localeFromCookie != null)
      {
         locale = localeFromCookie;
      }
      else
      {
         //      req.getLocales()
         locale = req.getLocale();
      }
      try 
      {
    	  bundle = ResourceBundle.getBundle(bundleName, locale);
          if(locale.getLanguage().equals(Locale.ENGLISH.getLanguage()))
          {
             if (log.isTraceEnabled())
             {
                log.trace("locale is "+locale+"; using FallbackResourceBundle"); //$NON-NLS-1$ //$NON-NLS-2$
             }
             bundle = new FallbackResourceBundle(bundle);
          }
      } 
      catch (MissingResourceException e) 
      {
         if(locale.getLanguage().equals(Locale.ENGLISH.getLanguage()))
         {
            if (log.isTraceEnabled())
            {
               log.trace("locale is "+locale+"; using IdentityResourceBundle"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            bundle = new IdentityResourceBundle();
         }
         else
         {
            if (log.isTraceEnabled())
            {
               log.trace("locale is "+locale+"; using MungingResourceBundle"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            bundle = new MungingResourceBundle(locale);
         }
      }

      req.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
      resp.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
      bundle = LoggingResourceBundle.wrap(bundle, bundleName, locale);
      req.setAttribute("messages", bundle); //$NON-NLS-1$
      I18nUtil.setRequestLocale(locale);
      httpReq.getSession().setAttribute("locale", locale); //$NON-NLS-1$
      HttpServletRequestWrapper wrapReq = new HttpServletRequestWrapper(httpReq)
      {
         public Locale getLocale() { return locale; }
      };
      if (path != null && path.endsWith(".html")) //$NON-NLS-1$
      {
         String jsp;
         if(path.endsWith("/index.html")) //$NON-NLS-1$
         {
            jsp = "."; //$NON-NLS-1$
         }
         else
         {
            int slash = path.lastIndexOf('/');
            jsp = path.substring(slash+1, path.length()-".html".length())+".jsp"; //$NON-NLS-1$ //$NON-NLS-2$
         }
         
         httpResp.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
         httpResp.addHeader("Location", jsp); //$NON-NLS-1$
      }
      else
      {
         chain.doFilter(wrapReq, resp);
      }
      I18nUtil.clearRequestLocale();
   }

}
