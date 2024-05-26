start:	*=$0600

	textout= 4000
	inter= 7000
	pointer =3000
	
	CRLF= $9B
	LF= $9C

	lda #169
	jsr tohex

	lda #<num
	sta pointer
	lda #>num
	sta pointer+1
	jsr print

	brk
	
	;; 
	;; bin to hex converter
	;; Converts a 8 bit integer to hex
	;;
	;; a  integer to convert
tohex:
	sta int			
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

	lda #<num
	sta pointer
	lda #>num
	sta pointer+1
	jsr print
	rts

int:
	.BYTE 0 		; Integer to convert
num:
	.BYTE 0,0," ",CRLF		; hex in ascii
hex:
	.BYTE "0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"
sav:
	.byte 0			; Temporary

	*=4000
print:
	rts


	*=$fffa 
	.WORD $a800		; nmi $fffa-b
	.WORD $0600		; Reset vector $fffc-d
