package com.jp2p.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * This class is used to read the seed json file containing the default settings of a peer.
 * It's a general purpose class that can be used to read any json file though.
 */
public class JsonReader {
    /**
     * The InputStream of the file to read from loaded from the resources folder of the application.
     */
    private static final InputStream is = JsonReader.class.getClassLoader().getResourceAsStream("seed.json");

    /**
     * The buffered reader to read the json file.
     */
    private static final BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(is)));

    /**
     * The Gson object to read the json file.
     * @see Gson
     */
    private static final Gson gson = new Gson();

    /**
     * The body of the json file.
     * @see JsonObject
     * @see Gson
     */
    private static final JsonObject body = gson.fromJson(br, JsonObject.class);

    /**
     * Get the value of a key in the json file.
     * @param root The root of the json object.
     * @param node The node to get the value of.
     * @return The value of the node.
     */
    public static String GetNode(String root, String node) {
        JsonObject root_object = body.getAsJsonObject(root);
        JsonElement element = root_object.get(node);

        return element.getAsString();
    }
}
