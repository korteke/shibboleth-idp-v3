/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.idp.installer.ant;

import org.apache.tools.ant.input.InputRequest;
import org.apache.tools.ant.input.SecureInputHandler;

/** Ant helper class to ask for passwords, rejecting zero length passwords and asking for confirmation. */
public class PasswordHandler extends SecureInputHandler {

    /** {@inheritDoc} */
    @Override
    public void handleInput(InputRequest arg0) {
        while (true) {
            System.console().printf("%s", arg0.getPrompt());
            System.console().flush();
            char[] result  = System.console().readPassword();
            if (null == result || result.length == 0) {
                System.console().printf("Password cannot be zero length\n");
                continue;
            }
            final String firstPass = String.copyValueOf(result);
            System.console().printf("Re-enter password: ");
            System.console().flush();
            result  = System.console().readPassword();
            if (null == result || result.length == 0) {
                System.console().printf("Password cannot be zero length\n");
                continue;
            }
            final String secondPass = String.copyValueOf(result);
            if (firstPass.equals(secondPass)) {
                arg0.setInput(firstPass);
                return;
            }
            System.console().printf("Passwords did not match\n");
        }
    }
    
}