# What is BIT-MiniCC framework?
BIT Mini C Compiler is a C compiler framework in Java for teaching.

# Building & Running
## Requirements
* JDK 1.8

# Supported targets
- x86
- MIPS
- RISC-V
- ARM (coming soon)

# Lab. projects
1. Lexical Analysis: input(C code), output(tokens in JSON)
2. Syntactic Analysis: input(tokens in JSON), output(AST in JSON)
3. Semantic Analysis: input(AST in JSON), output(errors)
4. IR Generation: input(AST in JSON), output(IR)
5. Target Code Generation: input(AST in JSON), output(x86/MIPS/RISC-V assembly)

# Implementation
* 2022: Song Tang

# What does the original framework implement?
- Concatenate execution frameworks for each compilation stage
- Abstract syntax tree (AST) requirements

# What functions have I implemented?
> I did not use any auxiliary tools except for the code included in the original framework.
* 预处理
  1. 注释消除
* 词法分析
  1. 对C语言各单词设计DFA（确定的有穷自动机）
  2. DFA状态合并
* 语法分析
  1. 确定文法子集
  2. 消除左递归
  3. 使用带有回溯的递归下降分析器进行语法分析构建抽象语法树AST
* 语义分析
  1. 遍历抽象语法树AST（包含语法错误检查）
  2. 使用拉链往返技术填写标号表
  3. 构建符号表
* 中间代码生成
  1. 生成四元式中间代码
* 目标代码生成
  1. 生成x86汇编代码


# Display
> Here I use a Fibonacci program to display.
#### Original File
1_Fibonacci.c
```c
int fibonacci(int num){
	
	int res;
	if(num < 1){
		res = 0;
	}else if(num <= 2){
		res = 1;
	}else{
		res = fibonacci(num-1)+fibonacci(num-2);
	}
	return res;
}
int main(){
	Mars_PrintStr("Please input a number:\n");
	int n = Mars_GetInt();
	int res = fibonacci(n);
	Mars_PrintStr("This number's fibonacci value is :\n");
	Mars_PrintInt(res);
    return 0;
}
```
#### Lexical Analysis 
output file: 1_Fibonacci.pp.tokens
```
[@0,0:2='int',<'int'>,2:0]
[@1,4:12='fibonacci',<Identifier>,2:4]
[@2,13:13='(',<'('>,2:13]
[@3,14:16='int',<'int'>,2:14]
[@4,18:20='num',<Identifier>,2:18]
[@5,21:21=')',<')'>,2:21]
[@6,22:22='{',<'{'>,2:22]
[@7,1:3='int',<'int'>,4:1]
[@8,5:7='res',<Identifier>,4:5]
[@9,8:8=';',<';'>,4:8]
[@10,1:2='if',<'if'>,5:1]
[@11,3:3='(',<'('>,5:3]
[@12,4:6='num',<Identifier>,5:4]
[@13,8:8='<',<'<'>,5:8]
[@14,10:10='1',<IntegerConstant>,5:10]
[@15,11:11=')',<')'>,5:11]
[@16,12:12='{',<'{'>,5:12]
[@17,2:4='res',<Identifier>,6:2]
[@18,6:6='=',<'='>,6:6]
[@19,8:8='0',<IntegerConstant>,6:8]
[@20,9:9=';',<';'>,6:9]
[@21,1:1='}',<'}'>,7:1]
[@22,2:5='else',<'else'>,7:2]
[@23,7:8='if',<'if'>,7:7]
[@24,9:9='(',<'('>,7:9]
[@25,10:12='num',<Identifier>,7:10]
[@26,14:15='<=',<'<='>,7:14]
[@27,17:17='2',<IntegerConstant>,7:17]
[@28,18:18=')',<')'>,7:18]
[@29,19:19='{',<'{'>,7:19]
[@30,2:4='res',<Identifier>,8:2]
[@31,6:6='=',<'='>,8:6]
[@32,8:8='1',<IntegerConstant>,8:8]
[@33,9:9=';',<';'>,8:9]
[@34,1:1='}',<'}'>,9:1]
[@35,2:5='else',<'else'>,9:2]
[@36,6:6='{',<'{'>,9:6]
[@37,2:4='res',<Identifier>,10:2]
[@38,6:6='=',<'='>,10:6]
[@39,8:16='fibonacci',<Identifier>,10:8]
[@40,17:17='(',<'('>,10:17]
[@41,18:20='num',<Identifier>,10:18]
[@42,21:21='-',<'-'>,10:21]
[@43,22:22='1',<IntegerConstant>,10:22]
[@44,23:23=')',<')'>,10:23]
[@45,24:24='+',<'+'>,10:24]
[@46,25:33='fibonacci',<Identifier>,10:25]
[@47,34:34='(',<'('>,10:34]
[@48,35:37='num',<Identifier>,10:35]
[@49,38:38='-',<'-'>,10:38]
[@50,39:39='2',<IntegerConstant>,10:39]
[@51,40:40=')',<')'>,10:40]
[@52,41:41=';',<';'>,10:41]
[@53,1:1='}',<'}'>,11:1]
[@54,1:6='return',<'return'>,12:1]
[@55,8:10='res',<Identifier>,12:8]
[@56,11:11=';',<';'>,12:11]
[@57,0:0='}',<'}'>,13:0]
[@58,0:2='int',<'int'>,14:0]
[@59,4:7='main',<Identifier>,14:4]
[@60,8:8='(',<'('>,14:8]
[@61,9:9=')',<')'>,14:9]
[@62,10:10='{',<'{'>,14:10]
[@63,1:13='Mars_PrintStr',<Identifier>,15:1]
[@64,14:14='(',<'('>,15:14]
[@65,15:40='"Please input a number:\n"',<StringLiteral>,15:15]
[@66,41:41=')',<')'>,15:41]
[@67,42:42=';',<';'>,15:42]
[@68,1:3='int',<'int'>,16:1]
[@69,5:5='n',<Identifier>,16:5]
[@70,7:7='=',<'='>,16:7]
[@71,9:19='Mars_GetInt',<Identifier>,16:9]
[@72,20:20='(',<'('>,16:20]
[@73,21:21=')',<')'>,16:21]
[@74,22:22=';',<';'>,16:22]
[@75,1:3='int',<'int'>,17:1]
[@76,5:7='res',<Identifier>,17:5]
[@77,9:9='=',<'='>,17:9]
[@78,11:19='fibonacci',<Identifier>,17:11]
[@79,20:20='(',<'('>,17:20]
[@80,21:21='n',<Identifier>,17:21]
[@81,22:22=')',<')'>,17:22]
[@82,23:23=';',<';'>,17:23]
[@83,1:13='Mars_PrintStr',<Identifier>,18:1]
[@84,14:14='(',<'('>,18:14]
[@85,15:52='"This number's fibonacci value is :\n"',<StringLiteral>,18:15]
[@86,53:53=')',<')'>,18:53]
[@87,54:54=';',<';'>,18:54]
[@88,1:13='Mars_PrintInt',<Identifier>,19:1]
[@89,14:14='(',<'('>,19:14]
[@90,15:17='res',<Identifier>,19:15]
[@91,18:18=')',<')'>,19:18]
[@92,19:19=';',<';'>,19:19]
[@93,4:9='return',<'return'>,20:4]
[@94,11:11='0',<IntegerConstant>,20:11]
[@95,12:12=';',<';'>,20:12]
[@96,0:0='}',<'}'>,21:0]
[@97,0:4='<EOF>',<EOF>,22:0]
```

