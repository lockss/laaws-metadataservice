package org.lockss.laaws.mdx.server;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;

@Provider
public class JacksonConfig implements ContextResolver<ObjectMapper> {
    private final ObjectMapper objectMapper;

    public JacksonConfig() throws Exception {

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JodaModule() {
            {
                addSerializer(DateTime.class, new StdSerializer<DateTime>(DateTime.class) {
                    @Override
                    public void serialize(DateTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
                        jgen.writeString(ISODateTimeFormat.dateTimeNoMillis().print(value));
                    }
                });
                addSerializer(LocalDate.class, new StdSerializer<LocalDate>(LocalDate.class) {
                    @Override
                    public void serialize(LocalDate value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
                        jgen.writeString(ISODateTimeFormat.date().print(value));
                    }
                });

            }
        });
    }

    @Override
    public ObjectMapper getContext(Class<?> arg0) {
        return objectMapper;
    }
}