/*
 * Developed by Topo Pal  (c) 2019.
 *
 */

package com.capitalone.dashboard.query.parser;

import com.capitalone.dashboard.query.model.QueryException;
import org.junit.Test;
import org.springframework.data.domain.Sort;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;

public class SortParserTest {

    @Test
    public void buildValid() throws QueryException {
        String s = "sort by description asc";
        Lexer lexer = new Lexer(new ByteArrayInputStream(s.getBytes()));
        SortParser sortParser = new SortParser(lexer, false);
        Sort actual = sortParser.build();
        Sort expected = new Sort(Sort.Direction.ASC, "description");
        assertEquals(actual, expected);
    }


    @Test(expected = QueryException.class)
    public void buildInvalid() throws QueryException {
        String s = "some sort thing";
        Lexer lexer = new Lexer(new ByteArrayInputStream(s.getBytes()));
        SortParser sortParser = new SortParser(lexer, false);
        Sort sort = sortParser.build();
    }

    @Test(expected = QueryException.class)
    public void buildInvalidDirection() throws QueryException {
        String s = "sort by description up";
        Lexer lexer = new Lexer(new ByteArrayInputStream(s.getBytes()));
        SortParser sortParser = new SortParser(lexer, false);
        Sort sort = sortParser.build();
    }

    @Test
    public void buildValidMultipleNoDirection() throws QueryException {
        String s = "sort by description, name";
        Lexer lexer = new Lexer(new ByteArrayInputStream(s.getBytes()));
        SortParser sortParser = new SortParser(lexer, false);
        Sort actual = sortParser.build();
        Sort expected = new Sort(Sort.Direction.ASC, "description").and(new Sort(Sort.Direction.ASC, "name"));
        assertEquals(actual, expected);
    }

    @Test
    public void buildValidMultipleWithDirection() throws QueryException {
        String s = "sort by description desc, name asc";
        Lexer lexer = new Lexer(new ByteArrayInputStream(s.getBytes()));
        SortParser sortParser = new SortParser(lexer, false);
        Sort actual = sortParser.build();
        Sort expected = new Sort(Sort.Direction.DESC, "description").and(new Sort(Sort.Direction.ASC, "name"));
        assertEquals(actual, expected);
    }

    @Test
    public void buildValidMultipleWithDirectionWithoutBy() throws QueryException {
        String s = "sort description desc, name asc";
        Lexer lexer = new Lexer(new ByteArrayInputStream(s.getBytes()));
        SortParser sortParser = new SortParser(lexer, false);
        Sort actual = sortParser.build();
        Sort expected = new Sort(Sort.Direction.DESC, "description").and(new Sort(Sort.Direction.ASC, "name"));
        assertEquals(actual, expected);
    }

    @Test
    public void buildValidMultipleWithDirectionWithoutByEndWithSemicolon() throws QueryException {
        String s = "sort description desc, name asc;";
        Lexer lexer = new Lexer(new ByteArrayInputStream(s.getBytes()));
        SortParser sortParser = new SortParser(lexer, false);
        Sort actual = sortParser.build();
        Sort expected = new Sort(Sort.Direction.DESC, "description").and(new Sort(Sort.Direction.ASC, "name"));
        assertEquals(actual, expected);
    }

    @Test
    public void buildValidFourThingsWithDirectionWithoutByEndWithSemicolon() throws QueryException {
        String s = "sort description desc, name asc, title, address";
        Lexer lexer = new Lexer(new ByteArrayInputStream(s.getBytes()));
        SortParser sortParser = new SortParser(lexer, false);
        Sort actual = sortParser.build();
        Sort expected = new Sort(Sort.Direction.DESC, "description")
                .and(new Sort(Sort.Direction.ASC, "name"))
                .and(new Sort(Sort.Direction.ASC, "title"))
                .and(new Sort(Sort.Direction.ASC, "address"));
        assertEquals(actual, expected);
    }

    @Test
    public void buildValidDuplicateProp() throws QueryException {
        String s = "sort description desc, description asc";
        Lexer lexer = new Lexer(new ByteArrayInputStream(s.getBytes()));
        SortParser sortParser = new SortParser(lexer, false);
        Sort actual = sortParser.build();
        Sort expected = new Sort(Sort.Direction.DESC, "description")
                .and(new Sort(Sort.Direction.ASC, "description"));
        assertEquals(actual, expected);
    }

