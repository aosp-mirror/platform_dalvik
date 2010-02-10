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

package dalvik.runner;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Parses command line options.
 *
 * Strings in the passed-in String[] are parsed left-to-right. Each
 * String is classified as a short option (such as "-v"), a long
 * option (such as "--verbose"), an argument to an option (such as
 * "out.txt" in "-f out.txt"), or a non-option positional argument.
 *
 * A simple short option is a "-" followed by a short option
 * character. If the option requires an argument (which is true of any
 * non-boolean option), it may be written as a separate parameter, but
 * need not be. That is, "-f out.txt" and "-fout.txt" are both
 * acceptable.
 *
 * It is possible to specify multiple short options after a single "-"
 * as long as all (except possibly the last) do not require arguments.
 *
 * A long option begins with "--" followed by several characters. If
 * the option requires an argument, it may be written directly after
 * the option name, separated by "=", or as the next argument. (That
 * is, "--file=out.txt" or "--file out.txt".)
 *
 * A boolean long option '--name' automatically gets a '--no-name'
 * companion. Given an option "--flag", then, "--flag", "--no-flag",
 * "--flag=true" and "--flag=false" are all valid, though neither
 * "--flag true" nor "--flag false" are allowed (since "--flag" by
 * itself is sufficient, the following "true" or "false" is
 * interpreted separately). You can use "yes" and "no" as synonyms for
 * "true" and "false".
 *
 * Each String not starting with a "-" and not a required argument of
 * a previous option is a non-option positional argument, as are all
 * successive Strings. Each String after a "--" is a non-option
 * positional argument.
 *
 * Parsing of numeric fields such byte, short, int, long, float, and
 * double fields is supported. This includes both unboxed and boxed
 * versions (e.g. int vs Integer). If there is a problem parsing the
 * argument to match the desired type, a runtime exception is thrown.
 *
 * File option fields are supported by simply wrapping the string
 * argument in a File object without testing for the existance of the
 * file.
 *
 * Parameterized Collection fields such as List<File> and Set<String>
 * are supported as long as the parameter type is otherwise supported
 * by the option parser. The collection field should be initialized
 * with an appropriate collection instance.
 *
 * The fields corresponding to options are updated as their options
 * are processed. Any remaining positional arguments are returned as a
 * List<String>.
 *
 * Here's a simple example:
 *
 * // This doesn't need to be a separate class, if your application doesn't warrant it.
 * // Non-@Option fields will be ignored.
 * class Options {
 *     @Option(names = { "-q", "--quiet" })
 *     boolean quiet = false;
 *
 *     // Boolean options require a long name if it's to be possible to explicitly turn them off.
 *     // Here the user can use --no-color.
 *     @Option(names = { "--color" })
 *     boolean color = true;
 *
 *     @Option(names = { "-m", "--mode" })
 *     String mode = "standard; // Supply a default just by setting the field.
 *
 *     @Option(names = { "-p", "--port" })
 *     int portNumber = 8888;
 *
 *     // There's no need to offer a short name for rarely-used options.
 *     @Option(names = { "--timeout" })
 *     double timeout = 1.0;
 *
 *     @Option(names = { "-o", "--output-file" })
 *     File output;
 *
 *     // Multiple options are added to the collection.
 *     // The collection field itself must be non-null.
 *     @Option(names = { "-i", "--input-file" })
 *     List<File> inputs = new ArrayList<File>();
 *
 * }
 *
 * class Main {
 *     public static void main(String[] args) {
 *         Options options = new Options();
 *         List<String> inputFilenames = new OptionParser(options).parse(args);
 *         for (String inputFilename : inputFilenames) {
 *             if (!options.quiet) {
 *                 ...
 *             }
 *             ...
 *         }
 *     }
 * }
 *
 * See also:
 *
 *  the getopt(1) man page
 *  Python's "optparse" module (http://docs.python.org/library/optparse.html)
 *  the POSIX "Utility Syntax Guidelines" (http://www.opengroup.org/onlinepubs/000095399/basedefs/xbd_chap12.html#tag_12_02)
 *  the GNU "Standards for Command Line Interfaces" (http://www.gnu.org/prep/standards/standards.html#Command_002dLine-Interfaces)
 */
public class OptionParser {
    private static final HashMap<Class<?>, Handler> handlers = new HashMap<Class<?>, Handler>();
    static {
        handlers.put(boolean.class, new BooleanHandler());
        handlers.put(Boolean.class, new BooleanHandler());

        handlers.put(byte.class, new ByteHandler());
        handlers.put(Byte.class, new ByteHandler());
        handlers.put(short.class, new ShortHandler());
        handlers.put(Short.class, new ShortHandler());
        handlers.put(int.class, new IntegerHandler());
        handlers.put(Integer.class, new IntegerHandler());
        handlers.put(long.class, new LongHandler());
        handlers.put(Long.class, new LongHandler());

        handlers.put(float.class, new FloatHandler());
        handlers.put(Float.class, new FloatHandler());
        handlers.put(double.class, new DoubleHandler());
        handlers.put(Double.class, new DoubleHandler());

        handlers.put(String.class, new StringHandler());
        handlers.put(File.class, new FileHandler());
    }
    Handler getHandler(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Class rawClass = (Class<?>) parameterizedType.getRawType();
            if (!Collection.class.isAssignableFrom(rawClass)) {
                throw new RuntimeException("cannot handle non-collection parameterized type " + type);
            }
            Type actualType = parameterizedType.getActualTypeArguments()[0];
            if (!(actualType instanceof Class)) {
                throw new RuntimeException("cannot handle nested parameterized type " + type);
            }
            return getHandler(actualType);
        }
        if (type instanceof Class) {
            if (Collection.class.isAssignableFrom((Class) type)) {
                // could handle by just having a default of treating
                // contents as String but consciously decided this
                // should be an error
                throw new RuntimeException(
                        "cannot handle non-parameterized collection " + type + ". " +
                        "use a generic Collection to specify a desired element type");
            }
            return handlers.get((Class<?>) type);
        }
        throw new RuntimeException("cannot handle unknown field type " + type);
    }

    private final Object optionSource;
    private final HashMap<String, Field> optionMap;

    /**
     * Constructs a new OptionParser for setting the @Option fields of 'optionSource'.
     */
    public OptionParser(Object optionSource) {
        this.optionSource = optionSource;
        this.optionMap = makeOptionMap();
    }

    /**
     * Parses the command-line arguments 'args', setting the @Option fields of the 'optionSource' provided to the constructor.
     * Returns a list of the positional arguments left over after processing all options.
     */
    public List<String> parse(String[] args) {
        return parseOptions(Arrays.asList(args).iterator());
    }

    private List<String> parseOptions(Iterator<String> args) {
        final List<String> leftovers = new ArrayList<String>();

        // Scan 'args'.
        while (args.hasNext()) {
            final String arg = args.next();
            if (arg.equals("--")) {
                // "--" marks the end of options and the beginning of positional arguments.
                break;
            } else if (arg.startsWith("--")) {
                // A long option.
                parseLongOption(arg, args);
            } else if (arg.startsWith("-")) {
                // A short option.
                parseGroupedShortOptions(arg, args);
            } else {
                // The first non-option marks the end of options.
                leftovers.add(arg);
                break;
            }
        }

        // Package up the leftovers.
        while (args.hasNext()) {
            leftovers.add(args.next());
        }
        return leftovers;
    }

    private Field fieldForArg(String name) {
        final Field field = optionMap.get(name);
        if (field == null) {
            throw new RuntimeException("unrecognized option '" + name + "'");
        }
        return field;
    }

    private void parseLongOption(String arg, Iterator<String> args) {
        String name = arg.replaceFirst("^--no-", "--");
        String value = null;

        // Support "--name=value" as well as "--name value".
        final int equalsIndex = name.indexOf('=');
        if (equalsIndex != -1) {
            value = name.substring(equalsIndex + 1);
            name = name.substring(0, equalsIndex);
        }

        final Field field = fieldForArg(name);
        final Handler handler = getHandler(field.getGenericType());
        if (value == null) {
            if (handler.isBoolean()) {
                value = arg.startsWith("--no-") ? "false" : "true";
            } else {
                value = grabNextValue(args, name, field);
            }
        }
        setValue(optionSource, field, arg, handler, value);
    }

    // Given boolean options a and b, and non-boolean option f, we want to allow:
    // -ab
    // -abf out.txt
    // -abfout.txt
    // (But not -abf=out.txt --- POSIX doesn't mention that either way, but GNU expressly forbids it.)
    private void parseGroupedShortOptions(String arg, Iterator<String> args) {
        for (int i = 1; i < arg.length(); ++i) {
            final String name = "-" + arg.charAt(i);
            final Field field = fieldForArg(name);
            final Handler handler = getHandler(field.getGenericType());
            String value;
            if (handler.isBoolean()) {
                value = "true";
            } else {
                // We need a value. If there's anything left, we take the rest of this "short option".
                if (i + 1 < arg.length()) {
                    value = arg.substring(i + 1);
                    i = arg.length() - 1;
                } else {
                    value = grabNextValue(args, name, field);
                }
            }
            setValue(optionSource, field, arg, handler, value);
        }
    }

    @SuppressWarnings("unchecked")
    private static void setValue(Object object, Field field, String arg, Handler handler, String valueText) {

        Object value = handler.translate(valueText);
        if (value == null) {
            final String type = field.getType().getSimpleName().toLowerCase();
            throw new RuntimeException("couldn't convert '" + valueText + "' to a " + type + " for option '" + arg + "'");
        }
        try {
            field.setAccessible(true);
            if (Collection.class.isAssignableFrom(field.getType())) {
                Collection collection = (Collection) field.get(object);
                collection.add(value);
            } else {
                field.set(object, value);
            }
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("internal error", ex);
        }
    }

    // Returns the next element of 'args' if there is one. Uses 'name' and 'field' to construct a helpful error message.
    private String grabNextValue(Iterator<String> args, String name, Field field) {
        if (!args.hasNext()) {
            final String type = field.getType().getSimpleName().toLowerCase();
            throw new RuntimeException("option '" + name + "' requires a " + type + " argument");
        }
        return args.next();
    }

    // Cache the available options and report any problems with the options themselves right away.
    private HashMap<String, Field> makeOptionMap() {
        final HashMap<String, Field> optionMap = new HashMap<String, Field>();
        final Class<?> optionClass = optionSource.getClass();
        for (Field field : optionClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Option.class)) {
                final Option option = field.getAnnotation(Option.class);
                final String[] names = option.names();
                if (names.length == 0) {
                    throw new RuntimeException("found an @Option with no name!");
                }
                for (String name : names) {
                    if (optionMap.put(name, field) != null) {
                        throw new RuntimeException("found multiple @Options sharing the name '" + name + "'");
                    }
                }
                if (getHandler(field.getGenericType()) == null) {
                    throw new RuntimeException("unsupported @Option field type '" + field.getType() + "'");
                }
            }
        }
        return optionMap;
    }

    static abstract class Handler {
        // Only BooleanHandler should ever override this.
        boolean isBoolean() {
            return false;
        }

        /**
         * Returns an object of appropriate type for the given Handle, corresponding to 'valueText'.
         * Returns null on failure.
         */
        abstract Object translate(String valueText);
    }

    static class BooleanHandler extends Handler {
        @Override boolean isBoolean() {
            return true;
        }

        Object translate(String valueText) {
            if (valueText.equalsIgnoreCase("true") || valueText.equalsIgnoreCase("yes")) {
                return Boolean.TRUE;
            } else if (valueText.equalsIgnoreCase("false") || valueText.equalsIgnoreCase("no")) {
                return Boolean.FALSE;
            }
            return null;
        }
    }

    static class ByteHandler extends Handler {
        Object translate(String valueText) {
            try {
                return Byte.parseByte(valueText);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    static class ShortHandler extends Handler {
        Object translate(String valueText) {
            try {
                return Short.parseShort(valueText);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    static class IntegerHandler extends Handler {
        Object translate(String valueText) {
            try {
                return Integer.parseInt(valueText);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    static class LongHandler extends Handler {
        Object translate(String valueText) {
            try {
                return Long.parseLong(valueText);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    static class FloatHandler extends Handler {
        Object translate(String valueText) {
            try {
                return Float.parseFloat(valueText);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    static class DoubleHandler extends Handler {
        Object translate(String valueText) {
            try {
                return Double.parseDouble(valueText);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    static class StringHandler extends Handler {
        Object translate(String valueText) {
            return valueText;
        }
    }

    static class FileHandler extends Handler {
        Object translate(String valueText) {
            return new File(valueText);
        }
    }
}
