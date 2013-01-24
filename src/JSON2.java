import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LL1 Parser for JSON data format
 * @author gaodayue
 *
 */
public class JSON2 {
    private String _input;
    private char _ch;
    private int _pos = -1;
    
    /** current token type and token text */
    private int _tokenType;
    private String _tokenText;
    
    /******************************************************
     * Lexical analysis code BEGIN 
     ******************************************************/
    static final char EOF = (char) -1;
    static final int T_INVALID  = 0;
    static final int T_LBRACKET = 1;
    static final int T_RBRACKET = 2;
    static final int T_LBRACE   = 3;
    static final int T_RBRACE   = 4;
    static final int T_COMMA    = 5;
    static final int T_COLON    = 6;
    static final int T_STRING   = 7;
    static final int T_NUMBER   = 8;
    static final int T_TRUE     = 9;
    static final int T_FALSE    = 10;
    static final int T_NULL     = 11;
    static final int T_EOF      = 12;
    static final String[] TOKEN_NAMES = {
            "<invlid>",
            "'['", "']'", "'{'", "'}'", "','", "':'", "STRING", "NUMBER", "TRUE", "FALSE", "NULL", "<EOF>"
        };
    
    /** read next char */
    private char readnext() {
        _pos++;
        _ch = (_pos < _input.length()) ? _input.charAt(_pos) : EOF;
        return _ch;
    }
    
    private void addToBuffer(StringBuilder buf) {
        buf.append(_ch);
        readnext();
    }
    
    /** if current character is x, read next; otherwise raise Exception */
    private void consume(char x) {
        if (_ch == x) readnext();
        else throw new JSON2ParseException("expecting '" + x + "' found " + _ch); 
    }
    
    private void consume(String s) {
        for (char x : s.toCharArray()) consume(x);
    }
    
    /** read the next token, return the token type */
    private int readToken() {
        // skip whitespaces
        while (Character.isWhitespace(_ch)) {
            readnext();
        }
        switch (_ch) {
        case EOF: _tokenType = T_EOF; break;
        case '[': _tokenType = T_LBRACKET;  readnext(); break;
        case ']': _tokenType = T_RBRACKET;  readnext(); break;
        case '{': _tokenType = T_LBRACE;    readnext(); break;
        case '}': _tokenType = T_RBRACE;    readnext(); break;
        case ',': _tokenType = T_COMMA;     readnext(); break;
        case ':': _tokenType = T_COLON;     readnext(); break;
        case 't': _tokenType = T_TRUE;      readTrue();  break;
        case 'f': _tokenType = T_FALSE;     readFalse(); break;
        case 'n': _tokenType = T_NULL;      readNull();  break;
        case '"':
            _tokenType = T_STRING;
            _tokenText = readStr();
            break;
        default:
            if (_ch == '-' || isDigit(_ch)) {
                _tokenType = T_NUMBER;
                _tokenText = readNum();
            } else
                throw new JSON2ParseException("unknown token '" + _ch + "' at " + _pos);
        }
        return _tokenType;
    }
    
    private String readStr() {
        StringBuilder buf = new StringBuilder();
        this.consume('"');
        while (_ch != '"' && _ch != EOF) {
            if (_ch == '\\') {  // handle escape characters
                switch (readnext()) {
                case '"' : buf.append('\"'); break;
                case '\\': buf.append('\\'); break;
                case '/' : buf.append('/');  break;
                case 'b' : buf.append('\b'); break;
                case 'f' : buf.append('\f'); break;
                case 'n' : buf.append('\n'); break;
                case 'r' : buf.append('\r'); break;
                case 't' : buf.append('\t'); break;
                case 'u' : // read four hex digit
                    StringBuilder hexes = new StringBuilder();
                    for (int i=0; i < 4; i++) {
                        char c = readnext();
                        if ((c >= '0' && c <= '9') ||
                            (c >= 'a' && c <= 'f') ||
                            (c >= 'A' && c <= 'F'))
                            hexes.append(c);
                        else
                            throw new JSON2ParseException("invalid hex number after \\u: " + c);
                    }
                    buf.append((char)Integer.parseInt(hexes.toString(), 16));
                    break;
                default:
                    throw new JSON2ParseException("invalid character after \\escape: " + _ch);
                }
            } else {
                buf.append(_ch);
            }
            readnext();
        }
        this.consume('"');
        return buf.toString();
    }
    
