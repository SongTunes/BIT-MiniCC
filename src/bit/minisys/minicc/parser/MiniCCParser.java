package bit.minisys.minicc.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.antlr.v4.gui.TreeViewer;
import org.python.antlr.PythonParser.del_list_return;

import com.fasterxml.jackson.databind.ObjectMapper;

import bit.minisys.minicc.MiniCCCfg;
import bit.minisys.minicc.internal.ir.A;
import bit.minisys.minicc.internal.util.MiniCCUtil;
import bit.minisys.minicc.parser.ast.*;
import mars.assembler.Tokenizer;

import java.util.LinkedList;
import java.util.List;

/*
 * PROGRAM     --> FUNC_LIST
 * FUNC_LIST   --> FUNC FUNC_LIST | e
 * FUNC        --> TYPE ID '(' ARGUMENTS ')' CODE_BLOCK
 * TYPE        --> INT
 * ARGS   	   --> e | ARG_LIST
 * ARG_LIST    --> ARG ',' ARGLIST | ARG
 * ARG    	   --> TYPE ID
 * CODE_BLOCK  --> '{' STMTS '}'
 * STMTS       --> STMT STMTS | e
 * STMT        --> RETURN_STMT
 *
 * RETURN STMT --> RETURN EXPR ';'
 *
 * EXPR        --> TERM EXPR'
 * EXPR'       --> '+' TERM EXPR' | '-' TERM EXPR' | e
 *
 * TERM        --> FACTOR TERM'
 * TERM'       --> '*' FACTOR TERM' | e
 *
 * FACTOR      --> ID  
 * 
 */

class ScannerToken {
    public String lexme;
    public String type;
    public int line;
    public int column;
}

public class MiniCCParser implements IMiniCCParser {

    private ArrayList<ScannerToken> tknList;
    private int tokenIndex;
    private ScannerToken nextToken;

    @Override
    public String run(String iFile) throws Exception {
        System.out.println("3. MiniCCParser Parsing...");

        String oFile = MiniCCUtil.removeAllExt(iFile) + MiniCCCfg.MINICC_PARSER_OUTPUT_EXT;
        String tFile = MiniCCUtil.removeAllExt(iFile) + MiniCCCfg.MINICC_SCANNER_OUTPUT_EXT;

        tknList = loadTokens(tFile);
        tokenIndex = 0;

        ASTNode root = program();

        // System.out.println(root.children);
        // System.out.println(root.getChildCount());

        // String[] dummyStrs = new String[16];
        // TreeViewer viewr = new TreeViewer(Arrays.asList(dummyStrs), root);
        // viewr.open();

        ObjectMapper mapper = new ObjectMapper(); // use the Jackson lib to generate json file
        mapper.writeValue(new File(oFile), root);

        // ?TODO: write to file

        return oFile;
    }

    private ArrayList<ScannerToken> loadTokens(String tFile) {
        /**
         * return the token list
         */
        tknList = new ArrayList<ScannerToken>();

        ArrayList<String> tknStr = MiniCCUtil.readFile(tFile);

        for (String str : tknStr) {
            if (str.trim().length() <= 0) {
                continue;
            }

            ScannerToken st = new ScannerToken();
            // [@0,0:2='int',<'int'>,1:0]
            String[] segs;
            if (str.indexOf("<','>") > 0) {
                str = str.replace("','", "'DOT'");

                segs = str.split(",");
                segs[1] = "=','";
                segs[2] = "<','>";

            } else {
                segs = str.split(",");
            }
            st.lexme = segs[1].substring(segs[1].indexOf("=") + 1 + 1, segs[1].length() - 1);
            st.type = segs[2].substring(segs[2].indexOf("<") + 1, segs[2].length() - 1);
            String[] lc = segs[3].split(":");
            st.line = Integer.parseInt(lc[0]);
            st.column = Integer.parseInt(lc[1].replace("]", ""));

            tknList.add(st);
        }

        return tknList;
    }

    private ScannerToken getToken(int index) {
        if (index < tknList.size()) {
            return tknList.get(index);
        }
        return null;
    }

    public void matchToken(String type) {
        /**
         * always update the token id to the newest.
         * tokenIndex++ if the next char is CORRECT.
         */
        if (tokenIndex < tknList.size()) {
            ScannerToken next = tknList.get(tokenIndex);
            if (!next.type.equals(type)) {
                System.out.println("[ERROR]Parser: unmatched token, expected = " + type + ", "
                        + "input = " + next.type);
                for (int i = 0; i < 10; i++) {
                    System.out.println(tknList.get(tokenIndex++).type);
                }
                System.exit(2);
            } else {
                tokenIndex++;
                nextToken = getToken(tokenIndex);
            }
        }
    }

    // CMPL_UNIT → EXTERNAL_DECLARATION CMP_UNIT_0
    public ASTNode program() {
        ASTCompilationUnit p = new ASTCompilationUnit();
        ASTNode ex_dec = externalDeclaration();
        p.items.add(ex_dec);

        ArrayList<ASTNode> p0 = cmplUnit0();
        if (p0 != null) {
            // p.getSubNodes().add(fl);
            p.items.addAll(p0);
        }
        p.children.addAll(p.items);

        return p;
    }

    // CMPL_UNIT_0 →EXTERNAL_DECLARATION CMPL_UNIT_0|ε
    public ArrayList<ASTNode> cmplUnit0() {
        ArrayList<ASTNode> cmpl_unit0 = new ArrayList<ASTNode>();

        nextToken = getToken(tokenIndex);
        if (nextToken.type.equals("EOF")) {
            return null; // is e
        } else {
            /**
             * write a function if can not express by a node.
             * the function's return type is the base class ASTNode.
             */
            ASTNode ex_dec = externalDeclaration();
            cmpl_unit0.add(ex_dec);

            ArrayList<ASTNode> _cmpl_unit0 = cmplUnit0(); // recursive
            if (_cmpl_unit0 != null) {
                cmpl_unit0.addAll(_cmpl_unit0); // add all elements in fl2 to fl
            }
            return cmpl_unit0;
        }
    }

