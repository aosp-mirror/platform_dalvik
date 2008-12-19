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

package java.text;

import java.awt.font.NumericShaper;
import java.awt.font.TextAttribute;
import java.util.Arrays;
import java.util.LinkedList;

import org.apache.harmony.text.BidiRun;
import org.apache.harmony.text.BidiWrapper;
import org.apache.harmony.text.internal.nls.Messages;

/**
 * Provides the Unicode Bidirectional Algorithm. The algorithm is
 * defined in the Unicode Standard Annex #9, version 13, also described in The
 * Unicode Standard, Version 4.0 .
 * 
 * Use a {@code Bidi} object to get the information on the position reordering of a
 * bidirectional text, such as Arabic or Hebrew. The natural display ordering of
 * horizontal text in these languages is from right to left, while they order
 * numbers from left to right.
 * 
 * If the text contains multiple runs, the information of each run can be
 * obtained from the run index. The level of any particular run indicates the
 * direction of the text as well as the nesting level. Left-to-right runs have
 * even levels while right-to-left runs have odd levels.
 * 
 * @since Android 1.0
 */
public final class Bidi {
    /**
     * Constant that indicates the default base level. If there is no strong
     * character, then set the paragraph level to 0 (left-to-right).
     * 
     * @since Android 1.0
     */
    public static final int DIRECTION_DEFAULT_LEFT_TO_RIGHT = -2;

    /**
     * Constant that indicates the default base level. If there is no strong
     * character, then set the paragraph level to 1 (right-to-left).
     * 
     * @since Android 1.0
     */
    public static final int DIRECTION_DEFAULT_RIGHT_TO_LEFT = -1;

    /**
     * Constant that specifies the default base level as 0 (left-to-right).
     * 
     * @since Android 1.0
     */
    public static final int DIRECTION_LEFT_TO_RIGHT = 0;

    /**
     * Constant that specifies the default base level as 1 (right-to-left).
     * 
     * @since Android 1.0
     */
    public static final int DIRECTION_RIGHT_TO_LEFT = 1;

    /**
     * Creates a {@code Bidi} object from the {@code
     * AttributedCharacterIterator} of a paragraph text. The RUN_DIRECTION
     * attribute determines the base direction of the bidirectional text. If it
     * is not specified explicitly, the algorithm uses
     * DIRECTION_DEFAULT_LEFT_TO_RIGHT by default. The BIDI_EMBEDDING attribute
     * specifies the level of embedding for each character. Values between -1
     * and -62 denote overrides at the level's absolute value, values from 1 to
     * 62 indicate embeddings, and the 0 value indicates the level is calculated
     * by the algorithm automatically. For the character with no BIDI_EMBEDDING
     * attribute or with a improper attribute value, such as a {@code null}
     * value, the algorithm treats its embedding level as 0. The NUMERIC_SHAPING
     * attribute specifies the instance of NumericShaper used to convert
     * European digits to other decimal digits before performing the bidi
     * algorithm.
     * 
     * @param paragraph
     *            the String containing the paragraph text to perform the
     *            algorithm.
     * @throws IllegalArgumentException
     *             if {@code paragraph} is {@code null}.
     * @see TextAttribute#BIDI_EMBEDDING
     * @see TextAttribute#NUMERIC_SHAPING
     * @see TextAttribute#RUN_DIRECTION
     * @since Android 1.0
     */
    public Bidi(AttributedCharacterIterator paragraph) {
        if (paragraph == null) {
            // text.14=paragraph is null
            throw new IllegalArgumentException(Messages.getString("text.14")); //$NON-NLS-1$
        }

        int begin = paragraph.getBeginIndex();
        int end = paragraph.getEndIndex();
        int length = end - begin;
        char text[] = new char[length + 1]; // One more char for
        // AttributedCharacterIterator.DONE

        if (length != 0) {
            text[0] = paragraph.first();
        } else {
            paragraph.first();
        }

        // First check the RUN_DIRECTION attribute.
        int flags = DIRECTION_DEFAULT_LEFT_TO_RIGHT;
        Object direction = paragraph.getAttribute(TextAttribute.RUN_DIRECTION);
        if (direction != null && direction instanceof Boolean) {
            if (direction.equals(TextAttribute.RUN_DIRECTION_LTR)) {
                flags = DIRECTION_LEFT_TO_RIGHT;
            } else {
                flags = DIRECTION_RIGHT_TO_LEFT;
            }
        }

        // Retrieve the text and gather BIDI_EMBEDDINGS
        byte embeddings[] = null;
        for (int textLimit = 1, i = 1; i < length; textLimit = paragraph
                .getRunLimit(TextAttribute.BIDI_EMBEDDING)
                - begin + 1) {
            Object embedding = paragraph
                    .getAttribute(TextAttribute.BIDI_EMBEDDING);
            if (embedding != null && embedding instanceof Integer) {
                int embLevel = ((Integer) embedding).intValue();

                if (embeddings == null) {
                    embeddings = new byte[length];
                }

                for (; i < textLimit; i++) {
                    text[i] = paragraph.next();
                    embeddings[i - 1] = (byte) embLevel;
                }
            } else {
                for (; i < textLimit; i++) {
                    text[i] = paragraph.next();
                }
            }
        }

        // Apply NumericShaper to the text
        Object numericShaper = paragraph
                .getAttribute(TextAttribute.NUMERIC_SHAPING);
        if (numericShaper != null && numericShaper instanceof NumericShaper) {
            ((NumericShaper) numericShaper).shape(text, 0, length);
        }

        long pBidi = createUBiDi(text, 0, embeddings, 0, length, flags);
        readBidiInfo(pBidi);
        BidiWrapper.ubidi_close(pBidi);
    }

