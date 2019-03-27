package com.capitalone.dashboard.query.builder;

import com.capitalone.dashboard.query.model.FieldSelection;
import com.capitalone.dashboard.query.model.QueryException;
import com.capitalone.dashboard.query.model.QueryResponse;
import com.capitalone.dashboard.query.model.SomeTestClass;
import com.google.common.collect.Lists;
import com.querydsl.core.BooleanBuilder;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class QueryBuilderTest {

    @Test(expected = QueryException.class)
    public void buildQuerySimpleBadField() throws QueryException {
        try {
            BooleanBuilder actual = QueryBuilder.buildQuery("field eq 'Field1'", SomeTestClass.class);
        } catch (QueryException qe) {
            assertEquals("Field 'field' not found in class 'com.capitalone.dashboard.query.model.SomeTestClass'", qe.getMessage());
            throw qe;
        }
    }

    @Test
    public void buildQueryEmpty() throws QueryException {
        BooleanBuilder actual = QueryBuilder.buildQuery("", SomeTestClass.class);
        assertNull(actual.getValue());
    }

    @Test
    public void buildQuerySimple1() throws QueryException {
        BooleanBuilder actual = QueryBuilder.buildQuery("field1 eq 'Field1'", SomeTestClass.class);
        assertEquals("com.capitalone.dashboard.query.model.SomeTestClass.field1 = Field1", actual.toString());
    }

    @Test
    public void buildQuerySimple2() throws QueryException {
        BooleanBuilder actual = QueryBuilder.buildQuery("field1 eq 'Field1' and field2 gt 0", SomeTestClass.class);
        assertEquals("com.capitalone.dashboard.query.model.SomeTestClass.field1 = Field1 && com.capitalone.dashboard.query.model.SomeTestClass.field2 > 0", actual.toString());
    }

    @Test
    public void buildQuerySimple3() throws QueryException {
        BooleanBuilder actual = QueryBuilder.buildQuery("field_1 equalsIgnoreCase 'Field1' and field2 gt 0", SomeTestClass.class);
        assertEquals("eqIc(com.capitalone.dashboard.query.model.SomeTestClass.field_1,Field1) && com.capitalone.dashboard.query.model.SomeTestClass.field2 > 0", actual.toString());
    }

    @Test
    public void buildQuerySimple4() throws QueryException {
        BooleanBuilder actual = QueryBuilder.buildQuery("field1 < 5 and field2 = 0", SomeTestClass.class);
        assertEquals("com.capitalone.dashboard.query.model.SomeTestClass.field1 < 5 && com.capitalone.dashboard.query.model.SomeTestClass.field2 = 0", actual.toString());
    }

    @Test
    public void buildSortASC() throws QueryException {
        Sort actual = QueryBuilder.buildSort("field1 eq 'Field1';sort field1 ASC");
        assertNotNull(actual);
        assertEquals("field1: ASC", actual.toString());
    }


    @Test
    public void buildSortDESC() throws QueryException {
        Sort actual = QueryBuilder.buildSort("field1 eq 'Field1';sort field1 DESC");
        assertNotNull(actual);
        assertEquals("field1: DESC", actual.toString());
    }

    @Test
    public void buildSortDefaultDirection() throws QueryException {
        Sort actual = QueryBuilder.buildSort("field1 eq 'Field1';sort field1");
        assertNotNull(actual);
        assertEquals("field1: ASC", actual.toString());
    }

    @Test
    public void buildSortMultiField() throws QueryException {
        Sort actual = QueryBuilder.buildSort("field1 eq 'Field1';sort field1, field2 DESC");
        assertNotNull(actual);
        assertEquals("field1: ASC,field2: DESC", actual.toString());
    }

    @Test
    public void buildSortNull() throws QueryException {
        Sort actual = QueryBuilder.buildSort("field1 eq 'Field1'");
        assertNull(actual);
    }

    @Test
    public void buildPage() throws QueryException {
        PageRequest actual = QueryBuilder.buildPage("field1 eq 'Field1';page=1, pageSize=30");
        assertNotNull(actual);
        assertEquals(1, actual.getPageNumber());
        assertEquals(30, actual.getPageSize());
        assertTrue(actual.getSort().isUnsorted());
    }

    @Test
    public void buildPageNull() throws QueryException {
        PageRequest actual = QueryBuilder.buildPage("");
        assertNull(actual);
    }

    @Test
    public void buildQueryBadSort() throws QueryException {
        Sort actual = QueryBuilder.buildSort("field1 eq 'Field1';something  DESC");
        assertNull(actual);
    }

    @Test
    public void buildFieldSelectionInclude() throws QueryException {
        FieldSelection fieldSelection = QueryBuilder.buildFieldSelection("field1, field2;include field1", SomeTestClass.class);
        assertNotNull(fieldSelection);
        assertEquals("include", fieldSelection.getType());
        assertEquals(1, fieldSelection.getSelections().size());
        assertEquals("field1", fieldSelection.getSelections().iterator().next().getName());
    }

    @Test
    public void buildFieldSelectionExclude() throws QueryException {
        FieldSelection fieldSelection = QueryBuilder.buildFieldSelection("field1, field2;exclude field1", SomeTestClass.class);
        assertNotNull(fieldSelection);
        assertEquals("exclude", fieldSelection.getType());
        assertEquals(1, fieldSelection.getSelections().size());
        assertEquals("field1", fieldSelection.getSelections().iterator().next().getName());
    }

    @Test(expected = QueryException.class)
    public void buildFieldSelectionBoth() throws QueryException {
        try {
        FieldSelection fieldSelection = QueryBuilder.buildFieldSelection("field1, field2;exclude field1;include field2", SomeTestClass.class);
        } catch (QueryException qe) {
            assertEquals("Can't have both included and excluded list of fields", qe.getMessage());
            throw qe;
        }
    }


    @Test
    public void buildResponseOneItem() {
        Page<SomeTestClass> items = new PageImpl<>(Lists.newArrayList(new SomeTestClass()));
        QueryBuilder<SomeTestClass> builder = new QueryBuilder<>();
        QueryResponse<SomeTestClass> response = builder.buildResponse(items);
        assertNotNull(response);
        assertEquals(1, response.getCount());
        assertEquals(1, response.getTotal());
        assertEquals(1, response.getTotalPages());
        assertNotNull(response.getValues());
        assertEquals(1, response.getValues().size());
    }

    @Test
    public void buildResponseManyItems() {
        List<SomeTestClass> items = IntStream.range(0, 100).mapToObj(i -> new SomeTestClass()).collect(Collectors.toList());
        Page<SomeTestClass> pages = new PageImpl<>(items);
        QueryBuilder<SomeTestClass> builder = new QueryBuilder<>();
        QueryResponse<SomeTestClass> response = builder.buildResponse(pages);
        assertNotNull(response);
        assertEquals(100, response.getCount());
        assertEquals(100, response.getTotal());
        assertEquals(1, response.getTotalPages());
        assertNotNull(response.getValues());
        assertEquals(100, response.getValues().size());
    }
}