    // ARGUMENT --> TYPE ID
    public ASTParamsDeclarator argument() {
        ASTParamsDeclarator pd = new ASTParamsDeclarator();
        ASTToken t = typeSpec();
        pd.specfiers.add(t);

        ASTIdentifier id = new ASTIdentifier(getToken(tokenIndex).lexme, tokenIndex);
        matchToken("Identifier");

        ASTVariableDeclarator vd = new ASTVariableDeclarator();
        vd.identifier = id;
        pd.declarator = vd;

        return pd;
    }

    // EXTERNAL_DECLARATION→FUNC_DEF | DECLARATION
    public ASTNode externalDeclaration() {

        // need to classify according to TYPE_DEC ID <?>
        ScannerToken st2 = getToken(tokenIndex + 2);
        if (st2.type.equals("'('")) {
            ASTNode func_def = funcDef();

            return func_def;
        } else {
            ASTNode dec = declaration();

            return dec;
        }
    }

    // +FUNC_DEF → TYPE_SPE ID (ARG_LIST ) CMPD_STMT
    public ASTNode funcDef() {

        ASTFunctionDefine func_def = new ASTFunctionDefine();
        /**
         * public ASTFunctionDefine(List<ASTToken> specList, ASTDeclarator declarator,
         * ASTCompoundStatement bodyStatement) {
         * }
         */

        ASTToken s = typeSpec();
        func_def.specifiers.add(s);
        // func_def.children.add(s);

        ASTFunctionDeclarator func_dec = new ASTFunctionDeclarator();
        /**
         * ASTDeclarator declarator,List<ASTParamsDeclarator> paramsDeclarators
         */

        ASTVariableDeclarator vd = new ASTVariableDeclarator();
        ASTIdentifier id = new ASTIdentifier(getToken(tokenIndex).lexme, tokenIndex);

        matchToken("Identifier");
        vd.identifier = id;

        matchToken("'('");
        ArrayList<ASTParamsDeclarator> pl = arguments(); // TODO:remember to set the value!
        matchToken("')'");

        func_dec.declarator = vd;
        if (pl != null) {
            func_dec.params.addAll(pl);
            // func_dec.children.addAll(pl);
        }

        ASTCompoundStatement cs = cmpdStmt();

        func_def.declarator = func_dec;
        // func_def.children.add(func_dec);

        func_def.body = cs;
        // func_def.children.add(cs);

        return func_def;

    }

    // ARGUMENTS→ε|ARG_LIST
    public ArrayList<ASTParamsDeclarator> arguments() {
        nextToken = tknList.get(tokenIndex);
        if (nextToken.type.equals("')'")) { // ending
            return null;
        } else {
            ArrayList<ASTParamsDeclarator> al = argList();
            return al;
        }
    }

    // ARG_LIST→ARGUMENT , ARG_LIST | ARGUMENT
    public ArrayList<ASTParamsDeclarator> argList() {
        ArrayList<ASTParamsDeclarator> pdl = new ArrayList<ASTParamsDeclarator>();
        ASTParamsDeclarator pd = argument();
        pdl.add(pd);

        nextToken = tknList.get(tokenIndex);
        if (nextToken.type.equals("','")) {
            matchToken("','");
            ArrayList<ASTParamsDeclarator> pdl2 = argList();
            pdl.addAll(pdl2);
        }

        return pdl;
    }

    // TYPE_SPEC → int|char|float|double|long|void
    public ASTToken typeSpec() {
        ScannerToken st = getToken(tokenIndex);

        ASTToken t = new ASTToken();
        if (st.type.equals("'int'") || st.type.equals("'char'") || st.type.equals("'float'")
                || st.type.equals("'double'") || st.type.equals("'long'") || st.type.equals("'void'")) {
            t.tokenId = tokenIndex;
            t.value = st.lexme;
            tokenIndex++;
        } else {
            System.out.println("[ERROR]Parser: unmatched token, expected = TYPE_SPEC, "
                    + "input = " + st.type);
        }
        return t;
    }

    // DECLARATION→TYPE_SPEC INIT_DECLARATOR_LIST ;
    public ASTDeclaration declaration() {
        ASTDeclaration dec = new ASTDeclaration();

        /**
         * public ASTDeclaration(List<ASTToken> specList, List<ASTInitList> initList) {
         * super("Declaration");
         * this.specifiers = specList;
         * this.initLists = initList;
         * }
         */
        ASTToken s = typeSpec();
        if (s != null) {
            dec.specifiers = new ArrayList<ASTToken>();
            dec.specifiers.add(s);
            // dec.children.add(s);
        }

        // INIT_DECLARATOR_LIST
        ArrayList<ASTInitList> init_dec_l = initDeclaratorList();
        if (init_dec_l != null) {
            dec.initLists = new ArrayList<ASTInitList>();
            dec.initLists.addAll(init_dec_l);
            // dec.children.addAll(init_dec_l);
        }
        matchToken("';'");
        return dec;
    }

