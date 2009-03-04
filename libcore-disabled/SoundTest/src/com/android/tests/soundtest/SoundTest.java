/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tests.soundtest;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.HandlerInterface;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.InputStream;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;


public class SoundTest extends Activity implements HandlerInterface {

    public Context mContext;
    private LinearLayout mLinearLayout;
    public LayoutParams mParams;
    Button button, button2;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Window wp = getWindow();
        mContext = wp.getContext();
        mParams = wp.getAttributes();

        mLinearLayout = new LinearLayout(this);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        setContentView(mLinearLayout);

        button = new Button(mContext);
        button.setMinimumWidth(300);
        button.setMinimumHeight(70);
        button.setTextSize(14);
        button.setText("Play sample");
        button.setOnClickListener(buttonListener);

        mLinearLayout.addView(button, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT));
        
        button2 = new Button(mContext);
        button2.setMinimumWidth(300);
        button2.setMinimumHeight(70);
        button2.setTextSize(14);
        button2.setText("Play MIDI");
        button2.setOnClickListener(buttonListener2);

        mLinearLayout.addView(button2, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT));
        
    }

    private OnClickListener buttonListener = new OnClickListener() {
        public void onClick(View v) {
            try {
                button.setText(button.getText() + ".");
                
                int RENDER_BUFF_SIZE = 1024*48;

                InputStream is = getAssets().open("fx_foghorn.mp3");
                
                AudioInputStream ais = null;

                ais = AudioSystem.getAudioInputStream(is);

                AudioFormat af = ais.getFormat();
                SourceDataLine sdl = null;
                DataLine.Info dli = new DataLine.Info(SourceDataLine.class, af);
                sdl = (SourceDataLine)AudioSystem.getLine(dli);

                sdl.open(af);
                sdl.start();

                int bytesReaded = 0;
                byte samplesBuff[] = new byte[RENDER_BUFF_SIZE];

                while (bytesReaded != -1) {
                    bytesReaded = ais.read(samplesBuff, 0, samplesBuff.length);
                    if (bytesReaded > 0) {
                        sdl.write(samplesBuff, 0, bytesReaded);
                    }
                }

                sdl.drain();
                sdl.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    };

    private OnClickListener buttonListener2 = new OnClickListener() {
        public void onClick(View v) {
            try {
                button2.setText(button2.getText() + ".");
                
                int RENDER_BUFF_SIZE = 1024*48;

                InputStream is = getAssets().open("Dancing_Queen.mid");
                
                Sequencer s = MidiSystem.getSequencer(); 
                s.open();
                s.setSequence(is);
                s.setLoopCount(1);
                s.start();
                
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    };
    
    public void handleMessage(Message arg0) {
    }
}
