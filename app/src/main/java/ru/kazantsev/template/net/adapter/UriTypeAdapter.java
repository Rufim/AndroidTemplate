package ru.kazantsev.template.net.adapter;

import android.net.Uri;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Created by 0shad on 03.03.2016.
 */
public class UriTypeAdapter extends TypeAdapter<Uri> {
    @Override
    public void write(JsonWriter out, Uri value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.toString());
    }

    @Override
    public Uri read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return Uri.parse(in.nextString());
    }
}