    // INIT_DECLARATOR_LIST →INIT_DECLARATOR INIT_DECLARATOR_LIST_0
    public ArrayList<ASTInitList> initDeclaratorList() {
        ArrayList<ASTInitList> init_dec_l = new ArrayList<ASTInitList>();

        ASTInitList init_dec = initDeclarator();
        init_dec_l.add(init_dec);
        /**
         * public ASTInitList(ASTDeclarator d, List<ASTExpression> e) {
         * super("InitList");
         * this.declarator = d;
         * this.exprs = e;
         * }
         */

        ArrayList<ASTInitList> init_dec_l_0 = initDeclaratorList0();
        if (init_dec_l_0 != null) {
            init_dec_l.addAll(init_dec_l_0);
        }

        return init_dec_l;
    }

    // INIT_DECLARATOR_LIST_0→, INIT_DECLARATOR INIT_DECLARATOR_LIST_0 | ε
    public ArrayList<ASTInitList> initDeclaratorList0() {
        nextToken = getToken(tokenIndex);
        if (nextToken.type.equals("';'")) {
            return null; // is e
        } else {
            matchToken("','");
            ArrayList<ASTInitList> init_dec_l_0 = new ArrayList<ASTInitList>();

            ASTInitList init_dec = initDeclarator();
            init_dec_l_0.add(init_dec);

            ArrayList<ASTInitList> _init_dec_l_0 = initDeclaratorList0();
            if (_init_dec_l_0 != null) {
                init_dec_l_0.addAll(_init_dec_l_0);
            }
            return init_dec_l_0;
        }
    }

    // INIT_DECLARATOR→ DIR_DECLARATOR | DIR_DECLARATOR = ASSIGN_EXPR
    public ASTInitList initDeclarator() {
        ASTInitList init_dec = new ASTInitList();
        init_dec.exprs = new ArrayList<ASTExpression>();

        ASTDeclarator dec = dirDeclarator();

        // ASTVariableDeclarator vd = new ASTVariableDeclarator();

        // ASTIdentifier id = new ASTIdentifier(getToken(tokenIndex).lexme, tokenIndex);

        // matchToken("Identifier");
        // vd.identifier = id;
        // init_dec.children.add(id);

        nextToken = tknList.get(tokenIndex);
        if (nextToken.type.equals("'='")) {
            matchToken("'='");
            ASTExpression as_expr = assignExpr();
            if (as_expr != null) {

                init_dec.exprs.add(as_expr);
                // init_dec.children.add(as_expr);
            }

        }
        init_dec.declarator = dec;
        // init_dec.children.add(dec);

        return init_dec;

    }

    // DIR_DECLARATOR→ID DIR_DECLARATOR_0
    public ASTDeclarator dirDeclarator() {
        ASTIdentifier id = new ASTIdentifier(getToken(tokenIndex).lexme, tokenIndex);

        matchToken("Identifier");
        nextToken = getToken(tokenIndex);
        if (nextToken.type.equals("'['")) {

            ASTArrayDeclarator ad0 = new ASTArrayDeclarator();

            ASTVariableDeclarator vd = new ASTVariableDeclarator();
            vd.identifier = id;

            ad0.declarator = vd;
            matchToken("'['");
            ASTExpression expr = assignExpr();
            if (expr != null) {
                ad0.expr = expr;
            }
            matchToken("']'");
            ASTArrayDeclarator ad = dirDeclarator0_array(ad0);
            // ad.declarator = vd;
            return ad;
        } else {
            ASTVariableDeclarator vd = dirDeclarator0_variable();
            vd.identifier = id;
            return vd;
        }

    }

    // add in lab6.
    // DIR_DECLARATOR_0→| [ASSIGN_EXPR] DIR_DECLARATOR_0| [] DIR_DECLARATOR_0 |ε
    public ASTArrayDeclarator dirDeclarator0_array(ASTArrayDeclarator ad0) {

        if (nextToken.type.equals("'['")) {
            ASTArrayDeclarator ad = new ASTArrayDeclarator();
            ad.declarator = ad0;
            matchToken("'['");
            ASTExpression expr = assignExpr();
            if (expr != null) {
                ad.expr = expr;
            }

            matchToken("']'");
            ASTArrayDeclarator ad1 = dirDeclarator0_array(ad);

            return ad1;
        }

        return ad0;

    }

    public ASTVariableDeclarator dirDeclarator0_variable() {
        ASTVariableDeclarator vd = new ASTVariableDeclarator();
        return vd;
    }

    // EXPR→ASSIGN_EXPR EXPR_0
    public ArrayList<ASTExpression> expr() {
        ArrayList<ASTExpression> expr = new ArrayList<ASTExpression>();

        ASTExpression as_expr = assignExpr();
        expr.add(as_expr);

        ArrayList<ASTExpression> expr_0 = expr0();
        if (expr_0 != null) {
            expr.addAll(expr_0);
        }

        return expr;
    }

    // EXPR_0→, ASSIGN_EXPR EXPR_0|ε
    public ArrayList<ASTExpression> expr0() {
        ArrayList<ASTExpression> expr0 = new ArrayList<ASTExpression>();
        nextToken = tknList.get(tokenIndex);
        if (!nextToken.type.equals("','")) {
            return null;
        } else {
            matchToken("','");

            ASTExpression as_expr = assignExpr();
            expr0.add(as_expr);

            ArrayList<ASTExpression> _expr0 = expr0();
            if (_expr0 != null) {
                expr0.addAll(_expr0);
            }
            return expr0;
        }
    }

    // add in lab6.
    // ACCESS→[ASSIGN_EXPR ] ACCESS | ε
    public ASTArrayAccess access(ASTArrayAccess aa0) {

        if (nextToken.type.equals("'['")) {
            ASTArrayAccess aa = new ASTArrayAccess();

            aa.arrayName = aa0;

            matchToken("'['");
            ASTExpression expr = assignExpr();
            if (expr != null) {
                aa.elements = new LinkedList<ASTExpression>();
                aa.elements.add(expr);
            }

            matchToken("']'");

            ASTArrayAccess aa1 = access(aa);
            return aa1;
        }

        return aa0;
    }

