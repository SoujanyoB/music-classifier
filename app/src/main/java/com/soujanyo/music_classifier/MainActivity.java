package com.soujanyo.music_classifier;

import static android.Manifest.permission.RECORD_AUDIO;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.jlibrosa.audio.JLibrosa;
import com.jlibrosa.audio.exception.FileFormatNotSupportedException;
import com.jlibrosa.audio.wavFile.WavFileException;
import com.soujanyo.music_classifier.utils.Recorder;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_PERMISSION_CODE = 1;

    private ImageView playButtonImageView, stopButtonImageView;
    private LinearLayout filePickerLinearLayout;
    private Recorder recorder;

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                // Handle the returned Uri
                Log.d("TAG", "onActivityResult: " + uri.toString());
                File file = new File(uri.getPath());

//                FFmpeg ffmpeg = FFmpeg.getInstance(getApplicationContext());
//
//                try {
//                    ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
//                        @Override
//                        public void onSuccess() {
//                            super.onSuccess();
//
//                            try {
//                                ffmpeg.execute(new String[] {"--version"}, new ExecuteBinaryResponseHandler() {
//                                    @Override
//                                    public void onStart() {
//                                        super.onStart();
//                                        Log.d("TAG", "onStart: started conversion");
//                                    }
//
//                                    @Override
//                                    public void onSuccess(String message) {
//                                        super.onSuccess(message);
//                                        Log.d("TAG", "onSuccess: conversion success");
//
//                                        JLibrosa jLibrosa = new JLibrosa();
//
//                                        int SAMPLE_RATE = 22050;
//                                        try {
//                                            ArrayList<Float> audioFeatures = jLibrosa.loadAndReadAsList(uri.getPath(),
//                                                    SAMPLE_RATE, 5);
//
//                                            Log.d("TAG", "librosa: success " + audioFeatures);
//
//
//                                        } catch (IOException | WavFileException | FileFormatNotSupportedException e) {
//                                            Log.e("TAG", "librosa: " + e.getLocalizedMessage());
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                });
//                            } catch (FFmpegCommandAlreadyRunningException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//                } catch (FFmpegNotSupportedException e) {
//                    e.printStackTrace();
//                }


                final String[] split = file.getPath().split(":");
                Log.i(Config.TAG, split[1]);

                String exe = "-i " + file.getAbsolutePath() + " -ar 22050 " + getOutputPath(file.getAbsolutePath());

                long exeId = FFmpeg.executeAsync(exe, (executionId, returnCode) -> {
                    if(returnCode == RETURN_CODE_SUCCESS) {
                        Log.d("TAG", "apply: " + executionId);
                    } else if(returnCode == RETURN_CODE_CANCEL) {
                        Log.i(Config.TAG, "Async command execution cancelled by user!");
                    } else {
                        Log.i(Config.TAG, "Async command execution failed!");
                    }


                });

                Log.d("TAG", "execution id: " + exeId);



            });


    private String getOutputPath(String path) {
        return path.substring(0, path.length() - 3) + ".wav";
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        recorder = new Recorder(getApplicationContext());

        playButtonImageView = findViewById(R.id.playButtonImageView);
        stopButtonImageView = findViewById(R.id.stopButtonImageView);
        filePickerLinearLayout = findViewById(R.id.filePickerLinearLayout);


        playButtonImageView.setOnClickListener(this);
        stopButtonImageView.setOnClickListener(this);
        filePickerLinearLayout.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {

        int id = view.getId();

        if (id == R.id.playButtonImageView) {
            // do something
            if (checkPermission()) {
                recorder = new Recorder(getApplicationContext());
                recorder.startRecord();
                Log.d("TAG", "onClick: play");
                playButtonImageView.setVisibility(View.GONE);
                playButtonImageView.setEnabled(false);
                stopButtonImageView.setVisibility(View.VISIBLE);
                stopButtonImageView.setEnabled(true);
            } else {
                requestPermission();

            }

        }

        if (id == R.id.stopButtonImageView) {
            recorder.stopRecord();
            Log.d("TAG", "onClick: stop");
            playButtonImageView.setVisibility(View.VISIBLE);
            playButtonImageView.setEnabled(true);
            stopButtonImageView.setVisibility(View.GONE);
            stopButtonImageView.setEnabled(false);
        }

        if (id == R.id.filePickerLinearLayout) {
            if (checkPermission()) {
                // do something
                mGetContent.launch("audio/*");

            } else {
                requestPermission();
            }
            Log.d("TAG", "onClick: file picker");
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{RECORD_AUDIO}, REQUEST_PERMISSION_CODE);
    }

    public boolean checkPermission() {

        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                boolean recordPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;


                if (recordPermission) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}