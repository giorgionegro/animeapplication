package com.example.myapplication;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class BackgroundService extends Service {


    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        BackgroundService getService() {
            return BackgroundService.this;
        }
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent intent1 = intent.setClassName("com.dv.adm", "com.dv.adm.AEditor");
        intent1.putExtra("com.dv.get.ACTION_LIST_ADD", (String) intent.getExtras().get("com.dv.get.ACTION_LIST_ADD")); // or "url1<line>url2...", or "url1<info>name_ext1<line>..."
// optional
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent1.putExtra("com.dv.get.ACTION_LIST_PATH", (String) intent.getExtras().get("com.dv.get.ACTION_LIST_PATH")); // destination directory (default "Settings - Downloading - Folder for files")
        intent1.putExtra("com.android.extra.filename", (String) intent.getExtras().get("com.android.extra.filename"));
        intent.putExtra("com.dv.get.ACTION_LIST_OPEN", false);
        try {

            startActivity(intent1);
            Intent startMain = new Intent(Intent.ACTION_MAIN);


            System.out.println("starting service");

        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Log.w("my_app", "not found");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}