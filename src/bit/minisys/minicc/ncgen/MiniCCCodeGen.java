package bit.minisys.minicc.ncgen;

import java.util.List;

import java.util.HashSet;
import java.util.Set;

import org.jaxen.FunctionCallException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import bit.minisys.minicc.MiniCCCfg;

import bit.minisys.minicc.icgen.*;

import bit.minisys.minicc.internal.util.MiniCCUtil;
import bit.minisys.minicc.ncgen.IMiniCCCodeGen;
import bit.minisys.minicc.parser.ast.ASTIdentifier;
import bit.minisys.minicc.parser.ast.ASTIntegerConstant;
import bit.minisys.minicc.parser.ast.ASTNode;
import bit.minisys.minicc.parser.ast.ASTStringConstant;
import bit.minisys.minicc.parser.internal.antlr.aS;

public class MiniCCCodeGen implements IMiniCCCodeGen {

	private String ncCodes;

	private String dataSegement;
	private String constSegment;

	private String functionDeclaration;
	private String functionDefine;

	private Set<Integer> flagSet;

	private String cmpOp;
	private String params;

	private static final String Mars_GetInt = "\nMars_GetInt proc\n"
			+ "\tpushad\n"
			+ "\tinvoke scanf, offset forIntNumber, offset IntNumberHolder\n"
			+ "\tpopad\n"
			+ "\tlea eax, IntNumberHolder\n"
			+ "\tmov eax, [eax]\n"
			+ "\tret\n"
			+ "Mars_GetInt endp\n";
	private static final String Mars_PrintInt = "\nMars_PrintInt proc param_0:dword\n"
			+ "\tpushad\n"
			+ "\tinvoke printf, addr forIntNumberPrint,  param_0\n"
			+ "\tpopad\n"
			+ "\tret\n"
			+ "Mars_PrintInt endp\n";
	private static final String Mars_PrintStr = "\nMars_PrintStr proc param_0:dword\n"
			+ "\tpushad\n"
			+ "\tinvoke printf, addr forString, param_0\n"
			+ "\tpopad\n"
			+ "\tret\n"
			+ "Mars_PrintStr endp\n";

	private static final String consts = "forIntNumber	db 	'%d', 0\n"
			+ "forIntNumberPrint	db	'%d ', 0\n"
			+ "forString	db	'%s', 0ah, 0\n"
			+ "forEnter		db	' ', 0\n";

	private static final String codeHeader = ".386\n"
			+ ".model flat, stdcall\n"
			+ "option casemap : none\n"
			+ "\nincludelib msvcrt.lib\n"
			+ "includelib user32.lib\n"
			+ "includelib kernel32.lib\n";

	// name for consts
	public static final String ci = "_int_";
	public static final String cf = "_float_";
	public static final String cc = "_char_";
	public static final String cs = "_string_";

	public MiniCCCodeGen() {
		ncCodes = "";
		functionDeclaration = "\nprintf proto c:dword,:vararg\nscanf proto c:dword,:vararg\n";
		functionDefine = "\n.code\n" + Mars_GetInt + Mars_PrintInt + Mars_PrintStr;

		flagSet = new HashSet<Integer>();

		cmpOp = "";
		params = "";

		dataSegement = "\n.data\nIntNumberHolder dd	0\n";
		constSegment = "\n.const\n" + consts;
	}

	public void getConsts(Scope s) {
		for (String key : s.table.keySet()) {
			SymbolRow sbr = s.table.get(key);
			if (sbr.type.equals("Constant")) {
				if (sbr.spec.equals("string")) {
					constSegment += cs;
					constSegment += String.valueOf(sbr.constId);
					constSegment += "\tdb\t";
					String content = "";
					for (int i = 0; i < sbr.name.length(); i++) {
						char c = sbr.name.charAt(i);
						if (c != '\\') {
							content += c;
						} else {
							if (sbr.name.charAt(i + 1) == 'n') {
								content += '"';
								content += ", ";
								if (i == 1) { // empty str
									content = "";
								}
								content += "0ah";
								if (sbr.name.charAt(i + 2) == '"') {
									break;
								} else {
									content += ", ";
									content += '"';
								}
							} else {
								content += c;
							}

						}
					}

					// constSegment += '"';
					constSegment += content;
					constSegment += ", 0";

					constSegment += "\n";
				}
			}
		}
		// search in children scopes
		for (Scope sc : s.childScopes) {
			getConsts(sc);
		}
	}

