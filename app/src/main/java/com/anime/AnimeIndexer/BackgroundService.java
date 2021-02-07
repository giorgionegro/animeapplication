package com.anime.AnimeIndexer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class BackgroundService extends Service {


    private final IBinder mBinder = new LocalBinder();

    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent intent1 = intent.setClassName("com.dv.adm", "com.dv.adm.AEditor");
        intent1.putExtra("com.dv.get.ACTION_LIST_ADD", (String) intent.getExtras().get("com.dv.get.ACTION_LIST_ADD")); // or "url1<line>url2...", or "url1<info>name_ext1<line>..."
// optional
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent1.putExtra("com.dv.get.ACTION_LIST_PATH", (String) intent.getExtras().get("com.dv.get.ACTION_LIST_PATH")); // destination directory (default "Settings - Downloading - Folder for files")
        intent1.putExtra("com.android.extra.filename", (String) intent.getExtras().get("com.android.extra.filename"));
        intent1.putExtra("com.dv.get.ACTION_LIST_OPEN", false);

        startActivity(intent1);


        System.out.println("starting service");

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public static class LocalBinder extends Binder {
    }
}