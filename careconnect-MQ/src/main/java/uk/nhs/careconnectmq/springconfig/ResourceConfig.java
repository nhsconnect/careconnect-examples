package uk.nhs.careconnectmq.springconfig;


import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.hooks.SpringContextHook;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.camel.component.jms.JmsConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

@Configuration
@PropertySource("classpath:ActiveMQ.properties")
public class ResourceConfig  {
	
	 
	@Autowired
	protected Environment env;
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	
	@Bean
	public BrokerService brokerService() 
	{
		BrokerService broker = new BrokerService();
		
		broker.setBrokerName("CareConnectBroker");
		broker.setUseJmx(true);
		//useShutdownHook="false" useJmx="true"
		try {
			broker.setShutdownHooks(Collections.<Runnable> singletonList(new SpringContextHook()));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        broker.setPersistent(true);
        broker.setDataDirectory("careconnectmq");
                
        TransportConnector vm = new TransportConnector();
        vm.setName("vm");
        try {
			vm.setUri(new URI("vm://CareConnectBroker"));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			broker.addConnector(vm);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        TransportConnector tcp = new TransportConnector();
        tcp.setName("tcp");
        try {
			tcp.setUri(new URI("tcp://0.0.0.0:61616"));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
		try {
			broker.addConnector(tcp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return broker;
				
	}

		
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
		pooled.setMaxConnections(8);
		return pooled;
	}
	
	@Bean(name="jmsConfig")
	public JmsConfiguration jmsConfiguration()
	{
		JmsConfiguration jmsConfig = new JmsConfiguration();
		jmsConfig.setConnectionFactory(pooledConnectionFactory());
		jmsConfig.setConcurrentConsumers(5);
		return jmsConfig;
	}
			  
	@Bean(name="activemq")
	public ActiveMQComponent activeMQComponent()
	{
		ActiveMQComponent activeMQComponent= new ActiveMQComponent();
		activeMQComponent.setConfiguration(jmsConfiguration());
		return activeMQComponent;	
	}
	  		  		

}
