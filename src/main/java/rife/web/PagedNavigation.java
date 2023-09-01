/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */

package rife.web;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;

import rife.engine.Context;
import rife.template.Template;

/**
 * This class provides utility methods to generate navigation for paged lists.
 * <p>The generation of the navigation depends on a collection of block and
 * value IDs that should be defined in a template. Following is a table of all
 * the IDs and their purpose:
 * <table border="1" cellpadding="3">
 * <tr valign="top">
 * <th>ID</th>
 * <th>Description</th>
 * </tr>
 * <tr valign="top">
 * <td><code>&lt;!--b&nbsp;nav:firstRange--&gt;&lt;!--/b--&gt;</code></td>
 * <td>Provides the content that will be used to jump to the first range. This
 * block has to contain a nav:route value that will be replaced with the
 * actual URL that will trigger the paging behaviour.</td>
 * </tr>
 * <tr valign="top">
 * <td><code>&lt;!--b&nbsp;nav:firstRange:disabled--&gt;&lt;!--/b--&gt;</code></td>
 * <td>Provides the content that will be used when jumping to the first range
 * is not appropriate, for instance when the first range is already the
 * current offset.</td>
 * </tr>
 * <tr valign="top">
 * <td><code>&lt;!--b&nbsp;nav:previousRange--&gt;&lt;!--/b--&gt;</code></td>
 * <td>Provides the content that will be used to jump to the previous range
 * according to the current offset. This block has to contain a nav:route
 * value that will be replaced with the actual URL that will trigger the
 * paging behaviour.</td>
 * </tr>
 * <tr valign="top">
 * <td><code>&lt;!--b&nbsp;nav:previousRange:disabled--&gt;&lt;!--/b--&gt;</code></td>
 * <td>Provides the content that will be used when jumping to the previous
 * range is not appropriate, for instance when the first range is the current
 * offset.</td>
 * </tr>
 * <tr valign="top">
 * <td><code>&lt;!--b&nbsp;nav:absoluteRange--&gt;&lt;!--/b--&gt;</code></td>
 * <td>Provides the content that will be used to jump directly to each
 * individual range. This block has to contain a nav:route value that will
 * be replaced with the actual URL that will trigger the paging behaviour.</td>
 * </tr>
 * <tr valign="top">
 * <td><code>&lt;!--b&nbsp;nav:absoluteRange:disabled--&gt;&lt;!--/b--&gt;</code></td>
 * <td>Provides the content that will be used when jumping directly to a
 * specific individual range is not appropriate, for instance when that range
 * corresponds to the current offset.</td>
 * </tr>
 * <tr valign="top">
 * <td><code>&lt;!--b&nbsp;nav:nextRange--&gt;&lt;!--/b--&gt;</code></td>
 * <td>Provides the content that will be used to jump to the next range
 * according to the current offset. This block has to contain a nav:route
 * value that will be replaced with the actual URL that will trigger the
 * paging behaviour.</td>
 * </tr>
 * <tr valign="top">
 * <td><code>&lt;!--b&nbsp;nav:nextRange:disabled--&gt;&lt;!--/b--&gt;</code></td>
 * <td>Provides the content that will be used when jumping to the next range
 * is not appropriate, for instance when the last range is the current offset.</td>
 * </tr>
 * <tr valign="top">
 * <td><code>&lt;!--b&nbsp;nav:lastRange--&gt;&lt;!--/b--&gt;</code></td>
 * <td>Provides the content that will be used to the last range. This block
 * has to contain a nav:route value that will be replaced with the actual
 * URL that will trigger the paging behaviour.</td>
 * </tr>
 * <tr valign="top">
 * <td><code>&lt;!--b&nbsp;nav:lastRange:disabled--&gt;&lt;!--/b--&gt;</code></td>
 * <td>Provides the content that will be used when jumping to the last range
 * is not appropriate, for instance when the last range is already the current
 * offset.</td>
 * </tr>
 * <tr valign="top">
 * <td><code>&lt;!--v&nbsp;nav:route/--&gt;</code></td>
 * <td>Will be replaced with the actual URL that will trigger the paging behaviour.</td>
 * </tr>
 * <tr valign="top">
 * <td><code>&lt;!--v&nbsp;nav:rangeCount/--&gt;</code></td>
 * <td>Will contain the number of ranges that are needed to display all the
 * information that is paged. This value is optional.</td>
 * </tr>
 * <tr valign="top">
 * <td><code>&lt;!--v&nbsp;nav:firstRange/--&gt;</code></td>
 * <td>Will contain the content that allows to jump to the first range. This
 * corresponds to the beginning of the paged data.</td>
 * </tr>
 * <tr valign="top">
 * <td><code>&lt;!--v&nbsp;nav:previousRange/--&gt;</code></td>
 * <td>Will contain the content that allows to jump to the previous range
 * according to the current offset.</td>
 * </tr>
 * <tr valign="top">
 * <td><code>&lt;!--v&nbsp;nav:absoluteRanges/--&gt;</code></td>
 * <td>Will contain the content that allows to jump directly to each
 * individual range that is available.</td>
 * </tr>
 * <tr valign="top">
 * <td><code>&lt;!--v&nbsp;nav:nextRange/--&gt;</code></td>
 * <td>Will contain the content that allows to jump to the next range
 * according to the current offset.</td>
 * </tr>
 * <tr valign="top">
 * <td><code>&lt;!--v&nbsp;nav:lastRange/--&gt;</code></td>
 * <td>Will contain the content that allows to jump to the last range. This
 * corresponds to the end of the paged data.</td>
 * </table>
 * <p>Besides these template conventions, you also have to provide one exit
 * and one output that will be used to create the links that will perform the
 * actual paging behaviour of the navigation. By default, the
 * <code>change_offset</code> exit and the offset <code>output</code> will be
 * used. It's up to you to create the datalink and flowlink and to correctly
 * handle the offset value when it changes.
 * <p>A very basic paged navigation could for example be defined like this:
 * <pre>
 * &lt;!--b&nbsp;nav:firstRange--&gt;&lt;a href="{{v nav:route/}}"&gt;&amp;lt;&amp;lt;&lt;/a&gt;&lt;!--/b--&gt;
 * &lt;!--b&nbsp;nav:firstRange:disabled--&gt;&amp;lt;&amp;lt;&lt;!--/b--&gt;
 * &lt;!--b&nbsp;nav:previousRange--&gt;&lt;a href="{{v nav:route/}}"&gt;&amp;lt;&lt;/a&gt;&lt;!--/b--&gt;
 * &lt;!--b&nbsp;nav:previousRange:disabled--&gt;&amp;lt;&lt;!--/b--&gt;
 * &lt;!--b&nbsp;nav:absoluteRange--&gt;&amp;nbsp;&lt;a href="{{v nav:route/}}"&gt;&lt;!--v&nbsp;absoluteRangeText/--&gt;&lt;/a&gt;&amp;nbsp;&lt;!--/b--&gt;
 * &lt;!--b&nbsp;nav:absoluteRange:disabled--&gt;&amp;nbsp;&lt;!--v&nbsp;absoluteRangeText/--&gt;&amp;nbsp;&lt;!--/b--&gt;
 * &lt;!--b&nbsp;nav:nextRange--&gt;&lt;a href="{{v nav:route/}}"&gt;&amp;gt;&lt;/a&gt;&lt;!--/b--&gt;
 * &lt;!--b&nbsp;nav:nextRange:disabled--&gt;&amp;gt;&lt;!--/b--&gt;
 * &lt;!--b&nbsp;nav:lastRange--&gt;&lt;a href="{{v nav:route/}}"&gt;&amp;gt;&amp;gt;&lt;/a&gt;&lt;!--/b--&gt;
 * &lt;!--b&nbsp;nav:lastRange:disabled--&gt;&amp;gt;&amp;gt;&lt;!--/b--&gt;
 *
 * Pages: &lt;!--v&nbsp;nav:rangeCount/--&gt; ( &lt;!--v&nbsp;nav:firstRange/--&gt; &lt;!--v&nbsp;nav:previousRange/--&gt; &lt;!--v&nbsp;nav:nextRange/--&gt; &lt;!--v&nbsp;nav:lastRange/--&gt; | &lt;!--v&nbsp;nav:absoluteRanges/--&gt; )
 * </pre>
 * <p>Which could result in the following output where all the underlined
 * parts are clickable and will trigger the <code>change_offset</code> exit
 * and provide a new corresponding value for the offset <code>output</code>:
 * <p><code>Pages: 9 ( &lt;&lt; &lt; <u>&gt;</u> <u>&gt;&gt;</u> | 1 <u>2</u>
 * <u>3</u> <u>4</u> <u>5</u> <u>6</u> <u>7</u> <u>8</u> <u>9</u> )</code>
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.1
 */
