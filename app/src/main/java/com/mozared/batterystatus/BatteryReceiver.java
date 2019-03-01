package com.mozared.batterystatus;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.mozared.batterystatus.notification.NotificationHelper;

/**
 * Created by kullanc on 7.10.2018.
 * For BatteryStatus
 */

public class BatteryReceiver extends BroadcastReceiver {
    NotificationCompat.Builder builder;
    private int defaultChargeTime = 0;
    private static final int DEFAULT_CHARGE_TIME_ESTIMATE_AC = 7200;
    private static final int DEFAULT_CHARGE_TIME_ESTIMATE_USB = 21600;
    private static final int DEFAULT_CHARGE_TIME_ESTIMATE_WIRELESS = 14400;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        context = context.getApplicationContext();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        context.registerReceiver(null, filter);
        context.registerReceiver(this, filter);
        //In this sample, we want to start/launch this sample app when user clicks on notification
        Intent intentToRepeat = new Intent(context, com.mozared.batterystatus.MainActivity.class);
        //set flag to restart/relaunch the app
        intentToRepeat.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //Pending intent to handle launch of Activity in intent above
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, NotificationHelper.ALARM_TYPE_RTC, intentToRepeat, PendingIntent.FLAG_UPDATE_CURRENT);

        //Build notification
        Notification repeatedNotification = buildLocalNotification(context, pendingIntent).build();

        //Send local notification
        NotificationHelper.getNotificationManager(context).notify(NotificationHelper.ALARM_TYPE_RTC, repeatedNotification);

        boolean hazirMi = intent.getBooleanExtra("present", true);
        int olcu = intent.getIntExtra("scale", -1);
        int durum = intent.getIntExtra("status", 0);
        int pilSeviye = intent.getIntExtra("level", -1);
        int sarjTipi = intent.getIntExtra("plugged", -1);

        getSarjTipi(sarjTipi);
        int seviye = 0;

        Bundle bundle = intent.getExtras();
        assert bundle != null;
        Log.i("Pil Seviyesi", bundle.toString());

        if (hazirMi) {
            if (pilSeviye > 0 && olcu > 0) {
                seviye = (pilSeviye * 100) / olcu;
            }

            long remainingTime = 0;
            long remainingChargeTime = 0;
            int defaultRemainingTime = (int) (79200.0f * (((float) seviye) / 100.0f));
            int maxRemainingTime = defaultRemainingTime * 4;
            int minRemainingTime = defaultRemainingTime / 4;
            if (remainingTime == 0) {
                remainingTime = (long) defaultRemainingTime;
            } else if (remainingTime < ((long) minRemainingTime)) {
                remainingTime = (long) minRemainingTime;
            } else if (remainingTime > ((long) maxRemainingTime)) {
                remainingTime = (long) maxRemainingTime;
            }

            int defaultRemainingChargeTime = (int) (((float) defaultChargeTime) * (((float) (100 - seviye)) / 100.0f));
            int maxRemainingChargeTime = defaultRemainingChargeTime * 8;
            int minRemainingChargeTime = defaultRemainingChargeTime / 8;
            if (remainingChargeTime == 0) {
                remainingChargeTime = (long) defaultRemainingChargeTime;
            } else if (remainingChargeTime < ((long) minRemainingChargeTime)) {
                remainingChargeTime = (long) minRemainingChargeTime;
            } else if (remainingChargeTime > ((long) maxRemainingChargeTime)) {
                remainingChargeTime = (long) maxRemainingChargeTime;
            }

            MainActivity.SARJ_SEVIYE = seviye;
            MainActivity.KALAN_SARJ=getTimeString(remainingTime/3,false);
            MainActivity.SARJ_OLMA_SURESI=getTimeString(remainingChargeTime,false);

                        builder.setContentTitle(MainActivity.SARJ_SEVIYE+"% - "+MainActivity.KALAN_SARJ);
            if (TextUtils.equals(getPilDurumu(durum), "Şarj Oluyor...")) {

                builder.setContentText("Tamamen Dolması için: "+MainActivity.SARJ_OLMA_SURESI)
                        .setSmallIcon(R.drawable.pb_charging_white);
                MainActivity.SARJ_DURUM = 1;
            } else {
                builder.setSmallIcon(R.drawable.pb_full_white);
                MainActivity.SARJ_DURUM = 0;
            }


        }



    }
    public NotificationCompat.Builder buildLocalNotification(Context context, PendingIntent pendingIntent) {

        builder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .setOnlyAlertOnce(true)
                        .setSmallIcon(R.drawable.pb_full_white)
                        .setContentTitle(MainActivity.SARJ_SEVIYE+"% - "+MainActivity.KALAN_SARJ)
                        .setOngoing(true)
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(true);

        if(MainActivity.SARJ_DURUM==1){
            builder.setContentText("Tamamen Dolması için: "+MainActivity.SARJ_OLMA_SURESI)
                    .setSmallIcon(R.drawable.pb_charging_white);
        }

        return builder;
    }
    public void getSarjTipi(int sarjTipi) {
        switch (sarjTipi) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                defaultChargeTime = DEFAULT_CHARGE_TIME_ESTIMATE_AC;
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                defaultChargeTime = DEFAULT_CHARGE_TIME_ESTIMATE_USB;
                break;

            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                defaultChargeTime = DEFAULT_CHARGE_TIME_ESTIMATE_WIRELESS;
                break;
            default:
                defaultChargeTime = DEFAULT_CHARGE_TIME_ESTIMATE_AC;
                break;
        }
    }

    public String getTimeString(long duration, boolean minutePrecision) {
        int days = (int) (((duration / 60) / 60) / 24);
        int hours = (int) (((duration / 60) / 60) % 24);
        int minutes = (int) ((duration / 60) % 60);
        StringBuilder append = new StringBuilder().append(days > 0 ? days +  "gün " : "");
        String str = (days > 0 || hours > 0) ? hours +"sa " : "";
        append = append.append(str);
        Object valueOf = (days > 0 || hours > 0 || minutePrecision) ? Integer.valueOf(minutes) : minutes >= 5 ? Integer.valueOf(minutes) : "<5";
        return append.append(valueOf).append("dk").toString();
    }

    public String getPilDurumu(int durum) {
        String pilDurum;
        if (durum == BatteryManager.BATTERY_STATUS_CHARGING) {
            pilDurum = "Şarj Oluyor...";
        } else {
            pilDurum = "Şarj Olmuyor";
        }
        return pilDurum;
    }

}
