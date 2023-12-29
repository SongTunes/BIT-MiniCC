package bit.minisys.minicc.semantic;

import bit.minisys.minicc.parser.ast.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MiniCCSemanticBuilder implements ASTVisitor {

    private Scope currentScope;
    private Scope globalScope;

    private SymbolRow currentRow; // 实时维护当前遍历状态填写的符号表项

    private Map<String, Scope> funcName2Scope; // funcion name to current Map

    private int times_for;
    private boolean needReturn;

    private String current_spec;

    private boolean es02;

    private String errorInfo;

    public MiniCCSemanticBuilder() {

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
        if (errorInfo.equals("")) {
            return;
        }
        System.out.println("errors:");
        System.out.println("----------------------------------------------");
        System.out.println(errorInfo);
        System.out.println("----------------------------------------------");
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
    }

    @Override
    public void visit(ASTArrayAccess arrayAccess) throws Exception {
        if (arrayAccess.arrayName instanceof ASTIdentifier) {
            ASTIdentifier i = (ASTIdentifier) arrayAccess.arrayName;
            String name = i.value;
            Integer dim = 0;
            // set the dim = 0
            // when the num is not const(such as a[1+2]), do not check.
            if (arrayAccess.elements.get(0) instanceof ASTIntegerConstant) {
                ASTIntegerConstant in = (ASTIntegerConstant) arrayAccess.elements.get(0);
                dim = in.value;
            }
            currentRow.addArrayDim(dim);
            // check the whole array_access.
            SymbolRow sbr = currentScope.getSymbolRow(name);
            if (currentRow.arrayDim != sbr.arrayDim) {
                // TODO: array size not match.
            } else {
                for (int k = 0; k < currentRow.arrayDim; k++) {
                    // System.out.println(arraySize.get(k));
                    // System.out.println(sbr.arraySize.get(k));
                    if (currentRow.arraySize.get(k) >= sbr.arraySize.get(k)) {
                        errorInfo += ("ES06 >> ArrayAccess:Out of Bounds.\n");
                        break;
                    }
                }
            }

        } else {
            /**
             * add the dim to the arraySize.
             */
            Integer dim = 0;
            // set the dim = 0
            // when the num is not const(such as a[1+2]), do not check.
            if (arrayAccess.elements.get(0) instanceof ASTIntegerConstant) {
                ASTIntegerConstant in = (ASTIntegerConstant) arrayAccess.elements.get(0);
                dim = in.value;
            }
            currentRow.addArrayDim(dim);
            ASTArrayAccess aa = (ASTArrayAccess) arrayAccess.arrayName;
            visit(aa);
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
        visit(binaryExpression.expr1);
        visit(binaryExpression.expr2);
    }

    @Override
    public void visit(ASTBreakStatement breakStat) throws Exception {
        if (times_for == 0) {
            errorInfo += ("ES03 >> BreakStatement:must be in a LoopStatement.\n");
        }
    }

    @Override
    public void visit(ASTContinueStatement continueStatement) throws Exception {

    }

    @Override
    public void visit(ASTCastExpression castExpression) throws Exception {

    }

    @Override
    public void visit(ASTCharConstant charConst) throws Exception {

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

    }

    @Override
    public void visit(ASTFunctionCall funcCall) throws Exception {
        SymbolRow sbr0 = new SymbolRow();
        sbr0.type = "Function";
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
            } else {
                // add a empty string. a empty string will always pass the check.
                sbr0.addFuncParams("");
            }

        }
        ASTIdentifier id = (ASTIdentifier) funcCall.funcname;
        String funcName = id.value;
        // if(funcName.equals("Mar"))
        SymbolRow sbr = currentScope.getSymbolRow(funcName);
        if (sbr0.paramNum != sbr.paramNum) {
            errorInfo += ("ES04 >> FunctionCall:" + funcName + "'s param num is not matched.\n");
        } else {
            for (int k = 0; k < sbr0.paramNum; k++) {
                // when "", do not check because can not get the type
                if (!sbr0.paramType.get(k).equals("")) {
                    if (!sbr0.paramType.get(k).equals(sbr.paramType.get(k))) {
                        errorInfo += ("ES04 >> FunctionCall:" + funcName + "'s param type is not matched.\n");
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void visit(ASTGotoStatement gotoStat) throws Exception {
        if (!currentScope.table.containsKey(gotoStat.label.value)) { // ES07
            errorInfo += ("ES07 >> Label: " + gotoStat.label.value + " is not defined.\n");
        }
    }

    @Override
    public void visit(ASTIdentifier identifier) throws Exception {
        // only arrive here when use(not declaration).
        String name = identifier.value;
        if (currentScope.getSymbolRow(name) == null) { // ES01
            errorInfo += ("ES01 >> Identifier " + name + " is not defined.\n");
        }
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
        }

    }

    @Override
    public void visit(ASTIntegerConstant intConst) throws Exception {

    }

    @Override
    public void visit(ASTIterationDeclaredStatement iterationDeclaredStat) throws Exception {

    }

    @Override
    public void visit(ASTIterationStatement iterationStat) throws Exception {
        times_for++;
        // TODO:init cond step

        // stat
        visit(iterationStat.stat);
        times_for--;
    }

    @Override
    public void visit(ASTLabeledStatement labeledStat) throws Exception {
        SymbolRow sbr = new SymbolRow();
        sbr.type = "GotoLabel";
        sbr.name = labeledStat.label.value;
        currentScope.table.put(sbr.name, sbr);

    }

    @Override
    public void visit(ASTMemberAccess memberAccess) throws Exception {

    }

    @Override
    public void visit(ASTPostfixExpression postfixExpression) throws Exception {

    }

    @Override
    public void visit(ASTReturnStatement returnStat) throws Exception {
        needReturn = false;
    }

    @Override
    public void visit(ASTSelectionStatement selectionStat) throws Exception {

    }

    @Override
    public void visit(ASTStringConstant stringConst) throws Exception {

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

        // 4. back to the upScope
        if (!es02) {
            // if es02, funcname has been redefined. no need to back to the upScope.
            currentScope = currentScope.upScope;
        } else {
            es02 = false;
        }

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
