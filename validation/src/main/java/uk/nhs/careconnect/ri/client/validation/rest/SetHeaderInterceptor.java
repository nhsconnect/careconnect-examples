package uk.nhs.careconnect.ri.client.validation.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Map;

public class SetHeaderInterceptor implements ClientHttpRequestInterceptor {

    private Map<String, String> headers;

    public SetHeaderInterceptor(final Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
                                        final ClientHttpRequestExecution execution) throws IOException {
        final HttpHeaders requestHeaders = request.getHeaders();
        headers.forEach(requestHeaders::add);
        return execution.execute(request, body);
    }
}
