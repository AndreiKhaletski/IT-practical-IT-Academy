package by.it_academy.jd2.account.controller.filter;

import by.it_academy.jd2.account.config.properites.ServiceHostProperty;
import by.it_academy.jd2.account.service.detailesservice.CustomUserDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
public class HttpSecurityClient {

    private static final String SECURITY_VERIFICATION_URI = "/security/token/verification";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.of(10, ChronoUnit.SECONDS))
            .build();

    private final JsonMapper jsonMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private final ServiceHostProperty serviceHostProperty;

    public HttpSecurityClient(ServiceHostProperty serviceHostProperty) {
        this.serviceHostProperty = serviceHostProperty;
    }

    public CustomUserDetails verifyToken(HttpServletRequest originalRequest) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(serviceHostProperty.getSecurity() + SECURITY_VERIFICATION_URI))
                .header(AUTHORIZATION, originalRequest.getHeader(AUTHORIZATION))
                .build();

        HttpResponse<String> httpResponse;
        try {
            httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (httpResponse.statusCode() != 200) {
            throw new IllegalArgumentException("");
        }

        try {
            return jsonMapper.readValue(httpResponse.body(), CustomUserDetails.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
