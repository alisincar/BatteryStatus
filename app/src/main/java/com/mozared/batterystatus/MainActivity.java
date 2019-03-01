package com.mozared.batterystatus;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

public class MainActivity extends Activity {
    TabHost tabHost;
    TabHost.TabSpec tb1, tb2;
    private static final int ID = 1;
    public static final String PERCENT_CHAR = "%";
    private static final int DEFAULT_CHARGE_TIME_ESTIMATE_AC = 7200;
    private static final int DEFAULT_CHARGE_TIME_ESTIMATE_USB = 21600;
    private static final int DEFAULT_CHARGE_TIME_ESTIMATE_WIRELESS = 14400;
    private int defaultChargeTime = 0;
    private VerticalProgressBar progressBar;
    private TextView progressValueTextView, pilDurumu, pilDurumu2;
    private ImageView chargingImg, connectionImg;
    public static int SARJ_SEVIYE,SARJ_DURUM;
    public static String KALAN_SARJ,SARJ_OLMA_SURESI;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tabHost = findViewById(R.id.tab_host);
        tabHost.setup();
        tb1 = tabHost.newTabSpec("Genel");
        tb2 = tabHost.newTabSpec("Detay");
        tb1.setIndicator("Genel");

        tb1.setContent(R.id.tab1);
        tabHost.addTab(tb1);
        tb2.setIndicator("Detay");
        tb2.setContent(R.id.tab2);
        tabHost.addTab(tb2);


        chargingImg = findViewById(R.id.charging);
        connectionImg = findViewById(R.id.connection);
        progressBar = findViewById(R.id.acd_id_proress_bar);
        progressValueTextView = findViewById(R.id.acd_id_proress_value);
        pilDurumu = findViewById(R.id.pilDurum);
        pilDurumu2 = findViewById(R.id.pilDurum2);

