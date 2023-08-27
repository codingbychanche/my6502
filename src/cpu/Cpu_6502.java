package cpu;

/**
 * Emulates the infamous 8- Bit CPU
 * 
 * @author Berthold
 *
 */
public class Cpu_6502 {

	private static final int START_ADDRESS_OF_STACK = 0x01ff;
	private static final int END_ADDRESS_OF_STACK = 0x0100;

	//
	// Disassembled code
	//
	StringBuilder diassembled = new StringBuilder();

	//
	// Registers
	//

	int a, x, y; // Only 8- Bit's are used. They are infact 8 But registers
	int pc; // Programm counter always points to the next command to be executed
	int s = -1; // Stack pointer (Stack is between $0100 and $01ff which is the top of stack)

	//
	// The Status register
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

		//
		// A human readable form of the processor status register
		//
		public String printStatus() {

			return (String.format("PC=$%04x", pc) + String.format(" SP=$%03x", START_ADDRESS_OF_STACK - s) + "// N=" + N
					+ " V=" + V + " -=" + U + " B=" + B + " D=" + D + " I=" + I + " Z=" + Z + " C=" + C + " // A=" + a
					+ " X=" + x + " Y=" + y + String.format(" // Status=$%02x", getStatusRegister()));
		}

		//
		// Returns the status register as an 8 bit byte value
		//
		public byte getStatusRegister() {
			byte s = (byte) (N * 1 + V * 2 + U * 4 + B * 8 + D * 16 + I * 32 + Z * 64 + C * 128);
			return s;
		}
	}

	public ProcessorStatus P = new ProcessorStatus();

	/**
	 * Shows the contents of the current stack.
	 * 
	 * @param ram Ram of the host machine.
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
	 * The diassembler. If the cpu medel was run with the 'execute' option set to
	 * false, a diassembled source code listing is returned.
	 * 
	 * @return The diasembled code.
	 */
	public String getDiassembledCode() {
		return diassembled.toString();
	}

	/**
	 * The Cpu- Model
	 * 
	 */
	public Cpu_6502() {

	}

	/**
	 * Executes or diassembles a series of commands, stored from start address
	 * upwards in specified ram.
	 * 
	 * @param ram     Asscoiated ram.
	 * @param address The address from where to run....
	 * @return The resulting ram contents.
	 */
	public byte[] execute(byte[] ram, int address, boolean doExecute, int length) {

		pc = address;

		//
		// Parser
		//
		while (P.B != 1 && length > 0) {

			int command = unsignedByte(ram[pc]);

			switch (command) {

			// brk
			// I Flag
			// push return address +2
			// push status flag
			//
			case 0x00:
				if (doExecute) {
					P.B = 1;
					s++;
					ram[START_ADDRESS_OF_STACK - s] = (byte) (pc + 2 / 256); // Low byte of retuirn address ??
					s++;
					ram[START_ADDRESS_OF_STACK - s] = (byte) (pc + 2); // High byte of return address ??
					s++;
					ram[START_ADDRESS_OF_STACK - s] = (byte) (P.getStatusRegister());
					// pc++;
				} else {
					diassembled.append(
							String.format("$%04x", pc) + String.format(" $%02x", unsignedByte(ram[pc])) + " brk\n");
					pc++;
				}
				break;

			// clc
			// Carry flag
			case 0x18:
				if (doExecute) {
					P.C = 0;
					pc++;
				} else {
					diassembled.append(
							String.format("$%04x", pc) + String.format(" $%02x", unsignedByte(ram[pc])) + " clc\n");
					pc++;
				}
				break;

			// sec
			// Carry flag
			case 0x38:
				if (doExecute) {
					P.C = 1;
					pc++;
				} else {
					diassembled.append(
							String.format("$%04x", pc) + String.format(" $%02x", unsignedByte(ram[pc])) + " sec\n");
					pc++;
				}
				break;

			// cli
			// Interrupt flag
			case 0x58:
				if (doExecute) {
					P.I = 0;
					pc++;
				} else {
					diassembled.append(
							String.format("$%04x", pc) + String.format(" $%02x", unsignedByte(ram[pc])) + " cli\n");
					pc++;
				}
				break;

			// sei
			// Interrupt flag
			case 0x78:
				if (doExecute) {
					P.I = 1;
					pc++;
				} else {
					diassembled.append(
							String.format("$%04x", pc) + String.format(" $%02x", unsignedByte(ram[pc])) + " sei\n");
					pc++;
				}
				break;

			// clv
			// Overflow flag
			case 0xb8:
				if (doExecute) {
					P.V = 0;
					pc++;
				} else {
					diassembled.append(
							String.format("$%04x", pc) + String.format(" $%02x", unsignedByte(ram[pc])) + " clv\n");
					pc++;
				}
				break;

			// cld
			// Decimal flag
			case 0xd8:
				if (doExecute) {
					P.D = 0;
					pc++;
				} else {
					diassembled.append(
							String.format("$%04x", pc) + String.format(" $%02x", unsignedByte(ram[pc])) + " cld\n");
					pc++;
				}
				break;

			// sed
			// decimal flag
			case 0xf8:
				if (doExecute) {
					P.D = 1;
					pc++;
				} else {
					diassembled.append(
							String.format("$%04x", pc) + String.format(" $%02x", unsignedByte(ram[pc])) + " sed\n");
					pc++;
				}
				break;

			// pha
			//
			case 0x48:
				if (doExecute) {
					s++;
					ram[START_ADDRESS_OF_STACK - s] = (byte) a;
					pc++;
				} else {
					diassembled.append(
							String.format("$%04x", pc) + String.format(" $%02x", unsignedByte(ram[pc])) + " pha\n");
					pc++;
				}
				break;

			// pla
			// A,Z and N.
			case 0x68:
				if (doExecute) {
					a = unsignedByte(ram[START_ADDRESS_OF_STACK - s]);
					s--;
					if (a == 0)
						P.Z = 1;
					else
						P.Z = 0;

					if (a > 127)
						P.N = 1;
					else
						P.N = 0;

					pc++;
				} else {
					diassembled.append(
							String.format("$%04x", pc) + String.format(" $%02x", unsignedByte(ram[pc])) + " pla\n");
					pc++;
				}
				break;

			// ldy #b
			// Z,N Flags
			case 0xa0:
				if (doExecute) {
					pc++;
					y = unsignedByte(ram[pc]);

					if (y == 0)
						P.Z = 1;
					else
						P.Z = 0;

					if (y > 127)
						P.N = 1;
					else
						P.N = 0;
					pc++;

				} else {
					diassembled.append(
							String.format("$%04x", pc) + String.format(" $%02x", unsignedByte(ram[pc])) + " ldy #");
					pc++;
					diassembled.append(String.format("$%02x", ram[pc]) + "\n");
					pc++;
				}
				break;

			// ldx #b
			// Z,N Flags
			case 0xa2:
				if (doExecute) {
					pc++;
					x = unsignedByte(ram[pc]);

					if (x == 0)
						P.Z = 1;
					else
						P.Z = 0;

					if (x > 127)
						P.N = 1;
					else
						P.N = 0;
					pc++;
				} else {
					diassembled.append(
							String.format("$%04x", pc) + String.format(" $%02x", unsignedByte(ram[pc])) + " ldx #");
					pc++;
					diassembled.append(String.format("$%02x", ram[pc]) + "\n");
					pc++;
				}
				break;

			// lda #b
			// Z,N Flags
			case 169:
				if (doExecute) {
					pc++;
					a = unsignedByte(ram[pc]);

					if (a == 0)
						P.Z = 1;
					else
						P.Z = 0;

					if (a > 127)
						P.N = 1;
					else
						P.N = 0;
					pc++;

				} else {
					diassembled.append(
							String.format("$%04x", pc) + String.format(" $%02x", unsignedByte(ram[pc])) + " lda #");
					pc++;
					diassembled.append(String.format("$%02x", ram[pc]) + "\n");
					pc++;
				}
				break;
			}

			//
			// For the diassembler solely...
			//
			length--;
		}
		System.out.println(diassembled.toString());
		return ram;
	}

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
