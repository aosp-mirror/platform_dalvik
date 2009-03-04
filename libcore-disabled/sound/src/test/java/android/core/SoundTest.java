/*
 * Copyright (C) 2008 The Android Open Source Project
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

package android.core;

import android.media.MediaPlayer;

import com.android.internal.sound.midi.AndroidSequencer;
import com.android.internal.sound.sampled.AndroidClip;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.File;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;

public class SoundTest extends TestCase {

  public static String TAG = "SoundTest";

    // Regression test for #000000: Completion of MIDI file doesn't fire
    // corresponding event.
//    private boolean eventReceived = false;
//  
//    public void testMidiFileCompletion() {
//        try {
//            MediaPlayer player = new MediaPlayer();
//            
//            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                public void onCompletion(MediaPlayer player) {
//                    eventReceived = true;
//                }
//            });
//
//            player.setDataSource("/system/sounds/test.mid");
//            player.prepare();
//            player.start();
//            Thread.sleep(20000);
//            assertFalse("Player must be stopped", player.isPlaying());
//            assertTrue("Completion event must have been received", eventReceived);
//
//        } catch (Exception ex) {
//            throw new RuntimeException(ex);
//        }
//    }
  
    // Regression test for #872614: General javax.sound weirdness.
    public void testMidiSupport() {
        try {
            Sequencer sequencer = MidiSystem.getSequencer();
            Assert.assertTrue("AndroidSequencer must exist", sequencer instanceof AndroidSequencer);
            
            MidiDevice.Info info = sequencer.getDeviceInfo();
            Assert.assertNotNull("Device info must exist", info);
    
            Sequence sequence = MidiSystem.getSequence(new File("/system/sounds/test.mid"));
            Assert.assertNotNull("Sequence must exist", sequence);
    
            Assert.assertFalse("Sequencer must not be open", sequencer.isOpen());
            sequencer.open();
            Assert.assertTrue("Sequencer must be open", sequencer.isOpen());
            
            Assert.assertNull("Sequencer must not have Sequence", sequencer.getSequence());
            sequencer.setSequence(sequence);
            Assert.assertNotNull("Sequencer must have Sequence", sequencer.getSequence());
            
            Assert.assertFalse("Sequencer must not be running", sequencer.isRunning());
            sequencer.start();
            Thread.sleep(1000);
            Assert.assertTrue("Sequencer must be running (after 1 second)", sequencer.isRunning());
    
            Thread.sleep(3000);
            
            Assert.assertTrue("Sequencer must be running", sequencer.isRunning());
            sequencer.stop();
            Thread.sleep(1000);
            Assert.assertFalse("Sequencer must not be running (after 1 second)", sequencer.isRunning());
            
            sequencer.close();
            Assert.assertFalse("Sequencer must not be open", sequencer.isOpen());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    } 

    // Regression test for #872614: General javax.sound weirdness.
    public void testSampledSupport() {
        try {
            Clip clip = AudioSystem.getClip();
            Assert.assertTrue("AndroidClip must exist", clip instanceof AndroidClip);
            
            Line.Info info = clip.getLineInfo();
            Assert.assertNotNull("Line info must exist", info);
    
            AudioInputStream stream = AudioSystem.getAudioInputStream(new File("/system/media/audio/ringtones/ringer.ogg"));
            Assert.assertNotNull("AudioInputStream must exist", stream);
    
            Assert.assertFalse("Clip must not be open", clip.isOpen());
            clip.open(stream);
            Assert.assertTrue("Clip must be open", clip.isOpen());
            
            Assert.assertFalse("Clip must not be running", clip.isRunning());
            clip.start();
            Thread.sleep(1000);
            Assert.assertTrue("Clip must be running (after 1 second)", clip.isRunning());
    
            Thread.sleep(2000);
            
            Assert.assertTrue("Clip must be running", clip.isRunning());
            clip.stop();
            Thread.sleep(1000);
            Assert.assertFalse("Clip must not be running (after 1 second)", clip.isRunning());
            
            clip.close();
            Assert.assertFalse("Clip must not be open", clip.isOpen());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    } 
    
}
