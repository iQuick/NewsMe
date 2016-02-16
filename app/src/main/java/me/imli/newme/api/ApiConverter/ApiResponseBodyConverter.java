package me.imli.newme.api.ApiConverter;

import com.google.gson.Gson;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.lang.reflect.Type;

import retrofit.Converter;

/**
 * Created by Em on 2015/11/26.
 */
public class ApiResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final Gson gson;
    private final Type type;

    ApiResponseBodyConverter(Gson gson, Type type) {
        this.gson = gson;
        this.type = type;
    }

    @Override public T convert(ResponseBody value) throws IOException {
        String str = value.string();
        try {
            int start = str.indexOf("\"pagebean\":") + "\"pagebean\":".length();
            int end = str.indexOf(",\"ret_code\"");
            return gson.fromJson(str.substring(start, end), type);
        } finally {
        }
    }
}
