/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import rife.config.RifeConfig;
import rife.scheduler.exceptions.FrequencyException;
import rife.tools.StringUtils;

import java.time.DayOfWeek;
import java.time.Month;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines the frequency at which a task should execute.
 * <p>
 * This is inspired by the standard unix crontab frequency specification.
 * <p>
 * A number of frequently used frequencies are available as static constants,
 * a DSL is provided to build the most features of frequency specification,
 * or a textual specification can be provided that will be parsed.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class Frequency {
    private static final int MAX_YEAR = 2050;

    private static final byte[] ALL_MINUTES = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59};
    private static final byte[] ALL_HOURS = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
    private static final byte[] ALL_DATES = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31};
    private static final byte[] ALL_MONTHS = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    private static final byte[] ALL_WEEKDAYS = new byte[]{1, 2, 3, 4, 5, 6, 7};
    private static final byte[] EMPTY_DATE_OVERFLOW = new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

    /**
     * Schedule every minute.
     *
     * @since 1.0
     */
    public static final Frequency MINUTELY = new Frequency("* * * * *");

    /**
     * Schedule every hour on the hour.
     *
     * @since 1.0
     */
    public static final Frequency HOURLY = new Frequency("0 * * * *");

    /**
     * Schedule every day at midnight.
     *
     * @since 1.0
     */
    public static final Frequency DAILY = new Frequency("0 0 * * *");

    /**
     * Schedule every monday at midnight.
     *
     * @since 1.0
     */
    public static final Frequency MONDAYS = new Frequency("0 0 * * 1");

    /**
     * Schedule every tuesday at midnight.
     *
     * @since 1.0
     */
    public static final Frequency TUESDAYS = new Frequency("0 0 * * 2");

    /**
     * Schedule every wednesday at midnight.
     *
     * @since 1.0
     */
    public static final Frequency WEDNESDAYS = new Frequency("0 0 * * 3");

    /**
     * Schedule every thursday at midnight.
     *
     * @since 1.0
     */
    public static final Frequency THURSDAYS = new Frequency("0 0 * * 4");

    /**
     * Schedule every friday at midnight.
     *
     * @since 1.0
     */
    public static final Frequency FRIDAYS = new Frequency("0 0 * * 5");

    /**
     * Schedule every saturday at midnight.
     *
     * @since 1.0
     */
    public static final Frequency SATURDAYS = new Frequency("0 0 * * 6");

    /**
     * Schedule every sunday at midnight.
     *
     * @since 1.0
     */
    public static final Frequency SUNDAYS = new Frequency("0 0 * * 7");

    /**
     * Schedule every month on the first day at midnight.
     *
     * @since 1.0
     */
    public static final Frequency MONTHLY = new Frequency("0 0 1 * *");

    /**
     * Schedule every quarter on the first day at midnight.
     *
     * @since 1.0
     */
    public static final Frequency QUARTERLY = new Frequency("0 0 1 */3 *");

    /**
     * Schedule every year on the first day at midnight.
     *
     * @since 1.0
     */
    public static final Frequency YEARLY = new Frequency("0 0 1 1 *");

    private final String[] parts_ = new String[]{"*", "*", "*", "*", "*"};

    private byte[] minutes_ = null;
    private byte[] hours_ = null;
    private byte[] dates_ = null;
    private byte[] datesUnderflow_ = null;
    private byte[] datesOverflow_ = null;
    private byte[] months_ = null;
    private byte[] weekdays_ = null;

    private boolean parsed_ = false;

    /**
     * Creates a new frequency instance that will schedule every minute.
     *
     * @since 1.0
     */
    public Frequency() {
        reset();
    }

    /**
     * Creates a new frequency instance from the provided crontab specification.
     *
     * @param specification the specification string that will be parsed
     * @throws FrequencyException when an error occurs during the parsing
     * @since 1.0
     */
    public Frequency(String specification)
    throws FrequencyException {
        parse(specification);
    }

    /**
     * Schedule at a specific time.
     *
     * @param hour the hour to schedule, 24-hours, 0-based
     * @param minute the time to schedule
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency at(int hour, int minute) {
        processHours(String.valueOf(hour));
        processMinutes(String.valueOf(minute));
        return this;
    }

    /**
     * Schedule on a specific day.
     *
     * @param month the month to schedule
     * @param date the day of the month to schedule, 1-based
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency on(Month month, int date) {
        processMonths(String.valueOf(month.getValue()));
        processDates(String.valueOf(date));
        return this;
    }

    /**
     * Schedule every minute of the hour.
     *
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency everyMinute() {
        processMinutes("*");
        return this;
    }

    /**
     * Schedule every n-th minute of the hour
     * <p>
     * For instance a step of 10 will schedule on minutes 0, 10, 20, 30, 40, and 50.
     *
     * @param step the step to use to skip over the minutes
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency everyMinute(int step) {
        processMinutes("*/" + step);
        return this;
    }

    /**
     * Schedule at a specific minute of the hour.
     *
     * @param minute to minute in the hour to schedule
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency atMinute(int minute) {
        processMinutes(String.valueOf(minute));
        return this;
    }

    /**
     * Schedule at specific minutes of the hour.
     *
     * @param minutes the minutes in the hour to schedule
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency atMinutes(int... minutes) {
        processMinutes(StringUtils.join(minutes, ","));
        return this;
    }

    /**
     * Schedule every minute of the hour in a range.
     *
     * @param first the first minute in the range
     * @param last the last minute in the range, included
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency duringMinutes(int first, int last) {
        processMinutes(first + "-" + last);
        return this;
    }

    /**
     * Schedule every hour of the day.
     *
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency everyHour() {
        processHours("*");
        return this;
    }

    /**
     * Schedule every n-th hour of the day
     * <p>
     * For instance a step of 6 will schedule on hours 0, 6, 12, 18, and 24.
     *
     * @param step the step to use to skip over the hours
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency everyHour(int step) {
        processHours("*/" + step);
        return this;
    }

    /**
     * Schedule at a specific hour of the day.
     *
     * @param hour to hour in the day to schedule
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency atHour(int hour) {
        processHours(String.valueOf(hour));
        return this;
    }

    /**
     * Schedule at specific hours of the day.
     *
     * @param hours the hours in the day to schedule
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency atHours(int... hours) {
        processHours(StringUtils.join(hours, ","));
        return this;
    }

    /**
     * Schedule every hour of the day in a range.
     *
     * @param first the first hour in the range
     * @param last the last hour in the range, included
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency duringHours(int first, int last) {
        processHours(first + "-" + last);
        return this;
    }

    /**
     * Schedule every day of the month.
     *
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency everyDate() {
        processDates("*");
        return this;
    }

    /**
     * Schedule every n-th day of the month
     * <p>
     * For instance a step of 12 will schedule on day 1, 13, and 25.
     *
     * @param step the step to use to skip over the days
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency everyDate(int step) {
        processDates("*/" + step);
        return this;
    }

    /**
     * Schedule on a specific day of the month, 1-based.
     *
     * @param date to day in the month to schedule
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency onDate(int date) {
        processDates(String.valueOf(date));
        return this;
    }

    /**
     * Schedule on specific days of the month, 1-based.
     *
     * @param dates the days in the month to schedule
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency onDates(int... dates) {
        processDates(StringUtils.join(dates, ","));
        return this;
    }

    /**
     * Schedule every day of the month in a range, 1-based.
     *
     * @param first the first day in the range
     * @param last the last day in the range, included
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency duringDates(int first, int last) {
        processDates(first + "-" + last);
        return this;
    }

    /**
     * Schedule every month of the year.
     *
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency everyMonth() {
        processMonths("*");
        return this;
    }

    /**
     * Schedule every n-th month of the year
     * <p>
     * For instance a step of 3 will schedule on months 1, 4, 7, and 10.
     *
     * @param step the step to use to skip over the months
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency everyMonth(int step) {
        processMonths("*/" + step);
        return this;
    }

    /**
     * Schedule in a specific month of the year.
     *
     * @param month to month of the year to schedule
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency in(Month month) {
        processMonths(String.valueOf(month.getValue()));
        return this;
    }

    /**
     * Schedule in specific months of the year.
     *
     * @param months to months of the year to schedule
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency in(Month... months) {
        processMonths(Arrays.stream(months).map(d -> String.valueOf(d.getValue())).collect(Collectors.joining(",")));
        return this;
    }

    /**
     * Schedule every month of the year.
     *
     * @param first the first month in the range
     * @param last the last month in the range, included
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency between(Month first, Month last) {
        processMonths(first.getValue() + "-" + last.getValue());
        return this;
    }

    /**
     * Schedule every day of the week.
     *
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency everyWeekday() {
        processWeekdays("*");
        return this;
    }

    /**
     * Schedule every n-th day of the week
     * <p>
     * For instance a step of 2 will schedule on day 1, 3, 5 and 7.
     *
     * @param step the step to use to skip over the days
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency everyWeekday(int step) {
        processWeekdays("*/" + step);
        return this;
    }

    /**
     * Schedule on a specific day of the week.
     *
     * @param weekday to day of the week to schedule
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency on(DayOfWeek weekday) {
        processWeekdays(String.valueOf(weekday.getValue()));
        return this;
    }

    /**
     * Schedule on a specific days of the week.
     *
     * @param weekdays to days of the week to schedule
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency on(DayOfWeek... weekdays) {
        processWeekdays(Arrays.stream(weekdays).map(d -> String.valueOf(d.getValue())).collect(Collectors.joining(",")));
        return this;
    }

    /**
     * Schedule every day of the week in a range.
     *
     * @param first the first day in the range
     * @param last the last day in the range, included
     * @return this frequency instance
     * @since 1.0
     */
    public Frequency between(DayOfWeek first, DayOfWeek last) {
        processWeekdays(first.getValue() + "-" + last.getValue());
        return this;
    }

    public String toString() {
        return getSpecification();
    }

    /**
     * Reset the frequency to be scheduler every minute.
     *
     * @since 1.0
     */
    public void reset() {
        parts_[0] = "*";
        parts_[1] = "*";
        parts_[2] = "*";
        parts_[3] = "*";
        parts_[4] = "*";
        minutes_ = Arrays.copyOf(ALL_MINUTES, ALL_MINUTES.length);
        hours_ = Arrays.copyOf(ALL_HOURS, ALL_HOURS.length);
        dates_ = Arrays.copyOf(ALL_DATES, ALL_DATES.length);
        datesUnderflow_ = null;
        datesOverflow_ = null;
        months_ = Arrays.copyOf(ALL_MONTHS, ALL_MONTHS.length);
        weekdays_ = Arrays.copyOf(ALL_WEEKDAYS, ALL_WEEKDAYS.length);
    }

    long getNextTimestamp(long start)
    throws FrequencyException {
        if (start < 0) throw new IllegalArgumentException("start should be positive");

        var calendar = RifeConfig.tools().getCalendarInstance();
        calendar.setTimeInMillis(start);

        var minute = calendar.get(Calendar.MINUTE);
        var hour = calendar.get(Calendar.HOUR_OF_DAY);
        var date = calendar.get(Calendar.DATE);
        var month = calendar.get(Calendar.MONTH) + 1;
        var year = calendar.get(Calendar.YEAR);

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
                var weekday = calendar.get(Calendar.DAY_OF_WEEK) - 2;
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

        for (var i = minute; i < minutes_.length; i++) {
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

        for (var i = hour; i < hours_.length; i++) {
            if (hours_[i] != -1) {
                return hours_[i];
            }
        }

        return -1;
    }

    private byte[] getDates(int month, int year) {
        assert month >= 1;
        assert year >= 0;

        var calendar = RifeConfig.tools().getCalendarInstance();
        calendar.set(year, month - 1, 1);
        var maximum_date = (byte) calendar.getActualMaximum(Calendar.DATE);
        byte[] dates = null;

        // only retain the dates that are valid for this month
        dates = new byte[ALL_DATES.length];
        Arrays.fill(dates, (byte) -1);
        System.arraycopy(dates_, 0, dates, 0, maximum_date);

        if (datesUnderflow_ != null &&
            datesOverflow_ != null) {
            // get the maximum date of the previous month
            calendar.roll(Calendar.MONTH, -1);
            var maximum_date_previous = (byte) calendar.getActualMaximum(Calendar.DATE);

            // integrate overflowed dates
            var end_value = ALL_DATES[ALL_DATES.length - 1];
            var difference = (byte) (end_value - maximum_date_previous);

            var start_position = ALL_DATES.length - 1;
            var target_position = 0;
            for (var i = start_position; i >= 0; i--) {
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

        var dates = getDates(month, year);

        for (var i = date - 1; i < dates.length; i++) {
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

        for (var i = month - 1; i < months_.length; i++) {
            if (months_[i] != -1) {
                return months_[i];
            }
        }

        return -1;
    }

    boolean isParsed() {
        return parsed_;
    }

    String getSpecification() {
        return StringUtils.join(parts_, " ");
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
        if (frequency.isEmpty()) throw new IllegalArgumentException("frequency can't be empty");

        reset();

        parsed_ = false;
        minutes_ = null;
        hours_ = null;
        dates_ = null;
        months_ = null;
        weekdays_ = null;

        var frequency_parts = StringUtils.split(frequency, " ");
        if (frequency_parts.size() != 5) {
            throw new FrequencyException("invalid frequency, should be 5 fields seperated by a space");
        }

        processMinutes(frequency_parts.get(0));
        processHours(frequency_parts.get(1));
        processDates(frequency_parts.get(2));
        processMonths(frequency_parts.get(3));
        processWeekdays(frequency_parts.get(4));

        parsed_ = true;
    }

    private void processMinutes(String minutes) {
        parts_[0] = minutes;
        minutes_ = processParts(StringUtils.split(minutes, ","), ALL_MINUTES, false, null, null);
    }

    private void processHours(String hours) {
        parts_[1] = hours;
        hours_ = processParts(StringUtils.split(hours, ","), ALL_HOURS, false, null, null);
    }

    private void processDates(String dates) {
        datesUnderflow_ = new byte[ALL_DATES.length];
        datesOverflow_ = new byte[ALL_DATES.length];
        parts_[2] = dates;
        dates_ = processParts(StringUtils.split(dates, ","), ALL_DATES, true, datesUnderflow_, datesOverflow_);
        if (Arrays.equals(datesUnderflow_, EMPTY_DATE_OVERFLOW)) {
            datesUnderflow_ = null;
        }
        if (Arrays.equals(datesOverflow_, EMPTY_DATE_OVERFLOW)) {
            datesOverflow_ = null;
        }
    }

    private void processMonths(String months) {
        parts_[3] = months;
        months_ = processParts(StringUtils.split(months, ","), ALL_MONTHS, false, null, null);
    }

    private void processWeekdays(String weekdays) {
        parts_[4] = weekdays;
        weekdays_ = processParts(StringUtils.split(weekdays, ","), ALL_WEEKDAYS, false, null, null);
    }

    private byte[] processParts(List<String> parts, byte[] allValues, boolean deferOverflowProcessing, byte[] underflowStorage, byte[] overflowStorage)
    throws FrequencyException {
        assert parts != null;
        assert !parts.isEmpty();
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

        var begin = allValues[0];
        var end = allValues[allValues.length - 1];

        for (var current_part : parts) {
            part = current_part;

            // plain wildcard
            if (current_part.equals("*")) {
                result_values = allValues;
                return result_values;
            }

            try {
                var separator = -1;
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
                }
                // range
                else if ((separator = current_part.indexOf("-")) != -1) {
                    var left = Byte.parseByte(current_part.substring(0, separator));
                    var right = Byte.parseByte(current_part.substring(separator + 1));

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
                            // preserve it separately for later underflow processing
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
                }
                // one number
                else {
                    if (divider != -1) {
                        throw new FrequencyException("invalid frequency part '" + part + "'");
                    }

                    var minute = Byte.parseByte(current_part);
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

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Frequency frequency = (Frequency) o;
        return Arrays.equals(parts_, frequency.parts_);
    }

    public int hashCode() {
        return Arrays.hashCode(parts_);
    }
}
