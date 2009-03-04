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

package javax.sound.sampled;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Properties;

import javax.sound.sampled.spi.AudioFileReader;
import javax.sound.sampled.spi.AudioFileWriter;
import javax.sound.sampled.spi.FormatConversionProvider;
import javax.sound.sampled.spi.MixerProvider;

import org.apache.harmony.sound.utils.ProviderService;

import org.apache.harmony.sound.internal.nls.Messages;

public class AudioSystem {

    public static final int NOT_SPECIFIED = -1;

    private final static String audioFileReaderPath = "META-INF/services/javax.sound.sampled.spi.AudioFileReader"; //$NON-NLS-1$

    private final static String audioFileWriterPath = "META-INF/services/javax.sound.sampled.spi.AudioFileWriter"; //$NON-NLS-1$

    private final static String formatConversionProviderPath = "META-INF/services/javax.sound.sampled.spi.FormatConversionProvider"; //$NON-NLS-1$

    private final static String mixerProviderPath = "META-INF/services/javax.sound.sampled.spi.MixerProvider"; //$NON-NLS-1$

    private final static String CLIP = "javax.sound.sampled.Clip"; //$NON-NLS-1$

    private final static String PORT = "javax.sound.sampled.Port"; //$NON-NLS-1$

    private final static String SOURCEDATALINE = "javax.sound.sampled.SourceDataLine"; //$NON-NLS-1$

    private final static String TARGETDATALINE = "javax.sound.sampled.TargetDataLine"; //$NON-NLS-1$

    public static Mixer.Info[] getMixerInfo() {
        List<Mixer.Info> result = new ArrayList<Mixer.Info>();
        for (Iterator providers = ProviderService.getProviders(
                mixerProviderPath).iterator(); providers.hasNext();) {
            try {
                Mixer.Info[] infos = ((MixerProvider) (providers.next()))
                        .getMixerInfo();
                for (Mixer.Info info : infos) {
                    result.add(info);
                }
            } catch (ClassCastException e) {}
        }
        Mixer.Info[] temp = new Mixer.Info[result.size()];
        return result.toArray(temp);
    }

    public static Mixer getMixer(Mixer.Info info) {
        Mixer.Info[] infos;
        Mixer.Info inf;
        if (info == null) {
            infos = getMixerInfo();
            if (infos == null) {
                throw new IllegalArgumentException(
                        "No system default mixer installed"); //$NON-NLS-1$
            }
            inf = infos[0];
        } else {
            inf = info;
        }

        for (Iterator providers = ProviderService.getProviders(
                mixerProviderPath).iterator(); providers.hasNext();) {
            try {
                return ((MixerProvider) (providers.next())).getMixer(inf);
            } catch (ClassCastException e) {} catch (IllegalArgumentException e) {}
        }
        throw new IllegalArgumentException("Mixer not supported: " + inf); //$NON-NLS-1$
    }

    public static Line.Info[] getSourceLineInfo(Line.Info info) {
        List<Line.Info> result = new ArrayList<Line.Info>();
        for (Iterator providers = ProviderService.getProviders(
                mixerProviderPath).iterator(); providers.hasNext();) {
            try {
                MixerProvider pr = (MixerProvider) providers.next();
                Mixer.Info[] mixinfos = pr.getMixerInfo();
                for (Mixer.Info mixinfo : mixinfos) {
                    Mixer mix = pr.getMixer(mixinfo);
                    Line.Info[] linfos = mix.getSourceLineInfo(info);
                    for (Line.Info linfo : linfos) {
                        result.add(linfo);
                    }
                }
            } catch (ClassCastException e) {}
        }
        Line.Info[] temp = new Line.Info[result.size()];
        return result.toArray(temp);
    }

    public static Line.Info[] getTargetLineInfo(Line.Info info) {
        List<Line.Info> result = new ArrayList<Line.Info>();
        for (Iterator providers = ProviderService.getProviders(
                mixerProviderPath).iterator(); providers.hasNext();) {
            try {
                MixerProvider pr = (MixerProvider) providers.next();
                Mixer.Info[] mixinfos = pr.getMixerInfo();
                for (Mixer.Info mixinfo : mixinfos) {
                    Mixer mix = pr.getMixer(mixinfo);
                    Line.Info[] linfos = mix.getTargetLineInfo(info);
                    for (Line.Info linfo : linfos) {
                        result.add(linfo);
                    }
                }
            } catch (ClassCastException e) {}
        }
        Line.Info[] temp = new Line.Info[result.size()];
        return result.toArray(temp);
    }

