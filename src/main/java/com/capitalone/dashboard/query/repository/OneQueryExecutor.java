/*
 * Developed by Topo Pal  (c) 2019.
 *
 */

package com.capitalone.dashboard.query.repository;

import com.capitalone.dashboard.query.builder.QueryBuilder;
import com.capitalone.dashboard.query.model.QueryException;
import com.capitalone.dashboard.query.parser.PageParser;
import com.google.common.reflect.TypeToken;
import com.querydsl.core.BooleanBuilder;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.List;


@NoRepositoryBean
public interface OneQueryExecutor<T, V extends Serializable> extends PagingAndSortingRepository<T, V>, QuerydslPredicateExecutor<T> {

    default long count(String query) throws QueryException {
        TypeToken<T> genericType = new TypeToken<T>(this.getClass()) {};
        BooleanBuilder booleanBuilder = QueryBuilder.buildQuery(query, genericType.getRawType());
        return count(booleanBuilder.getValue());
    }

    /**
     * This is the main query exposed by the package.
     *
     * @param query    Syntax: documented in README.md in the project root directory (done by Topo?)
     * @param sortSpec Syntax: "[+|-] field { , [+|-] field } *", where "-" means descending, "+" or no sign means ascending
     *                 When it is null, entries are not sorted (internal order)
     * @param pageNumber when it is negative, return all entries (no pagination)
     * @param pageSize when it is negative or pageNumber is negative, return all entries (no pagination); when it is 0,
     *                 use the default page size (25)
     * @param fields Syntax: "field { , field } *"
     *                 When it is null, all fields are returned
     * @return entries
     * @throws QueryException
     */
    default Iterable<T> find(String query, String sortSpec, int pageNumber, int pageSize, String[] fields) throws QueryException {
        TypeToken<T> genericType = new TypeToken<T>(this.getClass()) {};
        BooleanBuilder booleanBuilder = QueryBuilder.buildQuery0(query, genericType.getRawType());
        Sort sort = QueryBuilder.buildSort0(sortSpec);
        PageRequest page = QueryBuilder.buildPage0(pageNumber, pageSize);

        if (booleanBuilder == null || booleanBuilder.getValue() == null) {
            return IterableUtils.emptyIterable();
        }

        // sort and page are null
        if (sort == null && page == null) {
            return findAll(booleanBuilder.getValue());
        }

        // sort null and page not null
        if (sort == null) {
            return findAll(booleanBuilder.getValue(), page);
        } else if (page == null) {
            // sort not null and page is null
            return findAll(booleanBuilder.getValue(), sort);
        }

        // booleanBuilder, sort, page all are not null
        PageRequest newPageRequest = PageRequest.of(page.getPageNumber(), page.getPageSize(), sort);

        return findAll(booleanBuilder.getValue(), newPageRequest);
    }

    /**
     * This is an alternative form of the above method, where query, sortSpec, pageNumber, pageSize and fields are
     * specified as a single string, delimited by ";". The query part is required and must be the first entry.
     * Other parts are optional, detected by the first keyword described below:
     *
     * The sortSpec syntax (note this is different from above): "SORT [BY] field [ASC|DESC] { , field [ASC|DESC] } *"
     *
     * The pageNumber and pageSize syntax: "PAGE [=] pageNumber [ [,] PAGESIZE [=] pageSize ]
     *      If PAGESIZE is missing, the default page size of 25 is used
     *
     * fields are not yet implemented
     *
     * @param queryAndSortPageFields
     * @return
     * @throws QueryException
     */
    default Iterable<T> find(String queryAndSortPageFields) throws QueryException {
        TypeToken<T> genericType = new TypeToken<T>(this.getClass()) {};
        BooleanBuilder booleanBuilder = QueryBuilder.buildQuery(queryAndSortPageFields, genericType.getRawType());
        Sort sort = QueryBuilder.buildSort(queryAndSortPageFields);
        PageRequest page = QueryBuilder.buildPage(queryAndSortPageFields);

        if (booleanBuilder == null || booleanBuilder.getValue() == null) {
            return IterableUtils.emptyIterable();
        }

        // sort and page are null
        if (sort == null && page == null) {
            return findAll(booleanBuilder.getValue());
        }

        // sort null and page not null
        if (sort == null) {
            return findAll(booleanBuilder.getValue(), page);
        } else if (page == null) {
            // sort not null and page is null
            return findAll(booleanBuilder.getValue(), sort);
        }

        // booleanBuilder, sort, page all are not null
        PageRequest newPageRequest = PageRequest.of(page.getPageNumber(), page.getPageSize(), sort);

        return findAll(booleanBuilder.getValue(), newPageRequest);
    }




    default Page<T> findWithDefaultPaging(String query) throws QueryException {
        return (QueryBuilder.buildPage(query) == null) ? (Page<T>) find(page(query, 1, PageParser.DEFAULT_PAGESIZE)) : (Page<T>) find(query);
    }

    /**
     * Wrapper to mimic older version of findOne from querydsl.
     * @param query
     * @return
     * @throws QueryException
     */
    default T findOne(String query) throws QueryException {
        Iterable<T> items = find(query);
        if (IterableUtils.isEmpty(items)) {
            return null;
        } else {
            return IterableUtils.toList(items).get(0);
        }
    }

    /**
     * Helper method to 'and' parts of queries
     * @param query
     * @return modified query string
     */
    default String and(String query) {
        return StringUtils.isEmpty(query) ? "" : query + " and ";
    }

    /**
     * Helper method to 'or' parts of queries
     * @param query
     * @return modified query string
     */
    default String or(String query) {
        return StringUtils.isEmpty(query) ? "" : query + " or ";
    }

    /**
     * Helper method to quote a field in the query
     * @param s
     * @return
     */
    default String quote(String s) {
        return '\'' + s + '\'';
    }

    /**
     * Helper method to quote a field in the query
     * @param o
     * @return
     */
    default String quote(Object o) {
        return '\'' + o.toString() + '\'';
    }

    /**
     * Helper method to format 'in' fields in the query
     * @param list
     * @return Modified query string
     */
    default String in(List<String> list) {
        return String.format("('%s')", String.join("','", list));
    }

    /**
     * Helper method to add paging to the query
     * @param query
     * @param pageable
     * @return modified query
     */
    default String page(String query, Pageable pageable) {
        return page(query, pageable.getPageNumber(), pageable.getPageSize());
    }

    /**
     * Helper method to add paging to the query
     * @param query
     * @param number
     * @param size
     * @return
     */
    default String page(String query, int number, int size) {
        return query + ';' + "page=" + number + ", pageSize=" + size;
    }
}
