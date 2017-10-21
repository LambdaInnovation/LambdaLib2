package cn.lambdalib2.render;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.out;

public class ShaderScript {

    public enum PropertyType {
        Float, Vec2, Vec3, Vec4, PassData
    }

    public static class Property {
        public String name;
        public PropertyType type;
        public Object value;
    }

    public static ShaderScript load(String content) {
        return ShaderScriptParser.load(content);
    }

    public final List<Property> uniformProperties = new ArrayList<>();
    public final List<Property> instanceProperties = new ArrayList<>();
    public int drawOrder;

    public RenderStates renderStates = new RenderStates();

    public String vertexSource;
    public String fragmentSource;

    ShaderScript() {}

}

final class ShaderScriptParser {

    static final String
            regexID = "[a-zA-Z][0-9a-zA-Z\\-_]*",
            regexSpace = "(\\p{Space}|[\\r\\n])+",
            regexLeftBracket = "\\{",
            regexRightBracket = "\\}";

    static Token tknID = new Token("ID", regexID),
            tknSpace = new Token("SPACE", regexSpace),
            tknLeftBracket = new Token("LEFT_BRACKET", regexLeftBracket),
            tknRightBracket = new Token("RIGHT_BRACKET", regexRightBracket),
            tknInt = new Token("INT", "(\\+|\\-)?[0-9]+"),
            tknFloat = new Token("FLOAT", "[+\\-]?[0-9]+\\.[0-9]*([eE][0-9]+)?"),
            tknSemi = new Token("SEMI", ";");

    public static ShaderScript load(String content) {
        Lexer mainLexer = new Lexer(content,
                tknID, tknSpace, tknLeftBracket, tknRightBracket);

        ShaderScript script = new ShaderScript();

        while (true) {
            MatchedToken t = nextTokenSkipSpaces(mainLexer);
            if (t == null) break;

            if (t.token == tknID) {
                assertToken(mainLexer, nextTokenSkipSpaces(mainLexer), tknLeftBracket);
                int sectionEndPos = parseSection(mainLexer);

                String s = mainLexer.content.substring(mainLexer.currentIndex, sectionEndPos);
                switch (t.content) {
                    case "Properties": {
                        parseProperties(script, s, mainLexer.lineNumber);
                    } break;
                    case "Settings": {
                        parseSettings(script, s, mainLexer.lineNumber);
                    } break;
                    case "Vertex": {
                        script.vertexSource = s;
                    } break;
                    case "Fragment": {
                        script.fragmentSource = s;
                    } break;
                    default: errorLexer(mainLexer, "Invalid section " + t.content);
                }

                mainLexer.skipTo(sectionEndPos);
                assertToken(mainLexer, nextTokenSkipSpaces(mainLexer), tknRightBracket);

            } else if (t.token == tknSpace) {
                // DO NOTHING
            } else {
                errorLexerUnexpected(mainLexer, t, tknID);
            }
        }

        // Check shader integrity


        // Pre-compile shader

        return script;
    }

    private static void parseProperties(ShaderScript shader, String content, int lineNumber) {
        Lexer lexer = new Lexer(content,
                tknID, tknSpace);

        lexer.setInitPos(lineNumber, 0);

        // TODO implement
    }

    private static void parseSettings(ShaderScript shader, String content, int lineNumber) {
        Lexer lexer = new Lexer(content, tknID, tknSpace, tknFloat, tknInt, tknSemi);
        lexer.setInitPos(lineNumber, 0);

        RenderStates renderStates = shader.renderStates;

        while (lexer.hasNext()) {
            MatchedToken t = nextTokenSkipSpaces(lexer);
            if (t == null) break;

            List<MatchedToken> params = new ArrayList<>();
            while (true) {
                MatchedToken pt = nextTokenSkipSpaces(lexer);
                if (pt == null)
                    errorEOF();
                if (pt.token == tknSemi) {
                    break;
                } else {
                    params.add(pt);
                }
            }

            // Now on actual parsing of render states
            switch (t.content) {
                case "DepthTest": {
                     renderStates.depthTestMode = RenderStates.TestMode.valueOf(params.get(0).content);
                } break;
                case "DepthMask": {
                    renderStates.depthMask = parseOnOff(lexer, params.get(0).content);
                } break;
                case "AlphaTest": {
                    renderStates.alphaTestMode = RenderStates.TestMode.valueOf(params.get(0).content);
                    renderStates.alphaTestRef = Float.parseFloat(params.get(1).content);
                } break;
                case "Cull": {
                    renderStates.cullMode = RenderStates.CullMode.valueOf(params.get(0).content);
                } break;
                case "ColorMask": {
                    String s = params.get(0).content;
                    if (s.equals("0")) {
                        renderStates.colorMaskA =
                            renderStates.colorMaskR =
                            renderStates.colorMaskG =
                            renderStates.colorMaskB = false;
                    } else {
                        renderStates.colorMaskA = s.contains("A");
                        renderStates.colorMaskR = s.contains("R");
                        renderStates.colorMaskG = s.contains("G");
                        renderStates.colorMaskB = s.contains("B");
                    }
                } break;
                case "DrawOrder": {
                    shader.drawOrder = Integer.parseInt(params.get(0).content);
                } break;
                case "Blend": {
                    renderStates.blending = parseOnOff(lexer, params.get(0).content);
                } break;
                case "BlendFunc": {
                    renderStates.srcBlend = RenderStates.BlendFunc.valueOf(params.get(0).content);
                    renderStates.dstBlend = RenderStates.BlendFunc.valueOf(params.get(1).content);
                } break;
                default: {
                    errorLexer(lexer, "Invalid settings property " + t.content);
                }
            }
        }
    }

