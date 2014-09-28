package org.bits.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import org.bits.push.utils.Cast;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Cast.class)
public class MonitorChangeReceiver extends BroadcastReceiver {
    public void init(Context context) {
        Log.d("MonitorChangeReceiver", "init(context)");
        context.getApplicationContext().to(App.class).onMonitorActive(monitorActive(context));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MonitorChangeReceiver", "onReceive(context, intent)");
        if(Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            Log.d("MonitorChangeReceiver", "[BroadcastReceiver] Screen ON");
            context.getApplicationContext().to(App.class).onMonitorActive(true);
        }
        else if(Intent.ACTION_SCREEN_OFF.equals(intent.getAction())){
            Log.d("MonitorChangeReceiver", "[BroadcastReceiver] Screen OFF");
            context.getApplicationContext().to(App.class).onMonitorActive(false);
        }
    }

    private static boolean monitorActive(Context context) {
        Log.d("MonitorChangeReceiver", "monitorActive(context)");
        return context.getSystemService(Context.POWER_SERVICE).to(PowerManager.class).isScreenOn();
    }
}
