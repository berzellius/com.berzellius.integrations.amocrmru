package com.berzellius.integrations.amocrmru.service;

import com.berzellius.integrations.amocrmru.dto.api.amocrm.*;
import com.berzellius.integrations.amocrmru.dto.api.amocrm.auth.AmoCRMAuthResponse;
import com.berzellius.integrations.amocrmru.dto.api.amocrm.request.*;
import com.berzellius.integrations.amocrmru.dto.api.amocrm.response.*;
import com.berzellius.integrations.basic.exception.APIAuthException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by berz on 02.10.2015.
 */
@Service
public class AmoCRMServiceImpl extends AmoCRMAPIRequestsBuilder implements AmoCRMService {

    @Override
    public void logIn() throws APIAuthException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders requestHeaders = new HttpHeaders();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        params.add("USER_LOGIN", this.getUserLogin());
        params.add("USER_HASH", this.getUserHash());

        //HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, requestHeaders);
        HttpEntity<MultiValueMap<String, String>> request = requestByParams(params);
        HttpEntity<AmoCRMAuthResponse> response = restTemplate.exchange(this.getLoginUrl(), HttpMethod.POST, request, AmoCRMAuthResponse.class);

        if (response.getBody().getResponse().auth) {
            if (response.getHeaders().containsKey("Set-Cookie")) {
                this.cookies = response.getHeaders().get("Set-Cookie");
            }
        } else throw new APIAuthException("cant authentificate to AmoCRM!");
    }

    @Override
    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(this.errorHandler);

        return restTemplate;
    }

    @Override
    public AmoCRMCreatedTasksResponse editTasks(AmoCRMEntities amoCRMEntities) throws APIAuthException {
        AmoCRMTaskPostRequest amoCRMTaskPostRequest = new AmoCRMTaskPostRequest();
        amoCRMTaskPostRequest.setTasks(amoCRMEntities);

        AmoCRMPostRequest amoCRMJsonRequest = new AmoCRMPostRequest(amoCRMTaskPostRequest);

        HttpEntity<AmoCRMCreatedTasksResponse> amoCRMCreatedTasksResponse = request(amoCRMJsonRequest, "tasks/set", AmoCRMCreatedTasksResponse.class);
        if (amoCRMCreatedTasksResponse.getBody() == null) {
            throw new IllegalStateException("Ошибка в ходе обновления контактов! Пришел пустой ответ от AmoCRM API.");
        }

        return amoCRMCreatedTasksResponse.getBody();
    }

    @Override
    public List<AmoCRMCreatedEntityResponse> addTasks(ArrayList<AmoCRMTask> amoCRMTasks) throws APIAuthException {
        AmoCRMEntities amoCRMEntities = new AmoCRMEntities();
        amoCRMEntities.setAdd(amoCRMTasks);

        AmoCRMCreatedTasksResponse amoCRMCreatedTasksResponse = this.editTasks(amoCRMEntities);

        if(
                amoCRMCreatedTasksResponse.getResponse() == null ||
                    amoCRMCreatedTasksResponse.getResponse().getTasks().getAdd().size() != amoCRMTasks.size()
                ){
            throw new IllegalStateException("Задача не создана! Не возвращены все id созданных элементов!");
        }

        return amoCRMCreatedTasksResponse.getResponse().getTasks().getAdd();
    }

    @Override
    public AmoCRMCreatedEntityResponse addTask(AmoCRMTask amoCRMTask) throws APIAuthException {
        ArrayList<AmoCRMTask> amoCRMTasks = new ArrayList<>();
        amoCRMTasks.add(amoCRMTask);

        List<AmoCRMCreatedEntityResponse> responses = this.addTasks(amoCRMTasks);

        if(responses == null){
            throw new IllegalStateException("Задача не создана! (Ответ от AmoCRM пустой)");
        }

        return responses.get(0);
    }

    @Override
    public AmoCRMCreatedEntityResponse addContact(AmoCRMContact amoCRMContact) throws APIAuthException {
        AmoCRMEntities amoCRMEntities = new AmoCRMEntities();
        ArrayList<AmoCRMContact> amoCRMContacts = new ArrayList<>();

        amoCRMContacts.add(amoCRMContact);
        amoCRMEntities.setAdd(amoCRMContacts);
        amoCRMEntities.setUpdate(new ArrayList<AmoCRMContact>());

        AmoCRMCreatedContactsResponse amoCRMCreatedContactsResponse = this.editContacts(amoCRMEntities);

        if (
                amoCRMCreatedContactsResponse.getResponse() == null ||
                        amoCRMCreatedContactsResponse.getResponse().getContacts().getAdd().size() == 0
                ) {
            throw new IllegalStateException("Контакт не создан! Пришел пустой ответ от AmoCRM API.");
        }

        return amoCRMCreatedContactsResponse.getResponse().getContacts().getAdd().get(0);
    }

    @Override
    public AmoCRMCreatedContactsResponse editContacts(AmoCRMEntities amoCRMEntities) throws APIAuthException {
        if(this.isReadonlyMode()){
            return null;
        }

        AmoCRMContactPostRequest amoCRMContactPostRequest = new AmoCRMContactPostRequest();
        amoCRMContactPostRequest.setContacts(amoCRMEntities);

        AmoCRMPostRequest amoCRMJsonRequest = new AmoCRMPostRequest(amoCRMContactPostRequest);

        HttpEntity<AmoCRMCreatedContactsResponse> amoCRMCreatedContactsResponse = request(amoCRMJsonRequest, "contacts/set", AmoCRMCreatedContactsResponse.class);
        if (amoCRMCreatedContactsResponse.getBody() == null) {
            throw new IllegalStateException("Ошибка в ходе обновления контактов! Пришел пустой ответ от AmoCRM API.");
        }

        return amoCRMCreatedContactsResponse.getBody();
    }

    @Override
    public void addContactToLead(AmoCRMContact amoCRMContact, AmoCRMLead amoCRMLead) throws APIAuthException {
        ArrayList<AmoCRMContact> amoCRMContacts = new ArrayList<>();
        amoCRMContacts.add(amoCRMContact);

        this.addContactsToLead(amoCRMContacts, amoCRMLead);
    }

    @Override
    public AmoCRMCreatedNotesResponse addNoteToContact(AmoCRMNote amoCRMNote, AmoCRMContact amoCRMContact) throws APIAuthException {
        if(this.isReadonlyMode()){
            return null;
        }

        if (amoCRMContact.getId() == null) {
            throw new IllegalArgumentException("Передан не сохраненный в системе контакт!");
        }

        amoCRMNote.setElement_id(amoCRMContact.getId());
        amoCRMNote.setElement_type(1);

        AmoCRMEntities amoCRMEntities = new AmoCRMEntities();
        ArrayList<AmoCRMNote> amoCRMNotes = new ArrayList<>();
        amoCRMNotes.add(amoCRMNote);
        amoCRMEntities.setAdd(amoCRMNotes);

        AmoCRMNotesPostRequest amoCRMNotesPostRequest = new AmoCRMNotesPostRequest(amoCRMEntities);
        AmoCRMPostRequest amoCRMPostRequest = new AmoCRMPostRequest(amoCRMNotesPostRequest);

        HttpEntity<AmoCRMCreatedNotesResponse> amoCRMCreatedNotesResponseHttpEntity = request(amoCRMPostRequest, "notes/set", AmoCRMCreatedNotesResponse.class);

        if (amoCRMCreatedNotesResponseHttpEntity.getBody() == null) {
            throw new IllegalStateException("Ошибка при добавлении заметок! Пустой ответ от  AmoCRM API");
        }

        return amoCRMCreatedNotesResponseHttpEntity.getBody();
    }

    @Override
    public AmoCRMCreatedNotesResponse addNoteToLead(AmoCRMNote amoCRMNote, AmoCRMLead amoCRMLead) throws APIAuthException {
        if(this.isReadonlyMode()){
            return null;
        }

        if (amoCRMLead.getId() == null) {
            throw new IllegalArgumentException("Передана не сохраненная в системе сделка!");
        }

        amoCRMNote.setElement_id(amoCRMLead.getId());
        amoCRMNote.setElement_type(2);

        AmoCRMEntities amoCRMEntities = new AmoCRMEntities();
        ArrayList<AmoCRMNote> amoCRMNotes = new ArrayList<>();
        amoCRMNotes.add(amoCRMNote);
        amoCRMEntities.setAdd(amoCRMNotes);

        AmoCRMNotesPostRequest amoCRMNotesPostRequest = new AmoCRMNotesPostRequest(amoCRMEntities);
        AmoCRMPostRequest amoCRMPostRequest = new AmoCRMPostRequest(amoCRMNotesPostRequest);

        HttpEntity<AmoCRMCreatedNotesResponse> amoCRMCreatedNotesResponseHttpEntity = request(amoCRMPostRequest, "notes/set", AmoCRMCreatedNotesResponse.class);

        if (amoCRMCreatedNotesResponseHttpEntity.getBody() == null) {
            throw new IllegalStateException("Ошибка при добавлении заметок! Пустой ответ от  AmoCRM API");
        }

        return amoCRMCreatedNotesResponseHttpEntity.getBody();
    }

    @Override
    public void addContactsToLead(ArrayList<AmoCRMContact> amoCRMContacts, AmoCRMLead amoCRMLead) throws APIAuthException {
        for (AmoCRMContact amoCRMContact : amoCRMContacts) {
            amoCRMContact.addLinkedLeadById(amoCRMLead.getId());
        }

        AmoCRMEntities amoCRMEntities = new AmoCRMEntities();
        amoCRMEntities.setUpdate(amoCRMContacts);

        AmoCRMCreatedContactsResponse amoCRMCreatedContactsResponse = this.editContacts(amoCRMEntities);

        if (
                amoCRMCreatedContactsResponse.getResponse() == null
                ) {
            throw new IllegalStateException(
                    "Ошибка при добавлении контактов к сделке. Пустой ответ от AmoCRM! Response: " +
                            amoCRMCreatedContactsResponse.toString()
            );
        }
    }

    @Override
    public List<AmoCRMContactsLeadsLink> getContactsLeadsLinksByContact(AmoCRMContact amoCRMContact) throws APIAuthException {
        AmoCRMContactsLeadsLinksRequest amoCRMContactsLeadsLinksRequest = new AmoCRMContactsLeadsLinksRequest();
        amoCRMContactsLeadsLinksRequest.setContacts_link(amoCRMContact.getId());

        HttpEntity<AmoCRMContactsLeadsLinksResponse> response = request(
                amoCRMContactsLeadsLinksRequest, "contacts/links", AmoCRMContactsLeadsLinksResponse.class
        );

        AmoCRMContactsLeadsLinksResponse amoCRMContactsLeadsLinksResponse = response.getBody();

        if (amoCRMContactsLeadsLinksResponse == null || amoCRMContactsLeadsLinksResponse.getResponse() == null) {
            return new LinkedList<>();
        }

        return amoCRMContactsLeadsLinksResponse.getResponse().getLinks();
    }

    @Override
    public List<AmoCRMContact> getContactsByQuery(String query) throws APIAuthException {
        return this.getContactsByQuery(query, 500l, 0l);
    }

    @Override
    public List<AmoCRMLead> getLeadsByQuery(String query) throws APIAuthException {
        return this.getLeadsByQuery(query, 500l, 0l);
    }

    @Override
    public List<AmoCRMLead> getLeadsByQuery(String query, Long limit, Long offset) throws APIAuthException {
        System.out.println("query = " + query + "; limit/offset = " + limit + "/" + offset);
        AmoCRMLeadsGetRequest amoCRMLeadsGetRequest = new AmoCRMLeadsGetRequest();
        amoCRMLeadsGetRequest.setQuery(query);
        amoCRMLeadsGetRequest.setLimit_rows(limit);
        amoCRMLeadsGetRequest.setLimit_offset(offset);

        HttpEntity<AmoCRMLeadsResponse> response = request(
                amoCRMLeadsGetRequest, "leads/list", AmoCRMLeadsResponse.class
        );

        AmoCRMLeadsResponse amoCRMLeadsResponse = response.getBody();

        if (amoCRMLeadsResponse == null || amoCRMLeadsResponse.getResponse() == null) {
            return new ArrayList<>();
        }

        return amoCRMLeadsResponse.getResponse().getLeads();
    }

    @Override
    public List<AmoCRMNote> getNotesByTypeAndElementId(String type, Long elementId) throws APIAuthException {
        AmoCRMNotesGetRequest amoCRMNotesGetRequest = new AmoCRMNotesGetRequest();
        amoCRMNotesGetRequest.setElement_id(elementId);
        amoCRMNotesGetRequest.setType(type);

        HttpEntity<AmoCRMNotesResponse> response = request(
                amoCRMNotesGetRequest, "notes/list", AmoCRMNotesResponse.class
        );

        AmoCRMNotesResponse amoCRMNotesResponse = response.getBody();

        if(amoCRMNotesResponse == null || amoCRMNotesResponse.getResponse() == null){
            return new ArrayList<>();
        }

        return amoCRMNotesResponse.getResponse().getNotes();
    }

    @Override
    public AmoCRMContact getContactById(Long contactId) throws APIAuthException {
        AmoCRMContactsGetRequest amoCRMContactsGetRequest = new AmoCRMContactsGetRequest();
        amoCRMContactsGetRequest.setId(contactId);
        amoCRMContactsGetRequest.setLimit_rows(1l);
        amoCRMContactsGetRequest.setLimit_offset(0l);

        HttpEntity<AmoCRMContactsResponse> response = request(
                amoCRMContactsGetRequest, "contacts/list", AmoCRMContactsResponse.class
        );

        AmoCRMContactsResponse amoCRMContactsResponse = response.getBody();

        if (amoCRMContactsResponse == null || amoCRMContactsResponse.getResponse() == null) {
            return null;
        }

        return amoCRMContactsResponse.getResponse().getContacts().get(0);
    }

    @Override
    public AmoCRMLead getLeadById(Long leadId) throws APIAuthException {
        AmoCRMLeadsGetRequest amoCRMLeadsGetRequest = new AmoCRMLeadsGetRequest();
        amoCRMLeadsGetRequest.setId(leadId);
        amoCRMLeadsGetRequest.setLimit_rows(1l);
        amoCRMLeadsGetRequest.setLimit_offset(0l);

        HttpEntity<AmoCRMLeadsResponse> response = request(
                amoCRMLeadsGetRequest, "leads/list", AmoCRMLeadsResponse.class
        );

        AmoCRMLeadsResponse amoCRMLeadsResponse = response.getBody();

        if (amoCRMLeadsResponse == null || amoCRMLeadsResponse.getResponse() == null) {
            return null;
        }

        return amoCRMLeadsResponse.getResponse().getLeads().get(0);
    }

    @Override
    public AmoCRMCreatedEntityResponse addLead(AmoCRMLead amoCRMLead) throws APIAuthException {
        AmoCRMEntities amoCRMEntities = new AmoCRMEntities();
        ArrayList<AmoCRMLead> amoCRMLeads = new ArrayList<>();
        amoCRMLeads.add(amoCRMLead);

        amoCRMEntities.setAdd(amoCRMLeads);
        AmoCRMCreatedLeadsResponse amoCRMCreatedLeadsResponse = this.editLeads(amoCRMEntities);

        if (amoCRMCreatedLeadsResponse == null ||
                amoCRMCreatedLeadsResponse.getResponse() == null ||
                amoCRMCreatedLeadsResponse.getResponse().getLeads().getAdd().size() == 0
                ) {
            throw new IllegalStateException("Ошибка при создании сделки! Пришел пустой ответ от AmoCRM API!");
        }

        return amoCRMCreatedLeadsResponse.getResponse().getLeads().getAdd().get(0);
    }

    @Override
    public AmoCRMCreatedLeadsResponse editLeads(AmoCRMEntities amoCRMEntities) throws APIAuthException {

        if(this.isReadonlyMode()){
            return null;
        }

        updateLast_modified(amoCRMEntities);

        AmoCRMLeadPostRequest amoCRMLeadPostRequest = new AmoCRMLeadPostRequest(amoCRMEntities);
        AmoCRMPostRequest amoCRMPostRequest = new AmoCRMPostRequest(amoCRMLeadPostRequest);

        HttpEntity<AmoCRMCreatedLeadsResponse> amoCRMCreatedEntityResponse =
                request(amoCRMPostRequest, "leads/set", AmoCRMCreatedLeadsResponse.class);

        if (amoCRMCreatedEntityResponse == null || amoCRMCreatedEntityResponse.getBody() == null) {
            throw new IllegalStateException("Ошибка при обновлении сделок! Пришел пустой ответ от AmoCRM API");
        }

        return amoCRMCreatedEntityResponse.getBody();
    }



    @Override
    public String getUserLogin() {
        return userLogin;
    }

    @Override
    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    @Override
    public String getUserHash() {
        return userHash;
    }


    @Override
    public void setUserHash(String userHash) {
        this.userHash = userHash;
    }

    @Override
    public String getLoginUrl() {
        return loginUrl;
    }

    @Override
    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    @Override
    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    @Override
    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    @Override
    public ArrayList<Long> getLeadClosedStatusesIDs() {
        return leadClosedStatusesIDs;
    }

    @Override
    public void setLeadClosedStatusesIDs(ArrayList<Long> leadClosedStatusesIDs) {
        this.leadClosedStatusesIDs = leadClosedStatusesIDs;
    }

    @Override
    public void setErrorHandler(ResponseErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public void setMaxRelogins(Integer maxRelogins) {
        this.maxRelogins = maxRelogins;
    }
}
