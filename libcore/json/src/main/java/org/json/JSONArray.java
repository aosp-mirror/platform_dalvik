/*
 * Copyright (C) 2010 The Android Open Source Project
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

package org.json;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

// Note: this class was written without inspecting the non-free org.json sourcecode.

/**
 * An indexed sequence of JSON-safe values.
 */
public class JSONArray {

    private final List<Object> values;

    public JSONArray() {
        values = new ArrayList<Object>();
    }

    /* Accept a raw type for API compatibility */
    public JSONArray(Collection copyFrom) {
        this();
        Collection<?> copyFromTyped = (Collection<?>) copyFrom;
        values.addAll(copyFromTyped);
    }

    public JSONArray(JSONTokener readFrom) throws JSONException {
        /*
         * Getting the parser to populate this could get tricky. Instead, just
         * parse to temporary JSONArray and then steal the data from that.
         */
        Object object = readFrom.nextValue();
        if (object instanceof JSONArray) {
            values = ((JSONArray) object).values;
        } else {
            throw JSON.typeMismatch(object, "JSONArray");
        }
    }

    public JSONArray(String json) throws JSONException {
        this(new JSONTokener(json));
    }

    public int length() {
        return values.size();
    }

    public JSONArray put(boolean value) {
        values.add(value);
        return this;
    }

    public JSONArray put(double value) throws JSONException {
        values.add(JSON.checkDouble(value));
        return this;
    }

    public JSONArray put(int value) {
        values.add(value);
        return this;
    }

    public JSONArray put(long value) {
        values.add(value);
        return this;
    }

    public JSONArray put(Object value) {
        values.add(value);
        return this;
    }

    public JSONArray put(int index, boolean value) throws JSONException {
        return put(index, (Boolean) value);
    }

    public JSONArray put(int index, double value) throws JSONException {
        return put(index, (Double) value);
    }

    public JSONArray put(int index, int value) throws JSONException {
        return put(index, (Integer) value);
    }

    public JSONArray put(int index, long value) throws JSONException {
        return put(index, (Long) value);
    }

    public JSONArray put(int index, Object value) throws JSONException {
        if (value instanceof Number) {
            // deviate from the original by checking all Numbers, not just floats & doubles
            JSON.checkDouble(((Number) value).doubleValue());
        }
        while (values.size() <= index) {
            values.add(null);
        }
        values.set(index, value);
        return this;
    }

    public boolean isNull(int index) {
        Object value = opt(index);
        return value == null || value == JSONObject.NULL;
    }

    public Object get(int index) throws JSONException {
        try {
            Object value = values.get(index);
            if (value == null) {
                throw new JSONException("Value at " + index + " is null.");
            }
            return value;
        } catch (IndexOutOfBoundsException e) {
            throw new JSONException("Index " + index + " out of range [0.." + values.size() + ")");
        }
    }

    public Object opt(int index) {
        if (index < 0 || index >= values.size()) {
            return null;
        }
        return values.get(index);
    }

    public boolean getBoolean(int index) throws JSONException {
        Object object = get(index);
        Boolean result = JSON.toBoolean(object);
        if (result == null) {
            throw JSON.typeMismatch(index, object, "boolean");
        }
        return result;
    }

    public boolean optBoolean(int index) {
        return optBoolean(index, false);
    }

    public boolean optBoolean(int index, boolean fallback) {
        Object object = opt(index);
        Boolean result = JSON.toBoolean(object);
        return result != null ? result : fallback;
    }

    public double getDouble(int index) throws JSONException {
        Object object = get(index);
        Double result = JSON.toDouble(object);
        if (result == null) {
            throw JSON.typeMismatch(index, object, "double");
        }
        return result;
    }

    public double optDouble(int index) {
        return optDouble(index, Double.NaN);
    }

    public double optDouble(int index, double fallback) {
        Object object = opt(index);
        Double result = JSON.toDouble(object);
        return result != null ? result : fallback;
    }

    public int getInt(int index) throws JSONException {
        Object object = get(index);
        Integer result = JSON.toInteger(object);
        if (result == null) {
            throw JSON.typeMismatch(index, object, "int");
        }
        return result;
    }

    public int optInt(int index) {
        return optInt(index, 0);
    }

    public int optInt(int index, int fallback) {
        Object object = opt(index);
        Integer result = JSON.toInteger(object);
        return result != null ? result : fallback;
    }

    public long getLong(int index) throws JSONException {
        Object object = get(index);
        Long result = JSON.toLong(object);
        if (result == null) {
            throw JSON.typeMismatch(index, object, "long");
        }
        return result;
    }

    public long optLong(int index) {
        return optLong(index, 0L);
    }

    public long optLong(int index, long fallback) {
        Object object = opt(index);
        Long result = JSON.toLong(object);
        return result != null ? result : fallback;
    }

    public String getString(int index) throws JSONException {
        Object object = get(index);
        String result = JSON.toString(object);
        if (result == null) {
            throw JSON.typeMismatch(index, object, "String");
        }
        return result;
    }

    public String optString(int index) {
        return optString(index, "");
    }

    public String optString(int index, String fallback) {
        Object object = opt(index);
        String result = JSON.toString(object);
        return result != null ? result : fallback;
    }

    public JSONArray getJSONArray(int index) throws JSONException {
        Object object = get(index);
        if (object instanceof JSONArray) {
            return (JSONArray) object;
        } else {
            throw JSON.typeMismatch(index, object, "JSONArray");
        }
    }

    public JSONArray optJSONArray(int index) {
        Object object = opt(index);
        return object instanceof JSONArray ? (JSONArray) object : null;
    }

    public JSONObject getJSONObject(int index) throws JSONException {
        Object object = get(index);
        if (object instanceof JSONObject) {
            return (JSONObject) object;
        } else {
            throw JSON.typeMismatch(index, object, "JSONObject");
        }
    }

    public JSONObject optJSONObject(int index) {
        Object object = opt(index);
        return object instanceof JSONObject ? (JSONObject) object : null;
    }

    public JSONObject toJSONObject(JSONArray names) throws JSONException {
        JSONObject result = new JSONObject();
        int length = Math.min(names.length(), values.size());
        if (length == 0) {
            return null;
        }
        for (int i = 0; i < length; i++) {
            String name = JSON.toString(names.opt(i));
            result.put(name, opt(i));
        }
        return result;
    }

    public String join(String separator) throws JSONException {
        JSONStringer stringer = new JSONStringer();
        stringer.open(JSONStringer.Scope.NULL, "");
        for (int i = 0, size = values.size(); i < size; i++) {
            if (i > 0) {
                stringer.out.append(separator);
            }
            stringer.value(values.get(i));
        }
        stringer.close(JSONStringer.Scope.NULL, JSONStringer.Scope.NULL, "");
        return stringer.out.toString();
    }

    @Override public String toString() {
        try {
            JSONStringer stringer = new JSONStringer();
            writeTo(stringer);
            return stringer.toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public String toString(int indentSpaces) throws JSONException {
        JSONStringer stringer = new JSONStringer(indentSpaces);
        writeTo(stringer);
        return stringer.toString();
    }

    void writeTo(JSONStringer stringer) throws JSONException {
        stringer.array();
        for (Object value : values) {
            stringer.value(value);
        }
        stringer.endArray();
    }

    @Override public boolean equals(Object o) {
        return o instanceof JSONArray && ((JSONArray) o).values.equals(values);
    }

    @Override public int hashCode() {
        // diverge from the original, which doesn't implement hashCode
        return values.hashCode();
    }
}
