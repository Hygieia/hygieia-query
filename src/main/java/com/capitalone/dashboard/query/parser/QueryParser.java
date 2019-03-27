/*
 * Developed by Topo Pal  (c) 2019.
 *
 */

package com.capitalone.dashboard.query.parser;

import com.capitalone.dashboard.query.model.QueryException;
import com.querydsl.core.types.dsl.BooleanExpression;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class QueryParser {
    private AtomicReference<Lexer> lexer = new AtomicReference<>();
    private AtomicInteger symbol = new AtomicInteger();
    private AtomicReference<Class<?>> rootClass = new AtomicReference<>();
    private AtomicReference<AST<BooleanExpression>> root = new AtomicReference<>();
    private static final String IN = "in";
    private static final String ANYOF = "anyOf";
    private static final String ALLOF = "allOf";
    private static final String ALL = "all";


    public QueryParser(Lexer lexer, Class<?> rootClass) {
        this.rootClass.set(rootClass);
        this.lexer.set(lexer);
    }

    public AST<BooleanExpression> build() throws QueryException {
        expression();
        return root.get();
    }

    private void expression() throws QueryException {
        term();
        while (symbol.get() == Lexer.OR) {
            Or or = new Or();
            or.setLeft(root.get());
            term();
            or.setRight(root.get());
            root.set(or);
        }
    }

    private void term() throws QueryException {
        factor();
        while (symbol.get() == Lexer.AND) {
            And and = new And();
            and.setLeft(root.get());
            factor();
            and.setRight(root.get());
            root.set(and);
        }
    }

    private void factor() throws QueryException {
        symbol.set(lexer.get().nextSymbol());
        if (symbol.get() == Lexer.NOT) {
            Not not = new Not();
            factor();
            not.setChild(root.get());
            root.set(not);
        } else if (symbol.get() == Lexer.LEFT) {
            expression();
            symbol.set(lexer.get().nextSymbol()); // we don't care about ')'
        } else if (symbol.get() == Lexer.CONDITION_WORD) {
            condition();
            symbol.set(lexer.get().nextSymbol());
        } else {
            throw new QueryException("Query expression malformed at " + lexer.get().getTokenNumber(), QueryException.INVALID_QUERY_START);
        }
    }

    private void condition() throws QueryException {
        String key = lexer.get().getInput().sval;
        symbol.set(lexer.get().nextSymbol());
        if (IntStream.of(Lexer.CONDITION_WORD, Lexer.ISTRUE, Lexer.ISFALSE, Lexer.ISNOTNULL, Lexer.ISNULL, Lexer.LT, Lexer.GT, Lexer.EQUAL).allMatch(i1 -> symbol.get() != i1)) {
            throw new QueryException("Query expression malformed at " + lexer.get().getTokenNumber(), QueryException.INVALID_OPERATOR);
        }
        String op0 = lexer.get().getInput().sval;
        if (op0==null) {
            if (symbol.get() == Lexer.LT) {
                op0 = "lt";
            } else if (symbol.get() == Lexer.GT) {
                op0 = "gt";
            } else if (symbol.get() == Lexer.EQUAL) {
                op0 = "eq";
            }
        }
        String op = op0;
        if (Stream.of(IN, ALL, ALLOF, ANYOF).anyMatch(s -> s.equalsIgnoreCase(op))) {
            group(key, op);
        } else if (IntStream.of(Lexer.ISFALSE, Lexer.ISTRUE, Lexer.ISNOTNULL, Lexer.ISNULL).anyMatch(i1 -> symbol.get() == i1)) {
            root.set(new OneExpression(rootClass.get(), key, op, null));
        } else {
            symbol.set(lexer.get().nextSymbol());
            if (IntStream.of(Lexer.CONDITION_WORD, Lexer.TRUE, Lexer.FALSE, Lexer.ISNULL, Lexer.ISNOTNULL, Lexer.QUOTE, Lexer.CONDITION_NUMBER).allMatch(i -> symbol.get() != i)) {
                throw new QueryException("Query expression malformed at " + lexer.get().getTokenNumber(), QueryException.INVALID_VALUE);
            }
            root.set(new OneExpression(rootClass.get(), key, op, getVal()));
        }
    }

    private void group(String key, String op) throws QueryException {
        symbol.set(lexer.get().nextSymbol());
        if (symbol.get() != Lexer.LEFT) {
            throw new QueryException("Query expression malformed at " + lexer.get().getInput().lineno() + ". IN must start with (", QueryException.INVALID_VALUE);
        }
        symbol.set(lexer.get().nextSymbol());
        List<Object> values = new LinkedList<>();
        while (symbol.get() != Lexer.RIGHT) {
            if (IntStream.of(Lexer.CONDITION_NUMBER, Lexer.QUOTE, Lexer.CONDITION_WORD, Lexer.COMMA).allMatch(i -> symbol.get() != i)) {
                throw new QueryException("Query expression malformed at " + lexer.get().getTokenNumber(), QueryException.INVALID_VALUE);
            }
            Object val = getVal();
            if (val != null) {
                values.add(val);
            }
            symbol.set(lexer.get().nextSymbol());
        }
        root.set(new OneExpression(rootClass.get(), key, op, values));
    }

    private Object getVal() {
        if (symbol.get() != Lexer.COMMA) {
            Object val = lexer.get().getInput().sval != null ? lexer.get().getInput().sval : lexer.get().getInput().nval;
            if (symbol.get() == Lexer.TRUE || symbol.get() == Lexer.FALSE) {
                return Boolean.parseBoolean(String.valueOf(val));
            }
            return val instanceof Double ? ((Double) val % 1) == 0 ? Long.valueOf(((Double) val).longValue()) : val : val;
        }
        return null;
    }
}