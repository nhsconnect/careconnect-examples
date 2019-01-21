package uk.nhs.careconnect.ri.client.validation.rest;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class HttpClientBuilder {
    public HttpClientBuilder() {

    }

    public HttpClient build() {
        return new HttpClient(buildTemplate(getRestBuilder()));
    }
    private RestTemplateBuilder getRestBuilder() {
        return new RestTemplateBuilder(customRestTemplateCustomizer());
    }

    public CustomRestTemplateCustomizer customRestTemplateCustomizer() {
        return new CustomRestTemplateCustomizer();
    }

    private static RestTemplate buildTemplate(RestTemplateBuilder restBuilder) {
        final Map<String, String> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, APPLICATION_JSON_VALUE);
        headers.put(ACCEPT, APPLICATION_JSON_VALUE);
        RestTemplateBuilder httpTemplate = restBuilder.additionalInterceptors(
                new SetHeaderInterceptor(headers));
        return httpTemplate.build();
    }
}
