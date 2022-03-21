package com.soujanyo.music_classifier.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class StorageHelper {

    private static String TAG = "StorageHelper";


    private static String getRandomName() {
        String choice = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder s = new StringBuilder();

        do {
            s.append(choice.charAt(new Random().nextInt(choice.length())));
        } while (s.length() < 9);


        return s.toString();
    }

    public static File createFile(Context context) {

        File cacheDir = context.getCacheDir();

        File file = new File(cacheDir.getAbsolutePath() + "/" + getRandomName() + ".wav");

        if(!file.exists()) {
            try {
                if(file.createNewFile()) {
                    Log.d(TAG, "createFile: File created at " + file.getAbsolutePath());
                }
            } catch (IOException e) {
                Log.e(TAG, "createFile: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        return file;

    }

}
