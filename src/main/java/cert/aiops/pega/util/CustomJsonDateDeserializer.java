package cert.aiops.pega.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomJsonDateDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser jsonParser,
                              DeserializationContext deserializationContext) throws IOException, JsonProcessingException {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        long date = Long.parseLong(jsonParser.getText());
        return format.format(new Date(date));
    }

}