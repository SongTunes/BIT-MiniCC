package bit.minisys.minicc.icgen;

import bit.minisys.minicc.parser.ast.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class MiniCCICBuilder implements ASTVisitor {
    public static final String jt = "jt";
    public static final String jf = "jf";
    public static final String jmp = "jmp";
    public static final String cmp = "cmp";
    public static final String call = "call";
    public static final String param = "param";
    public static final String ret = "ret";
    public static final String inc = "inc";
    public static final String dec = "dec";
    public static final String lea = "lea";

    public static final String ci = "_int_";
    public static final String cf = "_float_";
    public static final String cc = "_char_";
    public static final String cs = "_string_";

    private Map<ASTNode, ASTNode> map; // ???map?锟斤拷???????????key???????value??????????value????????ASTIdentifier,ASTIntegerConstant,TemportaryValue...
    private List<Quat> quats; // ??????????锟斤拷?
    private Integer tmpId; // ??????????

    private Integer constIdInt;
    private Integer constIdFloat;
    private Integer constIdChar;
    private Integer constIdString;

    private Stack<Integer> stack_if;
    private Stack<Integer> stack_otherwise;

    private Stack<Integer> stack_for_l2;
    private Stack<Integer> stack_for_l3;

    // private Stack<Integer> stack_aryacs;
    private int cursor_ary;
    private int c_array;

    private Scope currentScope;
    public Scope globalScope;

    private SymbolRow currentRow; // 实时维护当前遍历状态填写的符号表项

    public Map<String, Scope> funcName2Scope; // funcion name to current Map

    private int times_for;
    private boolean needReturn;

    private String current_spec;

    private boolean es02;

    private String errorInfo;

    public MiniCCICBuilder() {

        map = new HashMap<ASTNode, ASTNode>();
        quats = new LinkedList<Quat>();
        tmpId = 0;

        constIdInt = 0;
        constIdFloat = 0;
        constIdChar = 0;
        constIdString = 0;

        stack_if = new Stack<>();
        stack_otherwise = new Stack<>();

        stack_for_l2 = new Stack<>();
        stack_for_l3 = new Stack<>();

        // stack_aryacs = new Stack<>();
        cursor_ary = 0;
        c_array = 0;

        globalScope = new Scope();

        currentScope = globalScope;
        funcName2Scope = new HashMap<String, Scope>();

        times_for = 0;
        needReturn = false;

        currentRow = null;
        errorInfo = "";

        currentRow = new SymbolRow();
        currentRow.type = "Function";
        currentRow.name = "Mars_GetInt";
        currentRow.spec = "int";
        // funcName2Scope.put(currentRow.name, globalScope);
        globalScope.table.put(currentRow.name, currentRow);

        currentRow = new SymbolRow();
        currentRow.type = "Function";
        currentRow.name = "Mars_PrintStr";
        currentRow.spec = "void";
        currentRow.addFuncParams("string");
        // funcName2Scope.put(currentRow.name, globalScope);
        globalScope.table.put(currentRow.name, currentRow);

        currentRow = new SymbolRow();
        currentRow.type = "Function";
        currentRow.name = "Mars_PrintInt";
        currentRow.spec = "void";
        currentRow.addFuncParams("int");
        // funcName2Scope.put(currentRow.name, globalScope);
        globalScope.table.put(currentRow.name, currentRow);
    }

    public List<Quat> getQuats() {
        return quats;
    }

    public Scope getScope() {
        return globalScope;
    }

    public Scope getScope(String funcName) {
        return funcName2Scope.get(funcName);
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void printErrorInfo() {
        System.out.println("errors:");
        System.out.println("----------------------------------------------");
        System.out.println(errorInfo);
        System.out.println("----------------------------------------------");
    }

    public String astStr(ASTNode node) {
        if (node == null) {
            return "";
        } else if (node instanceof ASTIdentifier) {
            return ((ASTIdentifier) node).value;
        } else if (node instanceof ASTIntegerConstant) {
            return ((ASTIntegerConstant) node).value + "";
        } else if (node instanceof TemporaryValue) {
            return ((TemporaryValue) node).name();
        } else {
            return "";
        }
    }

    public void addLabelRow(LabelRow lbr) {

        if (lbr.define == true) { // zipper refill
            int addr = currentScope.lb_table.get(lbr.name).address; // the address to update
            while (addr != 0) {

                Quat q1 = quats.get(addr - 1);
                int next_addr = Integer.parseInt(astStr(q1.getRes())); // the address to update next time

                ASTIntegerConstant a = new ASTIntegerConstant();
                a.value = lbr.address;
                Quat q = new Quat(q1.getOp(), a, q1.getOpnd1(), q1.getOpnd2());

                quats.set(addr - 1, q);

                addr = next_addr;

            }
        }

        currentScope.lb_table.put(lbr.name, lbr);
    }

    public ASTNode genNode(int id) {
        ASTIntegerConstant a = new ASTIntegerConstant();
        a.value = id;
        return a;
    }

    public ASTNode genNode(String s) {
        ASTIdentifier a = new ASTIdentifier();
        a.value = s;
        return a;
    }

    @Override
    public void visit(ASTCompilationUnit program) throws Exception {
        for (ASTNode node : program.items) {
            if (node instanceof ASTFunctionDefine) {
                visit((ASTFunctionDefine) node);
            } else if (node instanceof ASTDeclaration) {
                visit((ASTDeclaration) node);
            }
        }
    }

    @Override
    public void visit(ASTDeclaration declaration) throws Exception {
        // cannot generate the symbolRow now because can be more than one.

        current_spec = declaration.specifiers.get(0).value;
        // set the current_spec

        // visit the other children to fill the symbolRow.
        for (ASTNode node : declaration.initLists) {
            if (node instanceof ASTInitList) {
                visit((ASTInitList) node);
            }
        }

    }

    @Override
    public void visit(ASTArrayDeclarator arrayDeclarator) throws Exception {
        if (arrayDeclarator.declarator instanceof ASTVariableDeclarator) {

            Integer dim = Integer.MAX_VALUE;
            // when the [num] is not a const(such as a[1+2]), use the MAX_VALUE

            if (arrayDeclarator.expr instanceof ASTIntegerConstant) {
                ASTIntegerConstant i = (ASTIntegerConstant) arrayDeclarator.expr;
                dim = i.value;
            }
            currentRow.addArrayDim(dim);

            ASTVariableDeclarator vd = (ASTVariableDeclarator) arrayDeclarator.declarator;
            String name = vd.identifier.value;

            if (currentScope.table.containsKey(name)) {
                errorInfo += ("ES02 >> Declaration:" + name + " has been declarated.\n");
            } else {
                currentRow.name = name;

                currentScope.table.put(currentRow.name, currentRow);
            }

        } else { // still ASTArrayDeclarator
            ASTArrayDeclarator ad = (ASTArrayDeclarator) arrayDeclarator.declarator;

            Integer dim = Integer.MAX_VALUE;
            // when the [num] is not a const(such as a[1+2]), use the MAX_VALUE

            if (arrayDeclarator.expr instanceof ASTIntegerConstant) {
                ASTIntegerConstant i = (ASTIntegerConstant) arrayDeclarator.expr;
                dim = i.value;
            }

            currentRow.addArrayDim(dim);
            visit(ad);

        }
    }

    @Override
    public void visit(ASTVariableDeclarator variableDeclarator) throws Exception {

    }

    @Override
    public void visit(ASTFunctionDeclarator functionDeclarator) throws Exception {

        // 1. check function redefine.

        ASTDeclarator declarator = functionDeclarator.declarator;
        if (declarator instanceof ASTVariableDeclarator) {
            ASTVariableDeclarator vd = (ASTVariableDeclarator) declarator;
            String funcName = vd.identifier.value;
            currentRow.name = funcName;
            Scope newScope = new Scope(currentScope);
            currentScope.childScopes.add(newScope);

            if (currentScope.table.containsKey(funcName)) { // ES02
                errorInfo += ("ES02 >> FunctionDefine:" + funcName + " is defined.\n");
                es02 = true;
            } else {
                funcName2Scope.put(funcName, newScope);
                currentScope.table.put(funcName, currentRow);
                currentScope = newScope;
            }

        }

        // 2. generate function param list.
        for (ASTNode node : functionDeclarator.params) {
            if (node instanceof ASTParamsDeclarator) {
                visit((ASTParamsDeclarator) node);
            }
        }

    }

    @Override
    public void visit(ASTParamsDeclarator paramsDeclarator) throws Exception {
        String spec = paramsDeclarator.specfiers.get(0).value;
        currentRow.addFuncParams(spec);

        if (paramsDeclarator.declarator instanceof ASTVariableDeclarator) {
            ASTVariableDeclarator vd = (ASTVariableDeclarator) paramsDeclarator.declarator;
            String name = vd.getName();
            SymbolRow sbr = new SymbolRow();
            sbr.name = name;
            sbr.type = "Identifier";
            sbr.spec = spec;

            currentScope.table.put(name, sbr);
        }
    }

    @Override
    public void visit(ASTArrayAccess arrayAccess) throws Exception {
        String op1 = "*";
        String op2 = "+";
        String op3 = "-";
        String op4 = "=[]";

        if (arrayAccess.arrayName instanceof ASTIdentifier) {

            ASTIdentifier id = (ASTIdentifier) arrayAccess.arrayName;

            String name = id.value;
            // System.out.println(currentScope.table);
            currentRow = currentScope.getSymbolRow(name);
            // currentRow = currentScope.table.get(name);
            cursor_ary = 0;
            c_array = 1;

            for (int i = 0; i < currentRow.arrayDim - 1; i++) {
                c_array += currentRow.arraySize.get(i);
            }
            if (arrayAccess.elements.get(0) instanceof ASTPostfixExpression) {
                int a = 1;
                for (int i = 0; i < currentRow.arrayDim - cursor_ary - 1; i++) {
                    a *= currentRow.arraySize.get(i);
                }
                Quat q1 = new Quat(op1, new TemporaryValue(++tmpId),
                        (ASTIdentifier) ((ASTPostfixExpression) (arrayAccess.elements.get(0))).expr,
                        genNode(a));
                cursor_ary++;
                quats.add(q1);
                // Integer _tmpId = tmpId;
                // Quat q2 = new Quat(op2, new TemporaryValue(++tmpId),
                // (ASTIdentifier) ((ASTPostfixExpression) (arrayAccess.elements.get(0))).expr,
                // new TemporaryValue(_tmpId));
                // quats.add(q2);

                visit(arrayAccess.elements.get(0));
            } else {
                visit(arrayAccess.elements.get(0));
                int a = 1;
                for (int i = 0; i < currentRow.arrayDim - cursor_ary - 1; i++) {
                    a *= currentRow.arraySize.get(i);
                }
                Quat q1 = new Quat(op1, new TemporaryValue(++tmpId),
                        map.get(arrayAccess.elements.get(0)),
                        genNode(a));
                cursor_ary++;
                quats.add(q1);

            }

        } else {
            visit(arrayAccess.arrayName);

            Integer _tmpId_up = tmpId;
            if (arrayAccess.elements.get(0) instanceof ASTPostfixExpression) {
                int a = 1;
                for (int i = 0; i < currentRow.arrayDim - cursor_ary - 1; i++) {
                    a *= currentRow.arraySize.get(i);
                }
                Quat q1 = new Quat(op1, new TemporaryValue(++tmpId),
                        (ASTIdentifier) ((ASTPostfixExpression) (arrayAccess.elements.get(0))).expr,
                        genNode(a));
                cursor_ary++;
                quats.add(q1);

                Integer _tmpId = tmpId;
                Quat q2 = new Quat(op2, new TemporaryValue(++tmpId), new TemporaryValue(_tmpId_up),
                        new TemporaryValue(_tmpId));
                quats.add(q2);

                visit(arrayAccess.elements.get(0));
            } else {
                visit(arrayAccess.elements.get(0));

                int a = 1;
                for (int i = 0; i < currentRow.arrayDim - cursor_ary - 1; i++) {
                    a *= currentRow.arraySize.get(i);
                }
                Quat q1 = new Quat(op1, new TemporaryValue(++tmpId), map.get(arrayAccess.elements.get(0)),
                        genNode(a));
                cursor_ary++;
                quats.add(q1);

                Integer _tmpId = tmpId;
                Quat q2 = new Quat(op2, new TemporaryValue(++tmpId), new TemporaryValue(_tmpId_up),
                        new TemporaryValue(_tmpId));
                quats.add(q2);
            }

        }
        Integer _tmpId_index = tmpId;

        if (cursor_ary == currentRow.arrayDim) {
            // Quat q0 = new Quat(op3, new TemporaryValue(++tmpId),
            // genNode(currentRow.name), genNode(c_array));
            // quats.add(q0);
            // start with 0!

            Integer _tmpId_start = tmpId;

            TemporaryValue res = new TemporaryValue(++tmpId);
            Quat q = new Quat(op4, res, genNode(currentRow.name),
                    new TemporaryValue(_tmpId_start));
            quats.add(q);

            map.put(arrayAccess, res);

            // lea address
            Quat q1 = new Quat(lea, new TemporaryValue(++tmpId), genNode(currentRow.name),
                    new TemporaryValue(_tmpId_start));
            quats.add(q1);
        }
    }

    @Override
    public void visit(ASTBinaryExpression binaryExpression) throws Exception {
        /**
         * << >> & | ^ check the expr1 and expr2.
         */
        if (binaryExpression.expr1 instanceof ASTIdentifier && binaryExpression.expr2 instanceof ASTIdentifier) {

            ASTIdentifier i1 = (ASTIdentifier) binaryExpression.expr1;
            ASTIdentifier i2 = (ASTIdentifier) binaryExpression.expr2;

            String spec1 = currentScope.getSymbolRow(i1.value).spec;
            String spec2 = currentScope.getSymbolRow(i2.value).spec;

            if (spec1 != spec2) {
                errorInfo += ("ES05 >> BinaryExpression:(<< >> & | ^)expression's should be " + spec1 + ".\n");
            }
        }

        // IC Gen
        String op = binaryExpression.op.value;
        ASTNode res = null;
        ASTNode opnd1 = null;
        ASTNode opnd2 = null;

        if (op.equals("=")) {
            // ???????
            // ?????????????res
            visit(binaryExpression.expr1);
            res = map.get(binaryExpression.expr1);
            // array access
            if (binaryExpression.expr1 instanceof ASTArrayAccess) {
                res = new TemporaryValue(tmpId);
            }
            // ?锟斤拷????????????, ?????????a = b + c; ??????????????tmp1 = b + c; a =
            // tmp1;??????????????????????
            if (binaryExpression.expr2 instanceof ASTIdentifier) {
                opnd1 = binaryExpression.expr2;
            } else if (binaryExpression.expr2 instanceof ASTIntegerConstant) {
                opnd1 = binaryExpression.expr2;
            } else if (binaryExpression.expr2 instanceof ASTBinaryExpression) {
                if (!(binaryExpression.expr1 instanceof ASTArrayAccess)) {
                    ASTBinaryExpression value = (ASTBinaryExpression) binaryExpression.expr2;
                    op = value.op.value;
                    visit(value.expr1);
                    opnd1 = map.get(value.expr1);
                    visit(value.expr2);
                    opnd2 = map.get(value.expr2);
                } else {
                    visit(binaryExpression.expr2);
                    opnd1 = map.get(binaryExpression.expr2); // 5
                }

            } else if (binaryExpression.expr2 instanceof ASTFunctionCall) {
                visit(binaryExpression.expr2); // gen the quat of function call
                int addr = quats.size();
                Quat q0 = quats.get(addr - 1);
                Quat q = new Quat(q0.getOp(), res, q0.getOpnd1(), q0.getOpnd2());
                quats.set(addr - 1, q);
                tmpId--;
                return;

            } else if (binaryExpression.expr2 instanceof ASTArrayAccess) {
                visit(binaryExpression.expr2); // gen the quat of function call
                int addr = quats.size();
                Quat q0 = quats.get(addr - 1);
                Quat q = new Quat(q0.getOp(), res, q0.getOpnd1(), q0.getOpnd2());
                quats.set(addr - 1, q);
                tmpId--;
                return;

            } else {
                visit(binaryExpression.expr2);
                opnd1 = map.get(binaryExpression.expr2);

            }

        } else if (op.equals("+=") || op.equals("-=")) {
            op = op.substring(0, 1);
            visit(binaryExpression.expr1);
            visit(binaryExpression.expr2);
            res = map.get(binaryExpression.expr1);
            opnd1 = res;
            opnd2 = map.get(binaryExpression.expr2);
        } else if (op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/") || op.equals("%")
                || op.equals("<<")) {
            // ?????????????锟斤拷???锟斤拷????
            res = new TemporaryValue(++tmpId);
            visit(binaryExpression.expr1);
            opnd1 = map.get(binaryExpression.expr1);
            visit(binaryExpression.expr2);
            opnd2 = map.get(binaryExpression.expr2);

        } else if (op.equals(">") || op.equals("<") || op.equals(">=") || op.equals("<=") || op.equals("==")) {
            // same as the case up.
            res = new TemporaryValue(++tmpId);
            visit(binaryExpression.expr1);
            opnd1 = map.get(binaryExpression.expr1);
            visit(binaryExpression.expr2);
            opnd2 = map.get(binaryExpression.expr2);
        } else if (op.equals("&&") || op.equals("||")) {
            // same as the case up.
            res = new TemporaryValue(++tmpId);
            visit(binaryExpression.expr1);
            opnd1 = map.get(binaryExpression.expr1);
            visit(binaryExpression.expr2);
            opnd2 = map.get(binaryExpression.expr2);
        } else {
            // else..
        }

        // build quat
        Quat quat = new Quat(op, res, opnd1, opnd2);
        quats.add(quat);
        map.put(binaryExpression, res);

    }

    @Override
    public void visit(ASTBreakStatement breakStat) throws Exception {
        if (times_for == 0) {
            errorInfo += ("ES03 >> BreakStatement:must be in a LoopStatement.\n");
        }
        Quat q = new Quat(jmp, null, null, null);
        quats.add(q);
        stack_for_l3.push(quats.size());
    }

    @Override
    public void visit(ASTContinueStatement continueStatement) throws Exception {

    }

    @Override
    public void visit(ASTCastExpression castExpression) throws Exception {

    }

    @Override
    public void visit(ASTCharConstant charConst) throws Exception {

        // add the symbol row
        SymbolRow sbr = new SymbolRow();
        sbr.type = "Constant";
        sbr.spec = "char";
        sbr.name = charConst.value;
        // sbr.constId = constIdChar++;
        if (currentScope.table.containsKey(charConst.value)) {
            sbr.constId = currentScope.table.get(charConst.value).constId;
        } else {
            sbr.constId = constIdChar++;
            currentScope.table.put(sbr.name, sbr);
        }

        ASTStringConstant a = new ASTStringConstant();
        a.value = cc + Integer.toString(sbr.constId);
        map.put(charConst, a);

    }

    @Override
    public void visit(ASTCompoundStatement compoundStat) throws Exception {
        Scope newScope = new Scope(currentScope);
        currentScope.childScopes.add(newScope);

        // enter a new scope.
        currentScope = newScope;

        for (ASTNode node : compoundStat.blockItems) {
            if (node instanceof ASTDeclaration) {
                visit((ASTDeclaration) node);
            } else if (node instanceof ASTStatement) {
                visit((ASTStatement) node);
            }
        }

        // back to the up scope.
        currentScope = newScope.upScope;

    }

    @Override
    public void visit(ASTConditionExpression conditionExpression) throws Exception {

    }

    @Override
    public void visit(ASTExpression expression) throws Exception {
        if (expression instanceof ASTArrayAccess) {
            currentRow = new SymbolRow();
            currentRow.type = "Array";

            // first_dim = true;

            visit((ASTArrayAccess) expression);
        } else if (expression instanceof ASTBinaryExpression) {
            visit((ASTBinaryExpression) expression);
        } else if (expression instanceof ASTCastExpression) {
            visit((ASTCastExpression) expression);
        } else if (expression instanceof ASTCharConstant) {
            visit((ASTCharConstant) expression);
        } else if (expression instanceof ASTConditionExpression) {
            visit((ASTConditionExpression) expression);
        } else if (expression instanceof ASTFloatConstant) {
            visit((ASTFloatConstant) expression);
        } else if (expression instanceof ASTFunctionCall) {
            visit((ASTFunctionCall) expression);
        } else if (expression instanceof ASTIdentifier) {
            visit((ASTIdentifier) expression);
        } else if (expression instanceof ASTIntegerConstant) {
            visit((ASTIntegerConstant) expression);
        } else if (expression instanceof ASTMemberAccess) {
            visit((ASTMemberAccess) expression);
        } else if (expression instanceof ASTPostfixExpression) {
            visit((ASTPostfixExpression) expression);
        } else if (expression instanceof ASTStringConstant) {
            visit((ASTStringConstant) expression);
        } else if (expression instanceof ASTUnaryExpression) {
            visit((ASTUnaryExpression) expression);
        } else if (expression instanceof ASTUnaryTypename) {
            visit((ASTUnaryTypename) expression);
        }
    }

    @Override
    public void visit(ASTExpressionStatement expressionStat) throws Exception {
        for (ASTExpression node : expressionStat.exprs) {
            visit((ASTExpression) node);
        }
    }

    @Override
    public void visit(ASTFloatConstant floatConst) throws Exception {

        // add the symbol row
        SymbolRow sbr = new SymbolRow();
        sbr.type = "Constant";
        sbr.spec = "float";
        sbr.name = String.valueOf(floatConst.value);
        // sbr.constId = constIdChar++;
        if (currentScope.table.containsKey(sbr.name)) {
            sbr.constId = currentScope.table.get(sbr.name).constId;
        } else {
            sbr.constId = constIdFloat++;
            currentScope.table.put(sbr.name, sbr);
        }

        ASTStringConstant a = new ASTStringConstant();
        a.value = cf + Integer.toString(sbr.constId);
        map.put(floatConst, a);
    }

    @Override
    public void visit(ASTFunctionCall funcCall) throws Exception {
        SymbolRow sbr0 = new SymbolRow();
        sbr0.type = "Function";
        // preparation for check ES04 and calculate params
        for (ASTExpression node : funcCall.argList) {
            if (node instanceof ASTIntegerConstant || node instanceof ASTCharConstant
                    || node instanceof ASTFloatConstant || node instanceof ASTStringConstant) {
                ASTNode node1 = (ASTNode) node;
                if (node1.getType() == "IntegerConstant") {
                    sbr0.addFuncParams("int");
                } else if (node1.getType() == "FloatConstant") {
                    sbr0.addFuncParams("float");
                } else if (node1.getType() == "CharConstant") {
                    sbr0.addFuncParams("char");
                } else if (node1.getType() == "StringConstant") {
                    sbr0.addFuncParams("string");
                }

                visit(node);
            } else {
                // add a empty string. a empty string will always pass the check.
                sbr0.addFuncParams("");

                visit(node);
            }

        }

        // gen quats of params
        for (ASTExpression node : funcCall.argList) {

            Quat q = new Quat(param, map.get(node), null, null);
            quats.add(q);
        }

        // check ESO4
        ASTIdentifier id = (ASTIdentifier) funcCall.funcname;
        String funcName = id.value;
        // System.out.println(funcName);
        SymbolRow sbr = currentScope.getSymbolRow(funcName);
        if (sbr0.paramNum != sbr.paramNum) {
            errorInfo += ("ES04 >> FunctionCall:" + funcName + "'s param num is not matched.\n");
            return;
        } else {
            for (int k = 0; k < sbr0.paramNum; k++) {
                // when "", do not check because can not get the type
                if (sbr0.paramType.get(k) != "") {
                    if (!sbr0.paramType.get(k).equals(sbr.paramType.get(k))) {
                        errorInfo += ("ES04 >> FunctionCall:" + funcName + "'s param type is not matched.\n");
                        return;
                        // break;
                    }
                }
            }
        }
        // pass check
        if (sbr.spec.equals("void")) {
            Quat q = new Quat(call, null, id, null); //
            quats.add(q);
        } else {
            TemporaryValue res = new TemporaryValue(++tmpId);
            Quat q = new Quat(call, res, id, null); //
            quats.add(q);

            map.put(funcCall, res);
        }

        // System.out.println(globalScope.table.keySet());
    }

    @Override
    public void visit(ASTGotoStatement gotoStat) throws Exception {
        // if (!currentScope.table.containsKey(gotoStat.label.value)) { // ES07
        // errorInfo += ("ES07 >> Label: " + gotoStat.label.value + " is not
        // defined.\n");
        // }

        String _name = gotoStat.label.value;
        boolean _define = false;
        int _address = 0;
        if (currentScope.lb_table.containsKey(_name)) {
            _address = currentScope.lb_table.get(_name).address;
            _define = currentScope.lb_table.get(_name).define;
            if (_define) { // label ok

            } else {
                LabelRow lbr = new LabelRow(_name, _define, quats.size() + 1);
                addLabelRow(lbr);
            }
        } else {
            LabelRow lbr = new LabelRow(_name, _define, quats.size() + 1);
            addLabelRow(lbr);
        }

        Quat quat = new Quat(jmp, genNode(_address), null, null);
        quats.add(quat);

    }

    @Override
    public void visit(ASTIdentifier identifier) throws Exception {
        // only arrive here when use(not declaration).
        String name = identifier.value;
        if (currentScope.getSymbolRow(name) == null) { // ES01
            errorInfo += ("ES01 >> Identifier " + name + " is not defined.\n");
        }

        map.put(identifier, identifier);
    }

    @Override
    public void visit(ASTInitList initList) throws Exception {
        ASTDeclarator declarator = initList.declarator;
        if (declarator instanceof ASTVariableDeclarator) {
            SymbolRow sbr = new SymbolRow();
            currentRow = sbr;
            currentRow.type = "Identifier";
            currentRow.spec = current_spec;

            ASTVariableDeclarator vd = (ASTVariableDeclarator) initList.declarator;
            String name = vd.identifier.value;
            if (currentScope.table.containsKey(name)) {
                errorInfo += ("ES02 >> Declaration:" + name + " has been declarated.\n");
            } else {
                currentRow.name = name;
                currentScope.table.put(currentRow.name, currentRow);
            }

            // set the init value
            if (initList.exprs != null && !initList.exprs.isEmpty()) {
                ASTExpression es = initList.exprs.get(0);
                if (es instanceof ASTIntegerConstant) {
                    ASTIntegerConstant i = (ASTIntegerConstant) es;
                    Quat q = new Quat("=", vd.identifier, i, null);
                    quats.add(q);

                    currentRow.intValue = (i.value);
                }
            }

        } else if (declarator instanceof ASTArrayDeclarator) {
            SymbolRow sbr = new SymbolRow();
            currentRow = sbr;
            currentRow.type = "Array";
            currentRow.spec = current_spec;
            visit((ASTArrayDeclarator) declarator);
        }

        // expression
        if (!initList.exprs.isEmpty()) {
            visit(initList.exprs.get(0));
            if (initList.exprs.get(0) instanceof ASTFunctionCall &&
                    declarator instanceof ASTVariableDeclarator) {
                // refill the res of the quat
                Quat q = quats.get(quats.size() - 1);
                Quat _q = new Quat(q.getOp(), ((ASTVariableDeclarator) declarator).identifier, q.getOpnd1(),
                        q.getOpnd2());

                quats.set(quats.size() - 1, _q);
            }
        }

    }

    @Override
    public void visit(ASTIntegerConstant intConst) throws Exception {

        // add the symbol row
        SymbolRow sbr = new SymbolRow();
        sbr.type = "Constant";
        sbr.spec = "int";
        sbr.name = String.valueOf(intConst.value);
        // sbr.constId = constIdChar++;
        if (currentScope.table.containsKey(sbr.name)) {
            sbr.constId = currentScope.table.get(sbr.name).constId;
        } else {
            sbr.constId = constIdInt++;
            currentScope.table.put(sbr.name, sbr);
        }

        ASTStringConstant a = new ASTStringConstant();
        a.value = ci + Integer.toString(sbr.constId);
        map.put(intConst, intConst); //
    }

    @Override
    public void visit(ASTIterationDeclaredStatement iterationDeclaredStat) throws Exception {
        times_for++;
        // init cond step
        visit(iterationDeclaredStat.init);

        int l1 = quats.size() + 1;

        visit(iterationDeclaredStat.cond.get(0));
        // test the condition
        Quat q = new Quat(cmp, new TemporaryValue(++tmpId), quats.get(quats.size() - 1).getRes(), genNode(0));
        quats.add(q);

        Quat qt = new Quat(jt, null, new TemporaryValue(tmpId), null); // l2
        quats.add(qt);
        stack_for_l2.push(quats.size());

        Quat qf = new Quat(jf, null, new TemporaryValue(tmpId), null); // l3
        quats.add(qf);
        stack_for_l3.push(quats.size());

        // step
        int l4 = quats.size() + 1;
        visit(iterationDeclaredStat.step.get(0));

        Quat ql1 = new Quat(jmp, genNode(l1), null, null);
        quats.add(ql1);

        int bf = stack_for_l3.size(); // the stack size before stmt
        // stat
        int l2 = quats.size() + 1;
        visit(iterationDeclaredStat.stat);

        // refill l2
        int _l2 = stack_for_l2.pop();
        Quat qt11 = quats.get(_l2 - 1);
        Quat qt1 = new Quat(jt, genNode(l2), qt11.getOpnd1(), null);
        quats.set(_l2 - 1, qt1);

        // jmp to step
        Quat qj3 = new Quat(jmp, genNode(l4), null, null);
        quats.add(qj3);

        int l3 = quats.size() + 1;
        // refill l3
        // pop all, maybe break

        for (int i = stack_for_l3.size(); i >= bf; i--) {
            int _l3 = stack_for_l3.pop();
            Quat qf11 = quats.get(_l3 - 1);
            Quat qf1 = new Quat(qf11.getOp(), genNode(l3), qf11.getOpnd1(), qf11.getOpnd2());
            quats.set(_l3 - 1, qf1);
        }

        times_for--;
    }

    @Override
    public void visit(ASTIterationStatement iterationStat) throws Exception {
        times_for++;
        // init cond step
        visit(iterationStat.init.get(0));

        int l1 = quats.size() + 1;

        visit(iterationStat.cond.get(0));
        // test the condition
        Quat q = new Quat(cmp, new TemporaryValue(++tmpId), quats.get(quats.size() - 1).getRes(), genNode(0));
        quats.add(q);

        Quat qt = new Quat(jt, null, new TemporaryValue(tmpId), null); // l2
        quats.add(qt);
        stack_for_l2.push(quats.size());

        Quat qf = new Quat(jf, null, new TemporaryValue(tmpId), null); // l3
        quats.add(qf);
        stack_for_l3.push(quats.size());

        // step
        int l4 = quats.size() + 1;
        visit(iterationStat.step.get(0));

        Quat ql1 = new Quat(jmp, genNode(l1), null, null);
        quats.add(ql1);

        int bf = stack_for_l3.size(); // the stack size before stmt
        // stat
        int l2 = quats.size() + 1;
        visit(iterationStat.stat);

        // refill l2
        int _l2 = stack_for_l2.pop();
        Quat qt11 = quats.get(_l2 - 1);
        Quat qt1 = new Quat(jt, genNode(l2), qt11.getOpnd1(), null);
        quats.set(_l2 - 1, qt1);

        // jmp to step
        Quat qj3 = new Quat(jmp, genNode(l4), null, null);
        quats.add(qj3);

        int l3 = quats.size() + 1;
        // refill l3
        // pop all, maybe break

        for (int i = stack_for_l3.size(); i >= bf; i--) {
            int _l3 = stack_for_l3.pop();
            Quat qf11 = quats.get(_l3 - 1);
            Quat qf1 = new Quat(qf11.getOp(), genNode(l3), qf11.getOpnd1(), qf11.getOpnd2());
            quats.set(_l3 - 1, qf1);
        }

        times_for--;
    }

    @Override
    public void visit(ASTLabeledStatement labeledStat) throws Exception {
        // SymbolRow sbr = new SymbolRow();
        // sbr.type = "GotoLabel";
        // sbr.name = labeledStat.label.value;
        // currentScope.table.put(sbr.name, sbr);

        String _name = labeledStat.label.value;
        boolean _define = true;
        int _address = quats.size() + 1;
        LabelRow lbr = new LabelRow(_name, _define, _address);
        addLabelRow(lbr);

        // build quat

        visit(labeledStat.stat);
    }

    @Override
    public void visit(ASTMemberAccess memberAccess) throws Exception {

    }

    @Override
    public void visit(ASTPostfixExpression postfixExpression) throws Exception {
        visit(postfixExpression.expr);
        String op = "";
        if (postfixExpression.op.value.equals("++")) {
            op = inc;
        } else if (postfixExpression.op.value.equals("--")) {
            op = dec;
        }
        if (postfixExpression.expr instanceof ASTIdentifier) {
            ASTIdentifier id = (ASTIdentifier) postfixExpression.expr;

            Quat q = new Quat(op, null, map.get(id), null); // 8
            quats.add(q);

        } else {
            Integer _tmpId = tmpId;
            Quat q = new Quat(op, null, new TemporaryValue(_tmpId), null); // 8
            quats.add(q);
        }
    }

    @Override
    public void visit(ASTReturnStatement returnStat) throws Exception {
        needReturn = false;
        if (returnStat.expr == null) {
            Quat q = new Quat(ret, genNode(0), null, null);
            quats.add(q);
        } else if (returnStat.expr.get(0) == null) {
            Quat q = new Quat(ret, genNode(0), null, null);
            quats.add(q);
        } else if (returnStat.expr.get(0) instanceof ASTIdentifier) {
            ASTIdentifier id = (ASTIdentifier) returnStat.expr.get(0);
            Quat q = new Quat(ret, id, null, null);
            quats.add(q);
        } else {
            visit(returnStat.expr.get(0));
            Quat q = new Quat(ret, new TemporaryValue(tmpId), null, null);
            quats.add(q);
        }

    }

    @Override
    public void visit(ASTSelectionStatement selectionStat) throws Exception {
        visit(selectionStat.cond.get(0)); // cond

        // test the condition
        Quat q = new Quat(cmp, new TemporaryValue(++tmpId), quats.get(quats.size() - 1).getRes(), genNode(1)); // 1?
        quats.add(q);

        if (selectionStat.otherwise != null) {
            Quat qt = new Quat(jt, genNode(quats.size() + 3), new TemporaryValue(tmpId), null);
            quats.add(qt);

            Quat qf = new Quat(jf, null, new TemporaryValue(tmpId), null);
            quats.add(qf);
            stack_otherwise.push(quats.size());

        } else {
            Quat qt = new Quat(jt, genNode(quats.size() + 3), new TemporaryValue(tmpId), null);
            quats.add(qt);

            Quat qf = new Quat(jf, null, new TemporaryValue(tmpId), null);
            quats.add(qf);
            stack_otherwise.push(quats.size());

        }

        // then

        visit(selectionStat.then);

        // otherwise
        if (selectionStat.otherwise != null) {

            Quat qj = new Quat(jmp, null, null, null);
            quats.add(qj);
            stack_if.push(quats.size());

            // refill false
            int k = stack_otherwise.pop();
            Quat qj22 = quats.get(k - 1);
            Quat qj2 = new Quat(jf, genNode(quats.size() + 1), qj22.getOpnd1(), null);
            quats.set(k - 1, qj2);

            visit(selectionStat.otherwise);

            // refill if finish
            int i = stack_if.pop();
            Quat qj11 = quats.get(i - 1);
            Quat qj1 = new Quat(jmp, genNode(quats.size() + 1), null, null);
            quats.set(i - 1, qj1);

        } else {
            // refill false
            int k = stack_otherwise.pop();
            Quat qj22 = quats.get(k - 1);
            Quat qj2 = new Quat(jf, genNode(quats.size() + 1), qj22.getOpnd1(), null);
            quats.set(k - 1, qj2);
        }

    }

    @Override
    public void visit(ASTStringConstant stringConst) throws Exception {

        // add the symbol row
        SymbolRow sbr = new SymbolRow();
        sbr.type = "Constant";
        sbr.spec = "string";
        sbr.name = stringConst.value;
        // sbr.constId = constIdChar++;
        if (currentScope.table.containsKey(stringConst.value)) {
            sbr.constId = currentScope.table.get(stringConst.value).constId;
        } else {
            sbr.constId = constIdString++;
            currentScope.table.put(sbr.name, sbr);
        }

        ASTStringConstant a = new ASTStringConstant();
        a.value = cs + Integer.toString(sbr.constId);
        map.put(stringConst, a);
    }

    @Override
    public void visit(ASTTypename typename) throws Exception {

    }

    @Override
    public void visit(ASTUnaryExpression unaryExpression) throws Exception {

    }

    @Override
    public void visit(ASTUnaryTypename unaryTypename) throws Exception {

    }

    @Override
    public void visit(ASTFunctionDefine functionDefine) throws Exception {

        // 1. generate the symbolRow
        SymbolRow sbr = new SymbolRow();
        currentRow = sbr;
        currentRow.type = "Function";
        currentRow.spec = functionDefine.specifiers.get(0).value;

        currentRow.iId = quats.size(); //
        // 2. visit the other children to fill the symbolRow.
        visit(functionDefine.declarator);
        String funcName = currentRow.name;

        // 3. enter a new scope
        // set needReturn
        needReturn = true;
        visit(functionDefine.body);
        // check if has return statement
        if (needReturn) {
            needReturn = false;
            errorInfo += ("ES08 >> Function:" + funcName + " must have a return in the end.\n");
        }
        // refill the oId
        currentRow.oId = quats.size();
        SymbolRow s = globalScope.getSymbolRow(funcName);
        s.oId = currentRow.oId;

        // 4. back to the upScope
        if (!es02) {
            // if es02, funcname has been redefined. no need to back to the upScope.
            currentScope = currentScope.upScope;
        } else {
            es02 = false;
        }
        // System.out.println(globalScope.table.keySet());
    }

    @Override
    public void visit(ASTDeclarator declarator) throws Exception {
        if (declarator instanceof ASTFunctionDeclarator) {
            visit((ASTFunctionDeclarator) declarator);
        }

    }

    @Override
    public void visit(ASTStatement statement) throws Exception {
        if (statement instanceof ASTIterationDeclaredStatement) {
            visit((ASTIterationDeclaredStatement) statement);
        } else if (statement instanceof ASTIterationStatement) {
            visit((ASTIterationStatement) statement);
        } else if (statement instanceof ASTCompoundStatement) {
            visit((ASTCompoundStatement) statement);
        } else if (statement instanceof ASTSelectionStatement) {
            visit((ASTSelectionStatement) statement);
        } else if (statement instanceof ASTExpressionStatement) {
            visit((ASTExpressionStatement) statement);
        } else if (statement instanceof ASTBreakStatement) {
            visit((ASTBreakStatement) statement);
        } else if (statement instanceof ASTContinueStatement) {
            visit((ASTContinueStatement) statement);
        } else if (statement instanceof ASTReturnStatement) {
            visit((ASTReturnStatement) statement);
        } else if (statement instanceof ASTGotoStatement) {
            visit((ASTGotoStatement) statement);
        } else if (statement instanceof ASTLabeledStatement) {
            visit((ASTLabeledStatement) statement);
        }
    }

    @Override
    public void visit(ASTToken token) throws Exception {

    }

}
