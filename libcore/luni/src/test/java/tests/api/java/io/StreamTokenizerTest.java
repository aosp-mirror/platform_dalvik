/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tests.api.java.io;

import junit.framework.Assert;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringBufferInputStream;
import java.io.StringReader;

import tests.support.Support_StringReader;
// TODO: most of the assertTrue calls in this test case should be
// replaced with assertEquals (possibly two assertEquals) see
// test_ConstructorLjava_io_InputStream for example.
// This gives much more helpful error messages.

public class StreamTokenizerTest extends junit.framework.TestCase {
	Support_StringReader r;

	StreamTokenizer st;

	String testString;

	/**
	 * @tests java.io.StreamTokenizer#StreamTokenizer(java.io.InputStream)
	 */
	public void test_ConstructorLjava_io_InputStream() throws IOException {
		st = new StreamTokenizer(new StringBufferInputStream(
				"/comments\n d 8 'h'"));

		assertEquals("the next token returned should be the letter d",
			     StreamTokenizer.TT_WORD, st.nextToken());
		assertEquals("the next token returned should be the letter d",
			     "d", st.sval);

		assertEquals("the next token returned should be the digit 8",
			     StreamTokenizer.TT_NUMBER, st.nextToken());
		assertEquals("the next token returned should be the digit 8",
			     8.0, st.nval);

		assertEquals("the next token returned should be the quote character",
			     39, st.nextToken());
		assertEquals("the next token returned should be the quote character",
			     "h", st.sval);
	}

	/**
	 * @tests java.io.StreamTokenizer#StreamTokenizer(java.io.Reader)
	 */
	public void test_ConstructorLjava_io_Reader() throws IOException {
		setTest("/testing\n d 8 'h' ");
		assertEquals("the next token returned should be the letter d skipping the comments",
			     StreamTokenizer.TT_WORD, st.nextToken());
		assertEquals("the next token returned should be the letter d",
			     "d", st.sval);

		assertEquals("the next token returned should be the digit 8",
			     StreamTokenizer.TT_NUMBER, st.nextToken());
		assertEquals("the next token returned should be the digit 8",
			     8.0, st.nval);

		assertEquals("the next token returned should be the quote character",
			     39, st.nextToken());
		assertEquals("the next token returned should be the quote character",
			     "h", st.sval);
	}

	/**
	 * @tests java.io.StreamTokenizer#commentChar(int)
	 */
	public void test_commentCharI() throws IOException {
		setTest("*comment \n / 8 'h' ");
		st.ordinaryChar('/');
		st.commentChar('*');
		assertEquals("nextToken() did not return the character / skiping the comments starting with *",
			     47, st.nextToken());
		assertTrue("the next token returned should be the digit 8", st
			   .nextToken() == StreamTokenizer.TT_NUMBER
			   && st.nval == 8.0);
		assertTrue("the next token returned should be the quote character",
			   st.nextToken() == 39 && st.sval.equals("h"));
	}

	/**
	 * @tests java.io.StreamTokenizer#eolIsSignificant(boolean)
	 */
	public void test_eolIsSignificantZ() throws IOException {
		setTest("d 8\n");
		// by default end of line characters are not significant
		assertTrue("nextToken did not return d",
			   st.nextToken() == StreamTokenizer.TT_WORD
			   && st.sval.equals("d"));
		assertTrue("nextToken did not return 8",
			   st.nextToken() == StreamTokenizer.TT_NUMBER
			   && st.nval == 8.0);
		assertTrue("nextToken should be the end of file",
			   st.nextToken() == StreamTokenizer.TT_EOF);
		setTest("d\n");
		st.eolIsSignificant(true);
		// end of line characters are significant
		assertTrue("nextToken did not return d",
			   st.nextToken() == StreamTokenizer.TT_WORD
			   && st.sval.equals("d"));
		assertTrue("nextToken is the end of line",
			   st.nextToken() == StreamTokenizer.TT_EOL);
	}

