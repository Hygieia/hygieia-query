/*
 * Developed by Topo Pal  (c) 2019.
 *
 */

package com.capitalone.dashboard.query.builder;

import com.capitalone.dashboard.query.model.FieldSelection;
import com.capitalone.dashboard.query.model.QueryException;
import com.capitalone.dashboard.query.model.QueryResponse;
import com.capitalone.dashboard.query.parser.AST;
import com.capitalone.dashboard.query.parser.FieldSelectionParser;
import com.capitalone.dashboard.query.parser.Lexer;
import com.capitalone.dashboard.query.parser.PageParser;
import com.capitalone.dashboard.query.parser.QueryParser;
import com.capitalone.dashboard.query.parser.SortParser;
import com.google.common.collect.Lists;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.List;

public class QueryBuilder<T> {
    private static final String SORT = "sort";
    private static final String PAGE = "page";


    /**
     * Syntax: query with optional sorting and paging specifications, delimited by semicolon
     * See ??? for query syntax
     * @param query
     * @param rootClass
     * @return
     * @throws QueryException
     */
    public static BooleanBuilder buildQuery(String query, Class<?> rootClass) throws QueryException {
        String[] parts = query.split(";");
        return buildQuery0(parts[0], rootClass);
    }


    /**
     * Syntax: proper query only, without sorting and paging specifications
     * See ??? for query syntax
     * @param query
     * @param rootClass
     * @return
     * @throws QueryException
     */

    public static BooleanBuilder buildQuery0(String query, Class<?> rootClass) throws QueryException {
        BooleanBuilder builder = new BooleanBuilder();
        if (query==null || query.isEmpty()) {
            return builder;
        }
        Lexer lexer = new Lexer(new ByteArrayInputStream(query.getBytes()));
        QueryParser queryParser = new QueryParser(lexer, rootClass);
        AST<BooleanExpression> ast = queryParser.build();
        BooleanExpression expression = ast.evaluate();
        return builder.and(expression);
    }

    /**
     * Syntax: part of query-sort-page specification delimited by semicolon.
     * The sort part of the syntax: "SORT [BY] field [ASC|DESC] { , field [ASC|DESC] } *"
     * When no part starting with "SORT" (case insensitive), null is returned
     *
     * @param query
     * @return
     * @throws QueryException
     */
    public static Sort buildSort(String query) throws QueryException {
        String[] parts = query.split(";");
        int bound = parts.length;
        for (int i = 1; i < bound; i++) {
            if (parts[i].trim().toLowerCase().startsWith(SORT)) {
                Lexer lexer = new Lexer(new ByteArrayInputStream(parts[i].getBytes()));
                SortParser sortParser = new SortParser(lexer, false);
                return sortParser.build();
            }
        }
        return null;
    }

    /**
     * Syntax: "[+|-] field { , [+|-] field } *"
     * @param sort
     * @return
     * @throws QueryException
     */
    public static Sort buildSort0(String sort) throws QueryException {
        if (sort==null || sort.isEmpty()) return null;
        Lexer lexer = new Lexer(new ByteArrayInputStream(sort.getBytes()));
        SortParser sortParser = new SortParser(lexer, true);
        return sortParser.build();
    }

    /**
     * Syntax: part of query-sort-page specification delimited by semicolon.
     * The syntax for the paging part only: "PAGE [=] page_number [ [, ] PAGESIZE [=] page_size ]"
     * The default page number is 0, and the default page size is 25.
     * When no part starting with "PAGE" (case insensitive), null is returned
     *
     * @param query
     * @return
     * @throws QueryException
     */
    public static PageRequest buildPage(String query) throws QueryException {
        String[] parts = query.split(";");
        int bound = parts.length;
        for (int i = 1; i < bound; i++) {
            if (parts[i].trim().toLowerCase().startsWith(PAGE)) {
                Lexer lexer = new Lexer(new ByteArrayInputStream(parts[i].getBytes()));
                PageParser pageParser = new PageParser(lexer);
                return pageParser.build();
            }
        }
        return null;
    }

    /**
     *
     * @param pageNumber when it is negative, no pagination is used
     * @param pageSize when it is 0, the default page size (25) is used; when it is negative, no pagination is used
     * @return
     * @throws QueryException
     */
    public static PageRequest buildPage0(int pageNumber, int pageSize) throws QueryException {
        if (pageSize<0 || pageNumber<0) return null;
        return PageRequest.of(pageNumber, pageSize==0?PageParser.DEFAULT_PAGESIZE:pageSize);
    }

    public static FieldSelection buildFieldSelection(String query, Class<?> rootClass) throws QueryException {
        String include = null;
        String exclude = null;
        String[] parts = query.split(";");
        int bound = parts.length;
        for (int i = 1; i < bound; i++) {
            if (parts[i].trim().toLowerCase().startsWith(FieldSelectionParser.INCLUDE)) {
                include = parts[i];
            } else if (parts[i].trim().toLowerCase().startsWith(FieldSelectionParser.EXCLUDE)) {
                exclude = parts[i];
            }
        }
        if (!StringUtils.isEmpty(include) && (!StringUtils.isEmpty(exclude))) {
            throw new QueryException("Can't have both included and excluded list of fields", QueryException.INCLUDE_EXCLUDE_NOT_ALLOWED);
        }
        return FieldSelectionParser.parse(include, exclude, rootClass);
    }

    public QueryResponse<T> buildResponse(Page<T> items) {
        QueryResponse<T> response = new QueryResponse<>();
        if (!IterableUtils.isEmpty(items)) {
            List<T> values = Lists.newArrayList(items);
            response.setTotal(items.getTotalElements());
            response.setCount(items.getNumberOfElements());
            response.setTotalPages(items.getTotalPages());
            response.setValues(values);
        }
        return response;
    }


}