    // ASSIGN_EXPR→EQU_EXPR| DIR_DECLARATOR ASSIGN_OPT ASSIGN_EXPR
    public ASTExpression assignExpr() {
        int tid = tokenIndex;
        boolean array_access = false;
        nextToken = getToken(tokenIndex);
        if (nextToken.type.equals("Identifier")) {
            // nextToken = getToken(tokenIndex + 1);

            // 采用回溯
            matchToken("Identifier");
            nextToken = getToken(tokenIndex);
            if (nextToken.type.equals("'['")) {
                array_access = true;
                ASTArrayAccess a00 = new ASTArrayAccess();
                matchToken("'['");
                ASTExpression expr = assignExpr();
                if (expr != null) {
                    a00.elements = new LinkedList<ASTExpression>();
                    a00.elements.add(expr);
                }
                matchToken("']'");
                ASTArrayAccess aa0 = access(a00);
            }
            nextToken = getToken(tokenIndex);

            // lab 8
            // some problems occurs when multiple dims array.
            if (isAssignOpt()) {
                tokenIndex = tid;
                if (array_access) {
                    ASTBinaryExpression b = new ASTBinaryExpression();
                    ASTArrayAccess a0 = new ASTArrayAccess();
                    ASTIdentifier id = new ASTIdentifier(getToken(tokenIndex).lexme, tokenIndex);
                    matchToken("Identifier");
                    a0.arrayName = id;
                    matchToken("'['");
                    ASTExpression expr = assignExpr();
                    if (expr != null) {
                        a0.elements = new LinkedList<ASTExpression>();
                        a0.elements.add(expr);
                    }

                    matchToken("']'");
                    ASTArrayAccess aa = access(a0);

                    // aa.arrayName = id;

                    b.expr1 = aa;

                    // nextToken = getToken(tokenIndex);
                    ASTToken tkn = new ASTToken();
                    tkn.tokenId = tokenIndex;
                    tkn.value = nextToken.lexme;
                    // no need to matchToken because has match in isAssignOpt.
                    tokenIndex++;
                    // matchToken("'='");
                    b.op = tkn;

                    ASTExpression as = assignExpr();
                    b.expr2 = as;
                    return b;
                } else {
                    ASTBinaryExpression b = new ASTBinaryExpression();

                    ASTIdentifier id = new ASTIdentifier(getToken(tokenIndex).lexme, tokenIndex);
                    b.expr1 = id;
                    matchToken("Identifier");

                    ASTToken tkn = new ASTToken();
                    tkn.tokenId = tokenIndex;
                    tkn.value = nextToken.lexme;
                    // no need to matchToken because has 'match' in isAssignOpt.
                    tokenIndex++;
                    // matchToken("'='");
                    b.op = tkn;

                    ASTExpression as = assignExpr();
                    b.expr2 = as;
                    return b;
                }

            }

        }
        // 恢复
        tokenIndex = tid;
        ASTExpression as = equExpr();
        return as;
    }

    // assign_opt
    // ASSIGN_OPT→=|*=| /=| %=| +=| -=|<<=| >>=| &=| ^=| |=
    public boolean isAssignOpt() {
        nextToken = getToken(tokenIndex);
        if (nextToken.type.equals("'='") || nextToken.type.equals("'*='") || nextToken.type.equals("'/='")
                || nextToken.type.equals("'%='") || nextToken.type.equals("'+='") || nextToken.type.equals("'-='")
                || nextToken.type.equals("'<<='") || nextToken.type.equals("'>>='") || nextToken.type.equals("'&='")
                || nextToken.type.equals("'^='") || nextToken.type.equals("'|='")) {
            return true;
        }
        return false;
    }

    // EQU_EXPR→RELA_EXPR EQU_EXPR_0
    public ASTExpression equExpr() {
        ASTExpression a = relaExpr();
        ASTBinaryExpression b = equExpr0();

        if (b != null) {
            b.expr1 = a;
            return b;
        } else {
            return a;
        }
    }

    // EQU_EXPR_0→== RELA_EXPR EQU_EXPR_0 | != RELA_EXPR EQU_EXPR_0|ε
    public ASTBinaryExpression equExpr0() {
        nextToken = tknList.get(tokenIndex);
        if (nextToken.type.equals("';'"))
            return null;

        if (nextToken.type.equals("'=='")) {
            ASTBinaryExpression b = new ASTBinaryExpression();

            ASTToken tkn = new ASTToken();
            tkn.tokenId = tokenIndex;
            tkn.value = nextToken.lexme;
            matchToken("'=='");

            b.op = tkn;
            b.expr2 = relaExpr();

            ASTBinaryExpression expr = equExpr0();
            if (expr != null) {
                expr.expr1 = b;
                return expr;
            }

            return b;
        } else if (nextToken.type.equals("'!='")) {
            ASTBinaryExpression b = new ASTBinaryExpression();

            ASTToken tkn = new ASTToken();
            tkn.tokenId = tokenIndex;
            tkn.value = nextToken.lexme;
            matchToken("'!='");

            b.op = tkn;
            b.expr2 = relaExpr();

            ASTBinaryExpression expr = equExpr0();
            if (expr != null) {
                expr.expr1 = b;
                return expr;
            }

            return b;
        } else {
            return null;
        }
    }

    // add in lab6.
    // RELA_EXPR→SHIFT_EXPR RELA_EXPR_0
    public ASTExpression relaExpr() {
        ASTExpression a = shiftExpr();
        ASTBinaryExpression b = relaExpr0();

        if (b != null) {
            b.expr1 = a;
            return b;
        } else {
            return a;
        }
    }

