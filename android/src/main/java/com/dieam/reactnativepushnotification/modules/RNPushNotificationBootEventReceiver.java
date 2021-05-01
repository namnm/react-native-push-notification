package com.dieam.reactnativepushnotification.modules;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import java.util.Set;

import static com.dieam.reactnativepushnotification.modules.RNPushNotification.LOG_TAG;

/**
 * Set alarms for scheduled notification after system reboot.
 */
public class RNPushNotificationBootEventReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_TAG, "RNPushNotificationBootEventReceiver loading scheduled notifications");

        SharedPreferences sharedPreferences = context.getSharedPreferences(RNPushNotificationHelper.PREFERENCES_KEY, Context.MODE_PRIVATE);
        Set<String> ids = sharedPreferences.getAll().keySet();

        Application applicationContext = (Application) context.getApplicationContext();
        RNPushNotificationHelper rnPushNotificationHelper = new RNPushNotificationHelper(applicationContext);

        for (String id : ids) {
            try {
                String notificationAttributesJson = sharedPreferences.getString(id, null);
                if (notificationAttributesJson != null) {
                    RNPushNotificationAttributes notificationAttributes = RNPushNotificationAttributes.fromJson(notificationAttributesJson);

                    if (notificationAttributes.getFireDate() < System.currentTimeMillis()) {
                        Log.i(LOG_TAG, "RNPushNotificationBootEventReceiver: Showing notification for " +
                                notificationAttributes.getId());
                        Bundle bundle = notificationAttributes.toBundle();
                        if(bundle.getString("title") != null && bundle.getString("title").contains("Message from")){
                            bundle.putString("actions", "[\"ReplyInput\"]");
                            bundle.putString("reply_placeholder_text", "Write your response...");
                            bundle.putString("reply_button_text", "Reply");
                        }
                        bundle.putBoolean("invokeApp", true);
                        bundle.putBoolean("localNotification", true);
                        rnPushNotificationHelper.sendToNotificationCentre(bundle);
                    } else {
                        Log.i(LOG_TAG, "RNPushNotificationBootEventReceiver: Scheduling notification for " +
                                notificationAttributes.getId());
                        Bundle bundle = notificationAttributes.toBundle();
                        if(bundle.getString("title") != null && bundle.getString("title").contains("Message from")){
                            bundle.putString("actions", "[\"ReplyInput\"]");
                            bundle.putString("reply_placeholder_text", "Write your response...");
                            bundle.putString("reply_button_text", "Reply");
                        }
                        bundle.putBoolean("invokeApp", true);
                        bundle.putBoolean("localNotification", true);
                        rnPushNotificationHelper.sendNotificationScheduledCore(bundle);
                    }
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Problem with boot receiver loading notification " + id, e);
            }
        }
    }
}
