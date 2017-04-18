package com.berzellius.integrations.amocrmru.dto.api.amocrm;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by berz on 12.04.2017.
 */
public class TagsSerializer extends JsonSerializer<ArrayList<AmoCRMTag>> {
    @Override
    public void serialize(ArrayList<AmoCRMTag> tags, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        String res = "";
        if(tags != null){
            for(AmoCRMTag tag : tags){
                res = res.concat((res.equals("")? "" : ", ").concat(tag.getName()));
            }
        }

        jsonGenerator.writeString(res);
    }
}
