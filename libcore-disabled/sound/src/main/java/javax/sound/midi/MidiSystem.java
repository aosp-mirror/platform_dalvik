/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package javax.sound.midi;

import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.spi.MidiDeviceProvider;
import javax.sound.midi.spi.MidiFileReader;
import javax.sound.midi.spi.MidiFileWriter;
import javax.sound.midi.spi.SoundbankReader;

import org.apache.harmony.sound.utils.ProviderService;

public class MidiSystem {
    //path to javax.sound.midi.spi.MidiDeviceProvider file in the jar-file
    private final static String midiDeviceProviderPath = 
        "META-INF/services/javax.sound.midi.spi.MidiDeviceProvider";
    
    //path to javax.sound.midi.spi.MidiFileReader file in the jar-file
    private final static String midiFileReaderPath =
        "META-INF/services/javax.sound.midi.spi.MidiFileReader";
    
    //path to javax.sound.midi.spi.MidiFileWriter file in the jar-file
    private final static String midiFileWriterPath =
        "META-INF/services/javax.sound.midi.spi.MidiFileWriter";
    
    //path to javax.sound.midi.spi.SoundbankReader file in the jar-file
    private final static String soundbankReaderPath =
        "META-INF/services/javax.sound.midi.spi.SoundbankReader";
    
    //key to find default receiver in the sound.properties file
    private final static String receiverName = "javax.sound.midi.Receiver";
    
    //key to find default sequencer in the sound.properties file
    private final static String sequencerName = "javax.sound.midi.Sequencer";
    
    //key to find default synthesizer in the sound.properties file
    private final static String synthesizerName = "javax.sound.midi.Synthesizer";
    
    //key to find default transmitter in the sound.properties file
    private final static String transmitterName = "javax.sound.midi.Transmitter";
    
