/*
 * Copyright (C) 2012 The Android Open Source Project
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

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Exercise some locale-table-driven stuff.
 */
public class Main {

    public static void main(String[] args) {
        try {
            testCalendar();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            testDateFormatSymbols();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            testCurrency();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            testNormalizer();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            testTimeZone();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static void testCalendar() {
        TimeZone tz = TimeZone.getTimeZone("GMT");

        Locale usa = new Locale("en", "US");
        Calendar usaCal = Calendar.getInstance(tz, usa);
        usaCal.set(2012, Calendar.JANUARY, 1);

        Date when = usaCal.getTime();
        DateFormat fmt = DateFormat.getDateInstance(DateFormat.FULL, usa);
        System.out.println("USA: " + fmt.format(when));

        System.out.println("USA: first="
            + usaCal.getFirstDayOfWeek() + ", name="
            + usaCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, usa));


        Locale france = new Locale("fr", "FR");
        Calendar franceCal = Calendar.getInstance(tz, france);
        franceCal.set(2012, Calendar.JANUARY, 2);

        when = franceCal.getTime();
        fmt = DateFormat.getDateInstance(DateFormat.FULL, usa);
        System.out.println("France: " + fmt.format(when));

        System.out.println("France: first="
            + franceCal.getFirstDayOfWeek() + ", name="
            + franceCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, france));
    }

    static void testDateFormatSymbols() {
        Locale usa = new Locale("en", "US");
        DateFormatSymbols syms = DateFormatSymbols.getInstance(usa);
        String[] list = syms.getAmPmStrings();
        System.out.println("USA dfs: " + Arrays.deepToString(list));
    }

    static void testCurrency() {
        Locale usa = new Locale("en", "US");
        Currency dollars = Currency.getInstance(usa);

        System.out.println(usa.toString() + ": " + dollars.toString()
            + " " + dollars.getSymbol() + dollars.getDefaultFractionDigits());

        Locale japan = new Locale("jp", "JP");
        Currency yen = Currency.getInstance(japan);

        System.out.println(japan.toString() + ": " + yen.toString()
            + " " + yen.getSymbol() + yen.getDefaultFractionDigits());
    }

    static void testNormalizer() {
        String composed = "Bl\u00c1ah";
        String decomposed = "Bl\u0041\u0301ah";
        String res;

        res = Normalizer.normalize(composed, Normalizer.Form.NFD);
        if (!decomposed.equals(res)) {
            System.err.println("Bad decompose: '" + composed + "' --> '"
                + res + "'");
        }

        res = Normalizer.normalize(decomposed, Normalizer.Form.NFC);
        if (!composed.equals(res)) {
            System.err.println("Bad compose: '" + decomposed + "' --> '"
                + res + "'");
        }

        System.out.println("Normalizer passed");
    }

    static void testTimeZone() {
    }
}
