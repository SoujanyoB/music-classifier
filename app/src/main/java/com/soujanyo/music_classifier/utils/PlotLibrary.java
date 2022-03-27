package com.soujanyo.music_classifier.utils;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorSpace;
import android.util.Log;

import org.apache.commons.math3.complex.Complex;

public class PlotLibrary {


    private static final String TAG = "PlotLibrary";

    public static void plotSTFT(Complex[][] stft) {

        for (Complex[] complexes : stft) {
            for (Complex complex : complexes) {
                Log.d(TAG, "plotSTFT: ");
                complex.getReal();
            }
        }

    }


}
