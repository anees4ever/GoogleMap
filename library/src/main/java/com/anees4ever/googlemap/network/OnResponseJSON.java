package com.anees4ever.googlemap.network;

import org.json.JSONObject;

public interface OnResponseJSON {
    void onResponse(boolean status, JSONObject object);
}
