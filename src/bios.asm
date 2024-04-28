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
	
	;; 6502 Interrupt vector table
	;;
	;; Addreses are stored in a low - high byte
	;; order
	
	*=$1000 
	.WORD $aa		; nmi $fffa-b
	.WORD $bb		; Reset vector $fffc-d
	.WORD $CC		; IRQ vector $fffe-f
	
	
	
