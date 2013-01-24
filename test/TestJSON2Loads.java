import org.junit.Test;

import static org.junit.Assert.*;

public class TestJSON2Loads {
    
    @Test
    public void testNullInput() {
        try {
            JSON2.loads(null);
            fail("null input should raise exception");
        } catch (IllegalArgumentException e) {}
    }
    
    @Test
    public void testEmptyInput() {
        try {
            JSON2.loads("");
            fail();
        } catch (JSON2ParseException e) {}
        
        try {
            JSON2.loads("   \n\r  \t ");
            fail();
        } catch (JSON2ParseException e) {}
    }
    
    @Test
    public void testNumberGood() {
        long longval = (long) JSON2.loads("0");
        assertEquals(0L, longval);
        
        longval = (long) JSON2.loads("10");
        assertEquals(10L, longval);
        
        longval = (long) JSON2.loads("9999999999");
        assertEquals(9999999999L, longval);
        
        final double delta = 1e-5;
        double val = (double) JSON2.loads("0.128");
        assertEquals(0.128, val, delta);
        
        val = (double) JSON2.loads("123.456e-2");
        assertEquals(123.456e-2, val, delta);
        
        val = (double) JSON2.loads("-0.789e+11");
        assertEquals(-0.789e+11, val, delta);
        
    }
    
    @Test
    public void testNumberBad() {
        try {
            JSON2.loads("01");
            fail("number cannot start with 0");
        } catch (JSON2ParseException e) {}
        
        try {
            JSON2.loads("+10");
            fail("number cannot start with plus");
        } catch (JSON2ParseException e) {}
        
        try {
            JSON2.loads(".123");
            fail("number cannot start with perid");
        } catch (JSON2ParseException e) {}
    }
    
    @Test
    public void testStringGood() {
        String s1 = (String) JSON2.loads("\"any string\"");
        assertEquals("any string", s1);
        
        s1 = (String) JSON2.loads("\"\"");
        assertEquals("", s1);
        
        s1 = (String) JSON2.loads("\"  \"");
        assertEquals("  ", s1);
        
        s1 = (String) JSON2.loads("\"防火墙sucks\"");
        assertEquals("防火墙sucks", s1);
        
        // escape
        s1 = (String) JSON2.loads("\" \\\" \\\\ \\/ \\b \\f \\n \\r \\t \"");
        assertEquals(" \" \\ / \b \f \n \r \t ", s1);
        s1 = (String) JSON2.loads("\"\\u6211\\u7231\\u4f60\"");
        assertEquals("我爱你", s1);
        s1 = (String) JSON2.loads("\"\\u4e2D \\u56FD\"");
        assertEquals("中 国", s1);
        
    }
    
    @Test
    public void testStringBad() {
        try {
            JSON2.loads("'hello'");
            fail("string literal should use double quotes");
        } catch (JSON2ParseException e) {}
        
        try {
            JSON2.loads("\"missing");
            fail();
        } catch (JSON2ParseException e) {}
        
        try {
            JSON2.loads("\" \\' \"");
            fail("unsupported escape character");
        } catch (JSON2ParseException e) {}
        
        try {
            JSON2.loads("\"\\ua02h\"");
            fail("\\u should be followed by four hex digits");
        } catch (JSON2ParseException e) {}
    }
    
    
}