	/**
	 * @tests java.io.StreamTokenizer#lineno()
	 */
	public void test_lineno() throws IOException {
		setTest("d\n 8\n");
		assertEquals("the lineno should be 1", 1, st.lineno());
		st.nextToken();
		st.nextToken();
		assertEquals("the lineno should be 2", 2, st.lineno());
		st.nextToken();
		assertEquals("the next line no should be 3", 3, st.lineno());
	}

	/**
	 * @tests java.io.StreamTokenizer#lowerCaseMode(boolean)
	 */
	public void test_lowerCaseModeZ() throws Exception {
		// SM.
		setTest("HELLOWORLD");
		st.lowerCaseMode(true);

		st.nextToken();
		assertEquals("sval not converted to lowercase.", "helloworld", st.sval
			     );
	}

	/**
	 * @tests java.io.StreamTokenizer#nextToken()
	 */
	public void test_nextToken() throws IOException {
		// SM.
		setTest("\r\n/* fje fje 43.4 f \r\n f g */  456.459 \r\n"
				+ "Hello  / 	\r\n \r\n \n \r \257 Hi \'Hello World\'");
		st.ordinaryChar('/');
		st.slashStarComments(true);
		st.nextToken();
		assertTrue("Wrong Token type1: " + (char) st.ttype,
			   st.ttype == StreamTokenizer.TT_NUMBER);
		st.nextToken();
		assertTrue("Wrong Token type2: " + st.ttype,
			   st.ttype == StreamTokenizer.TT_WORD);
		st.nextToken();
		assertTrue("Wrong Token type3: " + st.ttype, st.ttype == '/');
		st.nextToken();
		assertTrue("Wrong Token type4: " + st.ttype,
			   st.ttype == StreamTokenizer.TT_WORD);
		st.nextToken();
		assertTrue("Wrong Token type5: " + st.ttype,
			   st.ttype == StreamTokenizer.TT_WORD);
		st.nextToken();
		assertTrue("Wrong Token type6: " + st.ttype, st.ttype == '\'');
		assertTrue("Wrong Token type7: " + st.ttype, st.sval
			   .equals("Hello World"));
		st.nextToken();
		assertTrue("Wrong Token type8: " + st.ttype, st.ttype == -1);

		final PipedInputStream pin = new PipedInputStream();
		PipedOutputStream pout = new PipedOutputStream(pin);
		pout.write("hello\n\r\r".getBytes());
		StreamTokenizer s = new StreamTokenizer(pin);
		s.eolIsSignificant(true);
		assertTrue("Wrong token 1,1",
			   s.nextToken() == StreamTokenizer.TT_WORD
			   && s.sval.equals("hello"));
		assertTrue("Wrong token 1,2", s.nextToken() == '\n');
		assertTrue("Wrong token 1,3", s.nextToken() == '\n');
		assertTrue("Wrong token 1,4", s.nextToken() == '\n');
		pout.close();
		assertTrue("Wrong token 1,5",
			   s.nextToken() == StreamTokenizer.TT_EOF);
		StreamTokenizer tokenizer = new StreamTokenizer(
								new Support_StringReader("\n \r\n#"));
		tokenizer.ordinaryChar('\n'); // make \n ordinary
		tokenizer.eolIsSignificant(true);
		assertTrue("Wrong token 2,1", tokenizer.nextToken() == '\n');
		assertTrue("Wrong token 2,2", tokenizer.nextToken() == '\n');
		assertEquals("Wrong token 2,3", '#', tokenizer.nextToken());
	}

	/**
	 * @tests java.io.StreamTokenizer#ordinaryChar(int)
	 */
	public void test_ordinaryCharI() throws IOException {
		// SM.
		setTest("Ffjein 893");
		st.ordinaryChar('F');
		st.nextToken();
		assertTrue("OrdinaryChar failed." + (char) st.ttype,
			   st.ttype == 'F');
	}

