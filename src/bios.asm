	;; Test BIOS for 6502 type processors.
	;;
	;; RetroZock 04/2024	

	;; Stack
	*=$0100
	.BYTE $aa,$bb,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0,0,0,0,0,0

	.BYTE 0,0,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0,0,0,0,0,0

	.BYTE 0,0,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0,0,0,0,0,0
	.BYTE 0,0,0,0,0

	;; Start of the operating system
	;;
	*=$0600
loop:	clc
	lda #1
	adc #10
	sta 3000
	brk
	
	;; 6502 Interrupt vector table
	;;
	;; Addreses are stored in a low - high byte
	;; order

	*=$fffa 
	.WORD $a800		; nmi $fffa-b
	.WORD $0600		; Reset vector $fffc-d

	;; $fffe and $ffff (IRQ Vector seems not to be compiling..
	
	
	
