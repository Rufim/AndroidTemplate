package ru.kazantsev.template.net.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created by 0shad on 03.03.2016.
 */
public class BigDecimalTypeAdapter extends TypeAdapter<BigDecimal> {
    @Override
    public void write(JsonWriter out, BigDecimal value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.toString());
    }

    @Override
    public BigDecimal read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return new BigDecimal(in.nextString());
    }
}
