package bit.minisys.minicc.scanner;

import java.util.ArrayList;
import java.util.HashSet;

import bit.minisys.minicc.MiniCCCfg;
import bit.minisys.minicc.internal.util.MiniCCUtil;
import mars.util.SystemIO;

enum DFA_STATES {
    DFA_STATE_INITIAL,
    /*
     * 整型常量和浮点型常量分为一组
     * 相同点：数字开头
     */

    // 整型常量
    DFA_STATE_0,
    DFA_STATE_NOZERO_DIGIT,
    DFA_STATE_HEX_PRE,
    DFA_STATE_HEX,
    DFA_STATE_OCT,
    DFA_STATE_INT_U,
    DFA_STATE_INT_l,
    DFA_STATE_INT_ll,
    DFA_STATE_INT_L,
    DFA_STATE_INT_LL,
    DFA_STATE_INT_UL,
    DFA_STATE_INT_Ul,
    // DFA_STATE_INT_LU,
    // DFA_STATE_INT_lU,

    // DFA_STATE_INT_ULOK,

    // 浮点型常量
    // DFA_STATE_0,
    // DFA_STATE_NOZERO_DIGIT,
    // DFA_STATE_HEX_PRE,
    // DFA_STATE_HEX,
    DFA_STATE_FLOAT_DEC_DOT,
    DFA_STATE_FLOAT_DEC_E,
    DFA_STATE_FLOAT_DEC_ADD, // OPT
    DFA_STATE_FLOAT_DEC_E_DIGIT,
    // DFA_STATE_FLOAT_DEC_FL,
    DFA_STATE_FLOAT_HEX_DOT,
    DFA_STATE_FLOAT_HEX_P,
    DFA_STATE_FLOAT_HEX_ADD,
    DFA_STATE_FLOAT_HEX_ADD_DIGIT,
    // DFA_STATE_FLOAT_HEX_FL,

    //

    /*
     * 字符常量和字符串字面量一组
     * 相同点：都有u|U|L前缀 字符串字面量还有u8前缀
     */

    // 字符常量 不能为''
    DFA_STATE_u,
    DFA_STATE_UL,
    DFA_STATE_L_SIN_QUO,
    DFA_STATE_L_DOU_QUO,

    DFA_STATE_CHAR,
    DFA_STATE_CHAR_SLA,
    DFA_STATE_CHAR_OCT,
    DFA_STATE_CHAR_HEX,
    // DFA_STATE_R_SIN_QUO,

    DFA_STATE_STR_SLA,
    DFA_STATE_STR_OCT,
    DFA_STATE_STR_HEX,
    // DFA_STATE_R_DOU_QUO,

    // 字符串字面量 可以为""

    /*
     * 标识符 关键字
     * 标识符与关键字部分与字符串字面量和字符常量部分有重合 u|U|L
     * 关键字不采用状态机 与示例一样使用HastSet存储所有关键字
     */
    DFA_STATE_NODIGIT,

    /*
     * 运算符 界限符
     * 
     */
    DFA_STATE_ADD,
    DFA_STATE_SUB,
    DFA_STATE_MUL,
    DFA_STATE_DIV,
    DFA_STATE_AND,
    DFA_STATE_EXCLA, // !
    DFA_STATE_MOD,
    DFA_STATE_MOD_COLO, // %:
    DFA_STATE_MOD_COLO_MOD, // %:%
    DFA_STATE_OR,
    DFA_STATE_DOT,
    DFA_STATE_DOT2, // ..
    DFA_STATE_LESS, // <
    DFA_STATE_LESS2, // <<
    DFA_STATE_MORE, // >
    DFA_STATE_MORE2, // >>
    DFA_STATE_EQU, // =
    DFA_STATE_COLO, // :
    DFA_STATE_SHARP // #
    // DFA_STATE_SEM, // ;
    // DFA_STATE_COM // ,

}

public class MiniCCScanner implements IMiniCCScanner {
    private int lIndex = 0;
    private int cIndex = 0;

    private ArrayList<String> srcLines;

    private HashSet<String> keywordSet;