public class PagedNavigation {
    public static final String PREFIX_NAV = "nav:";

    public static final String SUFFIX_DISABLED = ":disabled";

    public static final String ID_ROUTE_OFFSET = PREFIX_NAV + "route";
    public static final String ID_RANGE_COUNT = PREFIX_NAV + "rangeCount";

    public static final String ID_ABSOLUTE_RANGE_TEXT = "absoluteRangeText";

    public static final String ID_FIRST_RANGE = PREFIX_NAV + "firstRange";
    public static final String ID_PREVIOUS_RANGE = PREFIX_NAV + "previousRange";
    public static final String ID_ABSOLUTE_RANGES = PREFIX_NAV + "absoluteRanges";
    public static final String ID_ABSOLUTE_RANGE = PREFIX_NAV + "absoluteRange";
    public static final String ID_NEXT_RANGE = PREFIX_NAV + "nextRange";
    public static final String ID_LAST_RANGE = PREFIX_NAV + "lastRange";

    public static final String ID_FIRST_RANGE_DISABLED = PREFIX_NAV + "firstRange" + SUFFIX_DISABLED;
    public static final String ID_PREVIOUS_RANGE_DISABLED = PREFIX_NAV + "previousRange" + SUFFIX_DISABLED;
    public static final String ID_ABSOLUTE_RANGE_DISABLED = PREFIX_NAV + "absoluteRange" + SUFFIX_DISABLED;
    public static final String ID_NEXT_RANGE_DISABLED = PREFIX_NAV + "nextRange" + SUFFIX_DISABLED;
    public static final String ID_LAST_RANGE_DISABLED = PREFIX_NAV + "lastRange" + SUFFIX_DISABLED;

