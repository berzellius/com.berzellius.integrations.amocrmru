package com.berzellius.integrations.amocrmru.dto.api.amocrm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by berz on 07.10.2015.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmoCRMLead extends AmoCRMEntityWCustomFieldsAndTags {
    public AmoCRMLead(){}

    protected Long id;
    protected String name;
    protected Long created_user_id;
    protected Long pipeline_id;
    protected Long status_id;
    protected BigDecimal price;
    protected Long responsible_user_id;
    protected Long account_id;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCreated_user_id() {
        return created_user_id;
    }

    public void setCreated_user_id(Long created_user_id) {
        this.created_user_id = created_user_id;
    }

    public Long getStatus_id() {
        return status_id;
    }

    public void setStatus_id(Long status_id) {
        this.status_id = status_id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getResponsible_user_id() {
        return responsible_user_id;
    }

    public void setResponsible_user_id(Long responsible_user_id) {
        this.responsible_user_id = responsible_user_id;
    }

    public Long getAccount_id() {
        return account_id;
    }

    public void setAccount_id(Long account_id) {
        this.account_id = account_id;
    }

    public Long getPipeline_id() {
        return pipeline_id;
    }

    public void setPipeline_id(Long pipeline_id) {
        this.pipeline_id = pipeline_id;
    }
}