    /**
     * Creates a {@code Bidi} object.
     * 
     * @param text
     *            the char array of the paragraph text that is processed.
     * @param textStart
     *            the index in {@code text} of the start of the paragraph.
     * @param embeddings
     *            the embedding level array of the paragraph text, specifying
     *            the embedding level information for each character. Values
     *            between -1 and -61 denote overrides at the level's absolute
     *            value, values from 1 to 61 indicate embeddings, and the 0
     *            value indicates the level is calculated by the algorithm
     *            automatically.
     * @param embStart
     *            the index in {@code embeddings} of the start of the paragraph.
     * @param paragraphLength
     *            the length of the text to perform the algorithm.
     * @param flags
     *            indicates the base direction of the bidirectional text. It is
     *            expected that this will be one of the direction constant
     *            values defined in this class. An unknown value is treated as
     *            DIRECTION_DEFAULT_LEFT_TO_RIGHT.
     * @throws IllegalArgumentException
     *             if {@code textStart}, {@code embStart}, or {@code
     *             paragraphLength} is negative; if 
     *             {@code text.length < textStart + paragraphLength} or 
     *             {@code embeddings.length < embStart + paragraphLength}.
     * @see #DIRECTION_LEFT_TO_RIGHT
     * @see #DIRECTION_RIGHT_TO_LEFT
     * @see #DIRECTION_DEFAULT_RIGHT_TO_LEFT
     * @see #DIRECTION_DEFAULT_LEFT_TO_RIGHT
     * @since Android 1.0
     */
    public Bidi(char[] text, int textStart, byte[] embeddings, int embStart,
            int paragraphLength, int flags) {
        if (textStart < 0) {
            // text.0D=Negative textStart value {0}
            throw new IllegalArgumentException(Messages.getString(
                    "text.0D", textStart)); //$NON-NLS-1$
        }
        if (embStart < 0) {
            // text.10=Negative embStart value {0}
            throw new IllegalArgumentException(Messages.getString(
                    "text.10", embStart)); //$NON-NLS-1$
        }
        if (paragraphLength < 0) {
            // text.11=Negative paragraph length {0}
            throw new IllegalArgumentException(Messages.getString(
                    "text.11", paragraphLength)); //$NON-NLS-1$
        }
        long pBidi = createUBiDi(text, textStart, embeddings, embStart,
                paragraphLength, flags);
        readBidiInfo(pBidi);
        BidiWrapper.ubidi_close(pBidi);
    }

