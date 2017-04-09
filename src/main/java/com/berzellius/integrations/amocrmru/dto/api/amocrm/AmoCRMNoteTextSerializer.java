package com.berzellius.integrations.amocrmru.dto.api.amocrm;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Created by berz on 11.10.2015.
 * О необходимости отдельного сериализатора см. класс AmoCRMNoteText
 */
public class AmoCRMNoteTextSerializer extends JsonSerializer<AmoCRMNoteText> {
    @Override
    public void serialize(AmoCRMNoteText amoCRMNoteText, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {

        String str = "{" +
                (amoCRMNoteText.getUniq() != null ? "\"UNIQ\":\"" + amoCRMNoteText.getUniq() + "\"," : "") +
                (amoCRMNoteText.getSrc() != null ? "\"SRC\":\"" + amoCRMNoteText.getSrc() + "\"," : "") +
                "\"PHONE\":\"" + amoCRMNoteText.getPhone() + "\"," +
                "\"DURATION\":" + amoCRMNoteText.getDuration() + "," +
                "\"LINK\":\"" + amoCRMNoteText.getLink() + "\"," +
                "\"call_status\":\"" + amoCRMNoteText.getCall_status() + "\"," +
                "\"call_result\":\"" + amoCRMNoteText.getCall_result() + "\"}";
        jsonGenerator.writeString(str);
    }
}
