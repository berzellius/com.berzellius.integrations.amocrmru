package com.berzellius.integrations.amocrmru.dto.api.amocrm;


import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Created by berz on 24.10.2015.
 */

public class AmoCRMCustomFieldValueSerializer extends JsonSerializer<AmoCRMCustomFieldValue> {

    @Override
    //  Ранее было еще исключение codehaus
    public void serialize(AmoCRMCustomFieldValue amoCRMCustomFieldValue, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("value", amoCRMCustomFieldValue.getValue());
        jsonGenerator.writeStringField("enum", amoCRMCustomFieldValue.getEnumerated());
        jsonGenerator.writeEndObject();
    }

}