    /**
     * Creates a {@code Bidi} object.
     * 
     * @param paragraph
     *            the string containing the paragraph text to perform the
     *            algorithm on.
     * @param flags
     *            indicates the base direction of the bidirectional text. It is
     *            expected that this will be one of the direction constant
     *            values defined in this class. An unknown value is treated as
     *            DIRECTION_DEFAULT_LEFT_TO_RIGHT.
     * @see #DIRECTION_LEFT_TO_RIGHT
     * @see #DIRECTION_RIGHT_TO_LEFT
     * @see #DIRECTION_DEFAULT_RIGHT_TO_LEFT
     * @see #DIRECTION_DEFAULT_LEFT_TO_RIGHT
     * @since Android 1.0
     */
    public Bidi(String paragraph, int flags) {
        this((paragraph == null ? null : paragraph.toCharArray()), 0, null, 0,
                (paragraph == null ? 0 : paragraph.length()), flags);
    }

    // create the native UBiDi struct, need to be closed with ubidi_close().
    private static long createUBiDi(char[] text, int textStart,
            byte[] embeddings, int embStart, int paragraphLength, int flags) {
        char[] realText = null;

        byte[] realEmbeddings = null;

        if (text == null || text.length - textStart < paragraphLength) {
            throw new IllegalArgumentException();
        }
        realText = new char[paragraphLength];
        System.arraycopy(text, textStart, realText, 0, paragraphLength);

        if (embeddings != null) {
            if (embeddings.length - embStart < paragraphLength) {
                throw new IllegalArgumentException();
            }
            if (paragraphLength > 0) {
                Bidi temp = new Bidi(text, textStart, null, 0, paragraphLength,
                        flags);
                realEmbeddings = new byte[paragraphLength];
                System.arraycopy(temp.offsetLevel, 0, realEmbeddings, 0,
                        paragraphLength);
                for (int i = 0; i < paragraphLength; i++) {
                    byte e = embeddings[i];
                    if (e < 0) {
                        realEmbeddings[i] = (byte) (BidiWrapper.UBIDI_LEVEL_OVERRIDE - e);
                    } else if (e > 0) {
                        realEmbeddings[i] = e;
                    } else {
                        realEmbeddings[i] |= (byte) BidiWrapper.UBIDI_LEVEL_OVERRIDE;
                    }
                }
            }
        }

        if (flags > 1 || flags < -2) {
            flags = 0;
        }

        long bidi = BidiWrapper.ubidi_open();
        BidiWrapper.ubidi_setPara(bidi, realText, paragraphLength,
                (byte) flags, realEmbeddings);
        return bidi;
    }

    // private constructor, used by createLineBidi()
    private Bidi(long pBidi) {
        readBidiInfo(pBidi);
    }

    // read info from the native UBiDi struct
    private void readBidiInfo(long pBidi) {

        length = BidiWrapper.ubidi_getLength(pBidi);

        offsetLevel = (length == 0) ? null : BidiWrapper.ubidi_getLevels(pBidi);

        baseLevel = BidiWrapper.ubidi_getParaLevel(pBidi);

        int runCount = BidiWrapper.ubidi_countRuns(pBidi);
        if (runCount == 0) {
            unidirectional = true;
            runs = null;
        } else if (runCount < 0) {
            runs = null;
        } else {
            runs = BidiWrapper.ubidi_getRuns(pBidi);

            // Simplified case for one run which has the base level
            if (runCount == 1 && runs[0].getLevel() == baseLevel) {
                unidirectional = true;
                runs = null;
            }
        }

        direction = BidiWrapper.ubidi_getDirection(pBidi);
    }

    private int baseLevel;

    private int length;

    private byte[] offsetLevel;

    private BidiRun[] runs;

    private int direction;

    private boolean unidirectional;

