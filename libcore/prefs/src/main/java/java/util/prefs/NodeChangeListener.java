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


package java.util.prefs;

import java.util.EventListener;
import java.util.prefs.NodeChangeEvent;

/**
 * This interface is used to handle preference node change events.
 * The implementation of this interface can be installed by the {@code Preferences} instance.
 * 
 * @see NodeChangeEvent
 * 
 * @since Android 1.0
 */
public interface NodeChangeListener extends EventListener {

    /**
     * This method gets called whenever a child node is added to another node.
     * 
     * @param e
     *            the node change event.
     * @since Android 1.0
     */
    public void childAdded (NodeChangeEvent e);
    
    /**
     * This method gets called whenever a child node is removed from another
     * node.
     * 
     * @param e
     *            the node change event.
     * @since Android 1.0
     */
    public void childRemoved (NodeChangeEvent e);
}


 
