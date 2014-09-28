package org.bits.push;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.util.Log;
import android.widget.RemoteViews;

import org.bits.push.utils.Cast;

import java.util.Calendar;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Cast.class)
public class BitsAppWidgetProvider extends AppWidgetProvider {
    private static Status status = Status.DISABLED;
    private static final String url = "http://bits.poul.org/";
    private static PendingIntent self;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BitsAppWidgetProvider", "onReceive(context, intent)");
        super.onReceive(context, intent);
        try {
            status = Status.detachFrom(intent);
            Log.d("BitsAppWidgetProvider", status.toString());
            switch (status) {
                case CLOSED:
                    drawWidget(context, R.drawable.closed);
                    break;
                case DISABLED:
                    drawWidget(context, R.drawable.disabled);
                    break;
                case OPEN:
                    drawWidget(context, R.drawable.opened);
                    break;
            }
        } catch (NoSuchFieldError nsfe) {
            App app = context.getApplicationContext().to(App.class);
            app.revive();
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("BitsAppWidgetProvider", "onUpdate()");
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        switch (status) {
            case CLOSED:
                drawWidget(context, R.drawable.closed);
                break;
            case DISABLED:
                drawWidget(context, R.drawable.disabled);
                break;
            case OPEN:
                drawWidget(context, R.drawable.opened);
                break;
        }
    }

    private void drawWidget(Context context, int res) {
        Log.d("BitsAppWidgetProvider", "drawWidget(context, res)");
        Log.d("BitsAppWidgetProvider", status.toString());
        ComponentName thisWidget = new ComponentName(context, BitsAppWidgetProvider.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.bits_appwidget);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.bits_image, pendingIntent);

        views.setImageViewResource(R.id.bits_image, res);

        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onEnabled(Context context) {
        Log.d("BitsAppWidgetProvider", "onEnabled(context)");
        super.onEnabled(context);
        context.getApplicationContext().to(App.class).onWidgetEnabled(true);
        Intent intent = new Intent(context, BitsAppWidgetProvider.class);
        self = PendingIntent.getBroadcast(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = context.getSystemService(Context.ALARM_SERVICE).to(AlarmManager.class);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.add(Calendar.SECOND, 60);
        alarmManager.setInexactRepeating(AlarmManager.RTC, cal
                .getTimeInMillis(), 30000, self);

    }

    @Override
    public void onDisabled(Context context) {
        Log.d("BitsAppWidgetProvider", "onDisabled(context)");
        AlarmManager alarmManager = context.getSystemService(Context.ALARM_SERVICE).to(AlarmManager.class);
        alarmManager.cancel(self);
        context.getApplicationContext().to(App.class).onWidgetEnabled(false);
        super.onDisabled(context);
    }
}