    public static boolean isLineSupported(Line.Info info) {

        for (Iterator providers = ProviderService.getProviders(
                mixerProviderPath).iterator(); providers.hasNext();) {
            try {
                MixerProvider pr = (MixerProvider) providers.next();
                Mixer.Info[] mixinfos = pr.getMixerInfo();
                for (Mixer.Info mixinfo : mixinfos) {
                    Mixer mix = pr.getMixer(mixinfo);
                    if (mix.isLineSupported(info)) {
                        return true;
                    }
                }
            } catch (ClassCastException e) {}
        }
        return false;
    }

    private static Mixer getMixer(String propVal, Line.Info info,
            List<?> mixerProviders) {

        int index = propVal.indexOf("#"); //$NON-NLS-1$
        String className;
        String mixName;
        if (index == -1) {
            className = propVal.trim();
            mixName = ""; //$NON-NLS-1$
        } else {
            className = propVal.substring(0, index).trim();
            if (index == propVal.length()) {
                mixName = ""; //$NON-NLS-1$
            } else {
                mixName = propVal.substring(index + 1).trim();
            }
        }
        Mixer.Info[] minfos = null;
        if (!className.equals("")) { //$NON-NLS-1$
            for (Iterator providers = mixerProviders.iterator(); providers
                    .hasNext();) {
                try {
                    MixerProvider pr = (MixerProvider) (providers.next());
                    if (className.equals(pr.getClass().getName())) {
                        minfos = pr.getMixerInfo();
                        break;
                    }
                } catch (ClassCastException e) {}
            }
        }
        if (minfos == null) {
            minfos = getMixerInfo();
        }

        if (!mixName.equals("")) { //$NON-NLS-1$
            for (Mixer.Info minfo : minfos) {
                if (mixName.equals(minfo.getName())) {
                    return getMixer(minfo);
                }
            }
        }
        if (minfos.length > 0) {
            return getMixer(minfos[0]);
        }
        return null;
    }

    public static Line getLine(Line.Info info) throws LineUnavailableException {
        String propName = null;
        Class lineClass = info.getLineClass();

        if (Clip.class.isAssignableFrom(lineClass)) {
            propName = CLIP;
        } else if (Port.class.isAssignableFrom(lineClass)) {
            propName = PORT;
        } else if (SourceDataLine.class.isAssignableFrom(lineClass)) {
            propName = SOURCEDATALINE;
        } else if (TargetDataLine.class.isAssignableFrom(lineClass)) {
            propName = TARGETDATALINE;
        }
        return getLine(propName, info);
    }

    private static Line getLine(String propName, Line.Info info)
            throws LineUnavailableException {

        List<?> mixerProviders = ProviderService
                .getProviders(mixerProviderPath);

        if (propName != null) {
            String propVal = System.getProperty(propName);
            if (propVal != null) {
                Mixer m = getMixer(propVal, info, mixerProviders);
                if (m != null) {
                    Line l = m.getLine(info);
                    if (l != null) {
                        return l;
                    }
                }
            }

            Properties soundProperties = ProviderService.getSoundProperties();
            propVal = soundProperties.getProperty(propName);
            if (propVal != null) {
                Mixer m = getMixer(propVal, info, mixerProviders);
                if (m != null) {
                    Line l = m.getLine(info);
                    if (l != null) {
                        return l;
                    }
                }
            }
        }

        for (Iterator providers = ProviderService.getProviders(
                mixerProviderPath).iterator(); providers.hasNext();) {
            try {
                MixerProvider pr = (MixerProvider) (providers.next());
                Mixer.Info[] mixinfos = pr.getMixerInfo();
                for (Mixer.Info mixinfo : mixinfos) {
                    try {
                        Mixer mix = pr.getMixer(mixinfo);
                        return mix.getLine(info);
                    } catch (IllegalArgumentException e) {
                        // continue
                    }
                }
            } catch (ClassCastException e) {}
        }
        
        // BEGIN android-added
        if (CLIP.equals(propName)) {
            try {
                return (Clip)(Class.forName("com.android.internal.sound.sampled.AndroidClip").newInstance());
            } catch (Exception ex) {
                // Ignore
            }
        }
        // END android-added
        
        // sound.11=Could not get line
        throw new IllegalArgumentException(Messages.getString("sound.11")); //$NON-NLS-1$
    }

    public static Clip getClip() throws LineUnavailableException {
        return (Clip) getLine(new Line.Info(Clip.class));
    }

    public static Clip getClip(Mixer.Info mixerInfo)
            throws LineUnavailableException {
        return (Clip) (getMixer(mixerInfo).getLine(new Line.Info(Clip.class)));
    }