	/**
	 * @tests java.io.StreamTokenizer#ordinaryChars(int, int)
	 */
	public void test_ordinaryCharsII() throws IOException {
		// SM.
		setTest("azbc iof z 893");
		st.ordinaryChars('a', 'z');
		assertEquals("OrdinaryChars failed.", 'a', st.nextToken());
		assertEquals("OrdinaryChars failed.", 'z', st.nextToken());
	}

	/**
	 * @tests java.io.StreamTokenizer#parseNumbers()
	 */
	public void test_parseNumbers() throws IOException {
		// SM
		setTest("9.9 678");
		assertTrue("Base behavior failed.",
			   st.nextToken() == StreamTokenizer.TT_NUMBER);
		st.ordinaryChars('0', '9');
		assertEquals("setOrdinary failed.", '6', st.nextToken());
		st.parseNumbers();
		assertTrue("parseNumbers failed.",
			   st.nextToken() == StreamTokenizer.TT_NUMBER);
	}

	/**
	 * @tests java.io.StreamTokenizer#pushBack()
	 */
	public void test_pushBack() throws IOException {
		// SM.
		setTest("Hello 897");
		st.nextToken();
		st.pushBack();
		assertTrue("PushBack failed.",
			   st.nextToken() == StreamTokenizer.TT_WORD);
	}

	/**
	 * @tests java.io.StreamTokenizer#quoteChar(int)
	 */
	public void test_quoteCharI() throws IOException {
		// SM
		setTest("<Hello World<    HelloWorldH");
		st.quoteChar('<');
		assertEquals("QuoteChar failed.", '<', st.nextToken());
		assertEquals("QuoteChar failed.", "Hello World", st.sval);
		st.quoteChar('H');
		st.nextToken();
		assertEquals("QuoteChar failed for word.", "elloWorld", st.sval
			     );
	}

	/**
	 * @tests java.io.StreamTokenizer#resetSyntax()
	 */
	public void test_resetSyntax() throws IOException {
		// SM
		setTest("H 9\' ello World");
		st.resetSyntax();
		assertTrue("resetSyntax failed1." + (char) st.ttype,
			   st.nextToken() == 'H');
		assertTrue("resetSyntax failed1." + (char) st.ttype,
			   st.nextToken() == ' ');
		assertTrue("resetSyntax failed2." + (char) st.ttype,
			   st.nextToken() == '9');
		assertTrue("resetSyntax failed3." + (char) st.ttype,
			   st.nextToken() == '\'');
	}

	/**
	 * @tests java.io.StreamTokenizer#slashSlashComments(boolean)
	 */
	public void test_slashSlashCommentsZ() throws IOException {
		// SM.
		setTest("// foo \r\n /fiji \r\n -456");
		st.ordinaryChar('/');
		st.slashSlashComments(true);
		assertEquals("Test failed.", '/', st.nextToken());
		assertTrue("Test failed.",
			   st.nextToken() == StreamTokenizer.TT_WORD);
	}
    
    /**
     * @tests java.io.StreamTokenizer#slashSlashComments(boolean)
     */
    public void test_slashSlashComments_withSSOpen() throws IOException {
        Reader reader = new CharArrayReader( "t // t t t".toCharArray());

        StreamTokenizer st = new StreamTokenizer(reader);
        st.slashSlashComments(true);

        assertEquals(StreamTokenizer.TT_WORD,st.nextToken());
        assertEquals(StreamTokenizer.TT_EOF,st.nextToken());
    }

    /**
     * @tests java.io.StreamTokenizer#slashSlashComments(boolean)
     */
    public void test_slashSlashComments_withSSOpen_NoComment() throws IOException {
        Reader reader = new CharArrayReader( "// t".toCharArray());

        StreamTokenizer st = new StreamTokenizer(reader);
        st.slashSlashComments(true);
        st.ordinaryChar('/');

        assertEquals(StreamTokenizer.TT_EOF,st.nextToken());
    }
    
