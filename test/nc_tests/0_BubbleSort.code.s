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
_4sc	db "after bubble sort:",0ah,0
_2sc	db "before bubble sort:",0ah,0
_1sc	db "please input ten int number for bubble sort:",0ah,0
_3sc	db 0ah,0

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
	sub esp, 72
	lea edx, _1sc
	push edx
	call Mars_PrintStr
	mov edi, eax
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
	mov ebx, 0
	mov esi, ecx
	add esi, ebx
	mov ebx, esi
	imul ebx, ebx, 4
	mov edx, 0
	mov esi, edx
	add esi, ebx
	mov ebx, esi
	mov esi, ebp
	add esi, -68
	mov [esi], ebx
	mov esi, ebp
	add esi, -44
	mov [esi], ecx
	call Mars_GetInt
	mov edi, eax
	mov esi, ebp
	add esi, -68
	mov ebx, [esi]
	mov esi, ebp
	add esi, -44
	mov ecx, [esi]
	mov edx, edi
	mov esi, ebp
	sub esi, ebx
	sub esi, 4
	mov [esi], edx
_1LoopStepLabel:
	mov ebx, 1
	mov esi, ebx
	add esi, ecx
	mov ecx, esi
	jmp _1LoopCheckLabel
_1LoopEndLabel:
	lea ecx, _2sc
	push ecx
	call Mars_PrintStr
	mov edi, eax
	sub esp, 4
	mov ebx, 0
	mov ecx, ebx
_2LoopCheckLabel:
	mov ebx, 10
	push eax
	cmp ecx, ebx
	setl al
	mov esi, eax
	and esi, 1
	pop eax
	mov edx, esi
	mov ebx, edx
	cmp ebx, 0
	je _2LoopEndLabel
	mov edx, 0
	mov esi, ecx
	add esi, edx
	mov edx, esi
	imul edx, edx, 4
	mov ebx, 0
	mov esi, ebx
	add esi, edx
	mov edx, esi
	mov esi, ebp
	sub esi, edx
	sub esi, 4
	mov edx, [esi]
	mov esi, ebp
	add esi, -48
	mov [esi], ecx
	push edx
	call Mars_PrintInt
	mov edi, eax
	sub esp, 4
	mov esi, ebp
	add esi, -48
	mov ecx, [esi]
_2LoopStepLabel:
	mov ebx, 1
	mov esi, ebx
	add esi, ecx
	mov ecx, esi
	jmp _2LoopCheckLabel
_2LoopEndLabel:
	lea ecx, _3sc
	push ecx
	call Mars_PrintStr
	mov edi, eax
	sub esp, 4
	mov ebx, 0
	mov esi, ebp
	add esi, -52
	mov eax, [esi]
	mov eax, ebx
	mov esi, ebp
	add esi, -52
	mov [esi], eax
_3LoopCheckLabel:
	mov ebx, 10
	mov esi, ebp
	add esi, -52
	mov eax, [esi]
	push eax
	cmp eax, ebx
	setl al
	mov esi, eax
	and esi, 1
	pop eax
	mov edx, esi
	mov esi, ebp
	add esi, -52
	mov [esi], eax
	mov ebx, edx
	cmp ebx, 0
	je _3LoopEndLabel
	mov edx, 0
	mov esi, ebp
	add esi, -56
	mov eax, [esi]
	mov eax, edx
	mov esi, ebp
	add esi, -56
	mov [esi], eax
