package cpu;

/**
 * Emulates the infamous 8- Bit CPU
 * 
 * @author Berthold
 *
 */
public class Cpu_6502 {

	//
	// 6502 address space
	//
	private static final int START_ADDRESS_OF_STACK = 0x01ff;
	private static final int END_ADDRESS_OF_STACK = 0x0100;

	//
	// RAM
	//
	private byte[] ram;

	//
	//
	//
	public static int IRQ_VECTOR = 0xfffe;

	//
	// Virtual machine this processor is connected to..
	//
	VirtualMachine vt;

	//
	// Source
	//
	StringBuilder diassembled;

	//
	// Registers
	//
	int a, x, y; // Only 8- Bit's are used. They are infact 8 Bit registers
	int pc; // Programm counter always points to the next command to be executed
	int s = -1; // Stack pointer (Stack is between $0100 and $01ff which is the top of stack)

	// TODO: This is a workaround until hardware/ software interrupts are
	// implemented and working correctly. This is set to true, when a
	// brk-instruction
	// is executed...
	boolean endEmulation = true;

	//
	// The 6502 status register
	//
	public class ProcessorStatus {
		byte N; // Negative flag
		byte V; // Overflow flag
		byte U = 1; // Unused...
		byte B = 1; // Break flag
		byte D; // decimal mode flag
		byte I; // Interrupt disabeled flag
		byte Z; // Zero flag
		byte C; // Carry flag

		/**
		 * Show the status register.
		 *
		 * @return A string containing a human readable form of the register contents.
		 */
		public String printStatus() {
			return (String.format("PC=$%04x", pc) + String.format("-------SP=$%03x", START_ADDRESS_OF_STACK - s)
					+ "// N=" + N + " V=" + V + " -=" + U + " B=" + B + " D=" + D + " I=" + I + " Z=" + Z + " C=" + C
					+ " // A=" + a + " X=" + x + " Y=" + y + String.format(" // Status=$%02x", getStatusRegister()));
		}

		/**
		 * A 8 bit value of the status register.
		 * 
		 * @return Status register.
		 */
		public byte getStatusRegister() {
			byte s = (byte) (N * 128 + V * 64 + U * 32 + B * 16 + D * 8 + I * 4 + Z * 2 + C * 1);
			return s;
		}
	}

	public ProcessorStatus P;

	/**
	 * A new 6502- Processor.
	 * 
	 */
	public Cpu_6502(VirtualMachine vt) {
		this.vt = vt;
		this.P = new ProcessorStatus();
	}

	/**
	 * Executes a series of commands, stored from start address upwards in specified
	 * ram.
	 * 
	 * @param ram        Asscoiated ram.
	 * @param address    The address from where to run....
	 * @param clockSpeed The speed which each cycle takes in [ms]
	 * @return The resulting ram contents.
	 */
	public byte[] execute(byte[] ram, int address, long clockSpeed) {

		this.ram = ram;
		this.pc = address;
		this.diassembled = new StringBuilder(); // FIND ANOTHER WAY THAN USING THIS GLOBAL VAR....

		//
		// Parse
		//
		while (endEmulation) {
			int command = unsignedByte(this.ram[pc]);
			parser(command);
			vt.getProcessorState(this.P.printStatus());

			//
			// Whait to emulate the speed of the cpu...
			//
			// THIS HAS TO BE CHANGED.
			// IN ORDER TO EMULATE CORRECTLY A DEDICETD METHOD WHICH INCREASES
			// THE PC AND THEN WAITS HAS TO BE IMPLEMENTED. THIS WAY EACH
			// AFTER EACH CYCLE THERE IS A PAUSE AND NOT AFTER EACH COMMAND....
			//
			try {
				Thread.sleep(clockSpeed);
			} catch (Exception e) {
			}

		}
		return this.ram;
	}