        bilgileriKullan();
        bildirim();
      /*  NotificationHelper.scheduleRepeatingRTCNotification(getApplicationContext());
        NotificationHelper.enableBootReceiver(getApplicationContext());*/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pil, menu);
        return true;
    }

    public void updateViews(Percent percent) {
        progressBar.setCurrentValue(percent);
        progressValueTextView.setText(percent.asIntValue() + PERCENT_CHAR);
    }
    BroadcastReceiver alici = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean hazirMi = intent.getBooleanExtra("present", true);
            int olcu = intent.getIntExtra("scale", -1);
            int sarjTipi = intent.getIntExtra("plugged", -1);
            int pilSaglik = intent.getIntExtra("health", 0);
            int durum = intent.getIntExtra("status", 0);
            int pilSeviye = intent.getIntExtra("level", -1);
            int hamsicaklik = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
            int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
            String technology = intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
            if (TextUtils.isEmpty(technology)) {
                technology = "Bilinmiyor";
            }
            float sicaklik;
            if (hamsicaklik > 0) {
                sicaklik = ((float) hamsicaklik) / 10f;
            } else {
                sicaklik = 0;
            }
            int seviye = 0;

            Bundle bundle = intent.getExtras();
            assert bundle != null;
            Log.i("Pil Seviyesi", bundle.toString());

            if (hazirMi) {
                if (pilSeviye > 0 && olcu > 0) {
                    seviye = (pilSeviye * 100) / olcu;
                }
//                double kalansure=((getBataryaKapasite()/getchargeCounter())*0.7);

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



                Percent pSeviye = new Percent(seviye);
                updateViews(pSeviye);
                String tumBilgi = "Pil Seviyesi: %" + seviye + "\n";
                String tumBilgi2= "Toplam Batarya Kapasitesi: " + getBataryaKapasite() + " mAh\n";
                tumBilgi2 += "Mevcut Kapasite: " + getchargeCounter() + " mAh\n";
                tumBilgi += "Pil Sağlığı: " + getPilSaglik(pilSaglik) + "\n";
                tumBilgi += "Şarj Durumu: " + getPilDurumu(durum) + "\n";
                tumBilgi += "Bağlantı Tipi: " + getSarjTipi(sarjTipi) + "\n";
                tumBilgi2 += "Sıcaklık: " + sicaklik + "°C \n";
                tumBilgi2 += "Voltaj: " + voltage + "mV\n";
                tumBilgi2 += "Teknoloji: " + technology + "mV\n";
                tumBilgi += "Tahmini Kalan Süre: " + getTimeString(remainingTime/3) + "\n";
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




                if(TextUtils.equals(getPilDurumu(durum), "Şarj Oluyor...")){
                    SARJ_DURUM=1;
                       mBuilder.setContentText("Tamamen Dolması için: "+getTimeString(remainingChargeTime));
                    tumBilgi += "Tamamen Dolması için: " + getTimeString(remainingChargeTime) + "\n";
                     mBuilder.setSmallIcon(R.drawable.pb_charging_white);
                    progressValueTextView.setTextColor(Color.BLACK);
                    progressValueTextView.setTextSize(25);
                }else{
                    SARJ_DURUM=0;
                    progressValueTextView.setTextSize(30);
                    progressValueTextView.setTextColor(Color.WHITE);
                     mBuilder.setSmallIcon(R.drawable.pb_full_white);
                }

              mBuilder.setContentTitle("(%" + seviye + ") - "+getTimeString(remainingTime/3)+" kaldı");
                mBuilder.setProgress(100, seviye, false);
                mNotifyManager.notify(ID, mBuilder.build());

                setPilBilgisi(tumBilgi + "\n",1);
                setPilBilgisi(tumBilgi2 + "\n",2);

            } else {
                setPilBilgisi("Pil henüz hazır değil!",1);
                setPilBilgisi("Pil henüz hazır değil!",2);
            }
        }

    };

    public String getTimeString(long duration) {
        int days = (int) (((duration / 60) / 60) / 24);
        int hours = (int) (((duration / 60) / 60) % 24);
        int minutes = (int) ((duration / 60) % 60);
        StringBuilder append = new StringBuilder().append(days > 0 ? days + getString(R.string.notification_event_info_days) + " " : "");
        String str = (days > 0 || hours > 0) ? hours + getString(R.string.notification_event_info_hours) + " " : "";
        append = append.append(str);
        Object valueOf = (days > 0 || hours > 0) ? Integer.valueOf(minutes) : minutes >= 5 ? Integer.valueOf(minutes) : "<5";
        return append.append(valueOf).append(getString(R.string.notification_event_info_minutes)).toString();
    }

    public void setPilBilgisi(String bilgi, int i) {
        if(i==1) {
            pilDurumu.setText(bilgi);
        }else{
            pilDurumu2.setText(bilgi);
        }
    }

    public String getSarjTipi(int sarjTipi) {
        String tip;
        connectionImg.setVisibility(View.VISIBLE);
        switch (sarjTipi) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                connectionImg.setBackgroundResource(R.drawable.plug);
                defaultChargeTime = DEFAULT_CHARGE_TIME_ESTIMATE_AC;
                tip = "Priz";
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                connectionImg.setBackgroundResource(R.drawable.usb);
                defaultChargeTime = DEFAULT_CHARGE_TIME_ESTIMATE_USB;
                tip = "USB";
                break;

            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                connectionImg.setBackgroundResource(R.drawable.wireless);
                defaultChargeTime = DEFAULT_CHARGE_TIME_ESTIMATE_WIRELESS;
                tip = "Kablosuz";
                break;
            default:
                connectionImg.setVisibility(View.GONE);
                tip = "*";
                break;
        }
        return tip;
    }

    public double getchargeCounter() {
        double value = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager mBatteryManager = (BatteryManager) getApplicationContext().getSystemService(Context.BATTERY_SERVICE);
            int batLevel = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

            double capacity = getBataryaKapasite();

            double remaining = (capacity * batLevel) / 100;
            value = remaining;
        }

        return value;
    }

    public int getBataryaKapasite() {
        Object mPowerProfile_ = null;
        double batteryCapacity = 0;
        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
        try {
            mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class).newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            batteryCapacity = (Double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getAveragePower", java.lang.String.class)
                    .invoke(mPowerProfile_, "battery.capacity");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (int) batteryCapacity;
    }


    public String getPilSaglik(int pilSaglik) {
        String saglik;

        switch (pilSaglik) {
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                saglik = "Belirtilmemiş Arıza";
                break;
            case BatteryManager.BATTERY_HEALTH_COLD:
                saglik = "Soğuk";
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                saglik = "İyi";
                break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                saglik = "Ölü";
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                saglik = "Yüksek Gerilim";
                break;

            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                saglik = "Aşırı Sıcak";
                break;
            default:
                saglik = "Bilgi edinilemedi!";
                break;
        }
        return saglik;
    }

    public String getPilDurumu(int durum) {
        String pilDurum;

        switch (durum) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                chargingImg.setVisibility(View.VISIBLE);

                pilDurum = "Şarj Oluyor...";
                break;

            case BatteryManager.BATTERY_STATUS_FULL:
                chargingImg.setVisibility(View.GONE);
                pilDurum = "Pil Dolu";
                break;

            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                chargingImg.setVisibility(View.GONE);
                pilDurum = "Şarj Olmuyor...";
                break;

            case BatteryManager.BATTERY_STATUS_UNKNOWN:
                chargingImg.setVisibility(View.GONE);
                pilDurum = "Bilinemiyor";
                break;
            default:
                chargingImg.setVisibility(View.GONE);
                pilDurum = "Şarj Olmuyor";
                break;
        }

        return pilDurum;
    }

    public void bilgileriKullan() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(alici, filter);
    }

   public void bildirim() {
        mNotifyManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mBuilder = new NotificationCompat.Builder(this);
     //   mBuilder.setOngoing(true);
        mBuilder.setOnlyAlertOnce(true);
        mBuilder.setSmallIcon(R.drawable.pb_full_white);
        Intent appActivityIntent = new Intent(this, MainActivity.class);


       PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, appActivityIntent, 0);
        AlarmManager littlefluppy = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        assert littlefluppy != null;
        littlefluppy.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 2000, pendingIntent);
        PendingIntent contentAppActivityIntent =
                PendingIntent.getActivity(
                        this,  // calling from Activity
                        0,
                        appActivityIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(contentAppActivityIntent);


    }
}