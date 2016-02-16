package me.imli.newme.okhttp.builder;


import java.io.File;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import me.imli.newme.okhttp.request.PostFormRequest;
import me.imli.newme.okhttp.request.RequestCall;

/**
 * Created by zhy on 15/12/14.
 */
public class PostFormBuilder extends OkHttpRequestBuilder
{
    private List<FileInput> files = new ArrayList<>();

    @Override
    public RequestCall build()
    {
        return new PostFormRequest(url, tag, params, headers, files).build();
    }

    public PostFormBuilder addFile(String name, String filename, File file)
    {
        files.add(new FileInput(name, filename, file));
        return this;
    }

    public static class FileInput
    {
        public String key;
        public String filename;
        public File file;

        public FileInput(String name, String filename, File file)
        {
            this.key = name;
            this.filename = filename;
            this.file = file;
        }
    }

    //
    @Override
    public PostFormBuilder url(String url)
    {
        this.url = url;
        return this;
    }

    @Override
    public PostFormBuilder tag(Object tag)
    {
        this.tag = tag;
        return this;
    }

    @Override
    public PostFormBuilder params(Map<String, String> params)
    {
        this.params = params;
        return this;
    }

    @Override
    public PostFormBuilder addParams(String key, String val)
    {
        if (this.params == null)
        {
            params = new IdentityHashMap<>();
        }
        params.put(key, val);
        return this;
    }

    @Override
    public PostFormBuilder headers(Map<String, String> headers)
    {
        this.headers = headers;
        return this;
    }

    @Override
    public PostFormBuilder addHeader(String key, String val)
    {
        if (this.headers == null)
        {
            headers = new IdentityHashMap<>();
        }
        headers.put(key, val);
        return this;
    }
}
