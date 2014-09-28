package org.bits.push;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.bits.push.utils.Cast;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Cast.class)
public class NetworkChangeReceiver extends BroadcastReceiver {
    public void init(Context context) {
        Log.d("NetworkChangeReceiver", "init(context)");
        context.getApplicationContext().to(App.class).onConnectionChange(connectionEnabled(context));
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d("NetworkChangeReceiver", "onReceive(context, intent)");
        context.getApplicationContext().to(App.class).onConnectionChange(connectionEnabled(context));
    }

    private static boolean connectionEnabled(Context ctx) {
        Log.d("NetworkChangeReceiver", "connectionEnabled(ctx)");
        ConnectivityManager cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE).to(ConnectivityManager.class);
        try {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                Log.d("NetworkChangeReceiver", "Network available: true");
                return true;
            } else {
                Log.d("NetworkChangeReceiver", "Network available: false");
                return false;
            }
        } catch (Exception e) {
            return false;
        }

    }
}