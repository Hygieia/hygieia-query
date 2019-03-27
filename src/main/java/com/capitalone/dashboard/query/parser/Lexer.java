/*
 * Developed by Topo Pal  (c) 2019.
 *
 */

package com.capitalone.dashboard.query.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Lexer {

    private AtomicReference<StreamTokenizer> input;
    private AtomicInteger tokenNumber = new AtomicInteger();
    static final int EOL = -3;
    static final int EOF = -2;
    static final int INVALID = -1;
    static final int NONE = 0;
    static final int OR = 1;
    static final int AND = 2;
    static final int NOT = 3;
    static final int TRUE = 4;
    static final int FALSE = 5;
    static final int LEFT = 6;
    static final int RIGHT = 7;
    static final int CONDITION_WORD = 8;
    static final int CONDITION_NUMBER = 9;
    static final int QUOTE = 10;
    static final int SORT = 11;
    static final int COMMA = 12;
    static final int EQUAL = 13;
    static final int ISTRUE = 14;
    static final int ISFALSE = 15;
    static final int ISNOTNULL = 16;
    static final int ISNULL = 17;
    static final int LT = 18;
    static final int GT = 19;
    static final int PLUS = 20;
    static final int MINUS = 21;

    private static final String TRUE_LITERAL = "true";
    private static final String FALSE_LITERAL = "false";
    private static final String AND_LITERAL = "and";
    private static final String OR_LITERAL = "or";
    private static final String NOT_LITERAL = "not";
    private static final String SORT_LITERAL = "sort";
    private static final String ISTRUE_LITERAL = "isTrue";
    private static final String ISFALSE_LITERAL = "isFalse";
    private static final String ISNOTNULL_LITERAL = "isNotNull";
    private static final String ISNULL_LITERAL = "isNull";


    public Lexer(InputStream in) {
        Reader r = new BufferedReader(new InputStreamReader(in));
        input = new AtomicReference<>();
        input.set(new StreamTokenizer(r));
        input.get().resetSyntax();
        input.get().parseNumbers();
        input.get().wordChars('a', 'z');
        input.get().wordChars('A', 'Z');
        input.get().wordChars('_', '_');
        input.get().whitespaceChars('\u0000', '\u0000');
        input.get().whitespaceChars(' ', ' ');
        input.get().whitespaceChars('\t', '\t');
        input.get().whitespaceChars('\n', '\n');
        input.get().whitespaceChars('\r', '\r');
        input.get().whitespaceChars('\f', '\f');
        input.get().ordinaryChar('(');
        input.get().ordinaryChar(')');
        input.get().ordinaryChar('&');
        input.get().ordinaryChar('|');
        input.get().ordinaryChar('!');
        input.get().ordinaryChar(',');
        input.get().ordinaryChar('<');
        input.get().ordinaryChar('>');
        input.get().ordinaryChar('+');
        input.get().ordinaryChar('-');
        input.get().wordChars('$', '$');
        input.get().quoteChar('\'');
    }

    public int nextSymbol() {
        tokenNumber.incrementAndGet();
        int symbol = NONE;
        try {
            switch (input.get().nextToken()) {
                case StreamTokenizer.TT_EOL:
                    symbol = EOL;
                    break;
                case StreamTokenizer.TT_EOF:
                    symbol = EOF;
                    break;
                case StreamTokenizer.TT_WORD: {
                    if (input.get().sval.equalsIgnoreCase(TRUE_LITERAL)) {
                        symbol = TRUE;
                    } else if (input.get().sval.equalsIgnoreCase(FALSE_LITERAL)) {
                        symbol = FALSE;
                    } else if (input.get().sval.equalsIgnoreCase(AND_LITERAL)) {
                        symbol = AND;
                    } else if (input.get().sval.equalsIgnoreCase(OR_LITERAL)) {
                        symbol = OR;
                    } else if (input.get().sval.equalsIgnoreCase(NOT_LITERAL)) {
                        symbol = NOT;
                    } else if (input.get().sval.equalsIgnoreCase(SORT_LITERAL)) {
                        symbol = SORT;
                    } else if (input.get().sval.equalsIgnoreCase(ISFALSE_LITERAL)) {
                        symbol = ISFALSE;
                    } else if (input.get().sval.equalsIgnoreCase(ISTRUE_LITERAL)) {
                        symbol = ISTRUE;
                    } else if (input.get().sval.equalsIgnoreCase(ISNOTNULL_LITERAL)) {
                        symbol = ISNOTNULL;
                    } else if (input.get().sval.equalsIgnoreCase(ISNULL_LITERAL)) {
                        symbol = ISNULL;
                    } else {
                        symbol = CONDITION_WORD;
                    }
                    break;
                }
                case StreamTokenizer.TT_NUMBER:
                    symbol = CONDITION_NUMBER;
                    break;

                case '(':
                    symbol = LEFT;
                    break;
                case ')':
                    symbol = RIGHT;
                    break;
                case '&':
                    symbol = AND;
                    break;
                case '|':
                    symbol = OR;
                    break;
                case '!':
                    symbol = NOT;
                    break;
                case '\'':
                    symbol = QUOTE;
                    break;
                case ',':
                    symbol = COMMA;
                    break;
                case '=':
                    symbol = EQUAL;
                    break;
                case '<':
                    symbol = LT;
                    break;
                case '>':
                    symbol = GT;
                    break;
                case '+':
                    symbol = PLUS;
                    break;
                case '-':
                    symbol = MINUS;
                    break;
                default:
                    symbol = INVALID;
                    break;
            }
        } catch (IOException e) {
            symbol = EOF;
        }
        return symbol;
    }

    StreamTokenizer getInput() {
        return input.get();
    }

    int getTokenNumber() {return tokenNumber.get();}
}