package com.mozared.batterystatus.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.mozared.batterystatus.MainActivity;
import com.mozared.batterystatus.R;

/**
 * Created by ptyagi on 4/17/17.
 */

/**
 * AlarmReceiver handles the broadcast message and generates Notification
 */
public class AlarmReceiver extends BroadcastReceiver {
    private int defaultChargeTime = 0;
    @Override
    public void onReceive(Context context, Intent intent) {
        //Get notification manager to manage/send notifications



        //Intent to invoke app when click on notification.
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
    }


    public NotificationCompat.Builder buildLocalNotification(Context context, PendingIntent pendingIntent) {

        NotificationCompat.Builder builder =
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
}
