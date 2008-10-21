/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package java.sql;

/**
 * A class holding information about Driver Properties for making a Connection.
 * This class is returned from the <code>Driver.getDriverProperties</code>
 * method and is useful in using Connections in an advanced way.
 */
public class DriverPropertyInfo {

    /**
     * If the value member can be chosen from a set of possible values, they are
     * contained here. Otherwise choices is null.
     */
    public String[] choices;

    /**
     * A description of the property. May be null.
     */
    public String description;

    /**
     * The name of the property.
     */
    public String name;

    /**
     * True when the value member must be provided during Driver.connect. False
     * otherwise.
     */
    public boolean required;

    /**
     * The current value associated with this property. This is based on the
     * data gathered by the getPropertyInfo method, the general Java environment
     * and the default values for the driver.
     */
    public String value;

    /**
     * Creates a DriverPropertyInfo instance with the supplied name and value.
     * Other members take their default values.
     * 
     * @param name
     *            The property name
     * @param value
     *            The property value
     */
    public DriverPropertyInfo(String name, String value) {
        this.name = name;
        this.value = value;
        this.choices = null;
        this.description = null;
        this.required = false;
    }
}
