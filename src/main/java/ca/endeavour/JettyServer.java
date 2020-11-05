/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.endeavour;

/**
 *
 * @author Dave
 */
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

public class JettyServer
{
    //private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    //public static final List<W1Device>

    public static void main(String[] args) throws Exception
    {
        //ApplicationConfig applicationConfig = new ApplicationConfig();
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");

        //AnnotationConfigWebApplicationContext spring = new AnnotationConfigWebApplicationContext();
        //ResourceHandler resourceHandler = new ResourceHandler();
        //spring.setConfigLocation(getClass().getPackage().getName());
        //AnnotationConfigWebApplicationContext spring = new AnnotationConfigWebApplicationContext();
        //spring.set
        
        context.addEventListener(new ContextLoaderListener());
        //context.addEventListener(new RequestContextListener());
        context.setInitParameter("contextClass", AnnotationConfigWebApplicationContext.class.getName());
        context.setInitParameter("contextConfigLocation", SpringConfig.class.getName());

        ServletHolder jaxrs = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/rest/*");
        jaxrs.setInitParameter("jersey.config.server.provider.classnames", RestApi.class.getName());
        jaxrs.setInitOrder(0);
        
        ServletHolder staticServlet = context.addServlet(DefaultServlet.class,"/*");
        //staticServlet.setInitParameter("resourceBase","jar:file:!/webapp");
        staticServlet.setInitParameter("pathInfoOnly","true");
//        DefaultServlet defaultServlet = new DefaultServlet();
//        URL url = JettyServer.class.getClassLoader().getResource("/web");
        context.setBaseResource( Resource.newClassPathResource("/webapp")  ); //"jar:file:!/webapp");
        context.setWelcomeFiles(new String[] { "dashboard.html" });
//        context.addServlet(new ServletHolder(defaultServlet), "web/*");
        
        Server server = new Server(80);
        server.setHandler(context);

        // Tells the Jersey Servlet which REST service/class to load.
        try
        {
            server.start();
            server.join();
        }
        catch( Throwable t )
        {
            t.printStackTrace(System.err);
        }
        finally
        {
            server.destroy();
            //scheduler.shutdown();
        }
    }
}
