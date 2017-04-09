package com.berzellius.integrations.amocrmru.service;

import com.berzellius.integrations.amocrmru.dto.api.amocrm.AmoCRMContact;
import com.berzellius.integrations.amocrmru.dto.api.amocrm.AmoCRMEntities;
import com.berzellius.integrations.amocrmru.dto.api.amocrm.AmoCRMEntity;
import com.berzellius.integrations.amocrmru.dto.api.amocrm.request.AmoCRMContactsGetRequest;
import com.berzellius.integrations.amocrmru.dto.api.amocrm.request.AmoCRMRequest;
import com.berzellius.integrations.amocrmru.dto.api.amocrm.response.AmoCRMContactsResponse;
import com.berzellius.integrations.basic.dto.api.errorhandlers.APIRequestErrorException;
import com.berzellius.integrations.basic.exception.APIAuthException;
import com.berzellius.integrations.basic.service.APIServiceRequestsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by berz on 09.01.2017.
 */
public abstract class AmoCRMAPIRequestsBuilder extends APIServiceRequestsImpl {
    protected void updateLast_modified(AmoCRMEntities amoCRMEntities){
        if(amoCRMEntities.getUpdate() == null){
            return;
        }

        for(AmoCRMEntity amoCRMEntity : amoCRMEntities.getUpdate()){
            // Увеличиваем last_modified, иначе изменения не будут учтены
            amoCRMEntity.setLast_modified(amoCRMEntity.getLast_modified() + 1);
        }
    }

    protected void request(AmoCRMRequest amoCRMRequest, String url) throws APIAuthException {
        request(amoCRMRequest, url, String.class);
    }


    protected HttpEntity<AmoCRMRequest> requestByParams(AmoCRMRequest params){
        return requestByParamsAbstract(params);
    }

    protected HttpEntity<MultiValueMap<String, String>> requestByParams(MultiValueMap<String, String> params){
        return requestByParamsAbstract(params);
    }

    protected  <T> HttpEntity<T> request(AmoCRMRequest amoCRMRequest, String url, Class<T> cl) throws APIAuthException {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(this.errorHandler);

        HttpEntity<T> response;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            if (amoCRMRequest.getHttpMethod().equals(HttpMethod.GET)) {
                HttpEntity<MultiValueMap<String, String>> requestHttpEntity = plainHttpEntity();

                UriComponentsBuilder uriComponentsBuilder = this.uriComponentsBuilderByParams(
                        amoCRMRequest, this.getApiBaseUrl().concat(url)
                );

                response = restTemplate.exchange(
                        uriComponentsBuilder.build().encode().toUri(),
                        amoCRMRequest.getHttpMethod(), requestHttpEntity, cl
                );
            } else {
                HttpEntity<AmoCRMRequest> requestHttpEntity = jsonHttpEntity(amoCRMRequest);

                try {
                    System.out.println(objectMapper.writeValueAsString(requestHttpEntity.getBody()));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    System.out.println("Cant present response as JSON object!");
                }

                response = restTemplate.exchange(
                        this.getApiBaseUrl().concat(url),
                        amoCRMRequest.getHttpMethod(), requestHttpEntity, cl
                );
            }

            this.relogins = 0;

            return response;
        } catch (APIRequestErrorException e) {
            System.out.println("request to amocrm failed with error: " + e.getParams().toString());

            if (e.getParams().get("code") != null && e.getParams().get("code").equals("401")) {
                if (this.relogins < this.maxRelogins) {
                    this.logIn();
                    this.relogins++;
                    System.out.println("Login to AmoCRM!");
                    return request(amoCRMRequest, url, cl);
                } else {
                    System.out.println("Maximum relogins count (" + this.maxRelogins + ") reached for AmoCRM!");
                    return null;
                }
            } else return null;
        } catch (RuntimeException e){
            e.printStackTrace();
            return null;
        }
    }

    protected HttpEntity<MultiValueMap<String, String>> plainHttpEntity() throws APIAuthException {
        if (cookies == null) {
            this.logIn();
        }

        return super.plainHttpEntity();
    }

    protected HttpEntity<AmoCRMRequest> jsonHttpEntity(AmoCRMRequest req) throws APIAuthException {

        if (cookies == null) {
            this.logIn();
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add("Cookie", String.join(";", this.cookies));


        HttpEntity<AmoCRMRequest> requestHttpEntity = new HttpEntity<>(req, httpHeaders);

        return requestHttpEntity;
    }

    public List<AmoCRMContact> getContactsByQuery(String query, Long limit, Long offset) throws APIAuthException {
        AmoCRMContactsGetRequest amoCRMContactsGetRequest = new AmoCRMContactsGetRequest();
        amoCRMContactsGetRequest.setQuery(query);
        amoCRMContactsGetRequest.setLimit_rows(limit);
        amoCRMContactsGetRequest.setLimit_offset(offset);

        HttpEntity<AmoCRMContactsResponse> response = request(
                amoCRMContactsGetRequest, "contacts/list", AmoCRMContactsResponse.class);

        AmoCRMContactsResponse amoCRMContactsResponse = response.getBody();

        if (amoCRMContactsResponse == null || amoCRMContactsResponse.getResponse() == null) {
            return new LinkedList<>();
        }

        return amoCRMContactsResponse.getResponse().getContacts();
    }

    protected String userLogin;
    protected String userHash;
    protected String loginUrl;
    protected String apiBaseUrl;
    protected ArrayList<Long> leadClosedStatusesIDs = new ArrayList<>();

    protected Integer maxRelogins;
    protected Integer relogins = 0;

    protected boolean isReadonlyMode(){
        return false;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }
}