    // add in lab6.
    // RELA_EXPR_0→< SHIFT_EXPR RELA_EXPR_0 | > SHIFT_EXPR RELA_EXPR_0| <=
    // SHIFT_EXPR RELA_EXPR_0 | >= SHIFT_EXPR RELA_EXPR_0|ε
    public ASTBinaryExpression relaExpr0() {
        nextToken = tknList.get(tokenIndex);
        if (nextToken.type.equals("';'"))
            return null;

        if (nextToken.type.equals("'<'")) {
            ASTBinaryExpression b = new ASTBinaryExpression();

            ASTToken tkn = new ASTToken();
            tkn.value = nextToken.lexme;
            tkn.tokenId = tokenIndex;
            matchToken("'<'");

            b.op = tkn;
            b.expr2 = shiftExpr();

            ASTBinaryExpression expr = relaExpr0();
            if (expr != null) {
                expr.expr1 = b;
                return expr;
            }

            return b;
        } else if (nextToken.type.equals("'>'")) {
            ASTBinaryExpression b = new ASTBinaryExpression();

            ASTToken tkn = new ASTToken();
            tkn.tokenId = tokenIndex;
            tkn.value = nextToken.lexme;
            matchToken("'>'");

            b.op = tkn;
            b.expr2 = shiftExpr();

            ASTBinaryExpression expr = relaExpr0();
            if (expr != null) {
                expr.expr1 = b;
                return expr;
            }

            return b;
        } else if (nextToken.type.equals("'>='")) {
            ASTBinaryExpression b = new ASTBinaryExpression();

            ASTToken tkn = new ASTToken();
            tkn.tokenId = tokenIndex;
            tkn.value = nextToken.lexme;
            matchToken("'>='");

            b.op = tkn;
            b.expr2 = shiftExpr();

            ASTBinaryExpression expr = relaExpr0();
            if (expr != null) {
                expr.expr1 = b;
                return expr;
            }

            return b;
        } else if (nextToken.type.equals("'<='")) {
            ASTBinaryExpression b = new ASTBinaryExpression();

            ASTToken tkn = new ASTToken();
            tkn.tokenId = tokenIndex;
            tkn.value = nextToken.lexme;
            matchToken("'<='");

            b.op = tkn;
            b.expr2 = shiftExpr();

            ASTBinaryExpression expr = relaExpr0();
            if (expr != null) {
                expr.expr1 = b;
                return expr;
            }

            return b;
        } else {
            return null;
        }
    }

    // SHIFT_EXPR→ADD_EXPR SHIFT_EXPR_0
    public ASTExpression shiftExpr() {
        ASTExpression a = addExpr();
        ASTBinaryExpression b = shiftExpr0();

        if (b != null) {
            b.expr1 = a;
            return b;
        } else {
            return a;
        }
    }

    // SHIFT_EXPR_0→<< ADD_EXPR SHIFT_EXPR_0 | >> ADD_EXPR SHIFT_EXPR_0 | ε
    public ASTBinaryExpression shiftExpr0() {
        nextToken = tknList.get(tokenIndex);
        if (nextToken.type.equals("';'"))
            return null;

        if (nextToken.type.equals("'<<'")) {
            ASTBinaryExpression b = new ASTBinaryExpression();

            ASTToken tkn = new ASTToken();
            tkn.tokenId = tokenIndex;
            tkn.value = nextToken.lexme;
            matchToken("'<<'");

            b.op = tkn;
            b.expr2 = mulExpr();

            ASTBinaryExpression expr = addExpr0();
            if (expr != null) {
                expr.expr1 = b;
                return expr;
            }
            return b;
        } else if (nextToken.type.equals("'>>'")) {
            ASTBinaryExpression b = new ASTBinaryExpression();

            ASTToken tkn = new ASTToken();
            tkn.tokenId = tokenIndex;
            tkn.value = nextToken.lexme;
            matchToken("'>>'");

            b.op = tkn;
            b.expr2 = mulExpr();

            ASTBinaryExpression expr = addExpr0();
            if (expr != null) {
                expr.expr1 = b;
                return expr;
            }

            return b;
        } else {
            return null;
        }
    }

    // ADD_EXPR→MUL_EXPR ADD_EXPR_0
    public ASTExpression addExpr() {
        ASTExpression a = mulExpr();
        ASTBinaryExpression b = addExpr0();

        if (b != null) {
            b.expr1 = a;
            return b;
        } else {
            return a;
        }
    }

    // ADD_EXPR_0→+MUL_EXPR ADD_EXPR_0| -MUL_EXPR ADD_EXPR_0|ε
    public ASTBinaryExpression addExpr0() {
        nextToken = tknList.get(tokenIndex);
        if (nextToken.type.equals("';'"))
            return null;

        if (nextToken.type.equals("'+'")) {
            ASTBinaryExpression b = new ASTBinaryExpression();

            ASTToken tkn = new ASTToken();
            tkn.tokenId = tokenIndex;
            tkn.value = nextToken.lexme;
            matchToken("'+'");

            b.op = tkn;
            b.expr2 = mulExpr();

            ASTBinaryExpression expr = addExpr0();
            if (expr != null) {
                expr.expr1 = b;
                return expr;
            }
            return b;
        } else if (nextToken.type.equals("'-'")) {
            ASTBinaryExpression b = new ASTBinaryExpression();

            ASTToken tkn = new ASTToken();
            tkn.tokenId = tokenIndex;
            tkn.value = nextToken.lexme;
            matchToken("'-'");

            b.op = tkn;
            b.expr2 = mulExpr();

            ASTBinaryExpression expr = addExpr0();
            if (expr != null) {
                expr.expr1 = b;
                return expr;
            }

            return b;
        } else {
            return null;
        }
    }