    private static boolean parseOnOff(Lexer l, String str) {
        str = str.toLowerCase();
        if (str.equals("on"))
            return true;
        if (str.equals("off"))
            return false;
        throw errorLexer(l, "Parameter must be either On or Off");
    }

    private static int parseSection(Lexer lexer) {
        int indentLevel = 1;

        int i;
        for (i = lexer.currentIndex; i < lexer.content.length() && indentLevel != 0; ++i) {
            char ch = lexer.content.charAt(i);
            if (ch == '{') {
                indentLevel += 1;
            } else if (ch == '}') {
                indentLevel -= 1;
            }
        }

        if (indentLevel != 0) errorEOF();

        return i - 1;
    }

    private static MatchedToken nextTokenSkipSpaces(Lexer lexer) {
        while (true) {
            MatchedToken m = lexer.next();
            if (m == null || !m.token.name.equals("SPACE"))
                break;
        }

        if (!(lexer.last() == null || !lexer.last().token.name.equals("SPACE")))
            throw new RuntimeException("What the f");
        return lexer.last();
    }

    private static RuntimeException errorLexerUnexpected(Lexer lexer, MatchedToken token, Token... expected) {
        StringBuilder sb = new StringBuilder();
        sb.append("Expecting ");
        for (int i = 0; i < expected.length; ++i) {
            sb.append(expected[i].name);
            if (i != expected.length - 1) {
                sb.append('|');
            }
        }

        sb.append(", got ");
        sb.append(token.token.name + "(" + token.content + ")");
        throw errorLexer(lexer, sb.toString());
    }

    private static RuntimeException errorLexer(Lexer lexer, String msg) {
        throw error(String.format("[%d:%d] %s", lexer.lineNumber + 1, lexer.colNumber, msg));
    }

    private static RuntimeException errorEOF() {
        throw error("Premature EOF");
    }

    private static void assertToken(Lexer l, MatchedToken t, Token... expected) {
        boolean found = false;
        for (Token tk : expected) {
            if (tk == t.token) {
                found = true; break;
            }
        }

        if (!found) {
            errorLexerUnexpected(l, t, expected);
        }
    }

    private static RuntimeException error(String msg) {
        throw new RuntimeException(msg);
    }

}

final class Token {
    public final String name;
    public final Pattern regex;

    public Token(String name, String regex) {
        this.name = name;
        this.regex = Pattern.compile("^" + regex);
    }
}

final class MatchedToken {
    public final Token token;
    public final String content;

    public MatchedToken(Token token, String content) {
        this.token = token;
        this.content = content;
    }
}

final class Lexer {

    final Token[] tokens;
    public final String content;

    int currentIndex = 0;
    int lineNumber = 0;
    int colNumber = 0;

    MatchedToken lastMatched;

    public Lexer(String content, Token ...tokens) {
        this.content = content;
        this.tokens = tokens;
    }

    public void setInitPos(int lineNumber, int colNumber) {
        this.lineNumber = lineNumber;
        this.colNumber = colNumber;
    }

    public boolean hasNext() {
        return currentIndex != content.length();
    }

    public MatchedToken next() {
        if (!hasNext()) {
            lastMatched = null;
            return null;
        }

        for (Token token : tokens) {
            Matcher m = token.regex.matcher(content);
            m.region(currentIndex, content.length());
            if (m.find()) {
                int end = m.end();

                for (int i = currentIndex; i < end; ++i) {
                    char ch = content.charAt(i);
                    if (ch == '\n') {
                        colNumber = 0;
                        lineNumber++;
                    } else {
                        colNumber++;
                    }
                }

                MatchedToken ret = new MatchedToken(token, content.substring(currentIndex, end));
                currentIndex = end;

                lastMatched = ret;
                return ret;
            }
        }

        throw new RuntimeException("Invalid content " +
                content.substring(currentIndex, Math.min(currentIndex + 10, content.length())) + " ...");
    }

    public MatchedToken last() {
        return lastMatched;
    }

    public void skipTo(int newIndex) {
        for (int i = currentIndex; i < newIndex; ++i) {
            char ch = content.charAt(i);
            if (ch == '\n') {
                colNumber = 0;
                lineNumber++;
            } else {
                colNumber++;
            }
        }
        currentIndex = newIndex;
    }

}