package uk.nhs.careconnect.messagingapi.camel;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.hl7.fhir.instance.model.AuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConvertToJSON implements Processor {

	public ConvertToJSON (FhirContext ctx)
	{
		this.ctx = ctx;
	
	}
	private FhirContext ctx;
	
	private static final Logger log = LoggerFactory.getLogger(uk.nhs.careconnect.messagingapi.camel.ConvertToJSON.class);
	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		
		AuditEvent audit = null;
		
		Reader reader = new InputStreamReader(new ByteArrayInputStream ((byte[]) exchange.getIn().getBody(byte[].class)));

		IParser JsonParser = ctx.newJsonParser();
		IParser XMLParser = ctx.newXmlParser();

		// Assume JSON if not present
		if (exchange.getIn().getHeader(Exchange.CONTENT_TYPE) == null || exchange.getIn().getHeader(Exchange.CONTENT_TYPE).toString().contains("json"))
		{

			try
			{
				audit = JsonParser.parseResource(AuditEvent.class,reader);
			}
			catch(Exception ex)
			{
				log.error("#9 JSON Parse failed "+ex.getMessage());

				if (exchange.getIn().getHeader(Exchange.CONTENT_TYPE) == null) {

					try
					{
					    reader.reset();
						audit = XMLParser.parseResource(AuditEvent.class,reader);
					}
					catch(Exception ex2)
					{
						log.error("#10 XML Parse failed (after trying JSON parse) "+ex.getMessage());
					}
				}
			}
		}
		else
		{

			try
			{
				audit = XMLParser.parseResource(AuditEvent.class,reader);
			}
			catch(Exception ex)
			{
				log.error("#10 XML Parse failed "+ex.getMessage());
			}
		}
		
		// Stop http FHIR queries from processing input parameters
		exchange.getIn().removeHeaders("*");
		
		if (audit != null)
		{
						
			String Response = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(audit);
			
			exchange.getIn().setBody(Response);
			exchange.getIn().setHeader(Exchange.HTTP_QUERY,"");
			exchange.getIn().setHeader(Exchange.HTTP_METHOD,"POST");
			// Store in date indices
			String timeStamp = new SimpleDateFormat("yyyy.MM.dd").format(new Date());
			exchange.getIn().setHeader(Exchange.HTTP_PATH,"fhir-"+ timeStamp +"/AuditEvent");
			exchange.getIn().setHeader(Exchange.CONTENT_TYPE,"application/json");
			exchange.getIn().setBody(Response);
		}
	}
}


