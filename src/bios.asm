	;; Test BIOS for 6502 type processors
	;; for a virtual test- machine.
	;;
	;; RetroZock 04/2024	


	;; System equates	
	textout= 4000 	; OS routine emulating text output
	input= 8000	; OS routine reading user input from console
	
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

	;; Send text to copnsole
	;;
	
	ldx #<text
	ldy #>text
	jsr print

	;; Get text from console
	;; 
	buffer= 9000 		; User Input is stored here.
loop:	
	ldx #<buffer
	ldy #>buffer
	jsr input
	cpx #1	
	beq evaluate
	ldx #<wrgcom
	ldy #>wrgcom
	jsr print
	jmp goon
evaluate:
	lda buffer
	cmp #'q'
	bne  goon
	brk
goon:	
	jmp loop

	;; Leave
	;; 
out:	
	brk

	;; Textbuffer
	;; 
	CRLF= $9B
text:
	.BYTE "6502 Emulator, 05/2024 BF"
	.BYTE CRLF

prompt:	
	.BYTE ">"
	.BYTE CRLF

wrgcom:
	.BYTE "Not a valid command...."
	.BYTE CRLF

	;; Some demo os routines
	;; 

	;; Write text to console
	;; 
	*=textout
print:
	;; At this point the real hardware would contain
	;; code taking care of the user input....
	;; For the vm this is merly a placeholder.
	;; At this point the emulator calls th apropiate
	;; routine of the vm and returns to execute the next command...
	lda #255 		; Not much to do here. Just for debbuging 
	rts			; purposes. Output is done by VM
	
	;; Reads a string from console
	;; Returns in x=length of string.
	*=input
input:
	;; At this point the real hardware would contain
	;; code taking care of the user input....
	;; For the vm this is merly a placeholder.
	;; At this point the emulator calls th apropiate
	;; routine of the vm and returns to execute the next command...	
	ldx #0
l:
	lda buffer,x		; Get lenght of string
	cmp #CRLF
	beq o
	inx
	jmp l
o:	
	rts			; Length of string is stored in x
	
	;; 6502 Interrupt vector table
	;;
	;; Addreses are stored in a low - high byte
	;; order

	*=$fffa 
	.WORD $a800		; nmi $fffa-b
	.WORD $0600		; Reset vector $fffc-d


	
	