_4LoopCheckLabel:
	mov edx, 10
	mov esi, ebp
	add esi, -52
	mov ebx, [esi]
	mov esi, edx
	sub esi, ebx
	mov eax, esi
	mov esi, ebp
	add esi, -52
	mov [esi], ebx
	mov edx, eax
	mov eax, 1
	mov esi, edx
	sub esi, eax
	mov ecx, esi
	mov edx, ecx
	mov esi, ebp
	add esi, -56
	mov ebx, [esi]
	push eax
	cmp ebx, edx
	setl al
	mov esi, eax
	and esi, 1
	pop eax
	mov eax, esi
	mov esi, ebp
	add esi, -56
	mov [esi], ebx
	mov ecx, eax
	cmp ecx, 0
	je _4LoopEndLabel
	mov ecx, 0
	mov esi, ebp
	add esi, -56
	mov eax, [esi]
	mov esi, eax
	add esi, ecx
	mov ecx, esi
	mov esi, ebp
	add esi, -56
	mov [esi], eax
	imul ecx, ecx, 4
	mov eax, 0
	mov esi, eax
	add esi, ecx
	mov ecx, esi
	mov esi, ebp
	sub esi, ecx
	sub esi, 4
	mov ecx, [esi]
	mov eax, 1
	mov esi, ebp
	add esi, -56
	mov ebx, [esi]
	mov esi, eax
	add esi, ebx
	mov edx, esi
	mov esi, ebp
	add esi, -56
	mov [esi], ebx
	mov eax, edx
	mov edx, 0
	mov esi, eax
	add esi, edx
	mov edx, esi
	imul edx, edx, 4
	mov eax, 0
	mov esi, eax
	add esi, edx
	mov edx, esi
	mov esi, ebp
	sub esi, edx
	sub esi, 4
	mov edx, [esi]
	push eax
	cmp ecx, edx
	setg al
	mov esi, eax
	and esi, 1
	pop eax
	mov eax, esi
	mov edx, eax
	cmp edx, 0
	je _1otherwise1
	mov edx, 0
	mov esi, ebp
	add esi, -56
	mov eax, [esi]
	mov esi, eax
	add esi, edx
	mov edx, esi
	mov esi, ebp
	add esi, -56
	mov [esi], eax
	imul edx, edx, 4
	mov eax, 0
	mov esi, eax
	add esi, edx
	mov edx, esi
	mov esi, ebp
	sub esi, edx
	sub esi, 4
	mov edx, [esi]
	mov eax, edx
	mov edx, 0
	mov esi, ebp
	add esi, -60
	mov [esi], eax
	mov esi, ebp
	add esi, -56
	mov eax, [esi]
	mov esi, eax
	add esi, edx
	mov edx, esi
	mov esi, ebp
	add esi, -56
	mov [esi], eax
	mov esi, ebp
	add esi, -60
	mov eax, [esi]
	imul edx, edx, 4
	mov ecx, 0
	mov esi, ecx
	add esi, edx
	mov edx, esi
	mov ecx, 1
	mov esi, ebp
	add esi, -60
	mov [esi], eax
	mov esi, ebp
	add esi, -56
	mov eax, [esi]
	mov esi, ecx
	add esi, eax
	mov ebx, esi
	mov esi, ebp
	add esi, -56
	mov [esi], eax
	mov esi, ebp
	add esi, -60
	mov eax, [esi]
	mov ecx, ebx
	mov ebx, 0
	mov esi, ecx
	add esi, ebx
	mov ebx, esi
	imul ebx, ebx, 4
	mov ecx, 0
	mov esi, ecx
	add esi, ebx
	mov ebx, esi
	mov esi, ebp
	sub esi, ebx
	sub esi, 4
	mov ebx, [esi]
	mov esi, ebp
	sub esi, edx
	sub esi, 4
	mov [esi], ebx
	mov ebx, 1
	mov esi, ebp
	add esi, -60
	mov [esi], eax
	mov esi, ebp
	add esi, -56
	mov eax, [esi]
	mov esi, ebx
	add esi, eax
	mov edx, esi
	mov esi, ebp
	add esi, -56
	mov [esi], eax
	mov esi, ebp
	add esi, -60
	mov eax, [esi]
	mov ebx, edx
	mov edx, 0
	mov esi, ebx
	add esi, edx
	mov edx, esi
	imul edx, edx, 4
	mov ebx, 0
	mov esi, ebx
	add esi, edx
	mov edx, esi
	mov esi, ebp
	sub esi, edx
	sub esi, 4
	mov [esi], eax
	jmp _1endif
_1otherwise1:
_1endif:
_4LoopStepLabel:
	mov edx, 1
	mov esi, ebp
	add esi, -56
	mov eax, [esi]
	mov esi, edx
	add esi, eax
	mov eax, esi
	mov esi, ebp
	add esi, -56
	mov [esi], eax
	mov esi, ebp
	add esi, -56
	mov [esi], eax
	jmp _4LoopCheckLabel
_4LoopEndLabel:
_3LoopStepLabel:
	mov eax, 1
	mov esi, ebp
	add esi, -52
	mov ebx, [esi]
	mov esi, eax
	add esi, ebx
	mov ebx, esi
	mov esi, ebp
	add esi, -52
	mov [esi], ebx
	mov esi, ebp
	add esi, -52
	mov [esi], ebx
	jmp _3LoopCheckLabel
_3LoopEndLabel:
	lea edx, _4sc
	push edx
	call Mars_PrintStr
	mov edi, eax
	sub esp, 4
	mov eax, 0
	mov edx, eax
_5LoopCheckLabel:
	mov eax, 10
	push eax
	cmp edx, eax
	setl al
	mov esi, eax
	and esi, 1
	pop eax
	mov ebx, esi
	mov eax, ebx
	cmp eax, 0
	je _5LoopEndLabel
	mov ebx, 0
	mov esi, edx
	add esi, ebx
	mov ebx, esi
	imul ebx, ebx, 4
	mov eax, 0
	mov esi, eax
	add esi, ebx
	mov ebx, esi
	mov esi, ebp
	sub esi, ebx
	sub esi, 4
	mov ebx, [esi]
	mov esi, ebp
	add esi, -64
	mov [esi], edx
	push ebx
	call Mars_PrintInt
	mov edi, eax
	sub esp, 4
	mov esi, ebp
	add esi, -64
	mov edx, [esi]
_5LoopStepLabel:
	mov eax, 1
	mov esi, eax
	add esi, edx
	mov edx, esi
	jmp _5LoopCheckLabel
_5LoopEndLabel:
	mov edx, 0
	mov eax, edx
	mov esp, ebp
	pop ebp
	ret
end __init
