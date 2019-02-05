package uk.nhs.careconnect.ri.client.validation.rest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class HttpClient {

    private final RestTemplate restTemplate;

    public HttpClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String fetchData(String uri, HttpMethod method, String body) {
        MultiValueMap<String, String> headers =new LinkedMultiValueMap<>();

        // Query parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri);

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> httpResponse = restTemplate.exchange(builder.build().toUri(), method, httpEntity, String.class);
            return httpResponse.getBody();
        } catch (final HttpClientErrorException e) {
            return e.getResponseBodyAsString();
        }
        catch (final HttpServerErrorException e) {
            return e.getResponseBodyAsString();
        }
    }

}
