/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package java.util;

import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

public class InvalidPropertiesFormatException extends IOException {
    
    private static final long serialVersionUID = 7763056076009360219L;
    
    public InvalidPropertiesFormatException(String m) {
        super(m);
    }

    public InvalidPropertiesFormatException(Throwable c) {
        initCause(c);
    }
    
    private void writeObject(ObjectOutputStream out) 
            throws NotSerializableException{
        throw new NotSerializableException();        
    }
    
    private void readObject(ObjectInputStream in) 
            throws NotSerializableException{
        throw new NotSerializableException();        
    }
}
