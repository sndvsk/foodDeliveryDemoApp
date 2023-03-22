package com.example.FoodDeliveryDemoApp.service.ExternalWeatherData;

import com.example.FoodDeliveryDemoApp.exception.ExternalServiceException;
import com.example.FoodDeliveryDemoApp.exception.UnauthorizedException;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class ExternalWeatherDataServiceImpl implements ExternalWeatherDataService {

    @Value("${weather.data.url}")
    private String weatherObservationsUrl;

    private final WebClient webClient;

    public ExternalWeatherDataServiceImpl() {
        this.webClient = WebClient.builder()
                .defaultHeader("Accept", MediaType.APPLICATION_XML_VALUE)
                .build();
    }

    /**
     * Retrieves weather observations from an external service using WebClient.
     *
     * @return a string representation of the weather observations response.
     * @throws NotFoundException if the requested data is not found.
     * @throws UnauthorizedException if the request is unauthorized.
     * @throws ExternalServiceException if there is an error in the external service.
     * @throws RuntimeException if an unknown error occurs.
     */
    @Override
    public String retrieveWeatherObservations() {
        try {
            String userAgent = "PostmanRuntime/7.31.1";

            return webClient.get()
                    .uri(weatherObservationsUrl)
                    .header("User-Agent", userAgent)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        } catch (WebClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw new NotFoundException("Data not found");
            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new UnauthorizedException("Unauthorized access");
            } else if (e.getStatusCode().is5xxServerError()) {
                throw new ExternalServiceException("Error in external service");
            } else {
                throw new RuntimeException("Unknown error occurred");
            }
        }
    }
}
