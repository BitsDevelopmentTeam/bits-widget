package org.bits.push;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketOptions;
import org.bits.push.utils.Cast;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Cast.class)
public class WebSocketUpdateService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final AlarmManager am = this.getSystemService(Context.ALARM_SERVICE).to(AlarmManager.class);
    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final PendingIntent startSelf = PendingIntent.getService(this, 0, new Intent(this, WebSocketUpdateService.class), 0);
    private final WebSocketOptions options = new WebSocketOptions();
    private final WebSocketConnection connection = new WebSocketConnection();
    private final WebSocketConnectionHandler connectionHandler = new WebSocketConnectionHandler() {
        @Override
        public void onClose(int code, String reason) {
            super.onClose(code, reason);
            onWebSocketClose();
        }

        @Override
        public void onTextMessage(String payload) {
            super.onTextMessage(payload);
            onWebSocketMessage(payload);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        options.setReconnectInterval(5000);
        getAm().setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + 30000, 30000, getStartSelf());
    }

    @Override
    @SneakyThrows
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("WebSocketUpdateReceiver", "onStartCommand(intent, flags, startId)");
        int ret = super.onStartCommand(intent, flags, startId);

        if (!connection.isConnected()) {
            connection.connect(getUrl(), connectionHandler, options);
        }

        return ret;
    }

    @Override
    public void onDestroy() {
        Log.d("WebSocketUpdateReceiver", "onDestroy()");
        super.onDestroy();
        this.connection.disconnect();
        this.getAm().cancel(getStartSelf());
    }

    private void onWebSocketMessage(String s) {
        Log.d("WebSocketUpdateReceiver", "onWebSocketMessage(s: " + s + ")");
        if (s.contains("status")) {
            final Intent statusIntent = new Intent(this, BitsAppWidgetProvider.class);

            if (s.contains("open")) {
                Status.OPEN.attachTo(statusIntent);
            } else if (s.contains("close")) {
                Status.CLOSED.attachTo(statusIntent);
            }

            sendBroadcast(statusIntent);
        }
    }

    public void onWebSocketClose() {
        Log.d("WebSocketUpdateReceiver", "onWebSocketClose()");
        final Intent statusIntent = new Intent(this, BitsAppWidgetProvider.class);
        Status.DISABLED.attachTo(statusIntent);
        sendBroadcast(statusIntent);
    }

    private String getUrl() {
        if (isConnectedMobile(this) && carrierName(this).contains("vodafone")) {
            return "ws://bits.poul.org:8080/endpoint_degli_imbecilli.ws";
        } else {
            return "ws://bits.poul.org/endpoint_degli_imbecilli.ws";
        }
    }

    private static boolean isConnectedMobile(Context context){
        ConnectivityManager cm = context.getSystemService(Context.CONNECTIVITY_SERVICE).to(ConnectivityManager.class);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    private static String carrierName(Context context) {
        TelephonyManager manager = context.getSystemService(Context.TELEPHONY_SERVICE).to(TelephonyManager.class);
        return manager.getNetworkOperatorName();
    }
}