#### Syntactic Analysis 
output file: 1_Fibonacci.pp.ast.json

```JSON
{
	"type": "Program",
	"items": [{
		"type": "FunctionDefine",
		"specifiers": [{
			"type": "Token",
			"value": "int",
			"tokenId": 0
		}],
		"declarator": {
			"type": "FunctionDeclarator",
			"declarator": {
				"type": "VariableDeclarator",
				"identifier": {
					"type": "Identifier",
					"value": "fibonacci",
					"tokenId": 1
				}
			},
			"params": [{
				"type": "ParamsDeclarator",
				"specfiers": [{
					"type": "Token",
					"value": "int",
					"tokenId": 3
				}],
				"declarator": {
					"type": "VariableDeclarator",
					"identifier": {
						"type": "Identifier",
						"value": "num",
						"tokenId": 4
					}
				}
			}]
		},
		"body": {
			"type": "CompoundStatement",
			"blockItems": [{
				"type": "Declaration",
				"specifiers": [{
					"type": "Token",
					"value": "int",
					"tokenId": 7
				}],
				"initLists": [{
					"type": "InitList",
					"declarator": {
						"type": "VariableDeclarator",
						"identifier": {
							"type": "Identifier",
							"value": "res",
							"tokenId": 8
						}
					},
					"exprs": []
				}]
			}, {
				"type": "SelectionStatement",
				"cond": [{
					"type": "BinaryExpression",
					"op": {
						"type": "Token",
						"value": "<",
						"tokenId": 13
					},
					"expr1": {
						"type": "Identifier",
						"value": "num",
						"tokenId": 12
					},
					"expr2": {
						"type": "IntegerConstant",
						"value": 1,
						"tokenId": 14
					}
				}],
				"then": {
					"type": "CompoundStatement",
					"blockItems": [{
						"type": "ExpressionStatement",
						"exprs": [{
							"type": "BinaryExpression",
							"op": {
								"type": "Token",
								"value": "=",
								"tokenId": 18
							},
							"expr1": {
								"type": "Identifier",
								"value": "res",
								"tokenId": 17
							},
							"expr2": {
								"type": "IntegerConstant",
								"value": 0,
								"tokenId": 19
							}
						}]
					}]
				},
				"otherwise": {
					"type": "SelectionStatement",
					"cond": [{
						"type": "BinaryExpression",
						"op": {
							"type": "Token",
							"value": "<=",
							"tokenId": 26
						},
						"expr1": {
							"type": "Identifier",
							"value": "num",
							"tokenId": 25
						},
						"expr2": {
							"type": "IntegerConstant",
							"value": 2,
							"tokenId": 27
						}
					}],
					"then": {
						"type": "CompoundStatement",
						"blockItems": [{
							"type": "ExpressionStatement",
							"exprs": [{
								"type": "BinaryExpression",
								"op": {
									"type": "Token",
									"value": "=",
									"tokenId": 31
								},
								"expr1": {
									"type": "Identifier",
									"value": "res",
									"tokenId": 30
								},
								"expr2": {
									"type": "IntegerConstant",
									"value": 1,
									"tokenId": 32
								}
							}]
						}]
					},
					"otherwise": {
						"type": "CompoundStatement",
						"blockItems": [{
							"type": "ExpressionStatement",
							"exprs": [{
								"type": "BinaryExpression",
								"op": {
									"type": "Token",
									"value": "=",
									"tokenId": 38
								},
								"expr1": {
									"type": "Identifier",
									"value": "res",
									"tokenId": 37
								},
								"expr2": {
									"type": "BinaryExpression",
									"op": {
										"type": "Token",
										"value": "+",
										"tokenId": 45
									},
									"expr1": {
										"type": "FunctionCall",
										"funcname": {
											"type": "Identifier",
											"value": "fibonacci",
											"tokenId": 39
										},
										"argList": [{
											"type": "BinaryExpression",
											"op": {
												"type": "Token",
												"value": "-",
												"tokenId": 42
											},
											"expr1": {
												"type": "Identifier",
												"value": "num",
												"tokenId": 41
											},
											"expr2": {
												"type": "IntegerConstant",
												"value": 1,
												"tokenId": 43
											}
										}]
									},
									"expr2": {
										"type": "FunctionCall",
										"funcname": {
											"type": "Identifier",
											"value": "fibonacci",
											"tokenId": 46
										},
										"argList": [{
											"type": "BinaryExpression",
											"op": {
												"type": "Token",
												"value": "-",
												"tokenId": 49
											},
											"expr1": {
												"type": "Identifier",
												"value": "num",
												"tokenId": 48
											},
											"expr2": {
												"type": "IntegerConstant",
												"value": 2,
												"tokenId": 50
											}
										}]
									}
								}
							}]
						}]
					}
				}
			}, {
				"type": "ReturnStatement",
				"expr": [{
					"type": "Identifier",
					"value": "res",
					"tokenId": 55
				}]
			}]
		}
	}, {
		"type": "FunctionDefine",
		"specifiers": [{
			"type": "Token",
			"value": "int",
			"tokenId": 58
		}],
		"declarator": {
			"type": "FunctionDeclarator",
			"declarator": {
				"type": "VariableDeclarator",
				"identifier": {
					"type": "Identifier",
					"value": "main",
					"tokenId": 59
				}
			},
			"params": []
		},
		"body": {
			"type": "CompoundStatement",
			"blockItems": [{
				"type": "ExpressionStatement",
				"exprs": [{
					"type": "FunctionCall",
					"funcname": {
						"type": "Identifier",
						"value": "Mars_PrintStr",
						"tokenId": 63
					},
					"argList": [{
						"type": "StringConstant",
						"value": "\"Please input a number:\\n\"",
						"tokenId": 65
					}]
				}]
			}, {
				"type": "Declaration",
				"specifiers": [{
					"type": "Token",
					"value": "int",
					"tokenId": 68
				}],
				"initLists": [{
					"type": "InitList",
					"declarator": {
						"type": "VariableDeclarator",
						"identifier": {
							"type": "Identifier",
							"value": "n",
							"tokenId": 69
						}
					},
					"exprs": [{
						"type": "FunctionCall",
						"funcname": {
							"type": "Identifier",
							"value": "Mars_GetInt",
							"tokenId": 71
						},
						"argList": []
					}]
				}]
			}, {
				"type": "Declaration",
				"specifiers": [{
					"type": "Token",
					"value": "int",
					"tokenId": 75
				}],
				"initLists": [{
					"type": "InitList",
					"declarator": {
						"type": "VariableDeclarator",
						"identifier": {
							"type": "Identifier",
							"value": "res",
							"tokenId": 76
						}
					},
					"exprs": [{
						"type": "FunctionCall",
						"funcname": {
							"type": "Identifier",
							"value": "fibonacci",
							"tokenId": 78
						},
						"argList": [{
							"type": "Identifier",
							"value": "n",
							"tokenId": 80
						}]
					}]
				}]
			}, {
				"type": "ExpressionStatement",
				"exprs": [{
					"type": "FunctionCall",
					"funcname": {
						"type": "Identifier",
						"value": "Mars_PrintStr",
						"tokenId": 83
					},
					"argList": [{
						"type": "StringConstant",
						"value": "\"This number's fibonacci value is :\\n\"",
						"tokenId": 85
					}]
				}]
			}, {
				"type": "ExpressionStatement",
				"exprs": [{
					"type": "FunctionCall",
					"funcname": {
						"type": "Identifier",
						"value": "Mars_PrintInt",
						"tokenId": 88
					},
					"argList": [{
						"type": "Identifier",
						"value": "res",
						"tokenId": 90
					}]
				}]
			}, {
				"type": "ReturnStatement",
				"expr": [{
					"type": "IntegerConstant",
					"value": 0,
					"tokenId": 94
				}]
			}]
		}
	}]
}
```

