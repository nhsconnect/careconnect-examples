package uk.nhs.careconnect.messagingapi.spring;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.MessageHeader;
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by kevinmayfield on 12/07/2017.
 */

public class messasgeSplitter {
    @SuppressWarnings("unchecked")

    @Autowired
    protected FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(uk.nhs.careconnect.messagingapi.camel.Route.class);


    public List<Message> extractResources(final Exchange exchange,CamelContext camelContext) {
            List<Message> answer = new ArrayList<Message>();

        Bundle bundle = new Bundle();

       // FhirContext ctx = FhirContext.forDstu2();

        //ByteArrayInputStream xmlContentBytes = new ByteArrayInputStream ((byte[]) enrichment.getIn().getBody(byte[].class));
        Reader reader = new InputStreamReader(new ByteArrayInputStream((byte[]) exchange.getIn().getBody(byte[].class)));


        if (exchange.getIn().getHeader(Exchange.CONTENT_TYPE).toString().contains("json"))
        {
            //JsonParser parser = new JsonParser();
            IParser parser = ctx.newJsonParser();
            try
            {
                bundle = parser.parseResource(Bundle.class,reader);
            }
            catch(Exception ex)
            {
               // log.error("#9 JSON Parse failed "+ex.getMessage());
            }
        }
        else
        {
            // XmlParser parser = new XmlParser();
            IParser parser = ctx.newXmlParser();
            try
            {
                bundle = parser.parseResource(Bundle.class,reader);
            }
            catch(Exception ex)
            {
               // log.error("#10 XML Parse failed "+ex.getMessage());
            }
        }

        for (int f = 0; f < bundle.getEntry().size(); f++ ) {
            log.info("Entry numnber "+f);
            Resource resource = bundle.getEntry().get(f).getResource();
            log.info(resource.getClass().getName());
            log.info(resource.getIdElement().getId());
            if (!(resource instanceof MessageHeader)) {

                String response = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource);
                DefaultMessage message = new DefaultMessage();
                message.setHeader("FHIRResource",resource.getClass().getCanonicalName());
                message.setHeader("FHIRResourceId",resource.getId());
                message.setBody(response);
                answer.add(message);
            }

        }

        return answer;
        }

}
