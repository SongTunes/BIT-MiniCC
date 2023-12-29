.386
.model flat, stdcall
option casemap : none

includelib msvcrt.lib
includelib user32.lib
includelib kernel32.lib

printf proto c:dword,:vararg
scanf proto c:dword,:vararg
Mars_PrintInt proto :dword
Mars_PrintStr proto :dword
Mars_GetInt proto 
main proto 
YangHuiTriangle proto 

.const
forIntNumber	db 	'%d', 0
forIntNumberPrint	db	'%d ', 0
forString	db	'%s', 0ah, 0
forEnter		db	' ', 0
_string_0	db	0ah, 0

.data
IntNumberHolder dd	0
triangle	dd	64 dup(0)

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
	invoke printf, addr forIntNumberPrint,  param_0
	popad
	ret
Mars_PrintInt endp

Mars_PrintStr proc param_0:dword
	pushad
	invoke printf, addr forString, param_0
	popad
	ret
Mars_PrintStr endp

main proc 
	local @41:dword
	invoke YangHuiTriangle
	mov eax, @41
	ret
main endp

YangHuiTriangle proc 
	local @i:dword
	local @j:dword
	local @1:dword
	local @2:dword
	local @3:dword
	local @4:dword
	local @5:dword
	local @6:dword
	local @7:dword
	local @8:dword
	local @9:dword
	local @10:dword
	local @11:dword
	local @12:dword
	local @13:dword
	local @14:dword
	local @15:dword
	local @16:dword
	local @17:dword
	local @18:dword
	local @20:dword
	local @21:dword
	local @22:dword
	local @23:dword
	local @24:dword
	local @25:dword
	local @26:dword
	local @27:dword
	local @28:dword
	local @29:dword
	local @30:dword
	local @31:dword
	local @32:dword
	local @19:dword
	local @33:dword
	local @34:dword
	local @35:dword
	local @36:dword
	local @37:dword
	local @38:dword
	local @39:dword
	local @40:dword
	local @41:dword
	mov @i, 0
flag2:
	cmp @i, 8
	jl flag8
	jnl flag23
flag6:
	inc @i
	jmp flag2
flag8:
	mov @j, 0
flag9:
	cmp @j, 8
	jl flag15
	jnl flag22
flag13:
	inc @j
	jmp flag9
flag15:
	mov ebx, @i
	imul ebx, 8
	mov @5, ebx
	mov ebx, @j
	imul ebx, 1
	mov @6, ebx
	mov ebx, @5
	add ebx, @6
	mov @7, ebx
	mov edx, @7
	imul edx, 4
	mov eax, offset triangle
	mov ebx, [eax+edx]
	mov @8, ebx
	mov edx, @7
	imul edx, 4
	mov eax, offset triangle
	lea ecx, [eax+edx]
	mov @9, ecx
	mov eax, @9
	mov dword ptr[eax], 1
	jmp flag13
flag22:
	jmp flag6
flag23:
	mov @i, 2
flag24:
	cmp @i, 8
	jl flag30
	jnl flag59
flag28:
	inc @i
	jmp flag24
flag30:
	mov @j, 1
flag31:
	mov ebx, @j
	cmp ebx, @i
	jl flag37
	jnl flag58
flag35:
	inc @j
	jmp flag31
flag37:
	mov ebx, @i
	imul ebx, 8
	mov @14, ebx
	mov ebx, @j
	imul ebx, 1
	mov @15, ebx
	mov ebx, @14
	add ebx, @15
	mov @16, ebx
	mov edx, @16
	imul edx, 4
	mov eax, offset triangle
	mov ebx, [eax+edx]
	mov @17, ebx
	mov edx, @16
	imul edx, 4
	mov eax, offset triangle
	lea ecx, [eax+edx]
	mov @18, ecx
	mov ebx, @i
	sub ebx, 1
	mov @20, ebx
	mov ebx, @20
	imul ebx, 8
	mov @21, ebx
	mov ebx, @j
	imul ebx, 1
	mov @22, ebx
	mov ebx, @21
	add ebx, @22
	mov @23, ebx
	mov edx, @23
	imul edx, 4
	mov eax, offset triangle
	mov ebx, [eax+edx]
	mov @24, ebx
	mov edx, @23
	imul edx, 4
	mov eax, offset triangle
	lea ecx, [eax+edx]
	mov @25, ecx
	mov ebx, @i
	sub ebx, 1
	mov @26, ebx
	mov ebx, @26
	imul ebx, 8
	mov @27, ebx
	mov ebx, @j
	sub ebx, 1
	mov @28, ebx
	mov ebx, @28
	imul ebx, 1
	mov @29, ebx
	mov ebx, @27
	add ebx, @29
	mov @30, ebx
	mov edx, @30
	imul edx, 4
	mov eax, offset triangle
	mov ebx, [eax+edx]
	mov @31, ebx
	mov edx, @30
	imul edx, 4
	mov eax, offset triangle
	lea ecx, [eax+edx]
	mov @32, ecx
	mov ebx, @24
	add ebx, @31
	mov @19, ebx
	mov eax, @18
	mov ebx, @19
	mov dword ptr[eax], ebx
	jmp flag35
flag58:
	jmp flag28
flag59:
	mov @i, 0
flag60:
	cmp @i, 8
	jl flag66
	jnl flag84
flag64:
	inc @i
	jmp flag60
flag66:
	mov @j, 0
flag67:
	mov ebx, @j
	cmp ebx, @i
	jle flag73
	jnle flag81
flag71:
	inc @j
	jmp flag67
flag73:
	mov ebx, @i
	imul ebx, 8
	mov @37, ebx
	mov ebx, @j
	imul ebx, 1
	mov @38, ebx
	mov ebx, @37
	add ebx, @38
	mov @39, ebx
	mov edx, @39
	imul edx, 4
	mov eax, offset triangle
	mov ebx, [eax+edx]
	mov @40, ebx
	mov edx, @39
	imul edx, 4
	mov eax, offset triangle
	lea ecx, [eax+edx]
	mov @41, ecx
	invoke Mars_PrintInt, @40
	jmp flag71
flag81:
	invoke Mars_PrintStr, addr _string_0
	jmp flag64
flag84:
	mov eax, 0
	ret
YangHuiTriangle endp

end main

