/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import rife.database.queries.AbstractWhereDelegateQuery;
import rife.database.queries.Select;
import rife.database.querymanagers.generic.GenericQueryManager;
import rife.engine.Context;
import rife.web.PagedNavigation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Shows the instances of an entity in a paged table.
 * <p>The columns are the properties that have been constrained as
 * {@code listed}, in the order of their {@code position} constraints, and
 * each row provides the operations that apply to its instance.
 * <p>The instances of an entity that is ordered manually are shown in the
 * order of their ordinal, the others in the order of their identifier, so
 * that paging through them shows each of them exactly once.
 * <p>The table is searched through the columns that hold text, without
 * regard for case, and sorted by clicking the header of any column whose
 * value the database keeps. A page that searches or sorts doesn't offer the
 * move buttons, since an instance is moved between the neighbours that its
 * row sits between, which such a page doesn't show.
 * <p>The rows are shown as the instances describe themselves, since looking
 * every one of them up again would cost a query for each row of every page.
 * A callback that gives an instance another identifier therefore ends up
 * with a row whose buttons point elsewhere, which is exactly what the
 * callbacks of an administered bean are asked not to do.
 *
 * @see CrudAdmin
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class CrudBrowse<T> extends CrudElement<T> {
    CrudBrowse(CrudAdmin admin, CrudEntity<T> entity) {
        super(admin, entity);
    }

    protected String pageKind() {
        return "browse";
    }

    protected void perform(Context c) {
        var t = page(c);
        var manager = entity_.createManager(admin_.datasource());
        var routes = admin_.routes(entity_);

        var search = c.parameter("search");
        if (search != null && (search.isBlank() || entity_.getSearchableProperties().isEmpty())) {
            search = null;
        }
        // a sort that was typed into the URL falls back to the order that
        // the page always has, instead of reaching the query
        var sort = c.parameter("sort");
        if (sort != null && !entity_.isSortable(sort)) {
            sort = null;
        }
        var descending = "desc".equals(c.parameter("dir"));

        var limit = entity_.getOptions().getPageSize();
        var offset = Math.max(0, c.parameterInt("offset", 0));
        int count;
        if (search != null) {
            var count_query = manager.getCountQuery();
            applySearch(count_query, search);
            count = manager.count(count_query);
        } else {
            count = manager.count();
        }
        if (offset >= count) {
            offset = Math.max(0, ((count - 1) / limit) * limit);
        }

        t.setValueEncoded(ENTITY_LABEL_VALUE, entity_.getLabel());
        t.setValueEncoded("crud:entityLabelPlural", entity_.getLabelPlural());
        t.setValueEncoded("crud:addUrl", c.urlFor(routes.add));

        if (!entity_.getSearchableProperties().isEmpty()) {
            // an HTML form drops the query string of the URL it submits to,
            // so everything that has to survive a search rides along as a
            // field of the form
            t.setValueEncoded("crud:searchUrl", c.urlFor(routes.browse));
            t.setValueEncoded("crud:searchTerm", search == null ? "" : search);
            if (sort != null) {
                t.setValueEncoded("crud:sortProperty", sort);
                t.setValue("crud:sortDir", descending ? "desc" : "asc");
                t.setBlock("crud:searchSort", "crud:searchSort:present");
            } else {
                t.blankValue("crud:searchSort");
            }
            t.setBlock("crud:search", "crud:search:present");
        } else {
            t.blankValue("crud:search");
        }

        for (var property : entity_.getListedProperties()) {
            t.setValueEncoded("crud:columnLabel", entity_.getPropertyLabel(property));
            if (entity_.isSortable(property)) {
                var active = property.equals(sort);
                var url = c.urlFor(routes.browse).param("sort", property);
                if (active && !descending) {
                    url.param("dir", "desc");
                }
                if (search != null) {
                    url.param("search", search);
                }
                t.setValueEncoded("crud:sortUrl", url);
                if (active) {
                    t.setBlock("crud:sortMarker", descending ? "crud:sortMarker:desc" : "crud:sortMarker:asc");
                    t.setBlock("crud:sortAria", descending ? "crud:sortAria:desc" : "crud:sortAria:asc");
                } else {
                    t.blankValue("crud:sortMarker");
                    t.blankValue("crud:sortAria");
                }
                t.appendBlock("crud:columnHeaders", "crud:columnHeader:sortable");
            } else {
                t.appendBlock("crud:columnHeaders", "crud:columnHeader");
            }
        }

        // paging over an unordered result is only stable when the database
        // is told what the order is, so there's always an order by
        var query = manager.getRestoreQuery().limit(limit).offset(offset);
        if (search != null) {
            applySearch(query, search);
        }
        if (sort != null) {
            query.orderBy(entity_.getColumnName(sort), descending ? Select.DESC : Select.ASC);
            // rows that tie on the sorted column would drift between pages,
            // so the identifier pins them
            if (!sort.equals(entity_.getIdentifier())) {
                query.orderBy(entity_.getIdentifierColumn());
            }
        } else if (entity_.isOrdered()) {
            // a restricted ordinal only orders within its own list, so the
            // lists themselves have to be kept apart first
            if (entity_.getOrdinalRestriction() != null) {
                query.orderBy(entity_.getOrdinalRestrictionColumn());
            }
            query.orderBy(entity_.getOrdinalColumn());
        } else {
            query.orderBy(entity_.getIdentifierColumn());
        }
        var instances = manager.restore(query);

        // an instance is moved between the neighbours that its row sits
        // between, which a page that searches or sorts doesn't show
        var reorderable = entity_.isOrdered() && null == search && null == sort;

        // the operations of a row carry the state of this page along, so
        // that finishing or cancelling one lands back on it
        var state = new LinkedHashMap<String, String[]>();
        if (search != null) {
            state.put("search", new String[]{search});
        }
        if (sort != null) {
            state.put("sort", new String[]{sort});
            if (descending) {
                state.put("dir", new String[]{"desc"});
            }
        }
        if (offset > 0) {
            state.put("offset", new String[]{String.valueOf(offset)});
        }

        // a confirmed action reaches its confirmation page through a GET
        // form, which replaces the query string of its action with its own
        // fields, so the state that has to survive rides along as those
        // fields instead of in the action's URL
        var actionState = new StringBuilder();
        for (var entry : state.entrySet()) {
            t.setValueEncoded("crud:stateName", entry.getKey());
            t.setValueEncoded("crud:stateValue", entry.getValue()[0]);
            actionState.append(t.getBlock("crud:stateInput"));
        }
        t.setValue("crud:actionState", actionState.toString());

        // the boundaries of every list on this page are read at once, since
        // asking for each of them separately is two queries for every list
        // that the page happens to show
        var lists = reorderable ? listBounds(instances) : new HashMap<Long, int[]>();
        for (var instance : instances) {
            var id = entity_.identifierValue(instance);

            t.blankValue("crud:cells");
            for (var property : entity_.getListedProperties()) {
                var value = entity_.formattedValue(instance, property);
                var renderer = entity_.getOptions().column(property);
                var cell = renderer == null ? null : renderer.render(instance, value);
                if (null == cell || CrudCell.Kind.TEXT == cell.kind()) {
                    t.setValueEncoded("crud:cellValue", cell == null ? value : cell.content());
                    t.appendBlock("crud:cells", "crud:cell");
                } else if (CrudCell.Kind.BADGE == cell.kind()) {
                    t.setValueEncoded("crud:cellValue", cell.content());
                    if (cell.style() != null) {
                        t.setValueEncoded("crud:badgeStyleName", cell.style());
                        t.setBlock("crud:badgeStyle", "crud:badgeStyle:present");
                    } else {
                        t.blankValue("crud:badgeStyle");
                    }
                    t.appendBlock("crud:cells", "crud:cell:badge");
                } else {
                    // markup renders as it is, encoding it is the renderer's
                    // responsibility
                    t.setValue("crud:cellHtml", cell.content());
                    t.appendBlock("crud:cells", "crud:cell:html");
                }
            }

            t.blankValue("crud:rowActions");
            t.setValueEncoded("crud:editUrl", c.urlFor(routes.edit).pathInfo(String.valueOf(id)).params(state));
            t.appendBlock("crud:rowActions", "crud:editAction");

            for (var action : entity_.getOptions().getActions()) {
                if (!action.isVisible(instance)) {
                    continue;
                }
                t.setValueEncoded("crud:actionUrl", c.urlFor(routes.actions.get(action.getName())).pathInfo(String.valueOf(id)).params(state));
                t.setValueEncoded("crud:actionLabel", action.getLabel());
                t.appendBlock("crud:rowActions", action.isConfirmed() ? "crud:customAction:confirm" : "crud:customAction");
            }

            if (entity_.getOptions().isDeletable(instance)) {
                t.setValueEncoded("crud:deleteUrl", c.urlFor(routes.delete).pathInfo(String.valueOf(id)).params(state));
                t.appendBlock("crud:rowActions", "crud:deleteAction");
            }

            t.blankValue("crud:moveActions");
            if (reorderable) {
                // an instance is only at the boundary of the instances that
                // it's actually moved between, which are the ones of its own
                // list, whose ordinals don't have to start at zero or run
                // without gaps
                var ordinal = entity_.ordinalValue(instance);
                var bounds = lists.getOrDefault(entity_.ordinalRestrictionValue(instance),
                    new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE});
                if (ordinal > bounds[0]) {
                    t.setValueEncoded("crud:moveUrl", c.urlFor(routes.move).pathInfo(id + "/up").params(state));
                    t.appendBlock("crud:moveActions", "crud:moveUp");
                } else {
                    t.appendBlock("crud:moveActions", "crud:moveUp:disabled");
                }
                if (ordinal < bounds[1]) {
                    t.setValueEncoded("crud:moveUrl", c.urlFor(routes.move).pathInfo(id + "/down").params(state));
                    t.appendBlock("crud:moveActions", "crud:moveDown");
                } else {
                    t.appendBlock("crud:moveActions", "crud:moveDown:disabled");
                }
                t.setBlock("crud:moveCell", "crud:moveCell:present");
            } else {
                t.blankValue("crud:moveCell");
            }

            t.appendBlock("crud:rows", "crud:row");
        }

        if (0 == count) {
            if (search != null) {
                // a search that matches nothing isn't an entity that has
                // nothing, so it keeps its search box and offers the way back
                t.setValueEncoded("crud:browseUrl", c.urlFor(routes.browse));
                t.setBlock(CONTENT_VALUE, "crud:noresults");
            } else {
                t.setBlock(CONTENT_VALUE, "crud:empty");
            }
        } else {
            if (count > limit) {
                // every page link sets an offset of its own
                var parameters = new LinkedHashMap<>(state);
                parameters.remove("offset");
                var navigation = c.template("crud.navigation");
                PagedNavigation.generate(c, navigation, count, limit, offset, 3, "offset", parameters);
                t.setValue("crud:navigation", navigation.getBlock("content"));
            }
            if (reorderable) {
                t.setBlock("crud:moveHeader", "crud:moveHeader:present");
            }
            t.setBlock(CONTENT_VALUE, "crud:browse");
        }

        c.print(t);
    }

    // restricts a query to the instances that a search matches: every
    // searchable column is matched without regard for case, and any of them
    // matching makes the instance match
    // the LIKE wildcards are escaped so that a term with a percent or an
    // underscore matches the typed text instead of everything, with an
    // escape character that no string literal treats specially on any of
    // the databases, so the usual value quoting keeps it apart from the SQL
    private void applySearch(AbstractWhereDelegateQuery<?, ?> query, String search) {
        // the escape character itself comes first, so that the ones that the
        // wildcards are escaped with aren't escaped again
        var escaped = search.toLowerCase(Locale.ROOT)
            .replace("!", "!!").replace("%", "!%").replace("_", "!_");
        var term = admin_.datasource().getSqlConversion().getSqlValue("%" + escaped + "%");
        var first = true;
        for (var property : entity_.getSearchableProperties()) {
            var clause = "LOWER(" + entity_.getColumnName(property) + ") LIKE " + term + " ESCAPE '!'";
            if (first) {
                query.where(clause);
                first = false;
            } else {
                query.whereOr(clause);
            }
        }
    }

    // reads the lowest and the highest ordinal of every list that the
    // instances of a page sit in, which decides whether an instance can
    // still be moved further up or down; an instance is only at the boundary
    // of its own list, whose ordinals don't have to start at zero or run
    // without gaps, and the entity's sequence reads the lists of a whole
    // page at once
    private Map<Long, int[]> listBounds(List<T> instances) {
        var restrictions = new LinkedHashSet<Long>();
        for (var instance : instances) {
            restrictions.add(entity_.ordinalRestrictionValue(instance));
        }
        return entity_.createOrdinalSequence(admin_.datasource()).bounds(restrictions);
    }

}
