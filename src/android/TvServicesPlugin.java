package com.movistar.tvservices.cordova.plugin;

import org.apache.cordova.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.util.Log;

public class TvServicesPlugin extends CordovaPlugin {

    private static final String LOG_TAG = "TvServicesPlugin";

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("channels")) {

            try {

                JSONObject json = new JSONObject();

                json.put("id", 1);
                json.put("channel", "La 1");
                json.put("isHD", true);

                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
                return true;

            } catch (Exception e) {
                callbackContext.error ("Fatal error: " + e.toString());
                return false;
            }

        } else {

            callbackContext.error ("Invalid command");
            return false;

        }
    }
}
