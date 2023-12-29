# BIT-MiniCC
BIT Mini C Compiler is a C compiler framework in Java for teaching, which provides the implementation of connecting each stages in compiling as well as the AST tree structure. 

# Building & Running
## Requirements
* JDK 1.8 or higher
* Eclipse Mars or using VSCode

## Building & Running
1. Import the project into Eclipse
2. Set the input source file
3. run as Java applications from class BitMiniCC

# Supported targets
1. x86
2. MIPS
3. RISC-V
4. ARM (coming soon)

# Lab. projects
1. Lexical Analysis: input(C code), output(tokens in JSON)
2. Syntactic Analysis: input(tokens in JSON), output(AST in JSON)
3. Semantic Analysis: input(AST in JSON), output(errors)
4. IR Generation: input(AST in JSON), output(IR)
5. Target Code Generation: input(AST in JSON), output(x86/MIPS/RISC-V assembly)

# Correspondence
* Weixing Ji (jwx@bit.edu.cn) 

# Framework Contributor
* 2020: Hang Li
* 2019: Chensheng Yu, Yueyan Zhao
* 2017: Yu Hao
* 2016: Shuofu Ning
* 2015: YiFan Wu

# Implementation
* 2022: Song Tang

# Implementation Methods
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
  