    // MUL_EXPR→UNRY_EXPR MUL_EXPR_0
    public ASTExpression mulExpr() {
        ASTExpression a = unryExpr();
        ASTBinaryExpression b = mulExpr0();

        if (b != null) {
            b.expr1 = a;
            return b;
        } else {
            return a;
        }
    }

    // MUL_EXPR_0→*UNRY_EXPR MUL_EXPR_0|/UNRY_EXPR MUL_EXPR_0|ε
    public ASTBinaryExpression mulExpr0() {
        nextToken = tknList.get(tokenIndex);
        if (nextToken.type.equals("';'"))
            return null;

        if (nextToken.type.equals("'*'")) {
            ASTBinaryExpression b = new ASTBinaryExpression();

            ASTToken tkn = new ASTToken();
            tkn.tokenId = tokenIndex;
            tkn.value = nextToken.lexme;
            matchToken("'*'");

            b.op = tkn;
            b.expr2 = unryExpr();

            ASTBinaryExpression expr = mulExpr0();
            if (expr != null) {
                expr.expr1 = b;
                return expr;
            }

            return b;
        } else if (nextToken.type.equals("'/'")) {
            ASTBinaryExpression b = new ASTBinaryExpression();

            ASTToken tkn = new ASTToken();
            tkn.tokenId = tokenIndex;
            tkn.value = nextToken.lexme;
            matchToken("'/'");

            b.op = tkn;
            b.expr2 = unryExpr();

            ASTBinaryExpression expr = mulExpr0();
            if (expr != null) {
                expr.expr1 = b;
                return expr;
            }

            return b;
        } else if (nextToken.type.equals("'%'")) {
            ASTBinaryExpression b = new ASTBinaryExpression();

            ASTToken tkn = new ASTToken();
            tkn.tokenId = tokenIndex;
            tkn.value = nextToken.lexme;
            matchToken("'%'");

            b.op = tkn;
            b.expr2 = unryExpr();

            ASTBinaryExpression expr = mulExpr0();
            if (expr != null) {
                expr.expr1 = b;
                return expr;
            }

            return b;
        }

        else {
            return null;
        }
    }

    // UNRY_EXPR→POSTFX_EXPR|++ UNRY_EXPR|-- UNRY_EXPR
    public ASTExpression unryExpr() {
        nextToken = tknList.get(tokenIndex);
        if (nextToken.type.equals("'++'")) {
            ASTUnaryExpression u = new ASTUnaryExpression();
            ASTToken tkn = new ASTToken();
            tkn.tokenId = tokenIndex;
            tkn.value = nextToken.lexme;
            matchToken("'++'");
            u.op = tkn;
            u.expr = unryExpr();

            return u;

        } else if (nextToken.type.equals("'--'")) {
            ASTUnaryExpression u = new ASTUnaryExpression();
            ASTToken tkn = new ASTToken();
            tkn.tokenId = tokenIndex;
            tkn.value = nextToken.lexme;
            matchToken("'--'");
            u.op = tkn;
            u.expr = unryExpr();

            return u;
        } else {
            ASTExpression a = postfxExpr();
            return a;
        }

    }

    // POSTFX_EXPR→FACTOR POSTFX_EXPR_0
    // POSTFX_EXPR→FACTOR POSTFX_EXPR_0|FUNC_CALL POSTFX_EXPR_0|ARY_ACCESS
    // POSTFX_EXPR_0
    public ASTExpression postfxExpr() {
        /**
         * in this case, we need to classify FACTOR and FUNC_CALL and ARY_ACCESS .
         * FUNC_CALL has a '(' after ID.
         * ARY_ACCESS has a '[' after ID.
         * otherwise is FACTOR.
         */
        nextToken = tknList.get(tokenIndex);
        if (nextToken.type.equals("Identifier")) {
            ScannerToken st1 = tknList.get(tokenIndex + 1);
            if (st1.type.equals("'('")) {
                ASTFunctionCall func_call = funcCall();
                ASTPostfixExpression p = postfxExpr0();
                if (p != null) {
                    p.expr = func_call;
                    return p;
                } else {
                    return func_call;
                }
            } else if (st1.type.equals("'['")) {
                ASTArrayAccess ary_access = aryAccess();
                ASTPostfixExpression p = postfxExpr0();
                if (p != null) {
                    p.expr = ary_access;
                    return p;
                } else {
                    return ary_access;
                }
            }
        }
        // otherwise:factor
        ASTExpression f = factor();
        ASTPostfixExpression p = postfxExpr0();
        if (p != null) {
            p.expr = f;
            return p;
        } else {
            return f;
        }
    }

    // FUNC_CALL→ID (ARG_EXPR_LIST )
    public ASTFunctionCall funcCall() {
        ASTFunctionCall func_call = new ASTFunctionCall();
        func_call.argList = new LinkedList<ASTExpression>();// always []

        ASTIdentifier id = new ASTIdentifier(getToken(tokenIndex).lexme, tokenIndex);
        func_call.funcname = id;
        matchToken("Identifier");

        matchToken("'('");
        ArrayList<ASTExpression> l = argExprList();
        if (l != null) {

            func_call.argList.addAll(l);
        }

        matchToken("')'");
        return func_call;
    }

    // add in lab6.
    // ARY_ACCESS→ID [ASSIGN_EXPR ]ACCESS
    public ASTArrayAccess aryAccess() {
        ASTArrayAccess a0 = new ASTArrayAccess();

        ASTIdentifier id = new ASTIdentifier(getToken(tokenIndex).lexme, tokenIndex);
        matchToken("Identifier");

        a0.arrayName = id;

        matchToken("'['");
        ASTExpression expr = assignExpr();
        if (expr != null) {
            a0.elements = new LinkedList<ASTExpression>();
            a0.elements.add(expr);
        }

        matchToken("']'");
        ASTArrayAccess a = access(a0);

        return a;

    }

