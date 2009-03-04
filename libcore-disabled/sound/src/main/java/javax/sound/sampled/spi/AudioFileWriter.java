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

package javax.sound.sampled.spi;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFileFormat.Type;

public abstract class AudioFileWriter {

    public abstract AudioFileFormat.Type[] getAudioFileTypes();

    public abstract AudioFileFormat.Type[] getAudioFileTypes(AudioInputStream stream);

    public boolean isFileTypeSupported(AudioFileFormat.Type fileType) {
        AudioFileFormat.Type[] supported = getAudioFileTypes();
        for (Type element : supported) {
            if (fileType.equals(element)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFileTypeSupported(AudioFileFormat.Type fileType, AudioInputStream stream) {
        AudioFileFormat.Type[] supported = getAudioFileTypes(stream);
        for (Type element : supported) {
            if (fileType.equals(element)) {
                return true;
            }
        }
        return false;
    }

    public abstract int write(AudioInputStream stream,
            AudioFileFormat.Type fileType, File out) throws IOException;

    public abstract int write(AudioInputStream stream,
            AudioFileFormat.Type fileType, OutputStream out) throws IOException;
}