    @Test
    public void buildValidWithDesc() throws QueryException {
        String s = "sort desc desc";
        Lexer lexer = new Lexer(new ByteArrayInputStream(s.getBytes()));
        SortParser sortParser = new SortParser(lexer, false);
        Sort actual = sortParser.build();
        Sort expected = new Sort(Sort.Direction.DESC, "desc");
        assertEquals(actual, expected);
    }

    @Test
    public void buildValidWithDesc1Desc() throws QueryException {
        String s = "sort desc1 desc, desc desc";
        Lexer lexer = new Lexer(new ByteArrayInputStream(s.getBytes()));
        SortParser sortParser = new SortParser(lexer, false);
        Sort actual = sortParser.build();
        Sort expected = new Sort(Sort.Direction.DESC, "desc1").and(new Sort(Sort.Direction.DESC, "desc"));
        assertEquals(actual, expected);
    }

    @Test(expected = QueryException.class)
    public void buildBadStart() throws QueryException {
        String s = " desc1 desc, desc desc";
        Lexer lexer = new Lexer(new ByteArrayInputStream(s.getBytes()));
        SortParser sortParser = new SortParser(lexer, false);
        try {
            Sort actual = sortParser.build();
        } catch (QueryException e) {
            assertEquals("Query expression malformed at 1. Must start with SORT", e.getMessage());
            throw e;
        }
    }

    @Test(expected = QueryException.class)
    public void buildBadFormat() throws QueryException {
        String s = " sort  , =";
        Lexer lexer = new Lexer(new ByteArrayInputStream(s.getBytes()));
        SortParser sortParser = new SortParser(lexer, false);
        try {
            Sort actual = sortParser.build();
        } catch (QueryException e) {
            assertEquals("Query expression malformed at 1. Must be a alpha-numeric string", e.getMessage());
            throw e;
        }
    }

    @Test(expected = QueryException.class)
    public void buildBadFormat2() throws QueryException {
        String s = " sort  .  .";
        Lexer lexer = new Lexer(new ByteArrayInputStream(s.getBytes()));
        SortParser sortParser = new SortParser(lexer, false);
        try {
            Sort actual = sortParser.build();
        } catch (QueryException e) {
            assertEquals("Query expression malformed at 1. Must be a alpha-numeric string", e.getMessage());
            throw e;
        }
    }

    @Test
    public void buildCompactSimple() throws QueryException {
        String s = "description";
        Lexer lexer = new Lexer(new ByteArrayInputStream(s.getBytes()));
        SortParser sortParser = new SortParser(lexer, true);
        Sort actual = sortParser.build();
        Sort expected = new Sort(Sort.Direction.ASC, "description");
        assertEquals(actual, expected);
    }

    @Test
    public void buildCompactAscent() throws QueryException {
        String s = "+ description";
        Lexer lexer = new Lexer(new ByteArrayInputStream(s.getBytes()));
        SortParser sortParser = new SortParser(lexer, true);
        Sort actual = sortParser.build();
        Sort expected = new Sort(Sort.Direction.ASC, "description");
        assertEquals(actual, expected);
    }

    @Test
    public void buildCompactDescent() throws QueryException {
        String s = "-description";
        Lexer lexer = new Lexer(new ByteArrayInputStream(s.getBytes()));
        SortParser sortParser = new SortParser(lexer, true);
        Sort actual = sortParser.build();
        Sort expected = new Sort(Sort.Direction.DESC, "description");
        assertEquals(actual, expected);
    }

    @Test
    public void buildCompactdMultiple() throws QueryException {
        String s = "-description, name";
        Lexer lexer = new Lexer(new ByteArrayInputStream(s.getBytes()));
        SortParser sortParser = new SortParser(lexer, true);
        Sort actual = sortParser.build();
        Sort expected = new Sort(Sort.Direction.DESC, "description").and(new Sort(Sort.Direction.ASC, "name"));
        assertEquals(actual, expected);
    }

    @Test(expected = QueryException.class)
    public void buildCompactdBad() throws QueryException {
        String s = "description DESC, name";
        Lexer lexer = new Lexer(new ByteArrayInputStream(s.getBytes()));
        SortParser sortParser = new SortParser(lexer, true);
        Sort actual = sortParser.build();
        Sort expected = new Sort(Sort.Direction.DESC, "description").and(new Sort(Sort.Direction.ASC, "name"));
        assertEquals(actual, expected);
    }

}
