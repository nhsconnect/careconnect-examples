package uk.nhs.careconnect.server.dstu2;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.to.TesterConfig;
import ca.uhn.fhir.to.mvc.AnnotationMethodHandlerAdapterConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

//@formatter:off

/**
 * This spring config file configures the web testing module. It serves two
 * purposes:
 * 1. It imports FhirTesterMvcConfig, which is the spring config for the
 *    tester itself
 * 2. It tells the tester which server(s) to talk to, via the testerConfig()
 *    method below
 */
@Configuration
@EnableWebMvc
@ComponentScan(
        basePackages = {"ca.uhn.fhir.to"}
)
public class UIConfig extends WebMvcConfigurerAdapter {


    private static final Logger log = LoggerFactory.getLogger(UIConfig.class);


    public UIConfig() {

    }
	/**
	 * This bean tells the testing webpage which servers it should configure itself
	 * to communicate with. In this example we configure it to talk to the local
	 * server, as well as one public server. If you are creating a project to 
	 * deploy somewhere else, you might choose to only put your own server's 
	 * address here.
	 * 
	 * Note the use of the ${serverBase} variable below. This will be replaced with
	 * the base URL as reported by the server itself. Often for a simple Tomcat
	 * (or other container) installation, this will end up being something
	 * like "http://localhost:8080/hapi-fhir-jpaserver-example". If you are
	 * deploying your server to a place with a fully qualified domain name, 
	 * you might want to use that instead of using the variable.
	 */
	@Bean
	public TesterConfig testerConfig() {
		TesterConfig retVal = new TesterConfig();
		
		retVal
			.addServer()
				.withId("home")
				.withFhirVersion(FhirVersionEnum.DSTU2)
				.withBaseUrl("${serverBase}/Dstu2")
				.withName("Local Care Connect Test Server");

		return retVal;
	}

	/*
    @RequestMapping(value = { "/error" })
    public String actionHistoryServer(final HttpServletRequest theReq, final HomeRequest theRequest, final BindingResult theBindingResult, final ModelMap theModel) {
       // doActionHistory(theReq, theRequest, theBindingResult, theModel, "history-server", "Server History");
        log.info("OOOOPPPSIE");
        return "result";
    }

    private void doActionHistory(HttpServletRequest theReq, HomeRequest theRequest, BindingResult theBindingResult, ModelMap theModel, String s, String s1) {
    }

    @ControllerAdvice
    public class ExceptionHandlerController {
        @ExceptionHandler(Exception.class)
        public String handleException(Exception e) {
            log.info(e.getMessage());
            e.printStackTrace();
            return "404";// view name for 404 error
        }
    }
*/
	@Bean
	public SpringResourceTemplateResolver templateResolver() {
		SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
		resolver.setPrefix("classpath:/static/templates/");
		resolver.setSuffix(".html");
		resolver.setTemplateMode(TemplateMode.HTML);
		resolver.setCharacterEncoding("UTF-8");
		return resolver;
	}





    @Override
	public void addResourceHandlers(ResourceHandlerRegistry theRegistry) {
	    // Advice is to not map the resource handlers

	    String path = "";
        log.info("addResourceHandlers - I SHOULD BE CALLED TO SET UP THE RESOURCE HANDLERS");
		theRegistry.addResourceHandler(new String[]{"/css/**"}).addResourceLocations(new String[]{"/"});
		theRegistry.addResourceHandler(new String[]{"/fa/**"}).addResourceLocations(new String[]{"classpath:/fa/"});
		theRegistry.addResourceHandler(new String[]{"/fonts/**"}).addResourceLocations(new String[]{path+"/fonts/"});
		theRegistry.addResourceHandler(new String[]{"/img/**"}).addResourceLocations(new String[]{"classpath:/img/"});
		theRegistry.addResourceHandler(new String[]{"/js/**"}).addResourceLocations(new String[]{"classpath:/static/js/","classpath:/js/","file:classes/js/","file:/Development/FHIRTest/careconnect-hapi-jpa-dstu2-springboot/target/classes/js/"});


	}


	@Bean
	public AnnotationMethodHandlerAdapterConfigurer annotationMethodHandlerAdapterConfigurer() {
		return new AnnotationMethodHandlerAdapterConfigurer();
	}

	@Bean
	public ThymeleafViewResolver viewResolver() {
		ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
		viewResolver.setTemplateEngine(this.templateEngine());
		viewResolver.setCharacterEncoding("UTF-8");
		return viewResolver;
	}

	@Bean
	public SpringTemplateEngine templateEngine() {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.setTemplateResolver(this.templateResolver());
		return templateEngine;
	}
	
}
//@formatter:on
