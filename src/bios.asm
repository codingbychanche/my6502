	;; Test BIOS for 6502 type processors.
	;;
	;; RetroZock 04/2024	


	;; System equates	
	textout=4000 	; OS routine emulating text output
	input=8000		; OS routine reading user input from console
	
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
start:	*=$0600

	;; Demo of an os routine
	;;
	
	ldx #<text
	ldy #>text
	jsr print

	buffer= 9000
loop:	
	ldx #<buffer
	ldy #>buffer
	jsr input
	lda buffer
	cmp #'q'
	bne  goon
	brk
goon:	
	jsr print
	jmp loop
out:	
	brk
	
text:
	.BYTE "6502 Emulator, 05/2024 BF"
	.BYTE $9b

	;; Some demo os routines
	;; 
	*=textout
print:	
	lda #255 		; Not much to do here. Just for debbuging 
	rts			; purposes. Output is done by VM
	
	;; Reads a string from console
	;;	
	*=input
input:
	lda #255
	rts
	
	;; 6502 Interrupt vector table
	;;
	;; Addreses are stored in a low - high byte
	;; order

	*=$fffa 
	.WORD $a800		; nmi $fffa-b
	.WORD $0600		; Reset vector $fffc-d


	
	
