	;; Test BIOS for 6502 type processors
	;; for a virtual test- machine.
	;;
	;; RetroZock 04/2024	

	textout= 4000 		; OS routine emulating text output
	pointer= 7000		; Generic pointer
	input= 	8000		; OS routine reading user input from console
	buffer= 9000	 	; User Input is stored here.

	CRLF= $9B		; Carriage return and Linefeed
	LF= $9C			; Linefeed
	
	.MACRO linefeed
	pha
	txa
	pha
	tya
	pha

	lda #<feed
	sta pointer
	lda #>feed
	sta pointer+1
	jsr print

	pla
	tay
	pla
	tax
	pla

	.ENDM

	;; Stack		; 6502 Stack starts here...
	*=$0100

	;; --------------------------------------------------------------------------
	;; Start of the operating system
	;;
start:	*=$0600
	
	;; Say hello. Print Start message
	;;
	lda #<text
	sta pointer
	lda #>text
	sta pointer+1
	jsr print

	;; Sandbox
	;;

	
	;; Get command from console
	;;

loop:	lda #<prompt
	sta pointer
	lda #>prompt
	sta pointer+1
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
	lda #169		; Number to convert
	jsr tohex
	
	lda #<num		; Show result
	sta pointer
	lda #>num
	sta pointer+1
	jsr print
	linefeed
	
	jmp loop
error:		
	lda #<wrgcom
	sta pointer
	lda #>wrgcom
	sta pointer+1
	jsr print
goon:	
	jmp loop		; Main loop

	;; Leave
	;; 
out:	
	brk			; Leave emu...


test:	lda #255
	sta 4800
	jsr t2
	rts

t2:	lda #128
	sta 4800
	jsr t3
	rts

t3:	lda #$44
	sta 4800
	rts

	
	;; --------------------------------------------------------------------------
	;; 
	;; bin to hex converter
	;; Converts a 8 bit integer to hex
	;;
	;; a  integer to convert
tohex:

	sta int			
	pha
	txa
	pha
	tya
	pha
	
	lda int

	lsr			; Divide num by 16 to get ones...
	lsr
	lsr
	lsr
	tax			; now get first digit
	lda hex,x		; Ascii
	sta num			; Save first digit, a= ones in binary

	txa 			;; Tens=int-(a(ones) * 16)
	asl
	asl
	asl
	asl
	sta sav
	sec
	lda int			; Get number to convert
	sbc sav			; Calc tens
	tax
	lda hex,x		; Get diget
	sta num+1
	
	pla
	tya
	pla
	txa
	pla
	
	rts

int:	.BYTE 0 		; Integer to convert
num:	.BYTE 0,0," ",LF	; hex in ascii
hex:	.BYTE "0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"
sav:	.BYTE 0			; Temporary

	;; Dump memory contents
	;;
dump:	
	lda #<dtext
	sta pointer
	lda #>dtext
	sta pointer+1
	jsr print 
	
	lda #<$0630
	sta 200
	lda #>$0630
	sta 201
	ldx #100
ll:	
	ldy #0	
lll:	lda (200),y		; Show hex
	jsr tohex
	lda #<num
	sta pointer
	lda #>num
	sta pointer+1
	jsr print
	iny
	cpy #10
	bne lll

	ldy #0	
llll:
	lda (200),y		; Show ascii
	cmp #128 		; < 128
	bcc g1

	lda # '.'		; >127 nothing to show..
	sta char
	jmp gg
g1:
	sta char
gg:
	lda #<char
	sta pointer 
	lda #>char
	sta pointer+1
	jsr print
	iny
	cpy #10
	bne llll

	linefeed

	clc
	lda 200
	adc #10
	sta 200
	lda 201
	adc #0
	sta 201		
	dex
	bne ll
	
	rts



	
dtext:	
	.BYTE "dump:"
	.BYTE CRLF
char:
	.BYTE 0,LF
	

text:
	.BYTE "6502 Emulator, 05/2024 BF"
	.BYTE CRLF

prompt:	
	.BYTE ">"
	.BYTE LF
wrgcom:
	.BYTE "Not a valid command...."
	.BYTE CRLF

	;; If nedded, use :-)
feed:
	.BYTE " ",CRLF
	
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
	rts

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

	;; Text buffer pointer for in/ out routines
	;; 
	*=pointer
	.BYTE 0			; Address of textbuffer
	.BYTE 0
	
	;; 6502 Interrupt vector table
	;;
	;; Addreses are stored in a low - high byte
	;; order

	*=$fffa 
	.WORD $a800		; nmi $fffa-b
	.WORD $0600		; Reset vector $fffc-d


	
	