    // ARG_EXPR_LIST→ε|ASSIGN_EXPR| ASSIGN_EXPR , ARG_EXPR_LIST
    public ArrayList<ASTExpression> argExprList() {
        ArrayList<ASTExpression> l = new ArrayList<ASTExpression>();
        nextToken = getToken(tokenIndex);
        if (nextToken.type.equals("')'")) {
            return null;
        } else {
            ASTExpression assign_expr = assignExpr();
            l.add(assign_expr);
            nextToken = getToken(tokenIndex);
            if (nextToken.type.equals("','")) {
                matchToken("','");
                ArrayList<ASTExpression> _l = argExprList();
                if (_l != null) {
                    l.addAll(_l);
                }
            }
            return l;

        }
    }

    // POSTFX_EXPR_0→++ POSTFX_EXPR_0| -- POSTFX_EXPR_0|ε
    public ASTPostfixExpression postfxExpr0() {
        nextToken = tknList.get(tokenIndex);
        if (!nextToken.type.equals("'++'") && nextToken.type.equals("'--'")) {
            return null;
        }

        if (nextToken.type.equals("'++'")) {
            ASTPostfixExpression p = new ASTPostfixExpression();

            ASTToken tkn = new ASTToken();
            tkn.tokenId = tokenIndex;
            tkn.value = nextToken.lexme;
            matchToken("'++'");

            p.op = tkn;
            p.expr = postfxExpr0();

            return p;
        } else if (nextToken.type.equals("'--'")) {
            ASTPostfixExpression p = new ASTPostfixExpression();

            ASTToken tkn = new ASTToken();
            tkn.tokenId = tokenIndex;
            tkn.value = nextToken.lexme;
            matchToken("'--'");

            p.op = tkn;
            p.expr = postfxExpr0();

            return p;
        }

        else {
            return null;
        }
    }

    // FACTOR --> ID | CONST | (assign_expr)
    public ASTExpression factor() {
        nextToken = tknList.get(tokenIndex);
        if (nextToken.type.equals("Identifier")) {
            ASTIdentifier id = new ASTIdentifier(getToken(tokenIndex).lexme, tokenIndex);
            matchToken("Identifier");
            return id;
        } else if (nextToken.type.equals("IntegerConstant")) {
            ASTIntegerConstant it = new ASTIntegerConstant();
            it.tokenId = tokenIndex;
            it.value = Integer.parseInt(nextToken.lexme);
            matchToken("IntegerConstant");
            return it;
        } else if (nextToken.type.equals("FloatingConstant")) {
            ASTFloatConstant ft = new ASTFloatConstant();
            ft.tokenId = tokenIndex;
            ft.value = Double.parseDouble(nextToken.lexme);
            matchToken("FloatingConstant");
            return ft;
        } else if (nextToken.type.equals("CharacterConstant")) {
            ASTCharConstant ch = new ASTCharConstant();
            ch.tokenId = tokenIndex;
            ch.value = nextToken.lexme;
            matchToken("CharacterConstant");
            return ch;
        } else if (nextToken.type.equals("StringLiteral")) {
            ASTStringConstant sh = new ASTStringConstant();
            sh.tokenId = tokenIndex;
            sh.value = nextToken.lexme;
            matchToken("StringLiteral");
            return sh;
        } else if (nextToken.type.equals("'('")) {
            matchToken("'('");
            ASTExpression expr = assignExpr();
            matchToken("')'");
            return expr;
        } else {
            return null;
        }
    }

    // CMPD_STMT →{ BLOCK_ITEM_LIST }
    public ASTCompoundStatement cmpdStmt() {
        if (nextToken.type.equals("'{'")) {
            matchToken("'{'");
            ASTCompoundStatement cs = blockItemList();
            matchToken("'}'");
            return cs;
        } else if (nextToken.type.equals("';'")) {
            matchToken("';'");
            ASTCompoundStatement cs = new ASTCompoundStatement();
            return cs;
        } else {
            return null;
        }

    }

    // BLOCK_ITEM_LIST →BLOCK_ITEM BLOCK_ITEM_LIST |ε
    public ASTCompoundStatement blockItemList() {
        nextToken = getToken(tokenIndex);
        if (nextToken.type.equals("'}'"))
            return null;
        else {
            ASTCompoundStatement cs = new ASTCompoundStatement();
            ASTNode bk = blockItem();
            cs.blockItems.add(bk);

            ASTCompoundStatement cs2 = blockItemList();
            if (cs2 != null)
                cs.blockItems.addAll(cs2.blockItems);
            return cs;
        }
    }

    // BLOCK_ITEM→DECLARATION | STMT
    public ASTNode blockItem() {
        nextToken = tknList.get(tokenIndex);
        // if (nextToken.type.equals("'return'") || nextToken.type.equals("'break'") ||
        // nextToken.type.equals("'continue'")
        // || nextToken.type.equals("'goto'") || nextToken.type.equals("'{'") ||
        // nextToken.type.equals("'for'")
        // || nextToken.type.equals("'if'") || nextToken.type.equals("Identifier"))
        if (!nextToken.type.equals("'int'") && !nextToken.type.equals("'char'") && !nextToken.type.equals("'float'")
                && !nextToken.type.equals("'double'") && !nextToken.type.equals("'void'")) {
            ASTStatement s = stmt();
            return s;
        } else {
            ASTDeclaration dec = declaration();
            return dec;
        }

    }