	public String getType(String cType) {
		if (cType.equals("int") || cType.equals("string")) {
			return "dword";
		} else if (cType.equals("char")) {
			return "byte";
		} else if (cType.equals("float")) {
			return "real8";
		}

		return "dword";
	}

	public String getTypeOf3(ASTNode opnd1, ASTNode opnd2, ASTNode res, Scope scope) {
		/*
		 * if (opnd1 instanceof ASTIdentifier) {
		 * ASTIdentifier id = (ASTIdentifier) opnd1;
		 * return scope.getSymbolRow(id.value).spec;
		 * } else if (opnd2 instanceof ASTIdentifier) {
		 * ASTIdentifier id = (ASTIdentifier) opnd2;
		 * return scope.getSymbolRow(id.value).spec;
		 * } else if (res instanceof ASTIdentifier) {
		 * ASTIdentifier id = (ASTIdentifier) res;
		 * return scope.getSymbolRow(id.value).spec;
		 * }
		 */

		return "dword";
	}

	public String getName(ASTNode node) {
		if (node instanceof ASTIdentifier) {
			ASTIdentifier id = (ASTIdentifier) node;
			return "@" + id.value;
		} else if (node instanceof TemporaryValue) {
			TemporaryValue tmp = (TemporaryValue) node;
			return "@" + String.valueOf(tmp.getId());
		} else if (node instanceof ASTStringConstant) {
			ASTStringConstant s = (ASTStringConstant) node;
			return s.value;
		} else if (node instanceof ASTIntegerConstant) {
			return ((ASTIntegerConstant) node).value + "";
		}

		return "";
	}

	public String getNameNoAt(ASTNode node) {
		if (node instanceof ASTIdentifier) {
			ASTIdentifier id = (ASTIdentifier) node;
			return id.value;
		} else if (node instanceof TemporaryValue) {
			TemporaryValue tmp = (TemporaryValue) node;
			return String.valueOf(tmp.getId());
		} else if (node instanceof ASTStringConstant) {
			ASTStringConstant s = (ASTStringConstant) node;
			return s.value;
		} else if (node instanceof ASTIntegerConstant) {
			return ((ASTIntegerConstant) node).value + "";
		}

		return "";
	}

	@Override
	public String run(String iFile, MiniCCCfg cfg) throws Exception {
		String oFile = MiniCCUtil.remove2Ext(iFile) + MiniCCCfg.MINICC_CODEGEN_OUTPUT_EXT;

		if (cfg.target.equals("mips")) {
			System.out.println("can only generate x86!");
			System.out.println("7. Target code generation failed!");

		} else if (cfg.target.equals("riscv")) {
			System.out.println("can only generate x86!");
			System.out.println("7. Target code generation failed!");
		} else if (cfg.target.equals("x86")) {

			System.out.println("7. Target code generation finished!");
		}

		return oFile;
	}

