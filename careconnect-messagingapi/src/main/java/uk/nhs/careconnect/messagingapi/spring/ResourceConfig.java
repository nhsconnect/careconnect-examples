package uk.nhs.careconnect.messagingapi.spring;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IGenericClient;
import io.fabric8.insight.log.log4j.Log4jLogQuery;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.camel.component.jms.JmsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

//import io.fabric8.insight.log.log4j.Log4jLogQuery;


@Configuration
@PropertySource("classpath:careconnectmessagingapi.properties")
public class ResourceConfig  {

	@Bean
	public static FhirContext fhirContext()
	{
		return FhirContext.forDstu2();
	}

	@Bean
    public static IGenericClient GenericClient(FhirContext ctxFHIR) {
       // String serverBase = "http://127.0.0.1:8080/FHIRServer/DSTU2/";
        String serverBase = "http://fhirtest.uhn.ca/baseDstu2/";
        return ctxFHIR.newRestfulGenericClient(serverBase);
    }

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	 private static final Logger log = LoggerFactory.getLogger(ResourceConfig.class);

	  @Autowired
	  protected Environment env;

	@Bean(name ="jmsConnectionFactory" )
	public ActiveMQConnectionFactory activeMQConnectionFactory()
	{
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
		/*
		List<String> trusted = new ArrayList<String>();
		trusted.add("org.hl7.fhir.dstu3");
		trusted.add("ca.uhn.hl7v2.model");
		trusted.add("java.util.HashSet");

		factory.setTrustedPackages(trusted);*/
		factory.setTrustAllPackages(true);
		return factory;
	}

	@Bean(name="pooledConnectionFactory")
	public PooledConnectionFactory pooledConnectionFactory()
	{
		PooledConnectionFactory pooled = new PooledConnectionFactory();
		pooled.setConnectionFactory(activeMQConnectionFactory());
		pooled.setMaxConnections(5);
		return pooled;
	}

	@Bean(name="jmsConfig")
	public JmsConfiguration jmsConfiguration()
	{
		JmsConfiguration jmsConfig = new JmsConfiguration();
		jmsConfig.setConnectionFactory(pooledConnectionFactory());
		jmsConfig.setConcurrentConsumers(10);
		return jmsConfig;
	}

	@Bean(name="activemq")
	public ActiveMQComponent activeMQComponent()
	{
		ActiveMQComponent activeMQComponent= new ActiveMQComponent();
		activeMQComponent.setConfiguration(jmsConfiguration());
		return activeMQComponent;
	}


	   
	  @Autowired
	  Log4jLogQuery log4jLogQuery;


}
