//package com.ethanmad.engarde;
//
//import android.app.Activity;
//import android.media.AudioFormat;
//import android.media.AudioManager;
//import android.media.AudioTrack;
//import android.os.Bundle;
//import android.os.Handler;
//
//import com.ethanmad.engarde.R;
//
//
///**
// * Created by ethan on 7/31/14.
// */
//public class PlaySound extends Activity {
//    // originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
//    // and modified by Steve Pomeroy <steve@staticfree.info>
//    private final int duration = 3; // seconds
//    private final int sampleRate = 8000;
//    private final int numSamples = duration * sampleRate;
//    private final double sample[] = new double[numSamples];
//    private final byte generatedSnd[] = new byte[2 * numSamples];
//    private final double freqOfTone = 440; // hz
//    Handler handler = new Handler();
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main_activity);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        // Use a new tread as this can take a while
//        final Thread thread = new Thread(new Runnable() {
//            public void run() {
//                genTone();
//                handler.post(new Runnable() {
//
//                    public void run() {
//                        playSound();
//                    }
//                });
//            }
//        });
//        thread.start();
//    }
//
//    void genTone() {
//        // fill out the array
//        for (int i = 0; i < numSamples; ++i) {
//            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
//        }
//
//        // convert to 16 bit pcm sound array
//        // assumes the sample buffer is normalised.
//        int idx = 0;
//        for (final double dVal : sample) {
//            // scale to maximum amplitude
//            final short val = (short) ((dVal * 32767));
//            // in 16 bit wav PCM, first byte is the low order byte
//            generatedSnd[idx++] = (byte) (val & 0x00ff);
//            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
//
//        }
//    }
//
//    void playSound() {
//        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
//                sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
//                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
//                AudioTrack.MODE_STATIC);
//        audioTrack.write(generatedSnd, 0, generatedSnd.length);
//        audioTrack.play();
//    }
//}