    // STMT --> CMPD_STMT | JMP_STMT | ITER_STMT | SLCT_STMT |
    // EXPR_STMT
    public ASTStatement stmt() {
        nextToken = tknList.get(tokenIndex);

        if (nextToken.type.equals("'return'")) {
            matchToken("'return'");
            ASTReturnStatement jmp = new ASTReturnStatement();

            ASTExpression e = assignExpr();
            matchToken("';'");
            if (e != null) {
                jmp.expr.add(e);
            } else {
                jmp.expr = null;
            }

            return jmp;
        } else if (nextToken.type.equals("'break'")) {
            matchToken("'break'");
            ASTBreakStatement jmp = new ASTBreakStatement();
            matchToken("';'");
            return jmp;
        } else if (nextToken.type.equals("'continue'")) {
            matchToken("'continue'");
            ASTContinueStatement jmp = new ASTContinueStatement();
            matchToken("';'");
            return jmp;
        } else if (nextToken.type.equals("'goto'")) {
            matchToken("'goto'");
            ASTGotoStatement jmp = new ASTGotoStatement();
            ASTIdentifier id = new ASTIdentifier(getToken(tokenIndex).lexme, tokenIndex);
            matchToken("Identifier");
            jmp.label = id;
            matchToken("';'");
            return jmp;
        } else if (nextToken.type.equals("'if'")) {
            matchToken("'if'");
            matchToken("'('");

            // cond then otherwise
            ASTSelectionStatement jmp = new ASTSelectionStatement();

            ASTExpression cond = assignExpr();
            if (cond != null) {
                jmp.cond = new LinkedList<ASTExpression>();
                jmp.cond.add(cond);
            }

            matchToken("')'");

            ASTStatement st_then = stmt();
            jmp.then = st_then;

            nextToken = tknList.get(tokenIndex);
            if (nextToken.type.equals("'else'")) {
                matchToken("'else'");
                ASTStatement st_otherwise = stmt();
                jmp.otherwise = st_otherwise;
            }

            return jmp;
        } else if (nextToken.type.equals("'for'")) {

            matchToken("'for'");
            matchToken("'('");
            nextToken = getToken(tokenIndex);
            if (nextToken.type.equals("'int'") || nextToken.type.equals("'float'") || nextToken.type.equals("'double'")
                    || nextToken.type.equals("'char'") || nextToken.type.equals("'void'")) {

                ASTIterationDeclaredStatement jmp = new ASTIterationDeclaredStatement();

                /**
                 * this.init = init;
                 * this.cond = cond;
                 * this.step = step;
                 * this.stat = stat;
                 */

                ASTDeclaration dec = declaration(); // TODO:use expression
                if (dec != null) {
                    jmp.init = dec;
                }

                // matchToken("';'"); declaration has a ;
                ASTExpression cond = assignExpr();
                if (cond != null) {
                    jmp.cond = new LinkedList<ASTExpression>();
                    jmp.cond.add(cond);
                }

                matchToken("';'");
                ASTExpression step = assignExpr();
                if (step != null) {
                    jmp.step = new LinkedList<ASTExpression>();
                    jmp.step.add(step);
                }

                matchToken("')'");

                ASTStatement stat = stmt();
                jmp.stat = stat;

                return jmp;
            } else {
                ASTIterationStatement jmp = new ASTIterationStatement();

                /**
                 * this.init = init;
                 * this.cond = cond;
                 * this.step = step;
                 * this.stat = stat;
                 */

                ASTExpression init = assignExpr(); // TODO:use expression
                if (init != null) {
                    jmp.init = new LinkedList<ASTExpression>();
                    jmp.init.add(init);
                }

                matchToken("';'");
                ASTExpression cond = assignExpr();
                if (cond != null) {
                    jmp.cond = new LinkedList<ASTExpression>();
                    jmp.cond.add(cond);
                }

                matchToken("';'");
                ASTExpression step = assignExpr();
                if (step != null) {
                    jmp.step = new LinkedList<ASTExpression>();
                    jmp.step.add(step);
                }

                matchToken("')'");

                ASTStatement stat = stmt();
                jmp.stat = stat;

                return jmp;
            }

        } else if (nextToken.type.equals("'{'")) {
            ASTCompoundStatement cmpd_stmt = cmpdStmt();
            return cmpd_stmt;
        } else if (nextToken.type.equals("Identifier")) {
            /**
             * add in lab6.
             */
            nextToken = getToken(tokenIndex + 1);
            if (nextToken.type.equals("':'")) {

                ASTLabeledStatement lb_stmt = new ASTLabeledStatement();
                ASTIdentifier id = new ASTIdentifier(getToken(tokenIndex).lexme, tokenIndex);
                matchToken("Identifier");
                matchToken("':'");
                lb_stmt.label = id;
                ASTStatement stat = stmt();
                lb_stmt.stat = stat;

                return lb_stmt;
            } else {// EXPR_STMT

                ASTExpressionStatement expr_stmt = exprStmt();
                return expr_stmt;
            }

        } else { // EXPR_STMT
            ASTExpressionStatement expr_stmt = exprStmt();
            return expr_stmt;
        }
    }

    // EXPR_STMT→ASSIGN_EXPR; | ;
    public ASTExpressionStatement exprStmt() {
        ASTExpressionStatement e = new ASTExpressionStatement();
        // e.exprs = new LinkedList<ASTExpression>();
        nextToken = tknList.get(tokenIndex);

        ASTExpression expr = assignExpr();

        if (expr != null) {
            e.exprs = new LinkedList<ASTExpression>();
            e.exprs.add(expr);
        }

        matchToken("';'");

        return e;
    }

}
