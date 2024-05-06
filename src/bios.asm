	;; Test BIOS for 6502 type processors
	;; for a virtual test- machine.
	;;
	;; RetroZock 04/2024	


	;; System equates	
	textout= 4000 	; OS routine emulating text output
	input= 8000	; OS routine reading user input from console
	
	;; Stack
	*=$0100

	;; --------------------------------------------------------------------------
	;; Start of the operating system
	;;
start:	*=$0600

	;; Say hello. Print Start message
	;;
	lda #8
	lsr
	lsr
	lsr
	lsr
	lsr
	ldx #<text
	ldy #>text
	jsr print

	;; Get command from console
	;; 
	buffer= 9000 		; User Input is stored here.
loop:
	ldx #<prompt
	ldy #>prompt
	jsr print
	
	ldx #<buffer
	ldy #>buffer
	jsr input
	
evaluate:
	cpx #1			; We have 1 char commands....
	bne error 
	
	lda buffer
	cmp #'q' 		; Leave
	beq  out

	cmp #'d'		; Dump memmory contents
	bne h
	jsr dump
	jmp loop
h:	
	cmp #'h'		; Hex converter
	bne error		; Last command does not match => error
	jsr tohex
	jmp loop
error:		
	ldx #<wrgcom
	ldy #>wrgcom
	jsr print
goon:	
	jmp loop		; Main loop

	;; Leave
	;; 
out:	
	brk			; Leave emu...
	;; --------------------------------------------------------------------------

	;; Dump memory contents
	;;
dump:
	ldx #<dtext
	ldy #>dtext
	jsr print
	rts
	
dtext:	
	.BYTE "dump:"
	.BYTE CRLF
	
	;; 
	;; bin to hex converter
	;; Converts a 8 bit integer to hex
	;;
	;; a  integer to convert
tohex:
	lda #255		; For debbuging purposes remove when it works :-)
	sta num			
	lsr			; Divide num by 16 to get ones...
	lsr
	lsr
	lsr
	tax			; now get first digit
	lda hex,x		; Ascii
	sta num			; Save ones...

	txa			; Tens= int-(num * 16) is second digit			
	asl
	asl
	asl
	asl
	sta sav
	clc
	lda int
	sbc sav
	tax
	lda hex,x
	sta num+1		; Tens
	
	ldx #<num		; Print result...
	ldy #>num
	jsr print

	
	rts
int:
	.BYTE 0 		; Integer to convert
num:
	.BYTE 0,0,CRLF		; hex in ascii
hex:
	.BYTE "0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"
sav:
	.byte 0			; Temporary

	;; Textbuffer
	;; 
	CRLF= $9B
	LF= $9C
text:
	.BYTE "6502 Emulator, 05/2024 BF"
	.BYTE CRLF

prompt:	
	.BYTE ">"
	.BYTE LF
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


	
	