    public static SourceDataLine getSourceDataLine(AudioFormat format)
            throws LineUnavailableException {
        SourceDataLine line = (SourceDataLine) getLine(new Line.Info(
                SourceDataLine.class));
        line.open(format);
        return line;
    }

    public static SourceDataLine getSourceDataLine(AudioFormat format,
            Mixer.Info mixerinfo) throws LineUnavailableException {

        SourceDataLine line = (SourceDataLine) getMixer(mixerinfo).getLine(
                new Line.Info(SourceDataLine.class));
        line.open(format);
        return line;
    }

    public static TargetDataLine getTargetDataLine(AudioFormat format)
            throws LineUnavailableException {
        TargetDataLine line = (TargetDataLine) getLine(new Line.Info(
                TargetDataLine.class));
        line.open(format);
        return line;
    }

    public static TargetDataLine getTargetDataLine(AudioFormat format,
            Mixer.Info mixerinfo) throws LineUnavailableException {

        TargetDataLine line = (TargetDataLine) getMixer(mixerinfo).getLine(
                new Line.Info(TargetDataLine.class));
        line.open(format);
        return line;
    }

    public static AudioFormat.Encoding[] getTargetEncodings(
            AudioFormat.Encoding sourceEncoding) {

        List<AudioFormat.Encoding> result = new ArrayList<AudioFormat.Encoding>();
        for (Iterator providers = ProviderService.getProviders(
                formatConversionProviderPath).iterator(); providers.hasNext();) {
            try {
                FormatConversionProvider pr = (FormatConversionProvider) providers
                        .next();
                if (!pr.isSourceEncodingSupported(sourceEncoding)) {
                    continue;
                }
                AudioFormat.Encoding[] encodings = pr.getTargetEncodings();
                for (AudioFormat.Encoding encoding : encodings) {
                    result.add(encoding);
                }
            } catch (ClassCastException e) {}
        }
        AudioFormat.Encoding[] temp = new AudioFormat.Encoding[result.size()];
        return result.toArray(temp);
    }

    public static AudioFormat.Encoding[] getTargetEncodings(
            AudioFormat sourceFormat) {

        List<AudioFormat.Encoding> result = new ArrayList<AudioFormat.Encoding>();
        for (Iterator providers = ProviderService.getProviders(
                formatConversionProviderPath).iterator(); providers.hasNext();) {
            try {
                AudioFormat.Encoding[] encodings = ((FormatConversionProvider) (providers
                        .next())).getTargetEncodings(sourceFormat);
                for (AudioFormat.Encoding encoding : encodings) {
                    result.add(encoding);
                }
            } catch (ClassCastException e) {}
        }
        AudioFormat.Encoding[] temp = new AudioFormat.Encoding[result.size()];
        return result.toArray(temp);
    }

    public static boolean isConversionSupported(
            AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat) {

        for (Iterator providers = ProviderService.getProviders(
                formatConversionProviderPath).iterator(); providers.hasNext();) {
            if (((FormatConversionProvider) (providers.next()))
                    .isConversionSupported(targetEncoding, sourceFormat)) {
                return true;
            }
        }
        return false;
    }

    public static AudioInputStream getAudioInputStream(
            AudioFormat.Encoding targetEncoding, AudioInputStream sourceStream) {

        if (sourceStream.getFormat().getEncoding().equals(targetEncoding)) {
            return sourceStream;
        }
        for (Iterator providers = ProviderService.getProviders(
                formatConversionProviderPath).iterator(); providers.hasNext();) {
            try {
                return ((FormatConversionProvider) (providers.next()))
                        .getAudioInputStream(targetEncoding, sourceStream);
            } catch (ClassCastException e) {} catch (IllegalArgumentException e) {}
        }
        // sound.12=Could not get audio input stream from source stream
        throw new IllegalArgumentException(Messages.getString("sound.12")); //$NON-NLS-1$
    }

    public static AudioFormat[] getTargetFormats(
            AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat) {

        List<AudioFormat> result = new ArrayList<AudioFormat>();
        for (Iterator providers = ProviderService.getProviders(
                formatConversionProviderPath).iterator(); providers.hasNext();) {
            try {
                AudioFormat[] formats = ((FormatConversionProvider) (providers
                        .next()))
                        .getTargetFormats(targetEncoding, sourceFormat);
                for (AudioFormat format : formats) {
                    result.add(format);
                }
            } catch (ClassCastException e) {}
        }
        AudioFormat[] temp = new AudioFormat[result.size()];
        return result.toArray(temp);
    }

