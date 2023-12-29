.data
blank : .asciiz " "
.text
__init:
	lui $sp, 0x8000
	addi $sp, $sp, 0x0000
	move $fp, $sp
	add $gp, $gp, 0x8000
	jal main
	li $v0, 10
	syscall
Mars_PrintInt:
	li $v0, 1
	syscall
	li $v0, 4
	move $v1, $a0
	la $a0, blank
	syscall
	move $a0, $v1
	jr $ra
Mars_GetInt:
	li $v0, 5
	syscall
	jr $ra
Mars_PrintStr:
	li $v0, 4
	syscall
	jr $ra
main:
	subu $sp, $sp, 12
	li $25, 0
	move $24, $25
	li $25, 1
	move $23, $25
	li $25, 2
	move $22, $25
	add $25, $24, $23
	move $24, $25
	li $23, 3
	add $25, $22, $23
	move $22, $25
	add $23, $24, $22
	move $22, $23
	li $22, 0
	move $2, $22
	move $sp, $fp
	jr $31
