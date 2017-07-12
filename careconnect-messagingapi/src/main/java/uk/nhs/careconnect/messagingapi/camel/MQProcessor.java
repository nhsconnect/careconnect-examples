package uk.nhs.careconnect.messagingapi.camel;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.parser.IParser;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Created by kevinmayfield on 12/07/2017.
 */
public class MQProcessor implements Processor {

    FhirContext ctx;

    public MQProcessor(FhirContext ctx)
    {
        this.ctx = ctx;
    }
    @Override
    public void process(Exchange exchange) throws Exception {

     //   InputStream is = (InputStream) exchange.getIn().getBody();
     //   is.reset();
        String reader = exchange.getIn().getBody(String.class);
        IParser parser = ctx.newXmlParser();

        if (exchange.getIn().getHeader("FHIRResource").equals("Observation")) {
            Observation observation = parser.parseResource(Observation.class,reader);
            exchange.getIn().setHeader("FHIRConditionUrl","Observation?identifier="+observation.getIdentifier().get(0).getSystem()+"%7C"+observation.getIdentifier().get(0).getValue());
        }

    }
}
