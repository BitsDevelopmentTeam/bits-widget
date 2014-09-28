package org.bits.push;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Log;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

public class App extends Application {
    private AppStatus status = AppStatus.of(false, false, false);
    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    final private NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    final private MonitorChangeReceiver monitorChangeReceiver = new MonitorChangeReceiver();

    private void updatePolicy() {
        Log.d("App", "updatePolicy()");
        Log.d("App", status.toString());
        if (status.isWidgetEnabled() && status.isConnectionEnabled() && status.isMonitorActive()) {
            this.startService(new Intent(this, WebSocketUpdateService.class));
        } else {
            this.stopService(new Intent(this, WebSocketUpdateService.class));
        }

        if (status.isWidgetEnabled()) {
            getMonitorChangeReceiver().init(this);
            getNetworkChangeReceiver().init(this);
            registerReceiver(getMonitorChangeReceiver(), new IntentFilter(Intent.ACTION_SCREEN_ON));
            registerReceiver(getMonitorChangeReceiver(), new IntentFilter(Intent.ACTION_SCREEN_OFF));
            registerReceiver(getNetworkChangeReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            registerReceiver(getNetworkChangeReceiver(), new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        } else {
            try {
                unregisterReceiver(getMonitorChangeReceiver());
            } catch (IllegalArgumentException iae) {}
            try {
                unregisterReceiver(getNetworkChangeReceiver());
            } catch (IllegalArgumentException iae) {}
        }
    }

    public void revive() {
        Log.d("App", "revive()");
        ComponentName thisWidget = new ComponentName(this, BitsAppWidgetProvider.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        onWidgetEnabled(appWidgetIds.length > 0);
    }

    public void onConnectionChange(boolean enabled) {
        Log.d("App", "onConnectionChange(enabled: " + enabled + ")");
        AppStatus status = this.status.withConnectionEnabled(enabled);

        if (!this.status.equals(status)) {
            this.status = status;
            this.updatePolicy();
        }
    }

    public void onWidgetEnabled(boolean enabled) {
        Log.d("App","onWidgetEnabled(enabled: " + enabled + ")");
        AppStatus status = this.status.withWidgetEnabled(enabled);

        if (!this.status.equals(status)) {
            this.status = status;
            this.updatePolicy();
        }
    }

    public void onMonitorActive(boolean active) {
        Log.d("App", "onMonitorActive(active: " + active + ")");
        AppStatus status = this.status.withMonitorActive(active);

        if (!this.status.equals(status)) {
            this.status = status;
            this.updatePolicy();
        }
    }
}

@Value
@RequiredArgsConstructor(staticName = "of")
class AppStatus {
    @Wither
    boolean widgetEnabled;
    @Wither
    boolean connectionEnabled;
    @Wither
    boolean monitorActive;
}