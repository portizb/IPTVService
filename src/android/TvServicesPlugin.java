package com.movistar.tvservices.cordova.plugin;

import org.apache.cordova.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.util.Log;

public class TvServicesPlugin extends CordovaPlugin {

    // Event types for callbacks
    private enum Event {
        ACTIVATE, DEACTIVATE, FAILURE
    }

    private static final String LOG_TAG = "TvServicesPlugin";

    // Plugin namespace
    private static final String JS_NAMESPACE =
            "cordova.plugins.tvServices";
    // Flag indicates if the app is in background or foreground
    private boolean inBackground = false;
    // Flag indicates if the plugin is enabled or disabled
    private boolean isDisabled = true;
    // Flag indicates if the service is bind
    private boolean isBind = false;
    // Default settings for the notification
    private static JSONObject defaultSettings = new JSONObject();

    TVServices mTvServices;


    // Used to (un)bind the service to with the activity
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TVServices.ForegroundBinder binder =
                    (TVServices.ForegroundBinder) service;
            mTvServices = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
// Nothing to do here
        }
    };

    /**
     * Executes the request.
     *
     * @param action The action to execute.
     * @param args The exec() arguments.
     * @param callback The callback context used when
     * calling back into JavaScript.
     *
     * @return
     * Returning false results in a "MethodNotFound" error.
     *
     * @throws JSONException
     */
    @Override
    public boolean execute (String action, JSONArray args, CallbackContext callback) throws JSONException {

        if (action.equalsIgnoreCase("configure")) {
            JSONObject settings = args.getJSONObject(0);
            boolean update = args.getBoolean(1);

            if (update) {
                updateNotification(settings);
            } else {
                setDefaultSettings(settings);
            }
            return true;
        }
        else if (action.equalsIgnoreCase("enable")) {
            enableMode();
            return true;
        }
        else if (action.equalsIgnoreCase("disable")) {
            disableMode();
            return true;
        }

        return false;
    }

    /**
     * Called when the system is about to start resuming a previous activity.
     *
     * @param multitasking
     * Flag indicating if multitasking is turned on for app
     */
    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        inBackground = true;
        startService();
    }

    /**
     * Called when the activity will start interacting with the user.
     *
     * @param multitasking
     * Flag indicating if multitasking is turned on for app
     */
    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        inBackground = false;
        stopService();
    }

    /**
     * Called when the activity will be destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService();
    }
    /**
     * Enable the background mode.
     */
    private void enableMode() {
        isDisabled = false;
        if (inBackground) {
            startService();
        }
    }

    /**
     * Disable the background mode.
     */
    private void disableMode() {
        stopService();
        isDisabled = true;
    }

    /**
     * Update the default settings for the notification.
     *
     * @param settings
     * The new default settings
     */
    private void setDefaultSettings(JSONObject settings) {
        defaultSettings = settings;
    }

    /**
     * The settings for the new/updated notification.
     *
     * @return
     * updateSettings if set or default settings
     */
    protected static JSONObject getSettings() {
        return defaultSettings;
    }

    /**
     * Update the notification.
     *
     * @param settings
     * The config settings
     */
    private void updateNotification(JSONObject settings) {
        if (isBind) {
            mService.updateNotification(settings);
        }
    }

    /**
     * Bind the activity to a background service and put them into foreground
     * state.
     */
    private void startService() {
        Activity context = cordova.getActivity();
        if (isDisabled || isBind)
            return;
        Intent intent = new Intent(
                context, TvServices.class);
        try {
            context.bindService(intent,
                    connection, Context.BIND_AUTO_CREATE);
            fireEvent(Event.ACTIVATE, null);
            context.startService(intent);
        } catch (Exception e) {
            fireEvent(Event.FAILURE, e.getMessage());
        }
        isBind = true;
    }

    /**
     * Bind the activity to a background service and put them into foreground
     * state.
     */
    private void stopService() {
        Activity context = cordova.getActivity();
        Intent intent = new Intent(
                context, TvServices.class);
        if (!isBind)
            return;
        fireEvent(Event.DEACTIVATE, null);
        context.unbindService(connection);
        context.stopService(intent);
        isBind = false;
    }

    /**
     * Fire vent with some parameters inside the web view.
     *
     * @param event
     * The name of the event
     * @param params
     * Optional arguments for the event
     */
    private void fireEvent (Event event, String params) {
        String eventName;
        switch (event) {
            case ACTIVATE:
                eventName = "activate"; break;
            case DEACTIVATE:
                eventName = "deactivate"; break;
            default:
                eventName = "failure";
        }
        String active = event == Event.ACTIVATE ? "true" : "false";
        String flag = String.format("%s._isActive=%s;",
                JS_NAMESPACE, active);
        String fn = String.format("setTimeout('%s.on%s(%s)',0);",
                JS_NAMESPACE, eventName, params);
        final String js = flag + fn;
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:" + js);
            }
        });
    }
}
