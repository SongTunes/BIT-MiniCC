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
array4_4 proto 

.const
forIntNumber	db 	'%d', 0
forIntNumberPrint	db	'%d ', 0
forString	db	'%s', 0ah, 0
forEnter		db	' ', 0
_string_1	db	"Array A:", 0ah, 0
_string_3	db	"Array B:", 0ah, 0
_string_0	db	"Please Input 16 numbers:", 0ah, 0
_string_2	db	0ah, 0
_string_4	db	0ah, 0

.data
IntNumberHolder dd	0
A	dd	16 dup(0)
B	dd	16 dup(0)

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
	local @37:dword
	invoke array4_4
	mov eax, @37
	ret
main endp

array4_4 proc 
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
	local @19:dword
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
	local @33:dword
	local @34:dword
	local @35:dword
	local @36:dword
	local @37:dword
	invoke Mars_PrintStr, addr _string_0
	mov @i, 0
flag4:
	cmp @i, 4
	jl flag10
	jnl flag36
flag8:
	inc @i
	jmp flag4
flag10:
	mov @j, 0
flag11:
	cmp @j, 4
	jl flag17
	jnl flag35
flag15:
	inc @j
	jmp flag11
flag17:
	mov ebx, @i
	imul ebx, 4
	mov @5, ebx
	mov ebx, @j
	imul ebx, 1
	mov @6, ebx
	mov ebx, @5
	add ebx, @6
	mov @7, ebx
	mov edx, @7
	imul edx, 4
	mov eax, offset A
	mov ebx, [eax+edx]
	mov @8, ebx
	mov edx, @7
	imul edx, 4
	mov eax, offset A
	lea ecx, [eax+edx]
	mov @9, ecx
	invoke Mars_GetInt
	mov @9, eax
	mov ebx, 3
	sub ebx, @j
	mov @10, ebx
	mov ebx, @10
	imul ebx, 4
	mov @11, ebx
	mov ebx, @i
	imul ebx, 1
	mov @12, ebx
	mov ebx, @11
	add ebx, @12
	mov @13, ebx
	mov edx, @13
	imul edx, 4
	mov eax, offset B
	mov ebx, [eax+edx]
	mov @14, ebx
	mov edx, @13
	imul edx, 4
	mov eax, offset B
	lea ecx, [eax+edx]
	mov @15, ecx
	mov ebx, @i
	imul ebx, 4
	mov @16, ebx
	mov ebx, @j
	imul ebx, 1
	mov @17, ebx
	mov ebx, @16
	add ebx, @17
	mov @18, ebx
	mov edx, @18
	imul edx, 4
	mov eax, offset A
	mov ebx, [eax+edx]
	mov @19, ebx
	mov edx, @18
	imul edx, 4
	mov eax, offset A
	lea ecx, [eax+edx]
	mov @15, ecx
	jmp flag15
flag35:
	jmp flag8
flag36:
	invoke Mars_PrintStr, addr _string_1
	mov @i, 0
flag39:
	cmp @i, 4
	jl flag45
	jnl flag63
flag43:
	inc @i
	jmp flag39
flag45:
	mov @j, 0
flag46:
	cmp @j, 4
	jl flag52
	jnl flag60
flag50:
	inc @j
	jmp flag46
flag52:
	mov ebx, @i
	imul ebx, 4
	mov @24, ebx
	mov ebx, @j
	imul ebx, 1
	mov @25, ebx
	mov ebx, @24
	add ebx, @25
	mov @26, ebx
	mov edx, @26
	imul edx, 4
	mov eax, offset A
	mov ebx, [eax+edx]
	mov @27, ebx
	mov edx, @26
	imul edx, 4
	mov eax, offset A
	lea ecx, [eax+edx]
	mov @28, ecx
	invoke Mars_PrintInt, @27
	jmp flag50
flag60:
	invoke Mars_PrintStr, addr _string_2
	jmp flag43
flag63:
	invoke Mars_PrintStr, addr _string_3
	mov @i, 0
flag66:
	cmp @i, 4
	jl flag72
	jnl flag90
flag70:
	inc @i
	jmp flag66
flag72:
	mov @j, 0
flag73:
	cmp @j, 4
	jl flag79
	jnl flag87
flag77:
	inc @j
	jmp flag73
flag79:
	mov ebx, @i
	imul ebx, 4
	mov @33, ebx
	mov ebx, @j
	imul ebx, 1
	mov @34, ebx
	mov ebx, @33
	add ebx, @34
	mov @35, ebx
	mov edx, @35
	imul edx, 4
	mov eax, offset B
	mov ebx, [eax+edx]
	mov @36, ebx
	mov edx, @35
	imul edx, 4
	mov eax, offset B
	lea ecx, [eax+edx]
	mov @37, ecx
	invoke Mars_PrintInt, @36
	jmp flag77
flag87:
	invoke Mars_PrintStr, addr _string_4
	jmp flag70
flag90:
	mov eax, 0
	ret
array4_4 endp

end main

