.386
.model flat, stdcall
option casemap : none

includelib msvcrt.lib
includelib user32.lib
includelib kernel32.lib

printf proto c:dword,:vararg
scanf proto c:dword,:vararg
fibonacci proto :dword
Mars_PrintInt proto :dword
Mars_PrintStr proto :dword
Mars_GetInt proto 
main proto 

.const
forIntNumber	db 	'%d', 0
forIntNumberPrint	db	'%d ', 0
forString	db	'%s', 0ah, 0
forEnter		db	' ', 0
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