	/**
	 * 6502
	 * 
	 * Either executes an command or returns the associated mnomic.
	 * 
	 * @param command   The binary code of the 6502 command
	 * @param doExecute Execute=true, Diassemble=false....
	 */
	private void parser(int command) {

		String com;
		int address, low, high;

		switch (command) {

		// brk
		//
		// push return address +2
		// push status flag
		//
		// This is a software interrupt. In the status register the B- flag is always
		// set
		// and is never affected by the brk- instruction. To check if a soft- or hardwar
		// interrupt
		// took place, one has the check if the P- register on the stack.
		// If the B- flag is set => Software intterupt. If not => hardware interrupt.
		case 0x00:
			this.endEmulation = false;

			// Push pc+2 high, low byte on the stack. Finaly the status register.
			// TODO Implement hardware interupt behaviour => brk flag of the status register
			// pushed on the stack =0!
			this.pc = this.pc + 2;
			this.s++;
			this.ram[START_ADDRESS_OF_STACK - s] = (byte) this.high(this.pc);
			this.s++;
			this.ram[START_ADDRESS_OF_STACK - s] = (byte) this.low(this.pc);
			this.s++;
			this.ram[START_ADDRESS_OF_STACK - s] = (byte) (P.getStatusRegister());

			this.vt.getComandExecuted(String.format("$%04x", this.pc)
					+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " brk // " + this.dumpStack(ram));

			// TODO Implement jump through 6502's IRQ/ BREAK vector....
			this.pc = this.ram[this.IRQ_VECTOR] * 256 + this.ram[this.IRQ_VECTOR + 1];

			break;

		// clc
		// Carry flag
		case 0x18:

			P.C = 0;

			this.vt.getComandExecuted(String.format("$%04x", this.pc)
					+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " clc");

			this.pc++;
			break;

		// sec
		// Carry flag
		case 0x38:

			P.C = 1;
			this.pc++;

			this.vt.getComandExecuted(String.format("$%04x", this.pc)
					+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " sec");
			break;

		// cli
		// Interrupt flag
		case 0x58:

			P.I = 0;
			this.pc++;

			this.vt.getComandExecuted(String.format("$%04x", this.pc)
					+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " cli");
			break;

		// sei
		// Interrupt flag
		case 0x78:

			P.I = 1;
			this.pc++;

			this.vt.getComandExecuted(String.format("$%04x", this.pc)
					+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " sei");
			break;

		// clv
		// Overflow flag
		case 0xb8:

			P.V = 0;
			this.pc++;

			this.vt.getComandExecuted(String.format("$%04x", this.pc)
					+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " clv");
			break;

		// cld
		// Decimal flag
		case 0xd8:

			P.D = 0;
			this.pc++;

			this.vt.getComandExecuted(String.format("$%04x", this.pc)
					+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " cld");

			break;

		// sed
		// decimal flag
		case 0xf8:

			P.D = 1;
			this.pc++;

			this.vt.getComandExecuted(
					String.format("$%04x", this.pc) + String.format(" $%02x", unsignedByte(this.ram[pc])) + " sed");

			break;

		// pha
		//
		case 0x48:

			this.s++;
			this.ram[START_ADDRESS_OF_STACK - s] = (byte) this.a;
			this.pc++;

			this.vt.getComandExecuted(String.format("$%04x", this.pc)
					+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " pha " + this.dumpStack(ram));
			break;

		// pla
		// A,Z and N.
		case 0x68:

			this.a = unsignedByte(this.ram[(START_ADDRESS_OF_STACK - s)]);

			if (a == 0)
				P.Z = 1;
			else
				P.Z = 0;

			if (a > 127)
				P.N = 1;
			else
				P.N = 0;
			this.s--;
			this.pc++;

			this.vt.getComandExecuted(String.format("$%04x", this.pc)
					+ String.format(" $%02x", unsignedByte(ram[this.pc])) + " pla " + " // " + this.dumpStack(ram));
			break;

		// ldy #b
		// Z,N Flags
		case 0xa0:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " ldy #";

			this.pc++;
			y = unsignedByte(this.ram[this.pc]);

			this.vt.getComandExecuted(com + String.format("$%02x", this.ram[this.pc]));

			if (y == 0)
				P.Z = 1;
			else
				P.Z = 0;

			if (y > 127)
				P.N = 1;
			else
				P.N = 0;
			this.pc++;

			break;

		// ldy b
		// Z,N Flags
		case 0xac:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " ldy ";

			this.pc++;
			low = unsignedByte(this.ram[pc]);
			this.pc++;
			high = unsignedByte(this.ram[pc]);
			address = low + 256 * high;
			//this.ram[address] = (byte) this.a;
			this.y = unsignedByte(this.ram[address]);

			this.vt.getComandExecuted(com + String.format("$%04x", address));

			if (this.y == 0)
				P.Z = 1;
			else
				P.Z = 0;

			if (this.y > 127)
				P.N = 1;
			else
				P.N = 0;
			
			this.pc++;

			break;

		// ldx #b
		// Z,N Flags
		case 0xa2:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " ldx #";

			this.pc++;
			x = unsignedByte(this.ram[this.pc]);

			this.vt.getComandExecuted(com + String.format("$%02x", this.ram[this.pc]));

			if (x == 0)
				P.Z = 1;
			else
				P.Z = 0;

			if (x > 127)
				P.N = 1;
			else
				P.N = 0;
			this.pc++;
			break;

		// ldx b
		// Z,N Flags
		case 0xae:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " ldx ";

			this.pc++;
			low = unsignedByte(this.ram[pc]);
			this.pc++;
			high = unsignedByte(this.ram[pc]);
			address = low + 256 * high;
			//this.ram[address] = (byte) this.a;
			this.x = unsignedByte(this.ram[address]);

			this.vt.getComandExecuted(com + String.format("$%04x", address));

			if (this.x == 0)
				P.Z = 1;
			else
				P.Z = 0;

			if (this.x > 127)
				P.N = 1;
			else
				P.N = 0;
			
			this.pc++;

			break;
			
		// dex
		// N,Z
		case 0xca:

			this.x--;
			if (this.x == -1)
				this.x = 255;
			if (x == 0)
				this.P.Z = 1;
			else
				this.P.Z = 0;
			if (this.x >= 0 && x <= 127)
				this.P.N = 0;
			else
				this.P.N = 1;

			this.vt.getComandExecuted(String.format("$%04x", this.pc)
					+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " dex");
			this.pc++;

			break;

		// dey
		// N,Z
		case 0x88:

			this.y--;
			if (this.y == -1)
				this.y = 255;
			if (this.y == 0)
				this.P.Z = 1;
			else
				this.P.Z = 0;
			if (this.y >= 0 && this.y <= 127)
				this.P.N = 0;
			else
				this.P.N = 1;
			this.pc++;

			this.vt.getComandExecuted(String.format("$%04x", this.pc)
					+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " dey");
			break;

		// lda #b Immediate
		// Z,N Flags
		case 169:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " lda #";
			this.pc++;
			this.a = unsignedByte(this.ram[pc]);

			this.vt.getComandExecuted(com + String.format("$%02x", this.a));

			if (this.a == 0)
				P.Z = 1;
			else
				P.Z = 0;

			if (this.a > 127)
				P.N = 1;
			else
				P.N = 0;
			this.pc++;

			break;

			// lda b Absolut
			// Z,N Flags
			case 0xad:

				com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " lda ";
				
				this.pc++;
				low = unsignedByte(this.ram[pc]);
				this.pc++;
				high = unsignedByte(this.ram[pc]);
				address = low + 256 * high;
				this.ram[address] = (byte) this.a;
				this.a = unsignedByte(this.ram[address]);

				this.vt.getComandExecuted(com + String.format("$%04x", address));

				if (this.a == 0)
					P.Z = 1;
				else
					P.Z = 0;

				if (this.a > 127)
					P.N = 1;
				else
					P.N = 0;
				
				this.pc++;

				break;

		// sta xxxx Absolute
		//
		case 0x8d:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " sta ";
			this.pc++;
			low = unsignedByte(this.ram[pc]);
			this.pc++;
			high = unsignedByte(this.ram[pc]);
			address = low + 256 * high;
			this.ram[address] = (byte) this.a;

			this.vt.getComandExecuted(com + String.format("$%04x", address));
			this.pc++;
			break;

		// adc
		// Carry, overvlow, negative and zero flag
		case 0x69:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " adc #";
			this.pc++;
			this.vt.getComandExecuted(com + String.format("$%02x", this.ram[pc]));

			// BCD?
			if (this.P.D == 1) {
				// TODO Insert adc BCD- mode...

			} else {
				// Non BCD
				// Add with carry
				// TODO Debug Overflow flag.....

				this.a = this.a + this.ram[pc] + this.P.C;

				if (this.a == 0) {
					this.P.Z = 1;
					this.P.N = 0;
				} else
					this.P.Z = 0;

				if (this.a > 255) {
					this.a = 0;
					this.P.C = 1;
					this.P.Z = 1;
					this.P.N = 0;

				}
				// TODO Check behaviour of overflow flag
				// a< -128 or a> 127
				// if (this.a > 127 && this.a <= 255)
				// this.P.V = 0;
				// else
				// this.P.V = 1;

			}

			pc++;

			break;

		// bne
		// Branch on result not zero.
		// No flags...
		//
		// TODO: Check forward branches....
		case 0xd0:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " bne ";
			
			// pc already points to the current instruction. Now get the argument.
			this.pc++;
			int b = this.ram[pc];
		

			// Currently pc points to the argument, not the instruction
			// so we have to correct that.
			b++;
			
			int target = this.pc+ b;
			

			// What ever the result of the previous instruction was, if it was zero
			// we do not branch!
			if (this.P.Z != 1) {

				// The target address of the branch is now calculated by subtracting/ adding
				// b from the address of the instruction.
				this.pc = target;

			}
			// No branch!
			else
				// Next instruction.
				this.pc++;

			this.vt.getComandExecuted(com + String.format("$%04x", target));

			break;

		// jsr xxxx
		// Program counter, stack pointer
		case 0x20:
			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " jsr ";
			this.pc++;
			high = unsignedByte(this.ram[pc]);
			this.pc++;
			low = unsignedByte(this.ram[pc]);
			address = low + 256 * high; // Address of subroutine

			// Push return address on the stack, which is the current address
			// the pc contains (adddress of jsr- instruction +2)
			//
			this.s++;
			this.ram[START_ADDRESS_OF_STACK - s] = (byte) this.high(this.pc);
			this.s++;
			this.ram[START_ADDRESS_OF_STACK - s] = (byte) this.low(this.pc);

			this.vt.getComandExecuted(com + String.format("$%04x", address) + " // " + this.dumpStack(ram));

			this.pc = address; // jump to subroutine

			break;

		// rts
		//
		case 0x60:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " rts ";

			// pull return address from stack
			//
			low = ram[START_ADDRESS_OF_STACK - s];
			s--;
			high = ram[START_ADDRESS_OF_STACK - s];
			s--;

			address = low + 256 * high;

			address++;
			this.pc = address; // Return

			vt.getComandExecuted(com + " // " + this.dumpStack(ram));
			break;

		// jmp xxxx
		// Affects only the program counter
		case 0x4c:
			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " jmp ";
			this.pc++;
			high = unsignedByte(this.ram[pc]);
			this.pc++;
			low = unsignedByte(this.ram[pc]);
			address = low + 256 * high;
			this.vt.getComandExecuted(com + String.format("$%04x", address));

			this.pc = address;

			// Inform virtual machine.....
			vt.jmpAddressTrap(address);

			break;
		}
	}