    private String readNum() {
        StringBuilder buf = new StringBuilder();
        // INT part
        if (_ch == '-') this.addToBuffer(buf);
        char firstdigit = _ch;
        if (isDigit(_ch)) this.addToBuffer(buf);
        else throw new JSON2ParseException("invalid number, no digit after '-' at " + _pos);
        if (firstdigit != '0') {
            while (isDigit(_ch)) this.addToBuffer(buf);
        }
        // FRACTION part
        if (_ch == '.') {
            this.addToBuffer(buf);
            if (isDigit(_ch)) this.addToBuffer(buf);
            else throw new JSON2ParseException("invalid number, no digit after '.' at " + _pos);
            while (isDigit(_ch)) this.addToBuffer(buf);
        }
        // EXPONENTIAL part
        if (_ch == 'e' || _ch == 'E') {
            this.addToBuffer(buf);
            if (_ch == '+' || _ch == '-') this.addToBuffer(buf);
            if (isDigit(_ch)) this.addToBuffer(buf);
            else throw new JSON2ParseException("invalid number, no exponential number at " + _pos);
            while (isDigit(_ch)) this.addToBuffer(buf);
        }
        return buf.toString();
    }
    
    private void readTrue() {
        try {
            this.consume("true");
        } catch (Exception e) {
            throw new JSON2ParseException("error reading <true> token\n", e);
        }
    }
    
    private void readFalse() {
        try {
            this.consume("false");
        } catch (Exception e) {
            throw new JSON2ParseException("error reading <false> token\n", e);
        }
    }
    
    private void readNull() {
        try {
            this.consume("null");
        } catch (Exception e) {
            throw new JSON2ParseException("error reading <null> token\n", e);
        }
    }
    
    private boolean isDigit(char x) {
        return x >= '0' && x <= '9';
    }
    
    /******************************************************
     * Lexical analysis code END 
     ******************************************************/
    
    /******************************************************
     * Syntax analysis code BEGIN 
     ******************************************************/
    private void matchToken(int type) {
        if (_tokenType == type) this.readToken();
        else throw new JSON2ParseException("expecting token " + TOKEN_NAMES[type] + ", found " + TOKEN_NAMES[_tokenType]);
    }
    
    private Object _value() {
        switch (_tokenType) {
        case T_LBRACKET:    return _array();
        case T_LBRACE:      return _object();
        case T_STRING:      return _string();
        case T_NUMBER:      return _number();
        case T_TRUE:    readToken(); return true;
        case T_FALSE:   readToken(); return false;
        case T_NULL:    readToken(); return null;
        default:
            throw new JSON2ParseException("invalid json value, invalid token " + TOKEN_NAMES[_tokenType] + " near " + _pos);
        }
    }
    
    /** object: '{' '}' | '{' members '}' ;*/
    private Object _object() {
        Map<String, Object> obj = new HashMap<String, Object>();
        matchToken(T_LBRACE);
        if (_tokenType != T_RBRACE) {
            obj = _members();
        }
        matchToken(T_RBRACE);
        return obj;
    }
    
    /** members: string ':' value (',' string ':' value)* ;*/
    private Map<String, Object> _members() {
        Map<String, Object> obj = new HashMap<String, Object>();
        String key = _string();
        matchToken(T_COLON);
        obj.put(key, _value());
        while (_tokenType == T_COMMA) {
            readToken();
            key = _string();
            matchToken(T_COLON);
            obj.put(key, _value());
        }
        return obj;
    }
    
    /** array: '[' ']' | '[' elements ']' ;*/
    private Object _array() {
        List<Object> arr = new ArrayList<Object>();
        matchToken(T_LBRACKET);
        if (_tokenType != T_RBRACKET) {
            arr = _elements();
        }
        matchToken(T_RBRACKET);
        return arr;
    }
    
    /** elements: value (',' value)* ;*/
    private List<Object> _elements() {
        List<Object> arr = new ArrayList<Object>();
        arr.add(_value());
        while (_tokenType == T_COMMA) {
            readToken();
            arr.add(_value());
        }
        return arr;
    }
    
    private String _string() {
        String s = _tokenText;
        readToken();
        return s;
    }
    
    private Object _number() {
        String s = _tokenText;
        readToken();
        if (s.indexOf('.') >= 0 || s.indexOf('e') >= 0 ||
            s.indexOf('E') >= 0)
            return Double.parseDouble(s);
        else
            return Long.parseLong(s);
    }
    /******************************************************
     * Syntax analysis code END 
     ******************************************************/
    
    private JSON2(String input) {
        _input = input;
        readnext();
        readToken();
    }
    
    public static Object loads(String json) {
        if (json == null)
            throw new IllegalArgumentException("input cannot be null");
        JSON2 parser = new JSON2(json);
        Object obj = parser._value();
        if (parser._tokenType != T_EOF)
            throw new JSON2ParseException("extra data");
        return obj;
    }
}



class JSON2ParseException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public JSON2ParseException(String msg) { super(msg); }
    public JSON2ParseException(String msg, Throwable cause) { super(msg, cause); }
}