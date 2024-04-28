	*=$0600

	ldx #5
loop:
	lda 1700
	clc
	adc #1
	sta 1700
	dex
	bne loop
	brk

	