    /**
     * @tests java.io.StreamTokenizer#slashSlashComments(boolean)
     */
    public void test_slashSlashComments_withSSClosed() throws IOException {
        Reader reader = new CharArrayReader( "// t".toCharArray());

        StreamTokenizer st = new StreamTokenizer(reader);
        st.slashSlashComments(false);
        st.ordinaryChar('/');

        assertEquals('/',st.nextToken());
        assertEquals('/',st.nextToken());
        assertEquals(StreamTokenizer.TT_WORD,st.nextToken());
    }
    
	/**
	 * @tests java.io.StreamTokenizer#slashStarComments(boolean)
	 */
	public void test_slashStarCommentsZ() throws IOException {
		setTest("/* foo \r\n /fiji \r\n*/ -456");
		st.ordinaryChar('/');
		st.slashStarComments(true);
		assertTrue("Test failed.",
			   st.nextToken() == StreamTokenizer.TT_NUMBER);
	}

    /**
     * @tests java.io.StreamTokenizer#slashStarComments(boolean)
     */
    public void test_slashStarComments_withSTOpen() throws IOException {
        Reader reader = new CharArrayReader( "t /* t */ t".toCharArray());

        StreamTokenizer st = new StreamTokenizer(reader);
        st.slashStarComments(true);

        assertEquals(StreamTokenizer.TT_WORD,st.nextToken());
        assertEquals(StreamTokenizer.TT_WORD,st.nextToken());
        assertEquals(StreamTokenizer.TT_EOF,st.nextToken());
    }

    /**
     * @tests java.io.StreamTokenizer#slashStarComments(boolean)
     */
    public void test_slashStarComments_withSTClosed() throws IOException {
        Reader reader = new CharArrayReader( "t /* t */ t".toCharArray());

        StreamTokenizer st = new StreamTokenizer(reader);
        st.slashStarComments(false);

        assertEquals(StreamTokenizer.TT_WORD,st.nextToken());
        assertEquals(StreamTokenizer.TT_EOF,st.nextToken());
    }
    
	/**
	 * @tests java.io.StreamTokenizer#toString()
	 */
	public void test_toString() throws IOException {
		setTest("ABC Hello World");
		st.nextToken();
		assertTrue("toString failed." + st.toString(),
			   st.toString().equals(
						"Token[ABC], line 1"));
	}

	/**
	 * @tests java.io.StreamTokenizer#whitespaceChars(int, int)
	 */
	public void test_whitespaceCharsII() throws IOException {
		setTest("azbc iof z 893");
		st.whitespaceChars('a', 'z');
		assertTrue("OrdinaryChar failed.",
			   st.nextToken() == StreamTokenizer.TT_NUMBER);
	}

	/**
	 * @tests java.io.StreamTokenizer#wordChars(int, int)
	 */
	public void test_wordCharsII() throws IOException {
		setTest("A893 -9B87");
		st.wordChars('0', '9');
		assertTrue("WordChar failed1.",
			   st.nextToken() == StreamTokenizer.TT_WORD);
		assertEquals("WordChar failed2.", "A893", st.sval);
		assertTrue("WordChar failed3.",
			   st.nextToken() == StreamTokenizer.TT_NUMBER);
		st.nextToken();
		assertEquals("WordChar failed4.", "B87", st.sval);
		
		setTest("    Hello World");
		st.wordChars(' ', ' ');
		st.nextToken();
		assertEquals("WordChars failed for whitespace.", "Hello World", st.sval
			     );
		
		setTest("    Hello World\r\n  \'Hello World\' Hello\' World");
		st.wordChars(' ', ' ');
		st.wordChars('\'', '\'');
		st.nextToken();
		assertTrue("WordChars failed for whitespace: " + st.sval, st.sval
			   .equals("Hello World"));
		st.nextToken();
		assertTrue("WordChars failed for quote1: " + st.sval, st.sval
			   .equals("\'Hello World\' Hello\' World"));
	}

	private void setTest(String s) {
		testString = s;
		r = new Support_StringReader(testString);
		st = new StreamTokenizer(r);
	}

	protected void setUp() {
	}

	protected void tearDown() {
	}
	