	public String run1(String iFile, MiniCCCfg cfg, MiniCCICBuilder icBuilder) throws Exception {
		System.out.println("6. MiniCCCodeGen NC Generating...");

		String oFile = MiniCCUtil.remove2Ext(iFile) + MiniCCCfg.MINICC_CODEGEN_OUTPUT_EXT;

		if (cfg.target.equals("mips")) {
			System.out.println("can only generate x86!");
			// System.out.println("7. Target code generation failed!");

		} else if (cfg.target.equals("riscv")) {
			System.out.println("can only generate x86!");
			// System.out.println("7. Target code generation failed!");
		} else if (cfg.target.equals("x86")) {

			List<Quat> quats = icBuilder.getQuats();
			Scope globalScope = icBuilder.globalScope;

			// get the flag set
			for (int i = 0; i < quats.size(); i++) {
				Quat q = quats.get(i);
				String op = q.getOp();
				ASTNode res = q.getRes();
				if (op.equals("jt") || op.equals("jf") || op.equals("jmp")) {
					flagSet.add(((ASTIntegerConstant) res).value);
				}
			}

			// generate the consts
			getConsts(globalScope);

			for (String key : globalScope.table.keySet()) {
				SymbolRow sbr = globalScope.table.get(key);
				// generate the global
				if (sbr.type.equals("Identifier")) {
					dataSegement += sbr.name;
					dataSegement += "\t";
					dataSegement += getType(sbr.spec);
					dataSegement += "\t";
					dataSegement += String.valueOf(sbr.intValue);
					dataSegement += "\n";

				} else if (sbr.type.equals("Array")) {
					dataSegement += sbr.name;
					dataSegement += "\t";
					dataSegement += getType(sbr.spec);
					dataSegement += "\t";
					int l = 1;
					for (int i = 0; i < sbr.arrayDim; i++) {
						l *= sbr.arraySize.get(i);
					}
					dataSegement += String.valueOf(l);
					dataSegement += " dup(0)";
					dataSegement += "\n";
				} else if (sbr.type.equals("Function")) {

					// generate the function declaration
					functionDeclaration += sbr.name;
					functionDeclaration += " proto ";
					for (int i = 0; i < sbr.paramNum; i++) {
						functionDeclaration += ":";
						functionDeclaration += getType(sbr.paramType.get(i));
						if (i < sbr.paramNum - 1) {
							functionDeclaration += ", ";
						}
					}
					functionDeclaration += "\n";

					if (sbr.name.equals("Mars_GetInt") || sbr.name.equals("Mars_PrintInt")
							|| sbr.name.equals("Mars_PrintStr")) {
						continue;
					}

					// generate the function define
					// header
					Scope s0 = icBuilder.funcName2Scope.get(sbr.name);
					// s0 is the params scope
					functionDefine += "\n";
					functionDefine += sbr.name;
					functionDefine += " proc ";
					int index = 0;
					for (String keys0 : s0.table.keySet()) {
						functionDefine += "@" + keys0;

						functionDefine += ":";
						functionDefine += getType(sbr.paramType.get(index++));

						if (index < sbr.paramNum - 1) {
							functionDefine += ", ";
						}

					}
					functionDefine += "\n";
					// content
					// local and array
					String locals = "";
					int tmpId = 0; // the number of the temp expected to be defined.
					Scope s = icBuilder.funcName2Scope.get(sbr.name).childScopes.get(0);
					// local, array in this scope
					for (String key1 : s.table.keySet()) {
						SymbolRow sbr1 = s.table.get(key1);
						if (sbr1.type.equals("Identifier")) {
							locals += "\tlocal @";
							locals += sbr1.name;
							locals += ":";
							locals += getType(sbr1.spec);
							locals += "\n";
						}
						// array
						// array need to be defined in data segment.
						else if (sbr1.type.equals("Array")) {
							dataSegement += sbr1.name;
							dataSegement += "\tdd\t";
							int l = 1;
							for (int i = 0; i < sbr1.arrayDim; i++) {
								l *= sbr1.arraySize.get(i);
							}
							dataSegement += String.valueOf(l);
							dataSegement += " dup(0)";
							dataSegement += "\n";
						}
					}
					// local in children scope
					// TODO

					// codes
					String codes = "";
					int st = sbr.iId;
					int ed = sbr.oId;
					Set<Integer> tmpSet = new HashSet<Integer>();
					for (int i = st; i < ed; i++) {
						Quat q = quats.get(i);
						String op = q.getOp();
						ASTNode opnd1 = q.getOpnd1();
						ASTNode opnd2 = q.getOpnd2();
						ASTNode res = q.getRes();
						// temp local
						if (opnd1 instanceof TemporaryValue) {
							TemporaryValue tmp = (TemporaryValue) opnd1;
							if (!tmpSet.contains(tmp.getId())) {
								locals += "\tlocal @" + String.valueOf(tmp.getId());
								locals += ":" + getTypeOf3(opnd1, opnd2, res, s) + "\n";
								tmpId = tmp.getId() + 1;
								tmpSet.add(tmp.getId());
							}
						}
						if (opnd2 instanceof TemporaryValue) {
							TemporaryValue tmp = (TemporaryValue) opnd2;
							if (!tmpSet.contains(tmp.getId())) {
								locals += "\tlocal @" + String.valueOf(tmp.getId());
								locals += ":" + getTypeOf3(opnd1, opnd2, res, s) + "\n";
								tmpId = tmp.getId() + 1;
								tmpSet.add(tmp.getId());
							}
						}
						if (res instanceof TemporaryValue) {
							TemporaryValue tmp = (TemporaryValue) res;
							if (!tmpSet.contains(tmp.getId())) {
								locals += "\tlocal @" + String.valueOf(tmp.getId());
								locals += ":" + getTypeOf3(opnd1, opnd2, res, s) + "\n";
								tmpId = tmp.getId() + 1;
								tmpSet.add(tmp.getId());
							}
						}

						// jump enhance
						if (flagSet.contains(i + 1)) {
							flagSet.remove(i + 1);
							codes += "flag" + String.valueOf(i + 1) + ":\n";
						}

						//
						if (op.equals("<") || op.equals("<=") || op.equals(">") || op.equals(">=") || op.equals("==")) {
							cmpOp = op;
							Quat _q = quats.get(i + 1);
							String _op = _q.getOp();
							ASTNode _opnd1 = _q.getOpnd1();
							ASTNode _opnd2 = _q.getOpnd2();
							ASTNode _res = _q.getRes();

							if (_op.equals("cmp")) {
								// imm
								if (opnd2 instanceof ASTIntegerConstant) {
									codes += "\tcmp " + getName(opnd1) + ", " + getName(opnd2) + "\n";
									i++; // ignore the cmp ic
								} else {
									codes += "\tmov ebx, " + getName(opnd1) + "\n";
									codes += "\tcmp " + "ebx" + ", " + getName(opnd2) + "\n";
									i++; // ignore the cmp ic
								}

							}
						} else if (op.equals("jt")) {
							if (cmpOp.equals("<")) {
								codes += "\tjl " + "flag" + getName(res) + "\n";
							} else if (cmpOp.equals("<=")) {
								codes += "\tjle " + "flag" + getName(res) + "\n";
							} else if (cmpOp.equals(">")) {
								codes += "\tjg " + "flag" + getName(res) + "\n";
							} else if (cmpOp.equals(">=")) {
								codes += "\tjge " + "flag" + getName(res) + "\n";
							} else if (cmpOp.equals("==")) {
								codes += "\tjz " + "flag" + getName(res) + "\n";
							}
							// flagSet.add(((ASTIntegerConstant) res).value);
						} else if (op.equals("jf")) {
							if (cmpOp.equals("<")) {
								codes += "\tjnl " + "flag" + getName(res) + "\n";
							} else if (cmpOp.equals("<=")) {
								codes += "\tjnle " + "flag" + getName(res) + "\n";
							} else if (cmpOp.equals(">")) {
								codes += "\tjng " + "flag" + getName(res) + "\n";
							} else if (cmpOp.equals(">=")) {
								codes += "\tjnge " + "flag" + getName(res) + "\n";
							} else if (cmpOp.equals("==")) {
								codes += "\tjnz " + "flag" + getName(res) + "\n";
							}
							// flagSet.add(((ASTIntegerConstant) res).value);
						} else if (op.equals("jmp")) {
							codes += "\tjmp " + "flag" + getName(res) + "\n";

						} else if (op.equals("=")) {
							// imm
							if (opnd1 instanceof ASTIntegerConstant) {
								if (res instanceof TemporaryValue) { // lea
									codes += "\tmov eax, " + getName(res) + "\n";
									codes += "\tmov dword ptr[eax], " + getName(opnd1) + "\n";
								} else {
									codes += "\tmov " + getName(res) + ", " + getName(opnd1) + "\n";
								}

							}
							// mem
							else {
								if (res instanceof TemporaryValue) { // lea
									codes += "\tmov eax, " + getName(res) + "\n";
									codes += "\tmov ebx, " + getName(opnd1) + "\n";

									codes += "\tmov dword ptr[eax], ebx\n";
								} else {

									codes += "\tmov ebx, " + getName(opnd1) + "\n";

									codes += "\tmov " + getName(res) + ", ebx\n";
								}

							}
						} else if (op.equals("+")) {
							codes += "\tmov ebx, " + getName(opnd1) + "\n";

							codes += "\tadd ebx, " + getName(opnd2) + "\n";

							codes += "\tmov " + getName(res) + ", ebx\n";

						} else if (op.equals("-")) {
							if (opnd1 instanceof ASTIdentifier) {
								SymbolRow sbrs = s.getSymbolRow(getNameNoAt(opnd1));
								if (sbrs.type.equals("Array")) {
									codes += "\tmov edx, " + getName(opnd2) + "\n";
									codes += "\timul edx, 4\n";
									codes += "\tmov ebx, offset " + getNameNoAt(opnd1) + "\n";
									codes += "\tsub ebx, edx\n";
									codes += "\tmov " + getName(res) + ", ebx\n";
									continue;
								}
							}
							codes += "\tmov ebx, " + getName(opnd1) + "\n";
							codes += "\tsub ebx, " + getName(opnd2) + "\n";
							codes += "\tmov " + getName(res) + ", ebx\n";

							// TODO: opt

						} else if (op.equals("*")) {
							codes += "\tmov ebx, " + getName(opnd1) + "\n";

							codes += "\timul ebx, " + getName(opnd2) + "\n";

							codes += "\tmov " + getName(res) + ", ebx\n";

						} else if (op.equals("/")) {
							codes += "\txor edx, edx\n";
							codes += "\tmov eax, " + getName(opnd1) + "\n";
							if (opnd2 instanceof ASTIntegerConstant) {
								codes += "\tmov ecx, " + getName(opnd2) + "\n";
								codes += "\tidiv ecx\n";
							} else {
								codes += "\tidiv " + getName(opnd2) + "\n";
							}

							codes += "\tmov " + getName(res) + ", eax\n";

						} else if (op.equals("%")) {
							codes += "\txor edx, edx\n";
							codes += "\tmov eax, " + getName(opnd1) + "\n";

							codes += "\tidiv " + getName(opnd2) + "\n";

							codes += "\tmov " + getName(res) + ", edx\n";

						} else if (op.equals("inc")) {
							codes += "\tinc " + getName(opnd1) + "\n";

						} else if (op.equals("dec")) {
							codes += "\tdec " + getName(opnd1) + "\n";

						} else if (op.equals("=[]")) {
							codes += "\tmov edx, " + getName(opnd2) + "\n";
							codes += "\timul edx, 4\n";
							codes += "\tmov eax, offset " + getNameNoAt(opnd1) + "\n";
							codes += "\tmov ebx, [eax+edx]\n";
							codes += "\tmov " + getName(res) + ", ebx\n";

						} else if (op.equals("lea")) {
							codes += "\tmov edx, " + getName(opnd2) + "\n";
							codes += "\timul edx, 4\n";
							codes += "\tmov eax, offset " + getNameNoAt(opnd1) + "\n";
							codes += "\tlea ecx, [eax+edx]\n";
							codes += "\tmov " + getName(res) + ", ecx\n";

						} else if (op.equals("param")) {
							params += ", " + getName(res);

						} else if (op.equals("call")) {
							codes += "\tinvoke " + getNameNoAt(opnd1);
							if (getNameNoAt(opnd1).equals("Mars_PrintStr")) {
								int k = 0;
								while (params.charAt(k++) != ',') {

								}
								k++;
								int l = params.length();
								// System.out.println(params);
								params = params.substring(0, k) + "addr " + params.substring(k, l);

							} else if (getNameNoAt(opnd1).equals("Mars_PrintInt")
									|| getNameNoAt(opnd1).equals("Mars_GetInt")) {

							} else {
								// addr
								SymbolRow sbrc = globalScope.table.get(getNameNoAt(opnd1));
								int k = 0;
								for (int j = 0; j < sbrc.paramNum; j++) {
									// String ss = sbrc.paramType.get(j);
									// System.out.println(getName(opnd1));
									if (sbrc.paramType.get(j).equals("string")) {
										// find ',' and add addr
										while (params.charAt(k++) != ',') {
										}
										k++;
										int l = params.length();
										params = params.substring(0, k) + "addr " + params.substring(k, l);

									}
								}
							}

							codes += params + "\n";
							params = "";
							if (res != null) {
								// if (res instanceof TemporaryValue) { // lea
								// codes += "\tmov ebx, " + getName(res) + "\n";
								// codes += "\tmov dword ptr[ebx], eax\n";
								// } else {
								codes += "\tmov " + getName(res) + ", eax\n";
								// }

							}
						} else if (op.equals("ret")) {
							codes += "\tmov eax, " + getName(res) + "\n";
							codes += "\tret\n";
						}

					}
					// System.out.println(codes);
					// ending
					functionDefine += locals;
					functionDefine += codes;

					functionDefine += sbr.name;
					functionDefine += " endp\n";

				}
			}

			functionDefine += "\nend main\n";
			ncCodes += codeHeader;
			ncCodes += functionDeclaration;
			ncCodes += constSegment;
			ncCodes += dataSegement;
			ncCodes += functionDefine;
			// System.out.println(ncCodes);
			MiniCCNCPrinter ncPrinter = new MiniCCNCPrinter();
			ncPrinter.print(oFile, ncCodes);

			// System.out.println("7. Target code generation finished!");
		}
		return oFile;
	}

}
