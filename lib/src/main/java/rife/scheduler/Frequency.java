/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import rife.config.RifeConfig;
import rife.scheduler.exceptions.FrequencyException;
import rife.tools.Localization;
import rife.tools.StringUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

class Frequency {
    private static final int MAX_YEAR = 2050;

    private static final byte[] ALL_MINUTES = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59};
    private static final byte[] ALL_HOURS = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
    private static final byte[] ALL_DATES = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31};
    private static final byte[] ALL_MONTHS = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    private static final byte[] ALL_WEEKDAYS = new byte[]{1, 2, 3, 4, 5, 6, 7};
    private static final byte[] EMPTY_DATE_OVERFLOW = new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

    private String frequency_ = null;

    private byte[] minutes_ = null;
    private byte[] hours_ = null;
    private byte[] dates_ = null;
    private byte[] datesUnderflow_ = null;
    private byte[] datesOverflow_ = null;
    private byte[] months_ = null;
    private byte[] weekdays_ = null;

    private boolean parsed_ = false;

    Frequency(String frequency)
    throws FrequencyException {
        parse(frequency);
    }

    long getNextDate(long start)
    throws FrequencyException {
        if (start < 0) throw new IllegalArgumentException("start should be positive");

        Calendar calendar = Calendar.getInstance(RifeConfig.tools().getDefaultTimeZone(), Localization.getLocale());
        calendar.setTimeInMillis(start);

        int minute = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int date = calendar.get(Calendar.DATE);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        // got to next valid time
        minute++;
        if (-1 == (minute = getNextValidMinute(minute)) ||
            -1 == hours_[hour] ||
            -1 == months_[month - 1] ||
            -1 == getDates(month, year)[date - 1]) {
            hour++;
            if (-1 == (hour = getNextValidHour(hour)) ||
                -1 == months_[month - 1] ||
                -1 == getDates(month, year)[date - 1]) {
                date++;
                hour = getFirstValidHour();
            }
            minute = getFirstValidMinute();
        }

        // got to next valid date
        while (year < MAX_YEAR) {
            if (-1 == (date = getNextValidDate(date, month, year)) ||
                -1 == months_[month - 1]) {
                month++;
                if (-1 == (month = getNextValidMonth(month))) {
                    year++;
                    month = getFirstValidMonth();
                }
                date = getFirstValidDate(month, year);
                if (-1 == date) {
                    date = 1;
                    continue;
                }
            }

            calendar.set(year, month - 1, date, hour, minute);

            if (year == calendar.get(Calendar.YEAR) &&
                month == calendar.get(Calendar.MONTH) + 1) {
                int weekday = calendar.get(Calendar.DAY_OF_WEEK) - 2;
                if (-1 == weekday) {
                    weekday = 6;
                }

                if (weekdays_[weekday] != -1) {
                    return calendar.getTimeInMillis();
                }
            }

            date++;
        }

        throw new FrequencyException("no valid next date available");
    }

    private int getFirstValidMinute() {
        return getNextValidMinute(0);
    }

    private int getNextValidMinute(int minute) {
        assert minute >= 0;

        for (int i = minute; i < minutes_.length; i++) {
            if (minutes_[i] != -1) {
                return minutes_[i];
            }
        }

        return -1;
    }

    private int getFirstValidHour() {
        return getNextValidHour(0);
    }

    private int getNextValidHour(int hour) {
        assert hour >= 0;

        for (int i = hour; i < hours_.length; i++) {
            if (hours_[i] != -1) {
                return hours_[i];
            }
        }

        return -1;
    }

    private byte[] getDates(int month, int year) {
        assert month >= 1;
        assert year >= 0;

        Calendar calendar = Calendar.getInstance(RifeConfig.tools().getDefaultTimeZone(), Localization.getLocale());
        calendar.set(year, month - 1, 1);
        byte maximum_date = (byte) calendar.getActualMaximum(Calendar.DATE);
        byte[] dates = null;

        // only retain the dates that are valid for this month
        dates = new byte[ALL_DATES.length];
        Arrays.fill(dates, (byte) -1);
        System.arraycopy(dates_, 0, dates, 0, maximum_date);

        if (datesUnderflow_ != null &&
            datesOverflow_ != null) {
            // get the maximum date of the previous month
            calendar.roll(Calendar.MONTH, -1);
            byte maximum_date_previous = (byte) calendar.getActualMaximum(Calendar.DATE);

            // integrate overflowed dates
            byte end_value = ALL_DATES[ALL_DATES.length - 1];
            byte difference = (byte) (end_value - maximum_date_previous);

            int start_position = ALL_DATES.length - 1;
            int target_position = 0;
            for (int i = start_position; i >= 0; i--) {
                if (datesUnderflow_[i] != 0) {
                    // handle the possibility where due to the difference,
                    // the underflow turns into an overflow
                    if (i > maximum_date_previous - 1) {
                        target_position = i - maximum_date_previous;
                        if (target_position < datesUnderflow_[i] &&
                            target_position < maximum_date) {
                            dates[target_position] = ALL_DATES[target_position];
                        }
                    }
                }

                if (datesOverflow_[i] != 0) {
                    // handle the overflow of the end of the previous month
                    target_position = i + difference;
                    if (target_position < datesOverflow_[i] &&
                        target_position < maximum_date) {
                        dates[target_position] = ALL_DATES[target_position];
                    }
                }
            }
        }

        return dates;
    }

    private int getFirstValidDate(int month, int year) {
        return getNextValidDate(1, month, year);
    }

    private int getNextValidDate(int date, int month, int year) {
        assert date >= 1;
        assert month >= 1;
        assert year >= 0;

        byte[] dates = getDates(month, year);

        for (int i = date - 1; i < dates.length; i++) {
            if (dates[i] != -1) {
                return dates[i];
            }
        }

        return -1;
    }

    private int getFirstValidMonth() {
        return getNextValidMonth(1);
    }

    private int getNextValidMonth(int month) {
        assert month >= 1;

        for (int i = month - 1; i < months_.length; i++) {
            if (months_[i] != -1) {
                return months_[i];
            }
        }

        return -1;
    }

    boolean isParsed() {
        return parsed_;
    }

    String getFrequency() {
        return frequency_;
    }

    byte[] getMinutes() {
        return minutes_;
    }

    byte[] getHours() {
        return hours_;
    }

    byte[] getDates() {
        return dates_;
    }

    byte[] getDatesUnderflow() {
        return datesUnderflow_;
    }

    byte[] getDatesOverflow() {
        return datesOverflow_;
    }

    byte[] getMonths() {
        return months_;
    }

    byte[] getWeekdays() {
        return weekdays_;
    }

    void parse(String frequency)
    throws FrequencyException {
        if (null == frequency) throw new IllegalArgumentException("frequency can't be null");
        if (0 == frequency.length()) throw new IllegalArgumentException("frequency can't be empty");

        frequency_ = frequency;
        parsed_ = false;

        minutes_ = null;
        hours_ = null;
        dates_ = null;
        datesUnderflow_ = new byte[ALL_DATES.length];
        datesOverflow_ = new byte[ALL_DATES.length];
        months_ = null;
        weekdays_ = null;

        List<String> frequency_parts = StringUtils.split(frequency, " ");
        if (frequency_parts.size() != 5) {
            throw new FrequencyException("invalid frequency, should be 5 fields seperated by a space");
        }

        String minutes = frequency_parts.get(0);
        String hours = frequency_parts.get(1);
        String dates = frequency_parts.get(2);
        String months = frequency_parts.get(3);
        String weekdays = frequency_parts.get(4);

        minutes_ = processParts(StringUtils.split(minutes, ","), ALL_MINUTES, false, null, null);
        hours_ = processParts(StringUtils.split(hours, ","), ALL_HOURS, false, null, null);
        dates_ = processParts(StringUtils.split(dates, ","), ALL_DATES, true, datesUnderflow_, datesOverflow_);
        if (Arrays.equals(datesUnderflow_, EMPTY_DATE_OVERFLOW)) {
            datesUnderflow_ = null;
        }
        if (Arrays.equals(datesOverflow_, EMPTY_DATE_OVERFLOW)) {
            datesOverflow_ = null;
        }
        months_ = processParts(StringUtils.split(months, ","), ALL_MONTHS, false, null, null);
        weekdays_ = processParts(StringUtils.split(weekdays, ","), ALL_WEEKDAYS, false, null, null);

        parsed_ = true;
    }

    private byte[] processParts(List<String> parts, byte[] allValues, boolean deferOverflowProcessing, byte[] underflowStorage, byte[] overflowStorage)
    throws FrequencyException {
        assert parts != null;
        assert parts.size() > 0;
        assert allValues != null;
        assert allValues.length > 0;
        assert !deferOverflowProcessing || (deferOverflowProcessing && underflowStorage != null && overflowStorage != null);

        String part = null;
        byte[] result_values = null;

        // initialize the values to -1, the frequency syntax
        // will enable the specified array positions by copying them
        // from the reference array
        result_values = new byte[allValues.length];
        Arrays.fill(result_values, (byte) -1);
        if (underflowStorage != null) {
            Arrays.fill(underflowStorage, (byte) -1);
        }
        if (overflowStorage != null) {
            Arrays.fill(overflowStorage, (byte) -1);
        }

        byte begin = allValues[0];
        byte end = allValues[allValues.length - 1];

        for (String current_part : parts) {
            part = current_part;

            // plain wildcard
            if (current_part.equals("*")) {
                result_values = allValues;
                return result_values;
            }

            try {
                int separator = -1;
                byte divider = -1;

                // divider
                if ((separator = current_part.indexOf("/")) != -1) {
                    divider = Byte.parseByte(current_part.substring(separator + 1));
                    current_part = current_part.substring(0, separator);
                }

                // wildcard
                if (current_part.equals("*")) {
                    if (-1 == divider) {
                        throw new FrequencyException("invalid frequency part '" + part + "'");
                    }

                    for (byte i = 0; i < allValues.length; i += divider) {
                        result_values[i] = allValues[i];
                    }
                    continue;
                }
                // range
                else if ((separator = current_part.indexOf("-")) != -1) {
                    byte left = Byte.parseByte(current_part.substring(0, separator));
                    byte right = Byte.parseByte(current_part.substring(separator + 1));

                    if (left < begin ||
                        left > end) {
                        throw new FrequencyException("value out of range '" + left + "'");
                    }
                    if (right < begin ||
                        right > end) {
                        throw new FrequencyException("value out of range '" + right + "'");
                    }

                    if (left == right) {
                        if (divider != -1) {
                            throw new FrequencyException("invalid frequency part '" + part + "'");
                        }
                        result_values[left - begin] = allValues[left - begin];
                        continue;
                    }

                    if (-1 == divider) {
                        divider = 1;
                    }

                    if (right < left) {
                        if (deferOverflowProcessing) {
                            // the overflow processing should be done later

                            // store the underflow both in the regular fashion and
                            // preserve it seperately for later underflow processing
                            // since it might bleed into overflow
                            while (left <= end) {
                                result_values[left - begin] = allValues[left - begin];
                                // don't store the actual values after the overflow breakpoint
                                // but store the value of the rightmost
                                // limit of the corresponding range
                                if (underflowStorage[left - begin] < right) {
                                    underflowStorage[left - begin] = right;
                                }
                                left += divider;
                            }

                            left = (byte) (begin + (left - end) - 1);

                            // store the positions at which entries are located
                            // the positions contain the value of the rightmost
                            // limit of the corresponding range
                            // this is needed to be able to calculate the correct
                            // transformations later
                            while (left <= right) {
                                // preserve a later right limit
                                if (overflowStorage[left - begin] < right) {
                                    overflowStorage[left - begin] = right;
                                }
                                left += divider;
                            }
                            continue;
                        } else {
                            while (left <= end) {
                                result_values[left - begin] = allValues[left - begin];
                                left += divider;
                            }

                            left = (byte) (begin + (left - end) - 1);
                        }
                    }

                    while (left <= right) {
                        result_values[left - begin] = allValues[left - begin];
                        left += divider;
                    }
                    continue;
                }
                // one number
                else {
                    if (divider != -1) {
                        throw new FrequencyException("invalid frequency part '" + part + "'");
                    }

                    byte minute = Byte.parseByte(current_part);
                    if (minute < begin ||
                        minute > end) {
                        throw new FrequencyException("value out of range '" + minute + "'");
                    }
                    result_values[minute - begin] = allValues[minute - begin];
                }
            } catch (NumberFormatException e) {
                throw new FrequencyException("invalid frequency part '" + part + "'", e);
            }
        }

        return result_values;
    }
}