    public void test_basicStringTokenizerMethods()
    {
        String str = "Testing 12345 \n alpha \r\n omega";
        String strb = "-3.8 'BLIND mice' \r sEe /* how */ they run";
        StringReader aa = new StringReader(str);
        StringReader ba = new StringReader(strb);
        StreamTokenizer a = new StreamTokenizer(aa);
        StreamTokenizer b = new StreamTokenizer(ba);

        try {
            Assert.assertTrue(a.lineno() == 1);
            Assert.assertTrue(a.nextToken() == StreamTokenizer.TT_WORD);
            Assert.assertTrue(a.toString().equals("Token[Testing], line 1"));
            Assert.assertTrue(a.nextToken() == StreamTokenizer.TT_NUMBER);
            Assert.assertTrue(a.toString().equals("Token[n=12345.0], line 1"));
            Assert.assertTrue(a.nextToken() == StreamTokenizer.TT_WORD);
            Assert.assertTrue(a.toString().equals("Token[alpha], line 2"));
            Assert.assertTrue(a.nextToken() == StreamTokenizer.TT_WORD);
            Assert.assertTrue(a.toString().equals("Token[omega], line 3"));
            Assert.assertTrue(a.nextToken() == StreamTokenizer.TT_EOF);
            Assert.assertTrue(a.toString().equals("Token[EOF], line 3"));

            b.commentChar('u');
            b.eolIsSignificant(true);
            b.lowerCaseMode(true);
            b.ordinaryChar('y');
            b.slashStarComments(true);
            
            Assert.assertTrue(b.nextToken() == StreamTokenizer.TT_NUMBER);
            Assert.assertTrue(b.nval == -3.8);
            Assert.assertTrue(b.toString().equals("Token[n=-3.8], line 1"));
            Assert.assertTrue(b.nextToken() == 39); // '
            Assert.assertTrue(b.toString().equals("Token[BLIND mice], line 1"));
            Assert.assertTrue(b.nextToken() == 10); // \n
            Assert.assertTrue(b.toString().equals("Token[EOL], line 2"));
            Assert.assertTrue(b.nextToken() == StreamTokenizer.TT_WORD);
            Assert.assertTrue(b.toString().equals("Token[see], line 2"));
            Assert.assertTrue(b.nextToken() == StreamTokenizer.TT_WORD);
            Assert.assertTrue(b.toString().equals("Token[the], line 2"));
            Assert.assertTrue(b.nextToken() == 121); // y
            Assert.assertTrue(b.toString().equals("Token['y'], line 2"));
            Assert.assertTrue(b.nextToken() == StreamTokenizer.TT_WORD);
            Assert.assertTrue(b.toString().equals("Token[r], line 2"));
            Assert.assertTrue(b.nextToken() == StreamTokenizer.TT_EOF);
            Assert.assertTrue(b.toString().equals("Token[EOF], line 2"));
        }
        catch (Exception ex){
            System.out.println("Exception found in StreamTokenizer");
            ex.printStackTrace();
            throw new RuntimeException("error in test, see stdout");
        }
    }
    
    public void test_harmonyRegressionTest() {
        byte[] data = new byte[] {(byte) '-'};
        StreamTokenizer tokenizer = new StreamTokenizer(new ByteArrayInputStream(data));
        try {
            tokenizer.nextToken();
        } catch(Exception e) {
            Assert.fail(e.getMessage());
        }
        String result = tokenizer.toString();  
        Assert.assertEquals("Token['-'], line 1", result);
    }
    
    public void test_harmonyRegressionTest2() {
        byte[] data = new byte[] {(byte) '"',
                                  (byte) 'H',
                                  (byte) 'e',
                                  (byte) 'l',
                                  (byte) 'l',
                                  (byte) 'o',
                                  (byte) '"'};
        StreamTokenizer tokenizer = new StreamTokenizer(new ByteArrayInputStream(data));
        try {
            tokenizer.nextToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String result = tokenizer.toString();
        Assert.assertEquals("Token[Hello], line 1", result);
    }
}
