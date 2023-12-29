.386
.model flat, stdcall
option casemap : none

includelib msvcrt.lib
includelib user32.lib
includelib kernel32.lib

printf proto c:dword,:vararg
scanf proto c:dword,:vararg
prime proto :dword
Mars_PrintInt proto :dword
Mars_PrintStr proto :dword
Mars_GetInt proto 
main proto 

.const
forIntNumber	db 	'%d', 0
forIntNumberPrint	db	'%d ', 0
forString	db	'%s', 0ah, 0
forEnter		db	' ', 0
_string_1	db	"The number of prime numbers within n is:", 0ah, 0
_string_0	db	"Please input a number:", 0ah, 0

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

prime proc @n:dword
	local @flag:dword
	local @i:dword
	local @sum:dword
	local @j:dword
	local @1:dword
	local @2:dword
	local @4:dword
	local @3:dword
	local @5:dword
	local @7:dword
	local @6:dword
	local @8:dword
	local @9:dword
	local @10:dword
	mov @sum, 0
	mov @flag, 1
	mov @i, 2
flag4:
	mov ebx, @i
	cmp ebx, @n
	jle flag10
	jnle flag35
flag8:
	inc @i
	jmp flag4
flag10:
	mov @flag, 1
	mov @j, 2
flag12:
	mov ebx, @j
	imul ebx, @j
	mov @4, ebx
	mov ebx, @4
	cmp ebx, @i
	jle flag19
	jnle flag27
flag17:
	inc @j
	jmp flag12
flag19:
	xor edx, edx
	mov eax, @i
	idiv @j
	mov @7, edx
	cmp @7, 0
	jz flag24
	jnz flag26
flag24:
	mov @flag, 0
	jmp flag27
flag26:
	jmp flag17
flag27:
	cmp @flag, 1
	jz flag31
	jnz flag34
flag31:
	inc @sum
	invoke Mars_PrintInt, @i
flag34:
	jmp flag8
flag35:
	mov eax, @sum
	ret
prime endp

main proc 
	local @res:dword
	local @n:dword
	local @12:dword
	invoke Mars_PrintStr, addr _string_0
	invoke Mars_GetInt
	mov @n, eax
	invoke prime, @n
	mov @res, eax
	invoke Mars_PrintStr, addr _string_1
	invoke Mars_PrintInt, @res
	mov eax, @12
	ret
main endp

end main