    public static MidiDevice getMidiDevice(MidiDevice.Info info)
            throws MidiUnavailableException {
        //FIXME
        /*
         * this method must to throw out MidiUnavailableException if requested device
         * is not available
         */
        
        /* 
         * obtain the list of MidiDeviceProviders
         */
        List<?> deviceProviders = ProviderService.getProviders(midiDeviceProviderPath);
        /*
         * find device that describes by parameter info and return it
         */
        for (int i = 0; i < deviceProviders.size(); i++) {
            MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(i)).getDeviceInfo();
            for (Info element : deviceInfo) {
                if (element.equals(info)) {
                    return ((MidiDeviceProvider) deviceProviders.get(i)).getDevice(info);
                }
            }
        }
        /*
         * if we can't find device with requested info, we throw out IllegalArgumentException
         */
        throw new IllegalArgumentException("Requested device not installed: " + info.getName());
    }

    public static MidiDevice.Info[] getMidiDeviceInfo() {
        /*
         * obtain the list of MidiDeviceProviders
         */
        List<?> deviceProviders = ProviderService.getProviders(midiDeviceProviderPath);
        //variable to save MidiDevice.Info
        List<MidiDevice.Info> infos = new ArrayList<MidiDevice.Info>();
        /*
         * look through list of providers and save info of devices
         */
        for (int i = 0; i < deviceProviders.size(); i++) {
            MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(i)).getDeviceInfo();
            for (Info element : deviceInfo) {
                infos.add(element);
            }
        }
        
        MidiDevice.Info[] temp = new MidiDevice.Info[infos.size()];
        return infos.toArray(temp);
    }

    public static MidiFileFormat getMidiFileFormat(File file) throws InvalidMidiDataException,
            IOException {
        /*
         * obtain the list of MidiFileReaderProviders
         */
        List<?> fileReaderProviders = ProviderService.getProviders(midiFileReaderPath);
        if (fileReaderProviders.size() == 0) {
            //FIXME
            /*
             * I don't understand what type of exception we should throw out if we haven't
             * appropriate providers...
             * Maybe here is should be MidiUnavailableException
             */
            throw new Error("There is no MidiFileReaderProviders on your system!!!");
        }
        /*
         * It's not determine what provider for this service I should to use, and so
         * I use the first one
         */
        return ((MidiFileReader) fileReaderProviders.get(0)).getMidiFileFormat(file);
    }

    public static MidiFileFormat getMidiFileFormat(InputStream stream) throws InvalidMidiDataException, 
            IOException {
        /*
         * obtain the list of MidiFileReaderProviders
         */
        List<?> fileReaderProviders = ProviderService.getProviders(midiFileReaderPath);
        if (fileReaderProviders.size() == 0) {
            //FIXME
            /*
             * I don't understand what type of exception we should throw out if we haven't
             * appropriate providers...
             * Maybe here is should be MidiUnavailableException
             */
            throw new Error("There is no MidiFileReaderProviders on your system!!!");
        }
        /*
         * It's not determine what provider for this service I should to use, and so
         * I use the first one
         */
        return ((MidiFileReader) fileReaderProviders.get(0)).getMidiFileFormat(stream);
    }

    public static MidiFileFormat getMidiFileFormat(URL url) throws InvalidMidiDataException,
            IOException {
        /*
         * obtain the list of MidiFileReaderProviders
         */
        List<?> fileReaderProviders = ProviderService.getProviders(midiFileReaderPath);
        if (fileReaderProviders.size() == 0) {
            //FIXME
            /*
             * I don't understand what type of exception we should throw out if we haven't
             * appropriate providers...
             * Maybe here is should be MidiUnavailableException
             */
            throw new Error("There is no MidiFileReaderProviders on your system!!!");
        }
        /*
         * It's not determine what provider for this service I should to use, and so
         * I use the first one
         */
        return ((MidiFileReader) fileReaderProviders.get(0)).getMidiFileFormat(url);
    }

    public static int[] getMidiFileTypes() {
        /*
         * obtain the list of MidiFileWriterProviders
         */
        List<?> fileWriterProviders = ProviderService.getProviders(midiFileWriterPath);
        if (fileWriterProviders.size() == 0) {
            //FIXME
            /*
             * I don't understand what type of exception we should throw out if we haven't
             * appropriate providers...
             * Maybe here is should be MidiUnavailableException
             */
            throw new Error("There is no MidiFileWriterProviders on your system!!!");
        }
        /*
         * It's not determine what provider for this service I should to use, and so
         * I use the first one
         */
        return ((MidiFileWriter) fileWriterProviders.get(0)).getMidiFileTypes();
    }

    public static int[] getMidiFileTypes(Sequence sequence) {
        /*
         * obtain the list of MidiFileWriterProviders
         */
        List<?> fileWriterProviders = ProviderService.getProviders(midiFileWriterPath);
        if (fileWriterProviders.size() == 0) {
            //FIXME
            /*
             * I don't understand what type of exception we should throw out if we haven't
             * appropriate providers...
             * Maybe here is should be MidiUnavailableException
             */
            throw new Error("There is no MidiFileWriterProviders on your system!!!");
        }
        /*
         * It's not determine what provider for this service I should to use, and so
         * I use the first one
         */
        return ((MidiFileWriter) fileWriterProviders.get(0)).getMidiFileTypes(sequence);
    }

    public static Receiver getReceiver() throws MidiUnavailableException {
        /*
         * description of the default device for javax.sound.midi.Receiver
         */
        List<String> defaultDevice = ProviderService.getDefaultDeviceDescription(receiverName);
        /*
         * obtain the list of MidiDeviceProviders
         */
        List<?> deviceProviders = ProviderService.getProviders(midiDeviceProviderPath);
        String provName;
        int deviceNum = -1;
        /*
         * defaultDevice.get(0) --> provider
         * defaultDevice.get(1) --> name
         */      
        if (defaultDevice.size() != 0) {
            /*
             * obtain the provider number in the list of deviceProviders that is provider for default device
             */
            for (int i = 0; i < deviceProviders.size(); i++) {
                provName = deviceProviders.get(i).toString();
                if (provName.substring(0, provName.indexOf("@")).equals(defaultDevice.get(0))) {
                    deviceNum = i;
                    break;
                }
            }
            /*
             * the first case: find the same provider and name that describes by default device
             */
            if (deviceNum != -1) {
                MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDeviceInfo();
                for (Info element : deviceInfo) {
                    if (element.getName().equals(defaultDevice.get(1))) {
                        try {
                            return ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element).getReceiver();
                        } catch (MidiUnavailableException e) {}
                    }
                }
            for (Info element : deviceInfo) {
                    try {
                        return ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element).getReceiver();
                    } catch (MidiUnavailableException e) {}
                }
            }
            /*
             * if we don't find again, find any receivers describe by name
             */
            for (int i = 0; i < deviceProviders.size(); i++) {
                MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(i)).getDeviceInfo();
                for (Info element : deviceInfo) {
                    if (element.getName().equals(defaultDevice.get(1))) {
                        try {
                            return ((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element).getReceiver();
                        } catch (MidiUnavailableException e) {}
                    }
                }
            }
        }
        /*
         * in the last case we look throw all providers and find any receiver
         */
        for (int i = 0; i < deviceProviders.size(); i++) {
            MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(i)).getDeviceInfo();
            for (Info element : deviceInfo) {
                try {
                    return ((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element).getReceiver();
                } catch (MidiUnavailableException e) {}
            }
        }
        /*
         * if we don't find anyway, we throw out MidiUnavailableException
         */
        throw new MidiUnavailableException("There are no Recivers installed on your system!");
    }

    public static Sequence getSequence(File file) throws InvalidMidiDataException, IOException {
        /*
         * obtain the list of MidiFileReaderProviders
         */
        List<?> fileReaderProviders = ProviderService.getProviders(midiFileReaderPath);
        // BEGIN android-added
        try {
            ((List)fileReaderProviders).add((Object)Class.forName("com.android.internal.sound.midi.AndroidMidiFileReader").newInstance());
        } catch (Exception ex) {
            // Ignore
        }
        // END android-added
        if (fileReaderProviders.size() == 0) {
            //FIXME
            /*
             * I don't understand what type of exception we should throw out if we haven't
             * appropriate providers...
             * Maybe here is should be MidiUnavailableException
             */
            throw new Error("There is no MidiFileReaderProviders on your system!!!");
        }
        /*
         * It's not determine what provider for this service I should to use, and so
         * I use the first one
         */
        return ((MidiFileReader) fileReaderProviders.get(0)).getSequence(file);
    }

    public static Sequence getSequence(InputStream stream) throws InvalidMidiDataException,
            IOException {
        /*
         * obtain the list of MidiFileReaderProviders
         */
        List<?> fileReaderProviders = ProviderService.getProviders(midiFileReaderPath);
        // BEGIN android-added
        try {
            ((List)fileReaderProviders).add(Class.forName("com.android.internal.sound.midi.AndroidMidiFileReader").newInstance());
        } catch (Exception ex) {
            // Ignore
        }
        // END android-added
        if (fileReaderProviders.size() == 0) {
            //FIXME
            /*
             * I don't understand what type of exception we should throw out if we haven't
             * appropriate providers...
             * Maybe here is should be MidiUnavailableException
             */
            throw new Error("There is no MidiFileReaderProviders on your system!!!");
        }
        /*
         * It's not determine what provider for this service I should to use, and so
         * I use the first one
         */
        return ((MidiFileReader) fileReaderProviders.get(0)).getSequence(stream);
    }

    public static Sequence getSequence(URL url) throws InvalidMidiDataException, IOException {
        /*
         * obtain the list of MidiFileReaderProviders
         */
        List<?> fileReaderProviders = ProviderService.getProviders(midiFileReaderPath);
        // BEGIN android-added
        try {
            ((List)fileReaderProviders).add(Class.forName("com.android.internal.sound.midi.AndroidMidiFileReader").newInstance());
        } catch (Exception ex) {
            // Ignore
        }
        // END android-added
        if (fileReaderProviders.size() == 0) {
            //FIXME
            /*
             * I don't understand what type of exception we should throw out if we haven't
             * appropriate providers...
             * Maybe here is should be MidiUnavailableException
             */
            throw new Error("There is no MidiFileReaderProviders on your system!!!");
        }
        /*
         * It's not determine what provider for this service I should to use, and so
         * I use the first one
         */
        return ((MidiFileReader) fileReaderProviders.get(0)).getSequence(url);
    }

    public static Sequencer getSequencer() throws MidiUnavailableException {
        /*
         * this method is equals to method MidiSystem.getSequencer(true)
         */
        return getSequencer(true);
    }

    public static Sequencer getSequencer(boolean connected) throws MidiUnavailableException {
        /*
         * description of the default device for javax.sound.midi.Sequencer
         */
        List<String> defaultDevice = ProviderService.getDefaultDeviceDescription(sequencerName);
        /*
         * obtain the list of MidiDeviceProviders
         */
        List<?>  deviceProviders = ProviderService.getProviders(midiDeviceProviderPath);
        
        Sequencer sequencer;
        Transmitter seqTrans;
        Synthesizer synth;
        Receiver recv;
        String provName;
        int deviceNum = -1;
        /*
         * defaultDevice.get(0) --> provider
         * defaultDevice.get(1) --> name
         */      
        if (defaultDevice.size() != 0) {
            /*
             * obtain the provider number in the list of deviceProviders that is provider for default device
             */
            for (int i = 0; i < deviceProviders.size(); i++) {
                provName = deviceProviders.get(i).toString();
                if (provName.substring(0, provName.indexOf("@")).equals(defaultDevice.get(0))) {
                    deviceNum = i;
                    break;
                }
            }
            /*
             * the first case: find the same provider and name that describes by default device
             */
            if (deviceNum != -1) {
                MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDeviceInfo();
                for (Info element : deviceInfo) {
                    if (element.getName().equals(defaultDevice.get(1))) {
                        if (((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element) instanceof Sequencer) {
                            if (connected) {
                                sequencer = (Sequencer) ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element);
                                seqTrans = sequencer.getTransmitter();
                                try {
                                    synth = MidiSystem.getSynthesizer();
                                    recv = synth.getReceiver();                                    
                                } catch (MidiUnavailableException e) {
                                    /*
                                     * if we haven't Synthesizer in the system, we use default receiver
                                     */
                                    recv = MidiSystem.getReceiver();
                                }
                                seqTrans.setReceiver(recv);
                                return sequencer;
                            }
                            return (Sequencer) ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element);
                        }
                    }
                }
                for (Info element : deviceInfo) {
                    if (((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element) instanceof Sequencer) {
                        if (connected) {
                            sequencer = (Sequencer) ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element);
                            seqTrans = sequencer.getTransmitter();
                            try {
                                synth = MidiSystem.getSynthesizer();
                                recv = synth.getReceiver();                                    
                            } catch (MidiUnavailableException e) {
                                /*
                                 * if we haven't Synthesizer in the system, we use default receiver
                                 */
                                recv = MidiSystem.getReceiver();
                            }
                            seqTrans.setReceiver(recv);
                            return sequencer;
                        }
                        return (Sequencer) ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element);
                    }
                }
            }
            /*
             * if we don't find again, find any receivers describe by name
             */
            for (int i = 0; i < deviceProviders.size(); i++) {
                MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(i)).getDeviceInfo();
                for (Info element : deviceInfo) {
                    if (element.getName().equals(defaultDevice.get(1))) {
                        if (((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element) instanceof Sequencer) {
                            if (connected) {
                                sequencer = (Sequencer) ((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element);
                                seqTrans = sequencer.getTransmitter();
                                try {
                                    synth = MidiSystem.getSynthesizer();
                                    recv = synth.getReceiver();                                    
                                } catch (MidiUnavailableException e) {
                                    /*
                                     * if we haven't Synthesizer in the system, we use default receiver
                                     */
                                    recv = MidiSystem.getReceiver();
                                }
                                seqTrans.setReceiver(recv);
                                return sequencer;
                            }
                            return (Sequencer) ((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element);
                        }
                    }
                }
            }
        }
        /*
         * in the last case we look throw all providers and find any receiver
         */
        for (int i = 0; i < deviceProviders.size(); i++) {
            MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(i)).getDeviceInfo();
            for (Info element : deviceInfo) {
                if (((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element) instanceof Sequencer) {
                    if (connected) {
                        sequencer = (Sequencer) ((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element);
                        seqTrans = sequencer.getTransmitter();
                        try {
                            synth = MidiSystem.getSynthesizer();
                            recv = synth.getReceiver();                                    
                        } catch (MidiUnavailableException e) {
                            /*
                             * if we haven't Synthesizer in the system, we use default receiver
                             */
                            recv = MidiSystem.getReceiver();
                        }
                        seqTrans.setReceiver(recv);
                        return sequencer;
                    }
                    return (Sequencer) ((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element);
                }
            }
        }
        // BEGIN android-added
        try {
            return (Sequencer)(Class.forName("com.android.internal.sound.midi.AndroidSequencer").newInstance());
        } catch (Exception ex) {
            // Ignore
        }
        // END android-added
        /*
         * if we don't find anyway, we throw out MidiUnavailableException
         */
        throw new MidiUnavailableException("There are no Synthesizers installed on your system!");
    }

    public static Soundbank getSoundbank(File file) throws InvalidMidiDataException,
            IOException {
        /*
         * obtain the list of SoundbankReaderProviders
         */
        List<?> soundbankReaderProviders = ProviderService.getProviders(soundbankReaderPath);
        if (soundbankReaderProviders.size() == 0) {
            //FIXME
            /*
             * I don't understand what type of exception we should throw out if we haven't
             * appropriate providers...
             * Maybe here is should be MidiUnavailableException
             */
            throw new Error("There is no SoundbankReaderProviders on your system!!!");
        }
        /*
         * It's not determine what provider for this service I should to use, and so
         * I use the first one
         */
        return ((SoundbankReader) soundbankReaderProviders.get(0)).getSoundbank(file);
    }

    public static Soundbank getSoundbank(InputStream stream) throws InvalidMidiDataException, IOException {
        /*
         * obtain the list of SoundbankReaderProviders
         */
        List<?> soundbankReaderProviders = ProviderService.getProviders(soundbankReaderPath);
        if (soundbankReaderProviders.size() == 0) {
            //FIXME
            /*
             * I don't understand what type of exception we should throw out if we haven't
             * appropriate providers...
             * Maybe here is should be MidiUnavailableException
             */
            throw new Error("There is no SoundbankReaderProviders on your system!!!");
        }
        /*
         * It's not determine what provider for this service I should to use, and so
         * I use the first one
         */
        return ((SoundbankReader) soundbankReaderProviders.get(0)).getSoundbank(stream);
    }

    public static Soundbank getSoundbank(URL url) throws InvalidMidiDataException, IOException {
        /*
         * obtain the list of SoundbankReaderProviders
         */
        List<?> soundbankReaderProviders = ProviderService.getProviders(soundbankReaderPath);
        if (soundbankReaderProviders.size() == 0) {
            //FIXME
            /*
             * I don't understand what type of exception we should throw out if we haven't
             * appropriate providers...
             * Maybe here is should be MidiUnavailableException
             */
            throw new Error("There is no SoundbankReaderProviders on your system!!!");
        }
        /*
         * It's not determine what provider for this service I should to use, and so
         * I use the first one
         */
        return ((SoundbankReader) soundbankReaderProviders.get(0)).getSoundbank(url);
    }

    public static Synthesizer getSynthesizer() throws MidiUnavailableException {
        /*
         * description of the default device for javax.sound.midi.Synthesizer
         */
        List<String> defaultDevice = ProviderService.getDefaultDeviceDescription(synthesizerName);
        /*
         * obtain the list of MidiDeviceProviders
         */
        List<?> deviceProviders = ProviderService.getProviders(midiDeviceProviderPath);
        String provName;
        int deviceNum = -1;
        
        /*
         * defaultDevice.get(0) --> provider
         * defaultDevice.get(1) --> name
         */      
        if (defaultDevice.size() != 0) {
            /*
             * obtain the provider number in the list of deviceProviders that is provider for default device
             */
            for (int i = 0; i < deviceProviders.size(); i++) {
                provName = deviceProviders.get(i).toString();
                if (provName.substring(0, provName.indexOf("@")).equals(defaultDevice.get(0))) {
                    deviceNum = i;
                    break;
                }
            }
            /*
             * the first case: find the same provider and name that describes by default device
             */
            if (deviceNum != -1) {
                MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDeviceInfo();
                for (Info element : deviceInfo) {
                    if (element.getName().equals(defaultDevice.get(1))) {
                        if (((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element) instanceof Synthesizer) {
                            return (Synthesizer) ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element);
                        }
                    }
                }
                for (Info element : deviceInfo) {
                    if (((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element) instanceof Synthesizer) {
                        return (Synthesizer) ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element);
                    }
                }
            }
            /*
             * if we don't find again, find any receivers describe by name
             */
            for (int i = 0; i < deviceProviders.size(); i++) {
                MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(i)).getDeviceInfo();
                for (Info element : deviceInfo) {
                    if (element.getName().equals(defaultDevice.get(1))) {
                        if (((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element) instanceof Synthesizer) {
                            return (Synthesizer) ((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element);
                        }
                    }
                }
            }
        }
        /*
         * in the last case we look throw all providers and find any receiver
         */
        for (int i = 0; i < deviceProviders.size(); i++) {
            MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(i)).getDeviceInfo();
            for (Info element : deviceInfo) {
                if (((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element) instanceof Synthesizer) {
                    return (Synthesizer) ((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element);
                }
            }
        }
        /*
         * if we don't find anyway, we throw out MidiUnavailableException
         */
        throw new MidiUnavailableException("There are no Synthesizers installed on your system!");
    }

    public static Transmitter getTransmitter() throws MidiUnavailableException {
        /*
         * description of the default device for javax.sound.midi.Transmitter
         */
        List<String> defaultDevice = ProviderService.getDefaultDeviceDescription(transmitterName);
        /*
         * obtain the list of MidiDeviceProviders
         */
        List<?> deviceProviders = ProviderService.getProviders(midiDeviceProviderPath);
        String provName;
        int deviceNum = -1;
        /*
         * defaultDevice.get(0) --> provider
         * defaultDevice.get(1) --> name
         */      
        if (defaultDevice.size() != 0) {
            /*
             * obtain the provider number in the list of deviceProviders that is provider for default device
             */
            for (int i = 0; i < deviceProviders.size(); i++) {
                provName = deviceProviders.get(i).toString();
                if (provName.substring(0, provName.indexOf("@")).equals(defaultDevice.get(0))) {
                    deviceNum = i;
                    break;
                }
            }
            /*
             * the first case: find the same provider and name that describes by default device
             */
            if (deviceNum != -1) {
                MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDeviceInfo();
                for (Info element : deviceInfo) {
                    if (element.getName().equals(defaultDevice.get(1))) {
                        try {
                            return ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element).getTransmitter();
                        } catch (MidiUnavailableException e) {}
                    }
                }
                for (Info element : deviceInfo) {
                    try {
                        return ((MidiDeviceProvider) deviceProviders.get(deviceNum)).getDevice(element).getTransmitter();
                    } catch (MidiUnavailableException e) {}
                }
            }
            /*
             * if we don't find again, find any receivers describe by name
             */
            for (int i = 0; i < deviceProviders.size(); i++) {
                MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(i)).getDeviceInfo();
                for (Info element : deviceInfo) {
                    if (element.getName().equals(defaultDevice.get(1))) {
                        try {
                            return ((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element).getTransmitter();
                        } catch (MidiUnavailableException e) {}
                    }
                }
            }
        }
        /*
         * in the last case we look throw all providers and find any receiver
         */
        for (int i = 0; i < deviceProviders.size(); i++) {
            MidiDevice.Info[] deviceInfo = ((MidiDeviceProvider) deviceProviders.get(i)).getDeviceInfo();
            for (Info element : deviceInfo) {
                try {
                    return ((MidiDeviceProvider) deviceProviders.get(i)).getDevice(element).getTransmitter();
                } catch (MidiUnavailableException e) {}
            }
        }
        /*
         * if we don't find anyway, we throw out MidiUnavailableException
         */
        throw new MidiUnavailableException("There are no Transmitters installed on your system!");
    }

    public static boolean isFileTypeSupported(int fileType) {
        /*
         * obtain the list of MidiFileWriterProviders;
         * if we already obtain the list of providers, we don't obtain it again
         */
        List<?> fileWriterProviders = ProviderService.getProviders(midiFileWriterPath);
        if (fileWriterProviders.size() == 0) {
            //FIXME
            /*
             * I don't understand what type of exception we should throw out if we haven't
             * appropriate providers...
             * Maybe here is should be MidiUnavailableException
             */
            throw new Error("There is no MidiFileWriterProviders on your system!!!");
        }
        /*
         * It's not determine what provider for this service I should to use, and so
         * I use the first one
         */
        return ((MidiFileWriter) fileWriterProviders.get(0)).isFileTypeSupported(fileType);
    }

    public static boolean isFileTypeSupported(int fileType, Sequence sequence) {
        /*
         * obtain the list of MidiFileWriterProviders
         */
        List<?> fileWriterProviders = ProviderService.getProviders(midiFileWriterPath);
        if (fileWriterProviders.size() == 0) {
            //FIXME
            /*
             * I don't understand what type of exception we should throw out if we haven't
             * appropriate providers...
             * Maybe here is should be MidiUnavailableException
             */
            throw new Error("There is no MidiFileWriterProviders on your system!!!");
        }
        /*
         * It's not determine what provider for this service I should to use, and so
         * I use the first one
         */
        return ((MidiFileWriter) fileWriterProviders.get(0)).isFileTypeSupported(fileType, sequence);
    }

    public static int write(Sequence in, int type, File out) throws IOException {
        /*
         * obtain the list of MidiFileWriterProviders
         */
        List<?>  fileWriterProviders = ProviderService.getProviders(midiFileWriterPath);
        if (fileWriterProviders.size() == 0) {
            //FIXME
            /*
             * I don't understand what type of exception we should throw out if we haven't
             * appropriate providers...
             * Maybe here is should be MidiUnavailableException
             */
            throw new Error("There is no MidiFileWriterProviders on your system!!!");
        }
        /*
         * It's not determine what provider for this service I should to use, and so
         * I use the first one
         */
        return ((MidiFileWriter) fileWriterProviders.get(0)).write(in, type, out);
    }

    public static int write(Sequence in, int fileType, OutputStream out) throws IOException {
        /*
         * obtain the list of MidiFileWriterProviders
         */
        List<?>  fileWriterProviders = ProviderService.getProviders(midiFileWriterPath);
        if (fileWriterProviders.size() == 0) {
            //FIXME
            /*
             * I don't understand what type of exception we should throw out if we haven't
             * appropriate providers...
             * Maybe here is should be MidiUnavailableException
             */
            throw new Error("There is no MidiFileWriterProviders on your system!!!");
        }
        /*
         * It's not determine what provider for this service I should to use, and so
         * I use the first one
         */
        return ((MidiFileWriter) fileWriterProviders.get(0)).write(in, fileType, out);
    }
}
