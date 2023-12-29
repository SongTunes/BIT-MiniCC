.386
.model flat, stdcall
option casemap : none

includelib msvcrt.lib
includelib user32.lib
includelib kernel32.lib

printf proto c:dword,:vararg
scanf proto c:dword,:vararg
main proto 
perfectNumber proto :dword

.const
forIntNumber	db 	'%d', 0ah, 0
forString	db	'%s', 0ah, 0
forEnter		db	' ', 0
_string_0	db	"The sum is :", 0ah, 0
_string_1	db	"All perfect numbers within 100:", 0ah, 0

.data
IntNumberHolder dd	0
p	dd	80

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

main proc 
	local @14:dword
	invoke Mars_PrintStr, addr _string_1
	invoke perfectNumber, 100
	mov eax, @14
	ret
main endp

perfectNumber proc @n:dword
	local @s:dword
	local @c:dword
	local @num:dword
	local @count:dword
	local @i:dword
	local @1:dword
	local @2:dword
	local @5:dword
	local @4:dword
	local @3:dword
	local @6:dword
	local @8:dword
	local @7:dword
	local @9:dword
	local @10:dword
	local @11:dword
	local @12:dword
	local @13:dword
	local @14:dword
	mov @c, 0
	mov @num, 2
flag3:
	mov ebx, @num
	cmp ebx, @n
	jl flag9
	jnl flag40
flag7:
	inc @num
	jmp flag3
flag9:
	mov @count, 0
	mov ebx, @num
	mov @s, ebx
	mov @i, 1
flag12:
	xor edx, edx
	mov eax, @num
	mov ecx, 2
	idiv ecx
	mov @5, eax
	mov ebx, @5
	add ebx, 1
	mov @4, ebx
	mov ebx, @i
	cmp ebx, @4
	jl flag20
	jnl flag32
flag18:
	inc @i
	jmp flag12
flag20:
	xor edx, edx
	mov eax, @num
	idiv @i
	mov @8, edx
	cmp @8, 0
	jz flag25
	jnz flag31
flag25:
	mov ebx, @count
	imul ebx, 1
	mov @10, ebx
	inc @count
	mov edx, @10
	imul edx, 4
	mov eax, offset p
	mov ebx, [eax+edx]
	mov @11, ebx
	mov edx, @10
	imul edx, 4
	mov eax, offset p
	lea ecx, [eax+edx]
	mov @12, ecx
	mov eax, @12
	mov ebx, @i
	mov dword ptr[eax], ebx
	mov ebx, @s
	sub ebx, @i
	mov @s, ebx
flag31:
	jmp flag18
flag32:
	mov ebx, 0
	cmp ebx, @s
	jz flag36
	jnz flag39
flag36:
	invoke Mars_PrintInt, @num
	inc @c
flag39:
	jmp flag7
flag40:
	invoke Mars_PrintStr, addr _string_0
	invoke Mars_PrintInt, @c
	mov eax, 0
	ret
perfectNumber endp

end main

