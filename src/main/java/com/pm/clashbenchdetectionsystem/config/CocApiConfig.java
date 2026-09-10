package com.pm.clashbenchdetectionsystem.config;

import com.pm.clashbenchdetectionsystem.cocAPI.CocApiClient;
import com.pm.clashbenchdetectionsystem.common.exception.CocApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Slf4j
@Configuration
public class CocApiConfig {

    @Bean
    public CocApiClient cocApiClient(CocApiProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.timeout().connect());
        requestFactory.setReadTimeout(properties.timeout().read());

        DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory(properties.baseUrl());
        uriFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        RestClient restClient = RestClient.builder()
                .uriBuilderFactory(uriFactory)
                .defaultHeader("Authorization", "Bearer " + properties.token())
                .requestFactory(requestFactory)
                .requestInterceptor((request, body, execution) -> {
                    log.debug("CoC API request: {} {}", request.getMethod(), request.getURI());
                    return execution.execute(request, body);
                })
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    int status = response.getStatusCode().value();
                    log.warn("CoC API error {} for {}", status, request.getURI());
                    if (status == 403) throw CocApiException.forbidden();
                    if (status == 404) throw CocApiException.notFound(request.getURI().getPath());
                    if (status == 429) throw CocApiException.rateLimited();
                    throw CocApiException.serverError();
                })
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory.builderFor(adapter).build();
        return proxyFactory.createClient(CocApiClient.class);
    }
}
