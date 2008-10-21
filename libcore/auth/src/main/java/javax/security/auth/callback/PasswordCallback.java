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

package javax.security.auth.callback;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.harmony.auth.internal.nls.Messages;

public class PasswordCallback implements Callback, Serializable {

    private static final long serialVersionUID = 2267422647454909926L;

    private String prompt;

    boolean echoOn;

    private char[] inputPassword;

    private void setPrompt(String prompt) throws IllegalArgumentException {
        if (prompt == null || prompt.length() == 0) {
            throw new IllegalArgumentException(Messages.getString("auth.14")); //$NON-NLS-1$
        }
        this.prompt = prompt;
    }

    public PasswordCallback(String prompt, boolean echoOn) {
        super();
        setPrompt(prompt);
        this.echoOn = echoOn;
    }

    public String getPrompt() {
        return prompt;
    }

    public boolean isEchoOn() {
        return echoOn;
    }

    public void setPassword(char[] password) {
        if (password == null) {
            this.inputPassword = password;
        } else {
            inputPassword = new char[password.length];
            System.arraycopy(password, 0, inputPassword, 0, inputPassword.length);
        }
    }

    public char[] getPassword() {
        if (inputPassword != null) {
            char[] tmp = new char[inputPassword.length];
            System.arraycopy(inputPassword, 0, tmp, 0, tmp.length);
            return tmp;
        }
        return null;
    }

    public void clearPassword() {
        if (inputPassword != null) {
            Arrays.fill(inputPassword, '\u0000');
        }
    }
}
