.386
.model flat, stdcall
option casemap:none

includelib msvcrt.lib
includelib user32.lib
includelib kernel32.lib

printf PROTO C : ptr sbyte, :VARARG
scanf PROTO C : ptr sbyte, :VARARG

.data
forIntNumber	db '%d',0
forString	db '%s',0
forEnter	db ' ',0
IntNumberHolder dd 0

.code
__init:
	call main
	ret
Mars_PrintInt:
	mov esi, [esp+4]
	pushad
	invoke printf, offset forIntNumber, esi
	invoke printf, offset forEnter
	popad
	ret
Mars_GetInt:
	pushad
	invoke scanf, offset forIntNumber, offset IntNumberHolder
	popad
	lea eax, IntNumberHolder
	mov eax, [eax]
	ret
Mars_PrintStr:
	mov esi, [esp+4]
	pushad
	invoke printf, offset forString, esi
	popad
	ret
main:
	push ebp
	mov ebp, esp
	sub esp, 4
	mov edx, 0
	mov ecx, edx
_1LoopCheckLabel:
	mov edx, 10
	push eax
	cmp ecx, edx
	setl al
	mov esi, eax
	and esi, 1
	pop eax
	mov ebx, esi
	mov edx, ebx
	cmp edx, 0
	je _1LoopEndLabel
	mov ebx, 2
	mov ecx, ebx
_1LoopStepLabel:
	mov edx, 1
	mov esi, edx
	add esi, ecx
	mov ecx, esi
	jmp _1LoopCheckLabel
_1LoopEndLabel:
	mov ebx, 0
	mov ecx, ebx
	cmp ecx, 0
	je _1otherwise1
	mov ecx, 3
	jmp _1endif
_1otherwise1:
	mov ebx, 4
_1endif:
	mov ecx, 0
	mov eax, ecx
	mov esp, ebp
	pop ebp
	ret
end __init
