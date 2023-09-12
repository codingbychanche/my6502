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

	//
	// The 6502 status register
	//
	public class ProcessorStatus {
		byte N; // Negative flag
		byte V; // Overflow flag
		byte U = 1; // Unused...
		byte B; // Break flag
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
			return (String.format("-------SP=$%03x", START_ADDRESS_OF_STACK - s) + "// N=" + N + " V=" + V + " -=" + U
					+ " B=" + B + " D=" + D + " I=" + I + " Z=" + Z + " C=" + C + " // A=" + a + " X=" + x + " Y=" + y
					+ String.format(" // Status=$%02x", getStatusRegister()));
		}

		/**
		 * A 8 bit value of the status register.
		 * 
		 * @return Status register.
		 */
		public byte getStatusRegister() {
			byte s = (byte) (N * 128 + V * 64 + U * 32 + B * 16 + D * 8 + I * 4 + Z * 1 + C * 1);
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
		while (P.I != 1) {
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

		switch (command) {

		// brk
		// I Flag
		// push return address +2
		// push status flag
		//
		case 0x00:

			P.I = 1;
			s++;
			this.ram[START_ADDRESS_OF_STACK - s] = (byte) 0xff; // Low byte of retuirn address ??
			s++;
			this.ram[START_ADDRESS_OF_STACK - s] = (byte) 0xff; // High byte of return address ??
			s++;
			this.ram[START_ADDRESS_OF_STACK - s] = (byte) (P.getStatusRegister());
			// this.pc++;

			this.vt.getComandExecuted(String.format("$%04x", this.pc)
					+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " brk");
			break;

		// clc
		// Carry flag
		case 0x18:

			P.C = 0;
			this.pc++;

			this.vt.getComandExecuted(String.format("$%04x", this.pc)
					+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " clc");
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

			s++;
			this.ram[START_ADDRESS_OF_STACK - s] = (byte) a;
			this.pc++;

			this.vt.getComandExecuted(String.format("$%04x", this.pc)
					+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " pha");
			break;

		// pla
		// A,Z and N.
		case 0x68:

			a = unsignedByte(this.ram[(START_ADDRESS_OF_STACK - s)]);

			if (a == 0)
				P.Z = 1;
			else
				P.Z = 0;

			if (a > 127)
				P.N = 1;
			else
				P.N = 0;
			s--;
			this.pc++;

			this.vt.getComandExecuted(
					String.format("$%04x", this.pc) + String.format(" $%02x", unsignedByte(ram[this.pc])) + " pla");
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

		// lda #b
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

		// bne
		// Branch on result not zero.
		// No flags...
		case 0xd0:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " bne ";

			// pc already points to the current instruction. Now get the argument.
			this.pc++;
			int b = this.ram[pc];

			// Currently pc points to the argument, not the instruction
			// so we have to correct that.
			b++;
			int target = this.pc + b;

			// What ever the result of the previous instruction was, if it was zero
			// we do not branch!
			if (this.P.Z != 1) {

				// The target address of the branch is now calculated by subtracting/ adding
				// b from the address of the instruction.
				this.pc = this.pc + b;

			}
			// No branch!
			else
				// Next instruction.
				this.pc++;

			this.vt.getComandExecuted(com + String.format("$%04x", target));

			break;

		// jmp xxxx
		// Affects only the program counter
		case 0x4c:
			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " jmp ";
			this.pc++;
			int low = unsignedByte(this.ram[pc]);
			this.pc++;
			int high = unsignedByte(this.ram[pc]);
			int address = low + 256 * high;
			this.vt.getComandExecuted(com + String.format("$%04x", address));
			this.pc=address;
			
			break;
		}
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
			stackTrace.append(unsignedByte(ram[i]) + "  @" + String.format("%03x", i) + " | ");
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
