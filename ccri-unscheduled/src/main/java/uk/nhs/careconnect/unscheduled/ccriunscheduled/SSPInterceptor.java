package uk.nhs.careconnect.unscheduled.ccriunscheduled;

import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SSPInterceptor implements IClientInterceptor {
    @Override
    public void interceptRequest(IHttpRequest iHttpRequest) {
        iHttpRequest.addHeader(Constants.HEADER_AUTHORIZATION, "Bearer eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJpc3MiOiJodHRwczovL2RlbW9uc3RyYXRvci5jb20iLCJzdWIiOiJodHRwczovL2ZoaXIubmhzLnVrL0lkL3Nkcy1yb2xlLXByb2ZpbGUtaWR8ZmFrZVJvbGVJZCIsImF1ZCI6Imh0dHBzOi8vbnJscy5jb20vZmhpci9kb2N1bWVudHJlZmVyZW5jZSIsImV4cCI6MTUzOTg3NDE4MSwiaWF0IjoxNTM5ODczODgxLCJyZWFzb25fZm9yX3JlcXVlc3QiOiJkaXJlY3RjYXJlIiwic2NvcGUiOiJwYXRpZW50L0RvY3VtZW50UmVmZXJlbmNlLnJlYWQiLCJyZXF1ZXN0aW5nX3N5c3RlbSI6Imh0dHBzOi8vZmhpci5uaHMudWsvSWQvYWNjcmVkaXRlZC1zeXN0ZW18MjAwMDAwMDAwMTE3IiwicmVxdWVzdGluZ19vcmdhbml6YXRpb24iOiJodHRwczovL2ZoaXIubmhzLnVrL0lkL29kcy1vcmdhbml6YXRpb24tY29kZXxBTVMwMSIsInJlcXVlc3RpbmdfdXNlciI6Imh0dHBzOi8vZmhpci5uaHMudWsvSWQvc2RzLXJvbGUtcHJvZmlsZS1pZHxmYWtlUm9sZUlkIn0=.");
        iHttpRequest.addHeader("fromASID", "200000000117");
        iHttpRequest.addHeader("toASID","999999999999");

        System.out.println(iHttpRequest.getUri());
        Map<String, List<String>> headers = iHttpRequest.getAllHeaders();
        Iterator it = headers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
         //   it.remove(); // avoids a ConcurrentModificationException
        }
    }

    @Override
    public void interceptResponse(IHttpResponse iHttpResponse) throws IOException {

    }
}
