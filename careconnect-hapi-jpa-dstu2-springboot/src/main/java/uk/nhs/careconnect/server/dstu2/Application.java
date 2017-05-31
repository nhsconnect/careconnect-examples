package uk.nhs.careconnect.server.dstu2;


import io.hawt.config.ConfigFacade;
import io.hawt.springboot.EnableHawtio;
import io.hawt.system.ConfigManager;
import io.hawt.web.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.DispatcherServlet;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

@ServletComponentScan
@SpringBootApplication
@EnableHawtio
public class Application extends SpringBootServletInitializer implements WebApplicationInitializer {
	
	@Autowired
	private ServletContext servletContext;

	@Autowired
	private JpaServerDemo jpaServerDemo;

   // private static final Logger log = LoggerFactory.getLogger(Application.class);
	
	public static void main(String[] args) {
		System.setProperty(AuthenticationFilter.HAWTIO_AUTHENTICATION_ENABLED, "false");
		System.setProperty("server.port", "8181");
        System.setProperty("server.contextPath", "");
		//Systen.setProperty("spring.resources")
        SpringApplication.run(Application.class, args);
    }


	// This section copied from https://github.com/bowdoincollege/spring-boot-camel-sample

	@PostConstruct
	public void init() {
		final ConfigManager configManager = new ConfigManager();
		configManager.init();
		servletContext.setAttribute("ConfigManager", configManager);
	}


	@Bean
	public ConfigFacade configFacade() throws Exception {
		ConfigFacade config = new ConfigFacade() {
			public boolean isOffline() {
				return true;
			}
		};
		config.init();
		return config;
	}

	/*

	Mapping appears to be work but overlay is resulting in error
    */



    @Bean
    public ServletRegistrationBean hapiUI() {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.register(UIConfig.class);
        dispatcherServlet.setApplicationContext(applicationContext);
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(dispatcherServlet, "/");
        servletRegistrationBean.setName("spring");
        return servletRegistrationBean;
    }


	@Bean
	public ServletRegistrationBean FHIRServletRegistrationBean(JpaServerDemo jpaServer) {

		ServletRegistrationBean registration = new ServletRegistrationBean( jpaServer, "/Dstu2/*");
		registration.setName("fhirServlet");
        registration.addInitParameter("ImplementationDescription","Care Connect FHIR Server - Dstu2");
        registration.addInitParameter("FhirVersion","DSTU2");
        registration.setLoadOnStartup(1);
		return registration;
	}

	@Bean
	public FilterRegistrationBean corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		//config.addAllowedOrigin("http://example.com");
		config.addAllowedOrigin("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		source.registerCorsConfiguration("/**", config);
		FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
		bean.setOrder(0);
		return bean;
	}

 /*
    @Bean(name = "LogQuery", destroyMethod = "stop", initMethod = "start")
    @Scope("singleton")
    @Lazy(false)
    public Log4jLogQuery logQuery()
    {
        Log4jLogQuery log4j = new Log4jLogQuery();
        return log4j;
    }
*/

}