#### IR Generation
output file: 1_Fibonacci.pp.ic.txt
```
(<,num,1,%1)
(cmp,%1,1,%2)
(jt,%2,,5)
(jf,%2,,7)
(=,0,,res)
(jmp,,,20)
(<=,num,2,%3)
(cmp,%3,1,%4)
(jt,%4,,11)
(jf,%4,,13)
(=,1,,res)
(jmp,,,20)
(-,num,1,%5)
(param,,,%5)
(call,fibonacci,,%6)
(-,num,2,%7)
(param,,,%7)
(call,fibonacci,,%8)
(+,%6,%8,res)
(ret,,,res)
(param,,,_string_0)
(call,Mars_PrintStr,,)
(call,Mars_GetInt,,n)
(param,,,n)
(call,fibonacci,,res)
(param,,,_string_1)
(call,Mars_PrintStr,,)
(param,,,res)
(call,Mars_PrintInt,,)
(ret,,,%10)
```

#### Target Code Generation
output file: 1_Fibonacci.pp.code.s

```x86asm
.386
.model flat, stdcall
option casemap : none

includelib msvcrt.lib
includelib user32.lib
includelib kernel32.lib

printf proto c:dword,:vararg
scanf proto c:dword,:vararg
fibonacci proto :dword
main proto 

.const
forIntNumber	db '%d',0
forString	db '%s',0
forEnter	db ' ',0
_string_0	db	"Please input a number:", 0ah, 0
_string_1	db	"This number's fibonacci value is :", 0ah, 0

.data
IntNumberHolder dd	0

.code

Mars_GetInt proc
	pushad
	invoke scanf, offset forIntNumber, offset IntNumberHolder
	popad
	lea eax, IntNumberHolder
	mov eax, [eax]
	ret
Mars_GetInt endp

Mars_PrintInt proc param_0:dword
	pushad
	invoke printf, addr forIntNumber,  param_0
	popad
	ret
Mars_PrintInt endp

Mars_PrintStr proc param_0:dword
	pushad
	invoke printf, addr forString, param_0
	popad
	ret
Mars_PrintStr endp

fibonacci proc @num:dword
	local @res:dword
	local @1:dword
	local @2:dword
	local @3:dword
	local @4:dword
	local @5:dword
	local @6:dword
	local @7:dword
	local @8:dword
	cmp @num, 1
	jl flag5
	jnl flag7
flag5:
	mov @res, 0
	jmp flag20
flag7:
	cmp @num, 2
	jle flag11
	jnle flag13
flag11:
	mov @res, 1
	jmp flag20
flag13:
	mov ebx, @num
	sub ebx, 1
	mov @5, ebx
	invoke fibonacci, @5
	mov @6, eax
	mov ebx, @num
	sub ebx, 2
	mov @7, ebx
	invoke fibonacci, @7
	mov @8, eax
	mov ebx, @6
	add ebx, @8
	mov @res, ebx
flag20:
	mov eax, @res
	ret
fibonacci endp

main proc 
	local @res:dword
	local @n:dword
	local @10:dword
	invoke Mars_PrintStr, addr _string_0
	invoke Mars_GetInt
	mov @n, eax
	invoke fibonacci, @n
	mov @res, eax
	invoke Mars_PrintStr, addr _string_1
	invoke Mars_PrintInt, @res
	mov eax, @10
	ret
main endp

end main
```

  