    /**
     * Returns whether the base level is from left to right.
     * 
     * @return true if the base level is from left to right.
     * @since Android 1.0
     */
    public boolean baseIsLeftToRight() {
        return baseLevel % 2 == 0 ? true : false;
    }

    /**
     * Creates a new {@code Bidi} object containing the information of one line
     * from this object.
     * 
     * @param lineStart
     *            the start offset of the line.
     * @param lineLimit
     *            the limit of the line.
     * @return the new line Bidi object. In this new object, the indices will
     *         range from 0 to (limit - start - 1).
     * @throws IllegalArgumentException
     *             if {@code lineStart < 0}, {@code lineLimit < 0}, {@code
     *             lineStart > lineLimit} or if {@code lineStart} is greater
     *             than the length of this object's paragraph text.
     * @since Android 1.0
     */
    public Bidi createLineBidi(int lineStart, int lineLimit) {
        if (lineStart < 0 || lineLimit < 0 || lineLimit > length
                || lineStart > lineLimit) {
            // text.12=Invalid ranges (start={0}, limit={1}, length={2})
            throw new IllegalArgumentException(Messages.getString(
                    "text.12", new Object[] { lineStart, lineLimit, length })); //$NON-NLS-1$
        }
        char[] text = new char[this.length];
        Arrays.fill(text, 'a');
        byte[] embeddings = new byte[this.length];
        for (int i = 0; i < embeddings.length; i++) {
            embeddings[i] = (byte) -this.offsetLevel[i];
        }

        int dir = this.baseIsLeftToRight() ? Bidi.DIRECTION_LEFT_TO_RIGHT
                : Bidi.DIRECTION_RIGHT_TO_LEFT;

        long parent = createUBiDi(text, 0, embeddings, 0, this.length, dir);

        long line = BidiWrapper.ubidi_setLine(parent, lineStart, lineLimit);
        Bidi result = new Bidi(line);
        BidiWrapper.ubidi_close(line);
        BidiWrapper.ubidi_close(parent);
        return result;
    }

    /**
     * Returns the base level.
     * 
     * @return the base level.
     * @since Android 1.0
     */
    public int getBaseLevel() {
        return baseLevel;
    }

    /**
     * Returns the length of the text in the {@code Bidi} object.
     * 
     * @return the length.
     * @since Android 1.0
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns the level of a specified character.
     * 
     * @param offset
     *            the offset of the character.
     * @return the level.
     * @since Android 1.0
     */
    public int getLevelAt(int offset) {
        try {
            return offsetLevel[offset] & ~BidiWrapper.UBIDI_LEVEL_OVERRIDE;
        } catch (RuntimeException e) {
            return baseLevel;
        }
    }

    /**
     * Returns the number of runs in the bidirectional text.
     * 
     * @return the number of runs, at least 1.
     * @since Android 1.0
     */
    public int getRunCount() {
        return unidirectional ? 1 : runs.length;
    }

    /**
     * Returns the level of the specified run.
     * 
     * @param run
     *            the index of the run.
     * @return the level of the run.
     * @since Android 1.0
     */
    public int getRunLevel(int run) {
        return unidirectional ? baseLevel : runs[run].getLevel();
    }

    /**
     * Returns the limit offset of the specified run.
     * 
     * @param run
     *            the index of the run.
     * @return the limit offset of the run.
     * @since Android 1.0
     */
    public int getRunLimit(int run) {
        return unidirectional ? length : runs[run].getLimit();
    }

    /**
     * Returns the start offset of the specified run.
     * 
     * @param run
     *            the index of the run.
     * @return the start offset of the run.
     * @since Android 1.0
     */
    public int getRunStart(int run) {
        return unidirectional ? 0 : runs[run].getStart();
    }

    /**
     * Indicates whether the text is from left to right, that is, both the base
     * direction and the text direction is from left to right.
     * 
     * @return {@code true} if the text is from left to right; {@code false}
     *         otherwise.
     * @since Android 1.0
     */
    public boolean isLeftToRight() {
        return direction == BidiWrapper.UBiDiDirection_UBIDI_LTR;
    }

