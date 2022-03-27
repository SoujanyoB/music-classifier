package com.soujanyo.music_classifier.utils;

import static android.Manifest.permission.RECORD_AUDIO;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    private AudioRecord audioRecorder;
    private File recordFile;
    private Context context;

    private final static int BUFFER_SIZE = 4096;

    private final static int FMT_CHUNK_ID = 0x20746D66;
    private final static int DATA_CHUNK_ID = 0x61746164;
    private final static int RIFF_CHUNK_ID = 0x46464952;
    private final static int RIFF_TYPE_ID = 0x45564157;

    private static final int RECORDER_BPP = 16;
    private static final int RECORDER_SAMPLE_RATE = 22050;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;


    public Recorder(Context context) {
        this.context = context;
        this.mediaRecorder = new MediaRecorder();

        if (ContextCompat.checkSelfPermission(this.context,
                RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Recorder: hello");
            this.audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLE_RATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, BUFFER_SIZE);
        } else {
            Log.d(TAG, "Recorder: bye");
        }


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
//        mediaRecorder.start();

        this.audioRecorder.startRecording();

    }


    public File getRecordFile() {
        return recordFile;
    }

//    public File stopRecord() {
//        if (mediaRecorder != null) {
//            mediaRecorder.stop();
//            mediaRecorder.reset();
//            mediaRecorder = null;
//        }
//
//        return recordFile;
//    }

    public void stopRecord() {
        this.audioRecorder.stop();
    }
}