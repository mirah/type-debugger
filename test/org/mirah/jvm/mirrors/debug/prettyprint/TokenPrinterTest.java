/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mirah.jvm.mirrors.debug.prettyprint;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ribrdb
 */
public class TokenPrinterTest extends MirahSourceGenerator {

    public TokenPrinterTest() {
    }

    @Before
    public void setUp() {
        out.setLimit(10);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testToString() {
        addAll("a", "b", "c");
        assertEquals("abc", toString());
    }

    @Test
    public void testToString2() {
        addAll("a", NBR, "b", NBR, "c", NBR);
        assertEquals("abc", toString());
    }

    @Test
    public void testNewline() {
        addAll("a", NL, "b", NL, "c");
        assertEquals("a\nb\nc", toString());
    }

    @Test
    public void testSimpleWrap() {
        addAll("abcdefg", NBR, "hijkl");
        assertEquals("abcdefg\n    hijkl", toString());
    }

    @Test
    public void testWrapping2() {
        addAll("a", NL, "abcdefg", NBR, "hijkl", NL, "xyz");
        assertEquals(""
                + "a\n"
                + "abcdefg\n"
                + "    hijkl\n"
                + "xyz", toString());
    }

    @Test
    public void testIndent() {
        addAll("if a", INDENT, "b", DEDENT, "else", INDENT, "c", DEDENT, "end");
        assertEquals(""
                + "if a\n"
                + "  b\n"
                + "else\n"
                + "  c\n"
                + "end", toString());
    }

    @Test
    public void testContinueInsideIndent() {
        addAll("begin", INDENT, "abcdefg =", NBR, "12345", NBR, "6789", NL, "foo", DEDENT, "end");
        assertEquals(""
                + "begin\n"
                + "  abcdefg =\n"
                + "      12345\n"
                + "      6789\n"
                + "  foo\n"
                + "end", toString());
    }

    @Test
    public void testContinuedIndent() {
        addAll("abcdefg = ", NBR, "if a", INDENT, "b", DEDENT, "else", INDENT, "c", DEDENT, "end");
        assertEquals(""
                + "abcdefg = \n"
                + "    if a\n"
                + "      b\n"
                + "    else\n"
                + "      c\n"
                + "    end", toString());
    }

    @Test
    public void testContinueInsideContinuedIndent() {
        addAll("abcdefg = ", NBR, "if a", INDENT, "b = ", NBR, "12345", DEDENT, "else", INDENT, "c", DEDENT, "end");
        assertEquals(""
                + "abcdefg = \n"
                + "    if a\n"
                + "      b = \n"
                + "          12345\n"
                + "    else\n"
                + "      c\n"
                + "    end", toString());
    }

    @Test
    public void testList() {
        out.setLimit(20);
        addAll("a = [", LIST, "1, ", LI, "2, ", LI, "3", LI, "]", LIST_END);
        assertEquals("a = [1, 2, 3]", toString());
    }

    @Test
    public void testWrappedList() {
        addAll("a = [", LIST, "1, ", LI, "2, ", LI, "3", LI, "]", LIST_END);
        assertEquals("a = [\n    1, 2, \n    3]", toString());
    }

    @Test
    public void testWrappedList2() {
        addAll("a = [", LIST, "1, ", LI, "2 ", NBR, "+ 3, ", LI, "3", LI, "]", LIST_END);
        assertEquals(""
                + "a = [\n"
                + "    1, \n"
                + "    2 \n"
                + "        + 3, \n"
                + "    3]", toString());
    }

    @Test
    public void testNewlineInList() {
        out.setLimit(30);
        addAll("[", LIST, "1, ", LI, "begin", INDENT, "foo", DEDENT, "end]", LIST_END);
        assertEquals(""
                + "[\n"
                + "    1, \n"
                + "    begin\n"
                + "      foo\n"
                + "    end]", toString());
    }

    @Test
    public void testSetLimit() {
        out.setLimit(3);
        addAll("a", NBR, "b", NBR, "c", NBR, "d", NBR, "e", NBR);
        assertEquals("abc\n    d\n    e", toString());
    }

    @Test
    public void testListWrap3() {
        out.setLimit(80);
        addAll(
                NL, "class CallCompiler < BaseCompiler ", LIST,
                "implements ", "MemberVisitor", LIST_END, INDENT);
        assertEquals(""
                + "\n"
                + "class CallCompiler < BaseCompiler implements MemberVisitor\n",
                toString());
    }

    @Test
    public void testSoftParen() {
        addAll(SLP, "a", SRP);
        assertEquals("a", toString());
    }

    @Test
    public void testWrappedSoftParen() {
        addAll(SLP, "foo", NL, "bar", SRP);
        assertEquals(""
                + "(\n"
                + "    foo\n"
                + "    bar)", toString());
    }
}
