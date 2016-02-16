package me.imli.newme.api.ApiConverter;

import com.google.gson.Gson;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import retrofit.Converter;

/**
 * Created by Em on 2015/11/26.
 */
public class ApiConverterFactory extends Converter.Factory {

    /**
     * Create an instance using a default {@link Gson} instance for conversion. Encoding to JSON and
     * decoding from JSON (when no charset is specified by a header) will use UTF-8.
     */
    public static ApiConverterFactory create() {
        return create(new Gson());
    }

    /**
     * Create an instance using {@code gson} for conversion. Encoding to JSON and
     * decoding from JSON (when no charset is specified by a header) will use UTF-8.
     */
    public static ApiConverterFactory create(Gson gson) {
        return new ApiConverterFactory(gson);
    }

    private final Gson gson;

    private ApiConverterFactory(Gson gson) {
        if (gson == null) throw new NullPointerException("gson == null");
        this.gson = gson;
    }

    @Override
    public Converter<ResponseBody, ?> fromResponseBody(Type type, Annotation[] annotations) {
        return new ApiResponseBodyConverter<>(gson, type);
    }

    @Override
    public Converter<?, RequestBody> toRequestBody(Type type, Annotation[] annotations) {
        return new ApiRequestBodyConverter<>(gson, type);
    }

}