    public static boolean isConversionSupported(AudioFormat targetFormat,
            AudioFormat sourceFormat) {

        for (Iterator providers = ProviderService.getProviders(
                formatConversionProviderPath).iterator(); providers.hasNext();) {
            if (((FormatConversionProvider) (providers.next()))
                    .isConversionSupported(targetFormat, sourceFormat)) {
                return true;
            }
        }
        return false;
    }

    public static AudioInputStream getAudioInputStream(
            AudioFormat targetFormat, AudioInputStream sourceStream) {

        if (sourceStream.getFormat().matches(targetFormat)) {
            return sourceStream;
        }
        for (Iterator providers = ProviderService.getProviders(
                formatConversionProviderPath).iterator(); providers.hasNext();) {
            try {
                return ((FormatConversionProvider) (providers.next()))
                        .getAudioInputStream(targetFormat, sourceStream);
            } catch (ClassCastException e) {} catch (IllegalArgumentException e) {}
        }
        // sound.13=Could not get audio input stream from source stream
        throw new IllegalArgumentException(Messages.getString("sound.13")); //$NON-NLS-1$
    }

    public static AudioFileFormat getAudioFileFormat(InputStream stream)
            throws UnsupportedAudioFileException, IOException {

        for (Iterator providers = ProviderService.getProviders(
                audioFileReaderPath).iterator(); providers.hasNext();) {
            try {
                return ((AudioFileReader) (providers.next()))
                        .getAudioFileFormat(stream);
            } catch (ClassCastException e) {} catch (UnsupportedAudioFileException e) {}
        }
        // sound.14=File is not a supported file type
        throw new UnsupportedAudioFileException(Messages.getString("sound.14")); //$NON-NLS-1$
    }

    public static AudioFileFormat getAudioFileFormat(URL url)
            throws UnsupportedAudioFileException, IOException {

        for (Iterator providers = ProviderService.getProviders(
                audioFileReaderPath).iterator(); providers.hasNext();) {
            try {
                return ((AudioFileReader) (providers.next()))
                        .getAudioFileFormat(url);
            } catch (ClassCastException e) {} catch (UnsupportedAudioFileException e) {}
        }
        // sound.14=File is not a supported file type
        throw new UnsupportedAudioFileException(Messages.getString("sound.14")); //$NON-NLS-1$
    }

    public static AudioFileFormat getAudioFileFormat(File file)
            throws UnsupportedAudioFileException, IOException {

        for (Iterator providers = ProviderService.getProviders(
                audioFileReaderPath).iterator(); providers.hasNext();) {
            try {
                return ((AudioFileReader) (providers.next()))
                        .getAudioFileFormat(file);
            } catch (ClassCastException e) {} catch (UnsupportedAudioFileException e) {}
        }
        // sound.14=File is not a supported file type
        throw new UnsupportedAudioFileException(Messages.getString("sound.14")); //$NON-NLS-1$
    }

    public static AudioInputStream getAudioInputStream(InputStream stream)
            throws UnsupportedAudioFileException, IOException {

        if (stream instanceof AudioInputStream) {
            return (AudioInputStream) stream;
        }
        for (Iterator providers = ProviderService.getProviders(
                audioFileReaderPath).iterator(); providers.hasNext();) {
            try {
                return ((AudioFileReader) (providers.next()))
                        .getAudioInputStream(stream);
            } catch (ClassCastException e) {} catch (UnsupportedAudioFileException e) {}
        }
        // BEGIN android-added
        try {
            AudioFileReader reader = (AudioFileReader)(Class.forName("com.android.internal.sound.sampled.AndroidAudioFileReader").newInstance());
            return reader.getAudioInputStream(stream);
        } catch (Exception ex) {
            // Ignore
        }
        // END android-added
        // sound.15=Could not get audio input stream from input stream
        throw new UnsupportedAudioFileException(Messages.getString("sound.15")); //$NON-NLS-1$
    }

    public static AudioInputStream getAudioInputStream(URL url)
            throws UnsupportedAudioFileException, IOException {

        for (Iterator providers = ProviderService.getProviders(
                audioFileReaderPath).iterator(); providers.hasNext();) {
            try {
                return ((AudioFileReader) (providers.next()))
                        .getAudioInputStream(url);
            } catch (ClassCastException e) {} catch (UnsupportedAudioFileException e) {}
        }
        // BEGIN android-added
        try {
            AudioFileReader reader = (AudioFileReader)(Class.forName("com.android.internal.sound.sampled.AndroidAudioFileReader").newInstance());
            return reader.getAudioInputStream(url);
        } catch (Exception ex) {
            // Ignore
        }
        // END android-added
        // sound.16=Could not get audio input stream from input URL
        throw new UnsupportedAudioFileException(Messages.getString("sound.16")); //$NON-NLS-1$
    }