    public MiniCCScanner() {
        this.keywordSet = new HashSet<String>();
        this.keywordSet.add("auto");
        this.keywordSet.add("break");
        this.keywordSet.add("case");
        this.keywordSet.add("char");
        this.keywordSet.add("const");
        this.keywordSet.add("continue");
        this.keywordSet.add("default");
        this.keywordSet.add("do");
        this.keywordSet.add("double");
        this.keywordSet.add("else");
        this.keywordSet.add("enum");
        this.keywordSet.add("extern");
        this.keywordSet.add("float");
        this.keywordSet.add("for");
        this.keywordSet.add("goto");
        this.keywordSet.add("if");
        this.keywordSet.add("inline");
        this.keywordSet.add("int");
        this.keywordSet.add("long");
        this.keywordSet.add("register");
        this.keywordSet.add("restrict");
        this.keywordSet.add("return");
        this.keywordSet.add("short");
        this.keywordSet.add("signed");
        this.keywordSet.add("sizeof");
        this.keywordSet.add("static");
        this.keywordSet.add("struct");
        this.keywordSet.add("switch");
        this.keywordSet.add("typedef");
        this.keywordSet.add("union");
        this.keywordSet.add("unsigned");
        this.keywordSet.add("void");
        this.keywordSet.add("volatile");
        this.keywordSet.add("while");

    }

    private char getNextChar() {
        char c = Character.MAX_VALUE;
        while (true) {
            if (lIndex < this.srcLines.size()) {
                String line = this.srcLines.get(lIndex);
                if (cIndex < line.length()) {
                    c = line.charAt(cIndex);
                    cIndex++;
                    break;
                } else {
                    lIndex++;
                    cIndex = 0;
                }
            } else {
                break;
            }
        }
        if (c == '\u001a') {
            c = Character.MAX_VALUE;
        }
        return c;
    }