	/**
	 * HHigh byte part of an 16- bit integer
	 * 
	 * @param integer 16- bit integer
	 * @return High byte part of integer passed
	 */
	private int high(int integer) {
		return integer / 256;
	}

	/**
	 * Low byte part of an 16- bit integer
	 * 
	 * @param integer 16- bit integer
	 * @return Low byte part of integer passed
	 */
	private int low(int integer) {
		return integer - this.high(integer) * 256;
	}

	/**
	 * Two's complement
	 * 
	 * This checks if an integer contains a 8- Bit number which is bigger than 127,
	 * meanig it is a negative number.
	 * </p>
	 * 
	 * If so the following function is applied: b=b EOR 255:
	 * </p>
	 * 
	 * <b>Expample:</b>
	 * </p>
	 * 
	 * 00000101 = 5<br>
	 * 11111010 =-5
	 * </p>
	 * 
	 * If the parameter passed is negative it is converted and the nagative value
	 * returned.
	 * </p>
	 * 
	 * Java's byte works that way and one does not need this method to distinguish
	 * wether an byte value is positive or negative.
	 * </p>
	 * 
	 * 
	 * @param b Number to check/ convert.
	 * @return Converted integer.
	 */

	public int complement(int b) {
		if (b < 127)
			b = pc + b;
		else {
			b = (b ^ 255 * -1) + this.pc;
		}
		return b;
	}

	/**
	 * Shows the contents of the current stack.
	 * 
	 * @pram ram Ram of the host machine.
	 * @return String containing the contents of the stack.
	 */
	public String dumpStack(byte[] ram) {

		StringBuilder stackTrace = new StringBuilder();
		stackTrace.append("Stack | ");
		for (int i = START_ADDRESS_OF_STACK; i >= (START_ADDRESS_OF_STACK - s); i--)
			stackTrace.append(String.format("$%02x", unsignedByte(ram[i])) + "  @" + String.format("$%03x", i) + " | ");
		stackTrace.append("<< Top of Stack");
		return stackTrace.toString();
	}

	/**
	 * Other getters....
	 * 
	 * @return
	 */

	public ProcessorStatus getP() {
		return P;
	}

	public int getA() {
		return a;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getPc() {
		return pc;
	}

	public int getS() {
		return s;
	}

	public int unsignedByte(byte b) {
		return b & 0xff;
	}
}