    /**
     * Indicates whether the text direction is mixed.
     * 
     * @return {@code true} if the text direction is mixed; {@code false}
     *         otherwise.
     * @since Android 1.0
     */
    public boolean isMixed() {
        return direction == BidiWrapper.UBiDiDirection_UBIDI_MIXED;
    }

    /**
     * Indicates whether the text is from right to left, that is, both the base
     * direction and the text direction is from right to left.
     * 
     * @return {@code true} if the text is from right to left; {@code false}
     *         otherwise.
     * @since Android 1.0
     */
    public boolean isRightToLeft() {
        return direction == BidiWrapper.UBiDiDirection_UBIDI_RTL;
    }

    /**
     * Reorders a range of objects according to their specified levels. This is
     * a convenience function that does not use a {@code Bidi} object. The range
     * of objects at {@code index} from {@code objectStart} to {@code
     * objectStart + count} will be reordered according to the range of levels
     * at {@code index} from {@code levelStart} to {@code levelStart + count}.
     * 
     * @param levels
     *            the level array, which is already determined.
     * @param levelStart
     *            the start offset of the range of the levels.
     * @param objects
     *            the object array to reorder.
     * @param objectStart
     *            the start offset of the range of objects.
     * @param count
     *            the count of the range of objects to reorder.
     * @throws IllegalArgumentException
     *             if {@code count}, {@code levelStart} or {@code objectStart}
     *             is negative; if {@code count > levels.length - levelStart} or
     *             if {@code count > objects.length - objectStart}.
     * @since Android 1.0
     */
    public static void reorderVisually(byte[] levels, int levelStart,
            Object[] objects, int objectStart, int count) {
        if (count < 0 || levelStart < 0 || objectStart < 0
                || count > levels.length - levelStart
                || count > objects.length - objectStart) {
            // text.13=Invalid ranges (levels={0}, levelStart={1}, objects={2},
            // objectStart={3}, count={4})
            throw new IllegalArgumentException(Messages.getString("text.13", //$NON-NLS-1$
                    new Object[] { levels.length, levelStart, objects.length,
                            objectStart, count }));
        }
        byte[] realLevels = new byte[count];
        System.arraycopy(levels, levelStart, realLevels, 0, count);

        int[] indices = BidiWrapper.ubidi_reorderVisual(realLevels, count);

        LinkedList<Object> result = new LinkedList<Object>();
        for (int i = 0; i < count; i++) {
            result.addLast(objects[objectStart + indices[i]]);
        }

        System.arraycopy(result.toArray(), 0, objects, objectStart, count);
    }

    /**
     * Indicates whether a range of characters of a text requires a {@code Bidi}
     * object to display properly.
     * 
     * @param text
     *            the char array of the text.
     * @param start
     *            the start offset of the range of characters.
     * @param limit
     *            the limit offset of the range of characters.
     * @return {@code true} if the range of characters requires a {@code Bidi}
     *         object; {@code false} otherwise.
     * @throws IllegalArgumentException
     *             if {@code start} or {@code limit} is negative; {@code start >
     *             limit} or {@code limit} is greater than the length of this
     *             object's paragraph text.
     * @since Android 1.0
     */
    public static boolean requiresBidi(char[] text, int start, int limit) {
        int length = text.length;
        if (limit < 0 || start < 0 || start > limit || limit > length) {
            throw new IllegalArgumentException();
        }
        Bidi bidi = new Bidi(text, start, null, 0, limit - start, 0);
        return !bidi.isLeftToRight();
    }

    /**
     * Returns the internal message of the {@code Bidi} object, used in
     * debugging.
     * 
     * @return a string containing the internal message.
     * @since Android 1.0
     */
    @Override
    public String toString() {
        return super.toString()
                + "[direction: " + direction + " baselevel: " + baseLevel //$NON-NLS-1$ //$NON-NLS-2$
                + " length: " + length + " runs: " + (unidirectional ? "null" : runs.toString()) + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}