    public static final String DEFAULT_PARAMETER = "offset";

    /**
     * Generates the paged navigation for the given context, template and
     * range configuration. The default parameter <code>offset</code> will be
     * used when generating the links.
     *
     * @param context  The context that is populating the template. Its exit
     *                 will be triggered and its output will be set.
     * @param template The template that will be used for the generation of
     *                 the navigation.
     * @param count    The total number of items that are being paged.
     * @param limit    The maximum of items that will be shown in a range on a
     *                 page.
     * @param offset   The starting offset of the range that is currently
     *                 visible.
     * @param span     The maximum number of ranges that will be shown as
     *                 immediately accessible absolute ranges.
     * @since 1.1
     */
    public static void generate(Context context, Template template, long count, int limit, long offset, int span) {
        generate(context, template, count, limit, offset, span, DEFAULT_PARAMETER);
    }

    /**
     * Generates the paged navigation for the given context, template and
     * range configuration. This version allows you to provide your own name
     * for the parameter that will be used when generating the links.
     *
     * @param context   The context that is populating the template, whose exit
     *                  will be triggered and whose output will be set.
     * @param template  The template that will be used for the generation of
     *                  the navigation.
     * @param count     The total number of items that are being paged.
     * @param limit     The maximum of items that will be shown in a range on a
     *                  page.
     * @param offset    The starting offset of the range that is currently
     *                  visible.
     * @param span      The maximum number of ranges that will be shown as
     *                  immediately accessible absolute ranges.
     * @param parameter The name of the parameter that will contain the value of the
     *                  new range offset when the url if followed.
     * @since 1.1
     */
    public static void generate(Context context, Template template, long count, int limit, long offset, int span, String parameter) {
        var range_count = (long) Math.ceil(((double) count) / limit);
        if (range_count < 0) {
            range_count = 0;
        }
        var max_offset = (range_count - 1) * limit;
        if (max_offset < 0) {
            max_offset = 0;
        }
        if (template.hasValueId(ID_RANGE_COUNT)) {
            template.setValue(ID_RANGE_COUNT, range_count);
        }

        if (offset < 0) {
            offset = 0;
        } else if (offset > max_offset) {
            offset = max_offset;
        } else {
            offset = (long) (floor((double) offset / limit) * limit);
        }

        var first_offset = "0";
        var previous_offset = String.valueOf(offset - limit);
        var next_offset = String.valueOf(offset + limit);
        var last_offset = String.valueOf((long) floor((double) (count - 1) / limit) * limit);

        if (offset <= 0) {
            // turn first and prev off
            template.setBlock(ID_FIRST_RANGE, ID_FIRST_RANGE_DISABLED);
            template.setBlock(ID_PREVIOUS_RANGE, ID_PREVIOUS_RANGE_DISABLED);
        } else {
            template.setValue(ID_ROUTE_OFFSET, context.urlFor(context.route()).param(parameter, first_offset));
            template.setBlock(ID_FIRST_RANGE, ID_FIRST_RANGE);

            template.setValue(ID_ROUTE_OFFSET, context.urlFor(context.route()).param(parameter, previous_offset));
            template.setBlock(ID_PREVIOUS_RANGE, ID_PREVIOUS_RANGE);
        }

        if (offset + limit >= count) {
            // turn next and last off
            template.setBlock(ID_NEXT_RANGE, ID_NEXT_RANGE_DISABLED);
            template.setBlock(ID_LAST_RANGE, ID_LAST_RANGE_DISABLED);
        } else {
            template.setValue(ID_ROUTE_OFFSET, context.urlFor(context.route()).param(parameter, next_offset));
            template.setBlock(ID_NEXT_RANGE, ID_NEXT_RANGE);

            template.setValue(ID_ROUTE_OFFSET, context.urlFor(context.route()).param(parameter, last_offset));
            template.setBlock(ID_LAST_RANGE, ID_LAST_RANGE);
        }

        var absolute_range_end = (long) (floor((double) offset / limit) + span + 1);
        var absolute_range_page = (long) ((floor((double) offset / limit) + 1) - span);
        if (absolute_range_page < 1) {
            absolute_range_page = 1;
        }
        var absolute_range_offset = (absolute_range_page - 1) * limit;

        template.setValue(ID_ABSOLUTE_RANGES, "");

        if (absolute_range_page > 1) {
            template.setValue(ID_ABSOLUTE_RANGE_TEXT, "...");
            template.setBlock(ID_ABSOLUTE_RANGES, ID_ABSOLUTE_RANGE_DISABLED);
        }

        while (absolute_range_offset < count &&
               absolute_range_page <= absolute_range_end) {
            template.setValue(ID_ABSOLUTE_RANGE_TEXT, absolute_range_page);
            if (offset >= absolute_range_offset &&
                offset < absolute_range_offset + limit) {
                template.appendBlock(ID_ABSOLUTE_RANGES, ID_ABSOLUTE_RANGE_DISABLED);
            } else {
                template.setValue(ID_ROUTE_OFFSET, context.urlFor(context.route()).param(parameter, String.valueOf((int) absolute_range_offset)));
                template.appendBlock(ID_ABSOLUTE_RANGES, ID_ABSOLUTE_RANGE);
            }
            absolute_range_offset += limit;
            absolute_range_page++;
        }

        if (absolute_range_end < ceil((double) count / limit)) {
            template.setValue(ID_ABSOLUTE_RANGE_TEXT, "...");
            template.appendBlock(ID_ABSOLUTE_RANGES, ID_ABSOLUTE_RANGE_DISABLED);
        }

        template.removeValue(ID_ABSOLUTE_RANGE_TEXT);
    }
}