    public static AudioInputStream getAudioInputStream(File file)
            throws UnsupportedAudioFileException, IOException {

        for (Iterator providers = ProviderService.getProviders(
                audioFileReaderPath).iterator(); providers.hasNext();) {
            try {
                return ((AudioFileReader) (providers.next()))
                        .getAudioInputStream(file);
            } catch (ClassCastException e) {} catch (UnsupportedAudioFileException e) {}
        }
        // BEGIN android-added
        try {
            AudioFileReader reader = (AudioFileReader)(Class.forName("com.android.internal.sound.sampled.AndroidAudioFileReader").newInstance());
            return reader.getAudioInputStream(file);
        } catch (Exception ex) {
            // Ignore
        }
        // END android-added
        // sound.17=Could not get audio input stream from input file
        throw new UnsupportedAudioFileException(Messages.getString("sound.17")); //$NON-NLS-1$
    }

    public static AudioFileFormat.Type[] getAudioFileTypes() {
        List<AudioFileFormat.Type> result = new ArrayList<AudioFileFormat.Type>();
        for (Iterator providers = ProviderService.getProviders(
                audioFileWriterPath).iterator(); providers.hasNext();) {
            try {
                AudioFileFormat.Type[] types = ((AudioFileWriter) (providers
                        .next())).getAudioFileTypes();
                for (AudioFileFormat.Type type : types) {
                    result.add(type);
                }
            } catch (ClassCastException e) {}
        }
        AudioFileFormat.Type[] temp = new AudioFileFormat.Type[result.size()];
        return result.toArray(temp);
    }

    public static boolean isFileTypeSupported(AudioFileFormat.Type fileType) {

        for (Iterator providers = ProviderService.getProviders(
                audioFileWriterPath).iterator(); providers.hasNext();) {
            if (((AudioFileWriter) (providers.next()))
                    .isFileTypeSupported(fileType)) {
                return true;
            }
        }
        return false;
    }

    public static AudioFileFormat.Type[] getAudioFileTypes(
            AudioInputStream stream) {
        List<AudioFileFormat.Type> result = new ArrayList<AudioFileFormat.Type>();
        for (Iterator providers = ProviderService.getProviders(
                audioFileWriterPath).iterator(); providers.hasNext();) {
            try {
                AudioFileFormat.Type[] types = ((AudioFileWriter) (providers
                        .next())).getAudioFileTypes(stream);
                for (AudioFileFormat.Type type : types) {
                    result.add(type);
                }
            } catch (ClassCastException e) {}
        }
        AudioFileFormat.Type[] temp = new AudioFileFormat.Type[result.size()];
        return result.toArray(temp);
    }

    public static boolean isFileTypeSupported(AudioFileFormat.Type fileType,
            AudioInputStream stream) {

        for (Iterator providers = ProviderService.getProviders(
                audioFileWriterPath).iterator(); providers.hasNext();) {
            if (((AudioFileWriter) (providers.next())).isFileTypeSupported(
                    fileType, stream)) {
                return true;
            }
        }
        return false;
    }

    public static int write(AudioInputStream stream,
            AudioFileFormat.Type fileType, OutputStream out) throws IOException {
        AudioFileWriter writer;
        for (Iterator providers = ProviderService.getProviders(
                audioFileWriterPath).iterator(); providers.hasNext();) {
            writer = (AudioFileWriter) (providers.next());
            if (writer.isFileTypeSupported(fileType, stream)) {
                return writer.write(stream, fileType, out);
            }
        }
        // sound.18=Type is not supported
        throw new IllegalArgumentException(Messages.getString("sound.18")); //$NON-NLS-1$
    }

    public static int write(AudioInputStream stream,
            AudioFileFormat.Type fileType, File out) throws IOException {
        AudioFileWriter writer;
        for (Iterator providers = ProviderService.getProviders(
                audioFileWriterPath).iterator(); providers.hasNext();) {
            writer = (AudioFileWriter) (providers.next());
            if (writer.isFileTypeSupported(fileType, stream)) {
                return writer.write(stream, fileType, out);
            }
        }
        // sound.18=Type is not supported
        throw new IllegalArgumentException(Messages.getString("sound.18")); //$NON-NLS-1$
    }
}
