package com.soujanyo.music_classifier.utils;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.widget.TimePicker;
import android.widget.Toast;

import com.soujanyo.music_classifier.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Recorder {

    private static final String TAG = "RecorderClass";
    private MediaRecorder mediaRecorder;
    private File recordFile;
    private final Context context;


    public Recorder(Context context) {
        this.context = context;
        this.mediaRecorder = new MediaRecorder();

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        this.recordFile = StorageHelper.createFile(context);

        mediaRecorder.setOutputFile(this.recordFile.getAbsolutePath());

        try {
            mediaRecorder.prepare();
        } catch (IOException | IllegalStateException e) {
            Log.e(TAG, "startRecord: " + e.getLocalizedMessage());
        }
    }

    public void startRecord() {
        mediaRecorder.start();

    }

    public void stopRecord() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder = null;
        }
    }
}