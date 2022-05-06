package com.jp2p.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class ConfigurationReader {
    private static final InputStream is =  ConfigurationReader.class.getClassLoader().getResourceAsStream("config.json");
    private static final BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(is)));
    private static final Gson gson = new Gson();
    private static final JsonObject body = gson.fromJson(br, JsonObject.class);

    public static String GetNode(String root, String node)
    {
        JsonObject root_object = body.getAsJsonObject(root);
        JsonElement element = root_object.get(node);

        return element.getAsString();
    }
}
