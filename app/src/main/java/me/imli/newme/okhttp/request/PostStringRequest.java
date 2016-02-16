package me.imli.newme.okhttp.request;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.util.Map;

import me.imli.newme.okhttp.utils.Exceptions;

/**
 * Created by zhy on 15/12/14.
 */
public class PostStringRequest extends OkHttpRequest
{
    private static MediaType MEDIA_TYPE_PLAIN = MediaType.parse("text/plain;charset=utf-8");

    private String content;
    private MediaType mediaType;


    public PostStringRequest(String url, Object tag, Map<String, String> params, Map<String, String> headers, String content, MediaType mediaType)
    {
        super(url, tag, params, headers);
        this.content = content;
        this.mediaType = mediaType;

        if (this.content == null)
        {
            Exceptions.illegalArgument("the content can not be null !");
        }
        if (this.mediaType == null)
        {
            this.mediaType = MEDIA_TYPE_PLAIN;
        }

    }

    @Override
    protected RequestBody buildRequestBody()
    {
        return RequestBody.create(mediaType, content);
    }

    @Override
    protected Request buildRequest(Request.Builder builder, RequestBody requestBody)
    {
        return builder.post(requestBody).build();
    }

}
