package com.soujanyo.music_classifier;

import static android.Manifest.permission.RECORD_AUDIO;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.jlibrosa.audio.JLibrosa;
import com.jlibrosa.audio.exception.FileFormatNotSupportedException;
import com.jlibrosa.audio.wavFile.WavFile;
import com.jlibrosa.audio.wavFile.WavFileException;
import com.soujanyo.music_classifier.utils.ExtAudioRecorder;
import com.soujanyo.music_classifier.utils.StorageHelper;

import org.apache.commons.math3.complex.Complex;
import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;
import org.tensorflow.lite.task.core.BaseOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_PERMISSION_CODE = 1;
    private static final String TAG = "MainActivity";

    private ImageView playButtonImageView, stopButtonImageView;
    private LinearLayout filePickerLinearLayout;

    private File recordFile;
    private ExtAudioRecorder audioRecorder;

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                // Handle the returned Uri
                String path = getRealPath(uri);

                Log.d(TAG, ": " + path);


//                File file = new File(uri.toString());




                JLibrosa jLibrosa = new JLibrosa();

                try {
                    float[] signal = jLibrosa.loadAndRead(path, 22050, -1);

                    int noOfFrames = jLibrosa.getNoOfFrames();

                    int sampleRate = jLibrosa.getSampleRate();

                    Complex[][] stft = jLibrosa.generateSTFTFeatures(signal, sampleRate, 13);

                    float[][] mfccFeatureValues = jLibrosa.generateMFCCFeatures(signal, sampleRate, 13);


//                    Log.d(TAG, ": " + Arrays.deepToString(stft));


                    AudioClassifier.AudioClassifierOptions options =
                            AudioClassifier.AudioClassifierOptions.builder()
                                    .setBaseOptions(BaseOptions.builder().useGpu().build())
                                    .setMaxResults(1)
                                    .build();




                    AudioClassifier classifier = AudioClassifier.createFromFileAndOptions(getApplicationContext(), "model.tflite", options);

                    TensorAudio tensorAudio = classifier.createInputTensorAudio();



                    tensorAudio.load(signal);

                    List<Classifications> results = classifier.classify(tensorAudio);

                    Log.d(TAG, ": " + results.get(0).getHeadName());




                } catch (IOException | WavFileException | FileFormatNotSupportedException e) {
                    e.printStackTrace();
                }

            });


    public synchronized ArrayList<Short> getFileData(Uri uri) throws FileNotFoundException {

        ArrayList<Short> output = new ArrayList<>();

        final InputStream inputStream = getContentResolver().openInputStream(uri);




        return output;
    }

    private String getOutputPath(String path) {
        return path.substring(0, path.length() - 3) + ".wav";
    }

    public String getRealPath(Uri uri) {
        String docId = DocumentsContract.getDocumentId(uri);
        String[] split = docId.split(":");
        String type = split[0];
        Uri contentUri;
        switch (type) {
            case "image":
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;
            case "video":
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                break;
            case "audio":
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                break;
            default:
                contentUri = MediaStore.Files.getContentUri("external");
        }
        String selection = "_id=?";
        String[] selectionArgs = new String[]{
                split[1]
        };

        return getDataColumn(getApplicationContext(), contentUri, selection, selectionArgs);
    }

    public String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = "_data";
        String[] projection = {
                column
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(column);
                String value = cursor.getString(column_index);
                if (value.startsWith("content://") || !value.startsWith("/") && !value.startsWith("file://")) {
                    return null;
                }
                return value;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioRecorder = ExtAudioRecorder.getInstance(true);

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

                recordFile = StorageHelper.createFile(getApplicationContext());
                audioRecorder.setOutputFile(recordFile.getAbsolutePath());
                audioRecorder.prepare();
                audioRecorder.start();
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
            audioRecorder.stop();
            audioRecorder.release();
            Log.d("TAG", "onClick: stop");
            playButtonImageView.setVisibility(View.VISIBLE);
            playButtonImageView.setEnabled(true);
            stopButtonImageView.setVisibility(View.GONE);
            stopButtonImageView.setEnabled(false);


            Log.d(TAG, "onClick: " + getApplicationContext().getCacheDir());


            JLibrosa jLibrosa = new JLibrosa();

            try {
                float[] audioFeatures = jLibrosa.loadAndRead(recordFile.getAbsolutePath(), 22050, -1);

//                Log.d(TAG, "onClick: " + Arrays.toString(audioFeatures));

            } catch (IOException | WavFileException | FileFormatNotSupportedException e) {
                e.printStackTrace();
            }
        }

        if (id == R.id.filePickerLinearLayout) {
            if (checkPermission()) {
                // do something
                mGetContent.launch("audio/*");

//                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                intent.setType("audio/wav");
//                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                mGetContent.launch(intent);

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