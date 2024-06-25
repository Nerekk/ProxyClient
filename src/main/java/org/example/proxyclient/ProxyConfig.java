package org.example.proxyclient;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProxyConfig {
    private final JSONObject jsonData;

    public static final String SERVER_ID = "ServerID";
    public static final String LISTEN_ADDRESSES = "ListenAddresses";
    public static final String LISTEN_PORT = "ListenPort";
    public static final String TIME_OUT = "TimeOut";
    public static final String SIZE_LIMIT = "SizeLimit";
    public static final String ALLOWED_IP_ADDRESSES = "AllowedIPAdresses";

    public ProxyConfig(JSONObject jsonData) {
        this.jsonData = jsonData;
    }

    public JSONObject getJsonData() {
        return jsonData;
    }

    public Object getValue(String key) {
        return jsonData.get(key);
    }

    public String getServerId() {
        return jsonData.getString(SERVER_ID);
    }

    public int getListenedPort() {
        return jsonData.getInt(LISTEN_PORT);
    }

    public int getTimeOut() {
        return jsonData.getInt(TIME_OUT);
    }

    public int getSizeLimit() {
        return jsonData.getInt(SIZE_LIMIT);
    }

    public String[] getListenAddresses() {
        JSONArray jsonArray = jsonData.getJSONArray(LISTEN_ADDRESSES);

        String[] addresses = parseToStringArray(jsonArray);

        return addresses;
    }

    public String[] getAllowedAddresses() {
        JSONArray jsonArray = jsonData.getJSONArray(ALLOWED_IP_ADDRESSES);

        String[] addresses = parseToStringArray(jsonArray);

        return addresses;
    }

    private static String[] parseToStringArray(JSONArray jsonArray) {
        String[] addresses = new String[jsonArray.length()];

        for (int i = 0; i < jsonArray.length(); i++) {
            addresses[i] = jsonArray.getString(i);
        }
        return addresses;
    }
}