    private boolean inSourceCharSet(char c) {
        if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))
            return true;
        if (c >= '!' && c <= '#')
            return true;
        if (c >= '%' && c <= '/')
            return true;
        if (c >= ':' && c <= '?')
            return true;
        if (c >= '[' && c <= '_')
            return true;
        if (c >= '{' && c <= '~')
            return true;
        if (c == ' ')
            return true;
        return false;

    }

    private boolean isNewLineChar(char c) {
        if (c == '\r' || c == '\n')
            return true;
        return false;
    }

    private boolean isSimpEscpChar(char c) {
        if (c == 39 || c == '"' || c == '?' || c == 92
                || c == 'a' || c == 'b' || c == 'f' || c == 't' || c == 'v'
                || c == 'r' || c == 'n')
            return true;
        return false;
    }

    private boolean isNoZeroDigit(char c) {
        if (c >= '1' && c <= '9')
            return true;
        return false;
    }

    private boolean isZero(char c) {
        if (c == '0')
            return true;
        return false;
    }

    private boolean isOctDigit(char c) {
        if (c >= '0' && c <= '7')
            return true;
        return false;
    }

    private boolean isHexDigit(char c) {
        if (isDigit(c) || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f'))
            return true;
        return false;
    }

    private boolean isAlpha(char c) {
        return Character.isAlphabetic(c);
    }

    private boolean isDigit(char c) {
        return Character.isDigit(c);
    }

    private boolean isAlphaOrDigit(char c) {
        return Character.isLetterOrDigit(c);
    }

    private String genToken(int num, String lexme, String type) {
        return genToken(num, lexme, type, this.cIndex - 1, this.lIndex);
    }

    private String genToken2(int num, String lexme, String type) {
        // 用于超前读取生成属性字流
        return genToken(num, lexme, type, this.cIndex - 2, this.lIndex);
    }

    private String genToken(int num, String lexme, String type, int cIndex, int lIndex) {
        String strToken = "";

        strToken += "[@" + num + "," + (cIndex - lexme.length() + 1) + ":" + cIndex;
        strToken += "='" + lexme + "',<" + type + ">," + (lIndex + 1) + ":" + (cIndex - lexme.length() + 1) + "]\n";

        return strToken;
    }

    @Override
    public String run(String iFile) throws Exception {

        System.out.println("2. MiniCCScanner Scanning...");
        String strTokens = "";
        int iTknNum = 0;

        this.srcLines = MiniCCUtil.readFile(iFile);

        DFA_STATES state = DFA_STATES.DFA_STATE_INITIAL; // FA state
        String lexme = ""; // token lexme
        char c = ' '; // next char
        boolean keep = false; // keep current char
        boolean end = false;

        // System.out.println("start");

        while (!end) { // scanning loop
            if (!keep) {
                c = getNextChar();
            }
            // System.out.println(c);
            keep = false;

            switch (state) {
                case DFA_STATE_INITIAL:
                    lexme = "";

                    if (c == '[') {
                        strTokens += genToken(iTknNum, "[", "'['");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else if (c == ']') {
                        strTokens += genToken(iTknNum, "]", "']'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else if (c == '(') {
                        strTokens += genToken(iTknNum, "(", "'('");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else if (c == ')') {
                        strTokens += genToken(iTknNum, ")", "')'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else if (c == '{') {
                        strTokens += genToken(iTknNum, "{", "'{'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else if (c == '}') {
                        strTokens += genToken(iTknNum, "}", "'}'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else if (c == ';') {
                        strTokens += genToken(iTknNum, ";", "';'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else if (c == ',') {
                        strTokens += genToken(iTknNum, ",", "','");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    }

                    else if (c == '-') {
                        state = DFA_STATES.DFA_STATE_SUB;
                        lexme = lexme + c;
                    } else if (c == '+') {
                        state = DFA_STATES.DFA_STATE_ADD;
                        lexme = lexme + c;
                    } else if (c == '&') {
                        state = DFA_STATES.DFA_STATE_AND;
                        lexme = lexme + c;
                    } else if (c == '*') {
                        state = DFA_STATES.DFA_STATE_MUL;
                        lexme = lexme + c;
                    } else if (c == '!') {
                        state = DFA_STATES.DFA_STATE_EXCLA;
                        lexme = lexme + c;
                    } else if (c == '/') {
                        state = DFA_STATES.DFA_STATE_DIV;
                        lexme = lexme + c;
                    } else if (c == '%') {
                        state = DFA_STATES.DFA_STATE_MOD;
                        lexme = lexme + c;
                    } else if (c == '|') {
                        state = DFA_STATES.DFA_STATE_OR;
                        lexme = lexme + c;
                    } else if (c == '.') {
                        state = DFA_STATES.DFA_STATE_DOT;
                        lexme = lexme + c;
                    } else if (c == '<') {
                        state = DFA_STATES.DFA_STATE_LESS;
                        lexme = lexme + c;
                    } else if (c == '>') {
                        state = DFA_STATES.DFA_STATE_MORE;
                        lexme = lexme + c;
                    } else if (c == '=') {
                        state = DFA_STATES.DFA_STATE_EQU;
                        lexme = lexme + c;
                    } else if (c == ':') {
                        state = DFA_STATES.DFA_STATE_COLO;
                        lexme = lexme + c;
                    } else if (c == '#') {
                        state = DFA_STATES.DFA_STATE_SHARP;
                        lexme = lexme + c;
                    }

                    else if (isZero(c)) { // 0
                        state = DFA_STATES.DFA_STATE_0;
                        lexme = lexme + c;
                    } else if (isNoZeroDigit(c)) { // 1-9
                        state = DFA_STATES.DFA_STATE_NOZERO_DIGIT;
                        lexme = lexme + c;
                    } else if (c == 'U' || c == 'L') {
                        state = DFA_STATES.DFA_STATE_UL;
                        lexme = lexme + c;
                    } else if (c == 'u') {
                        c = getNextChar();
                        if (c != 39 && c != '"' && c != '8') {
                            keep = true;
                            state = DFA_STATES.DFA_STATE_NODIGIT;
                            lexme = lexme + c;
                        } else {
                            state = DFA_STATES.DFA_STATE_u;
                            lexme = lexme + c;
                        }

                    } else if (isAlpha(c) || c == '_') {
                        state = DFA_STATES.DFA_STATE_NODIGIT;
                        lexme = lexme + c;
                    } else if (c == 34) { // "
                        state = DFA_STATES.DFA_STATE_L_DOU_QUO;
                        lexme = lexme + c;
                    } else if (c == 39) { // ' in ASCII=39
                        state = DFA_STATES.DFA_STATE_L_SIN_QUO;
                        lexme = lexme + c;
                    }

                    else if (c == Character.MAX_VALUE) {
                        cIndex = 5;
                        strTokens += genToken(iTknNum, "<EOF>", "EOF");
                        end = true;
                    }

                    break;
                case DFA_STATE_0:
                    if (c == 'x' || c == 'X') {
                        state = DFA_STATES.DFA_STATE_HEX_PRE;
                        lexme = lexme + c;
                    } else if (isOctDigit(c)) {

                        state = DFA_STATES.DFA_STATE_OCT;
                        lexme = lexme + c;
                    } else if (c == 'e' || c == 'E') {
                        state = DFA_STATES.DFA_STATE_FLOAT_DEC_E;
                        lexme = lexme + c;
                    }
                    // add in lab6.
                    else if (c == '.') {
                        state = DFA_STATES.DFA_STATE_FLOAT_DEC_DOT;
                        lexme = lexme + c;
                    } else { // OPT : 0 is legal
                             // lexme = lexme + c;
                        strTokens += genToken2(iTknNum, lexme, "IntegerConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                        // System.out.println("[ERROR]DFA_STATE_0. should: 0x|0X|0e|0E");
                        // System.exit(0);
                    }

                    break;
                case DFA_STATE_NOZERO_DIGIT:
                    if (isDigit(c)) {
                        // state = DFA_STATE.DFA_STATE_NOZERO_DIGIT;
                        lexme = lexme + c;
                    } else if (c == 'u' || c == 'U') {
                        state = DFA_STATES.DFA_STATE_INT_U;
                        lexme = lexme + c;
                    } else if (c == 'l') {
                        state = DFA_STATES.DFA_STATE_INT_l;
                        lexme = lexme + c;
                    } else if (c == 'L') {
                        state = DFA_STATES.DFA_STATE_INT_L;
                        lexme = lexme + c;
                    } else if (c == '.') {
                        state = DFA_STATES.DFA_STATE_FLOAT_DEC_DOT;
                        lexme = lexme + c;
                    } else if (c == 'e' || c == 'E') {
                        state = DFA_STATES.DFA_STATE_FLOAT_DEC_E;
                        lexme = lexme + c;
                    } else {// OPT : without .|e|E|u|l|ll|etc. can also be legal

                        strTokens += genToken2(iTknNum, lexme, "IntegerConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }

                    break;
                case DFA_STATE_FLOAT_DEC_DOT:
                    if (isDigit(c)) {
                        lexme = lexme + c;
                    } else if (c == 'e' || c == 'E') {
                        state = DFA_STATES.DFA_STATE_FLOAT_DEC_E;
                        lexme = lexme + c;
                    } else if (c == 'f' || c == 'F' || c == 'l' || c == 'L') {// OPT : without e|E can alse be legal
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "FloatingConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else {// OPT : without e|E | f|F|l|L can also be legal

                        strTokens += genToken2(iTknNum, lexme, "FloatingConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;

                    }

                    break;
                case DFA_STATE_FLOAT_DEC_E:
                    if (isDigit(c)) {
                        state = DFA_STATES.DFA_STATE_FLOAT_DEC_E_DIGIT;
                        lexme = lexme + c;
                    } else if (c == '+' || c == '-') { // OPT 1e9 == 1e+9 and 1e-9 is legal
                        state = DFA_STATES.DFA_STATE_FLOAT_DEC_ADD;
                        lexme = lexme + c;
                    } else {
                        System.out.println("[ERROR]DFA_STATE_FLOAT_DEC_E. should be 1.2e9|1.2E9.");
                        System.exit(0);
                    }
                    break;
                case DFA_STATE_FLOAT_DEC_ADD:
                    if (isDigit(c)) {
                        state = DFA_STATES.DFA_STATE_FLOAT_DEC_E_DIGIT;
                        lexme = lexme + c;
                    } else {
                        System.out.println("[ERROR]DFA_STATE_FLOAT_DEC_ADD. should be 1.2e9|1.2E9.");
                        System.exit(0);
                    }
                    break;
                case DFA_STATE_FLOAT_DEC_E_DIGIT:
                    if (isDigit(c)) {
                        // state = DFA_STATES.DFA_STATE_FLOAT_DEC_E_DIGIT;
                        lexme = lexme + c;
                    } else if (c == 'f' || c == 'F' || c == 'l' || c == 'L') {
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "FloatingConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;

                    } else {// OPT:without f|l|F|L can alse be legal

                        strTokens += genToken2(iTknNum, lexme, "FloatingConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_INT_U:
                    if (c == 'l') {
                        state = DFA_STATES.DFA_STATE_INT_Ul;
                        lexme = lexme + c;
                    } else if (c == 'L') {
                        state = DFA_STATES.DFA_STATE_INT_UL;
                        lexme = lexme + c;
                    } else {// OPT
                        strTokens += genToken2(iTknNum, lexme, "IntegerConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_INT_L:
                    if (c == 'L') {
                        state = DFA_STATES.DFA_STATE_INT_LL;
                        lexme = lexme + c;
                    } else if (c == 'u' || c == 'U') {
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "IntegerConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;

                    } else {// OPT
                        strTokens += genToken2(iTknNum, lexme, "IntegerConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_INT_l:
                    if (c == 'l') {
                        state = DFA_STATES.DFA_STATE_INT_ll;
                        lexme = lexme + c;
                    } else if (c == 'u' || c == 'U') {
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "IntegerConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;

                    } else {// OPT
                        // System.out.print(lexme);
                        strTokens += genToken2(iTknNum, lexme, "IntegerConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_INT_LL:
                    if (c == 'u' || c == 'U') {
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "IntegerConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else {
                        strTokens += genToken2(iTknNum, lexme, "IntegerConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_INT_ll:
                    if (c == 'u' || c == 'U') {
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "IntegerConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else {
                        strTokens += genToken2(iTknNum, lexme, "IntegerConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_INT_UL:
                    if (c == 'L') {
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "IntegerConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else {// OPT
                        strTokens += genToken2(iTknNum, lexme, "IntegerConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_INT_Ul:
                    if (c == 'l') {
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "IntegerConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else {// OPT
                        strTokens += genToken2(iTknNum, lexme, "IntegerConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_HEX_PRE:
                    if (isHexDigit(c)) {
                        state = DFA_STATES.DFA_STATE_HEX;
                        lexme = lexme + c;
                    } else {// OPT 0x|0X without a digital behind is inlegal
                        System.out.println("[ERROR]DFA_STATE_HEX_PRE. should be 0x9|0X9.");
                        System.exit(0);
                    }

                    break;
                case DFA_STATE_HEX:
                    if (isHexDigit(c)) {
                        lexme = lexme + c;
                    } else if (c == 'u' || c == 'U') {
                        state = DFA_STATES.DFA_STATE_INT_U;
                        lexme = lexme + c;
                    } else if (c == 'l') {
                        state = DFA_STATES.DFA_STATE_INT_l;
                        lexme = lexme + c;
                    } else if (c == 'L') {
                        state = DFA_STATES.DFA_STATE_INT_L;
                        lexme = lexme + c;
                    } else if (c == '.') {
                        state = DFA_STATES.DFA_STATE_FLOAT_HEX_DOT;
                        lexme = lexme + c;
                    } else if (c == 'p' || c == 'P') {
                        state = DFA_STATES.DFA_STATE_FLOAT_HEX_P;
                        lexme = lexme + c;
                    } else {// OPT:0x9|0X9 without .|p|P|u|l|etc. can alse be legal
                        strTokens += genToken2(iTknNum, lexme, "IntegerConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_OCT:
                    // System.out.println("oct:" + lexme);
                    if (isOctDigit(c)) {
                        lexme = lexme + c;
                    } else if (c == 'u' || c == 'U') {
                        state = DFA_STATES.DFA_STATE_INT_U;
                        lexme = lexme + c;
                    } else if (c == 'l') {
                        state = DFA_STATES.DFA_STATE_INT_l;
                        lexme = lexme + c;
                    } else if (c == 'L') {
                        state = DFA_STATES.DFA_STATE_INT_L;
                        lexme = lexme + c;
                    } else {//
                        strTokens += genToken2(iTknNum, lexme, "IntegerConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;

                case DFA_STATE_FLOAT_HEX_DOT:
                    if (isHexDigit(c)) {
                        lexme = lexme + c;
                    } else if (c == 'p' || c == 'P') {
                        state = DFA_STATES.DFA_STATE_FLOAT_HEX_P;
                        lexme = lexme + c;
                    } else {// OPT: 0x9. without p|P can alse be legal
                        strTokens += genToken2(iTknNum, lexme, "FloatingConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_FLOAT_HEX_P:
                    if (c == '+' || c == '-') {
                        state = DFA_STATES.DFA_STATE_FLOAT_HEX_ADD;
                        lexme = lexme + c;
                    } else if (isDigit(c)) { // OPT : p+9 == p9
                        state = DFA_STATES.DFA_STATE_FLOAT_HEX_ADD_DIGIT;
                        lexme = lexme + c;
                    } else { // OPT:0XP without a +|- behind
                        System.out.println("[ERROR]DFA_STATE_FLOAT_HEX_P. should be 0x9p+|0xp9p-.");
                        System.exit(0);
                    }
                    break;
                case DFA_STATE_FLOAT_HEX_ADD:
                    if (isDigit(c)) {
                        state = DFA_STATES.DFA_STATE_FLOAT_HEX_ADD_DIGIT;
                        lexme = lexme + c;
                    } else {// OPT
                        System.out.println("[ERROR]DFA_STATE_FLOAT_HEX_ADD. should be 0x9p+8|0xp9p-8.");
                        System.exit(0);
                    }
                    break;
                case DFA_STATE_FLOAT_HEX_ADD_DIGIT:
                    if (isDigit(c)) {
                        lexme = lexme + c;
                    } else if (c == 'f' || c == 'F' || c == 'l' || c == 'L') {
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "FloatingConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else {// OPT:without f|F|l|L also legal
                        strTokens += genToken2(iTknNum, lexme, "FloatingConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;

                // 字符常量 字符串字面量

                case DFA_STATE_u:
                    if (c == '8') {
                        state = DFA_STATES.DFA_STATE_UL;
                        lexme = lexme + c;
                    } else if (c == 34) { // "
                        state = DFA_STATES.DFA_STATE_L_DOU_QUO;
                        lexme = lexme + c;
                    } else if (c == 39) { // ' in ASCII=39
                        state = DFA_STATES.DFA_STATE_L_SIN_QUO;
                        lexme = lexme + c;
                    } else if (isAlphaOrDigit(c) || c == '_') {
                        state = DFA_STATES.DFA_STATE_NODIGIT;
                        lexme = lexme + c;
                    } else {
                        System.out.println("[ERROR]DFA_STATE_u. ");
                        System.exit(0);
                    }
                    break;
                case DFA_STATE_UL:
                    if (c == 34) { // "
                        state = DFA_STATES.DFA_STATE_L_DOU_QUO;
                        lexme = lexme + c;
                    } else if (c == 39) { // ' in ASCII=39
                        state = DFA_STATES.DFA_STATE_L_SIN_QUO;
                        lexme = lexme + c;
                    }
                    break;
                case DFA_STATE_L_DOU_QUO:
                    // \ in ASCII =92
                    if (inSourceCharSet(c) && c != 34 && c != 92 && !isNewLineChar(c)) {
                        lexme = lexme + c;
                    } else if (c == 92) {
                        state = DFA_STATES.DFA_STATE_STR_SLA;
                        lexme = lexme + c;
                    } else if (c == 34) {
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "StringLiteral");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else {
                        System.out.println("[ERROR]DFA_STATE_L_DOU_QUO. should be \"something\"||\"\".");
                        System.exit(0);
                    }
                    break;
                case DFA_STATE_L_SIN_QUO:
                    if (inSourceCharSet(c) && c != 39 && c != 92 && !isNewLineChar(c)) {
                        state = DFA_STATES.DFA_STATE_CHAR;
                        lexme = lexme + c;
                    } else if (c == 92) {
                        state = DFA_STATES.DFA_STATE_CHAR_SLA;
                        lexme = lexme + c;
                    } else {
                        System.out.println("[ERROR]DFA_STATE_L_SIN_QUO. should be \'something\'.");
                        System.exit(0);
                    }
                    break;

                case DFA_STATE_STR_SLA:
                    if (isSimpEscpChar(c)) {
                        state = DFA_STATES.DFA_STATE_L_DOU_QUO;
                        lexme = lexme + c;
                    } else if (isOctDigit(c)) {
                        state = DFA_STATES.DFA_STATE_STR_OCT;
                        lexme = lexme + c;
                    } else if (c == 'x') {
                        state = DFA_STATES.DFA_STATE_STR_HEX;
                        lexme = lexme + c;
                    } else {
                        System.out.println("[ERROR]DFA_STATE_STR_SLA. should be \\OctDit||\\x||\\SimpEscpChar.");
                        System.exit(0);
                    }
                    break;
                case DFA_STATE_STR_OCT:
                    if (isOctDigit(c)) {
                        lexme = lexme + c;
                    } else {
                        state = DFA_STATES.DFA_STATE_L_DOU_QUO;
                        keep = true;
                    }
                    break;
                case DFA_STATE_STR_HEX:
                    if (isHexDigit(c)) {
                        // state = DFA_STATES.DFA_STATE_L_DOU_QUO;
                        lexme = lexme + c;
                    } else {
                        state = DFA_STATES.DFA_STATE_L_DOU_QUO;
                        keep = true;
                    }
                    break;
                case DFA_STATE_CHAR:
                    if (inSourceCharSet(c) && c != 39 && c != 92 && !isNewLineChar(c)) {
                        lexme = lexme + c;
                    } else if (c == 92) {
                        state = DFA_STATES.DFA_STATE_CHAR_SLA;
                        lexme = lexme + c;
                    } else if (c == 39) {
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "CharacterConstant");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else {
                        System.out.println("[ERROR]DFA_STATE_CHAR. should be \'something\'.");
                        System.exit(0);
                    }
                    break;

                case DFA_STATE_CHAR_SLA:
                    if (isSimpEscpChar(c)) {
                        state = DFA_STATES.DFA_STATE_CHAR;
                        lexme = lexme + c;
                    } else if (isOctDigit(c)) {
                        state = DFA_STATES.DFA_STATE_CHAR_OCT;
                        lexme = lexme + c;
                    } else if (c == 'x') {
                        state = DFA_STATES.DFA_STATE_CHAR_HEX;
                        lexme = lexme + c;
                    } else {
                        System.out.println("[ERROR]DFA_STATE_CHAR_SLA. should be \\OctDit||\\x||\\SimpEscpChar.");
                        System.exit(0);
                    }
                    break;
                case DFA_STATE_CHAR_OCT:
                    if (isOctDigit(c)) {
                        lexme = lexme + c;
                    } else {
                        state = DFA_STATES.DFA_STATE_L_SIN_QUO;
                        keep = true;
                    }
                    break;
                case DFA_STATE_CHAR_HEX:
                    if (isHexDigit(c)) {
                        // state = DFA_STATES.DFA_STATE_CHAR;
                        lexme = lexme + c;
                    } else {
                        state = DFA_STATES.DFA_STATE_CHAR;
                        keep = true;
                    }
                    break;
                // 字符串字面量 字符常量 done

                // 标识符
                case DFA_STATE_NODIGIT:
                    if (isAlphaOrDigit(c) || c == '_') {
                        lexme = lexme + c;
                    } else {
                        if (this.keywordSet.contains(lexme)) {
                            strTokens += genToken2(iTknNum, lexme, "'" + lexme + "'");
                        } else {
                            strTokens += genToken2(iTknNum, lexme, "Identifier");
                        }
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;

                // 标识符 done

                // 运算符 界限符
                case DFA_STATE_ADD:
                    if (c == '+' || c == '=') { // 状态虽然不一致 但是代码上合并
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else {
                        strTokens += genToken2(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_SUB:
                    if (c == '-' || c == '=' || c == '>') { // 状态虽然不一致 但是代码上合并
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else {
                        strTokens += genToken2(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_MUL:
                    if (c == '=') { // 状态虽然不一致 但是代码上合并
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else {
                        strTokens += genToken2(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_DIV:
                    if (c == '=') { // 状态虽然不一致 但是代码上合并
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else {
                        strTokens += genToken2(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_AND:
                    if (c == '&' || c == '=') { // 状态虽然不一致 但是代码上合并
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else {
                        strTokens += genToken2(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_EXCLA: // !
                    if (c == '=') { // 状态虽然不一致 但是代码上合并
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else {
                        strTokens += genToken2(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_MOD:
                    if (c == '>' || c == '=') { // 状态虽然不一致 但是代码上合并
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else if (c == ':') {
                        state = DFA_STATES.DFA_STATE_MOD_COLO;
                        lexme = lexme + c;
                    } else {
                        strTokens += genToken2(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_MOD_COLO: // %:
                    if (c == '%') { // 状态虽然不一致 但是代码上合并
                        lexme = lexme + c;
                        state = DFA_STATES.DFA_STATE_MOD_COLO_MOD;
                    } else {
                        strTokens += genToken2(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_MOD_COLO_MOD: // %:%
                    if (c == ':') { // 状态虽然不一致 但是代码上合并
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else {
                        System.out.println("[ERROR]DFA_STATE_MOD_COLO_MOD. shoule be %:%:.");
                        System.exit(0);
                    }
                    break;
                case DFA_STATE_OR:
                    if (c == '|' || c == '=') { // 状态虽然不一致 但是代码上合并
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else {
                        strTokens += genToken2(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_DOT:
                    if (c == '.') { // 状态虽然不一致 但是代码上合并
                        lexme = lexme + c;
                        state = DFA_STATES.DFA_STATE_DOT2;
                    } else {
                        strTokens += genToken2(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_DOT2:
                    if (c == '.') { // 状态虽然不一致 但是代码上合并
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else {
                        System.out.println("[ERROR]DFA_STATE_DOT2. shoule be ... .");
                        System.exit(0);
                    }
                    break;

                case DFA_STATE_LESS: // <
                    if (c == ':' || c == '=' || c == '%') { // 状态虽然不一致 但是代码上合并
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else if (c == '<') {
                        lexme = lexme + c;
                        state = DFA_STATES.DFA_STATE_LESS2;
                    } else {
                        strTokens += genToken2(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_LESS2: // <<
                    if (c == '=') { // 状态虽然不一致 但是代码上合并
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else {
                        strTokens += genToken2(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_MORE: // >
                    if (c == '=') { // 状态虽然不一致 但是代码上合并
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else if (c == '>') {
                        lexme = lexme + c;
                        state = DFA_STATES.DFA_STATE_MORE2;
                    } else {
                        strTokens += genToken2(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;

                case DFA_STATE_MORE2: // >>
                    if (c == '=') { // 状态虽然不一致 但是代码上合并
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else {
                        strTokens += genToken2(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_EQU: // =
                    if (c == '=') { // 状态虽然不一致 但是代码上合并
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else {
                        strTokens += genToken2(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_COLO: // :
                    if (c == '>') { // 状态虽然不一致 但是代码上合并
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else {
                        strTokens += genToken2(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;
                case DFA_STATE_SHARP: // #
                    if (c == '#') { // 状态虽然不一致 但是代码上合并
                        lexme = lexme + c;
                        strTokens += genToken(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                    } else {
                        strTokens += genToken2(iTknNum, lexme, "'" + lexme + "'");
                        iTknNum++;
                        state = DFA_STATES.DFA_STATE_INITIAL;
                        keep = true;
                    }
                    break;

                // 运算符 界限符 done

                default:
                    System.out.println("[ERROR]Scanner:line " + lIndex + ", column=" + cIndex +
                            ", unreachable state!");
                    System.exit(0);
                    break;
            }
        }

        // System.out.println("done");
        String oFile = MiniCCUtil.removeAllExt(iFile) +
                MiniCCCfg.MINICC_SCANNER_OUTPUT_EXT;
        MiniCCUtil.createAndWriteFile(oFile, strTokens);

        return oFile;
    }
}