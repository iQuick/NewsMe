package me.imli.newme.model;

import com.google.gson.Gson;

/**
 * Created by Em on 2015/11/26.
 */
public class BaseModel {

    public String toJson() {
        return new Gson().toJson(this);
    }
}
