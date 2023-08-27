package cpu;

/**
 * Emulates the infamous 8- Bit CPU
 * 
 * @author Berthold
 *
 */
public class Cpu_6502 {

	//
	// Specifies wether to execute the given code or to build a
	// source code listing.
	//
	private static final boolean EXECUTE = true;
	private static final boolean DIASSEMBLE = false;

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
			return (String.format("PC=$%04x", pc) + String.format(" SP=$%03x", START_ADDRESS_OF_STACK - s) + "// N=" + N
					+ " V=" + V + " -=" + U + " B=" + B + " D=" + D + " I=" + I + " Z=" + Z + " C=" + C + " // A=" + a
					+ " X=" + x + " Y=" + y + String.format(" // Status=$%02x", getStatusRegister()));
		}

		/**
		 * A 8 bit value of the status register.
		 * 
		 * @return Status register.
		 */
		public byte getStatusRegister() {
			byte s = (byte) (N * 1 + V * 2 + U * 4 + B * 8 + D * 16 + I * 32 + Z * 64 + C * 128);
			return s;
		}
	}
	public ProcessorStatus P;

	/**
	 * A new 6502- Processor.
	 * 
	 */
	public Cpu_6502() {
		this.P = new ProcessorStatus();
	}

	/**
	 * Executes a series of commands, stored from start address upwards in specified
	 * ram.
	 * 
	 * @param ram     Asscoiated ram.
	 * @param address The address from where to run....
	 * @return The resulting ram contents.
	 */
	public byte[] execute(byte[] ram, int address) {

		this.ram = ram;

		this.pc = address;

		//
		// Parse
		//
		while (P.B != 1) {
			int command = unsignedByte(this.ram[pc]);
			parser(command, EXECUTE);
		}
		return this.ram;
	}

	/**
	 * Diassembles the ram contents.
	 * 
	 * @param ram Specifed ram.
	 * @param start Start address.
	 * @param end End address.
	 * @return The diassemble source code listing.
	 */
	public String dissasemble(byte[] ram, int start, int end) {
		this.ram = ram;
		this.pc = start; // Set start, is updated py parser...
		this.diassembled = new StringBuilder();

		while (this.pc != end) {
			int command = unsignedByte(this.ram[this.pc]);
			parser(command, DIASSEMBLE);
		}
		return diassembled.toString();
	}

	/**
	 * 6502
	 * 
	 * Either executes an command or returns the associated mnomic.
	 * 
	 * @param command The binary code of the 6502 command 
	 * @param doExecute Execute=true, Diassemble=false....
	 */
	private void parser(int command, boolean doExecute) {
		
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
				this.ram[START_ADDRESS_OF_STACK - s] = (byte) 0xff; // Low byte of retuirn address ??
				s++;
				this.ram[START_ADDRESS_OF_STACK - s] = (byte) 0xff; // High byte of return address ??
				s++;
				this.ram[START_ADDRESS_OF_STACK - s] = (byte) (P.getStatusRegister());
				// this.pc++;
			} else {
				diassembled.append(String.format("$%04x", this.pc)
						+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " brk\n");
				this.pc++;
			}
			break;

		// clc
		// Carry flag
		case 0x18:
			if (doExecute) {
				P.C = 0;
				this.pc++;
			} else {
				diassembled.append(String.format("$%04x", this.pc)
						+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " clc\n");
				this.pc++;
			}
			break;

		// sec
		// Carry flag
		case 0x38:
			if (doExecute) {
				P.C = 1;
				this.pc++;
			} else {
				diassembled.append(String.format("$%04x", this.pc)
						+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " sec\n");
				this.pc++;
			}
			break;

		// cli
		// Interrupt flag
		case 0x58:
			if (doExecute) {
				P.I = 0;
				this.pc++;
			} else {
				diassembled.append(String.format("$%04x", this.pc)
						+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " cli\n");
				this.pc++;
			}
			break;

		// sei
		// Interrupt flag
		case 0x78:
			if (doExecute) {
				P.I = 1;
				this.pc++;
			} else {
				diassembled.append(String.format("$%04x", this.pc)
						+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " sei\n");
				this.pc++;
			}
			break;

		// clv
		// Overflow flag
		case 0xb8:
			if (doExecute) {
				P.V = 0;
				this.pc++;
			} else {
				diassembled.append(String.format("$%04x", this.pc)
						+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " clv\n");
				this.pc++;
			}
			break;

		// cld
		// Decimal flag
		case 0xd8:
			if (doExecute) {
				P.D = 0;
				this.pc++;
			} else {
				diassembled.append(String.format("$%04x", this.pc)
						+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " cld\n");
				this.pc++;
			}
			break;

		// sed
		// decimal flag
		case 0xf8:
			if (doExecute) {
				P.D = 1;
				this.pc++;
			} else {
				diassembled.append(String.format("$%04x", this.pc) + String.format(" $%02x", unsignedByte(this.ram[pc]))
						+ " sed\n");
				this.pc++;
			}
			break;

		// pha
		//
		case 0x48:
			if (doExecute) {
				s++;
				this.ram[START_ADDRESS_OF_STACK - s] = (byte) a;
				this.pc++;
			} else {
				diassembled.append(String.format("$%04x", this.pc)
						+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " pha\n");
				this.pc++;
			}
			break;

		// pla
		// A,Z and N.
		case 0x68:
			if (doExecute) {
				a = unsignedByte(this.ram[(START_ADDRESS_OF_STACK-s)]);

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
			} else {
				diassembled.append(String.format("$%04x", this.pc) + String.format(" $%02x", unsignedByte(ram[this.pc]))
						+ " pla\n");
				this.pc++;
			}
			break;

		// ldy #b
		// Z,N Flags
		case 0xa0:
			if (doExecute) {
				this.pc++;
				y = unsignedByte(this.ram[this.pc]);

				if (y == 0)
					P.Z = 1;
				else
					P.Z = 0;

				if (y > 127)
					P.N = 1;
				else
					P.N = 0;
				this.pc++;

			} else {
				diassembled.append(String.format("$%04x", this.pc)
						+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " ldy #");
				this.pc++;
				diassembled.append(String.format("$%02x", this.ram[this.pc]) + "\n");
				this.pc++;
			}
			break;

		// ldx #b
		// Z,N Flags
		case 0xa2:
			if (doExecute) {
				this.pc++;
				x = unsignedByte(this.ram[this.pc]);

				if (x == 0)
					P.Z = 1;
				else
					P.Z = 0;

				if (x > 127)
					P.N = 1;
				else
					P.N = 0;
				this.pc++;
			} else {
				diassembled.append(String.format("$%04x", this.pc)
						+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " ldx #");
				this.pc++;
				diassembled.append(String.format("$%02x", this.ram[this.pc]) + "\n");
				this.pc++;
			}
			break;
		
		// dex
		// N,Z
		case 0xca:
			if (doExecute) {
				this.x--;
				if (this.x==-1)
					this.x=255;
				if (x==0)
					this.P.Z=1;
				else
					this.P.Z=0;
				if (this.x>=0 && x<=127)
					this.P.N=0;
				else 
					this.P.N=1;
				this.pc++;
			}else {
				diassembled.append(
						String.format("$%04x", this.pc) + String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " dex\n");
				this.pc++;
			}
			break;
			
			// dey
			// N,Z
			case 0x88:
				if (doExecute) {
					this.y--;
					if (this.y==-1)
						this.y=255;
					if (this.y==0)
						this.P.Z=1;
					else
						this.P.Z=0;
					if (this.y>=0 && this.y<=127)
						this.P.N=0;
					else 
						this.P.N=1;
					this.pc++;
				}else {
					diassembled.append(
							String.format("$%04x", this.pc) + String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " dey\n");
					this.pc++;
				}
				break;
				
			

		// lda #b
		// Z,N Flags
		case 169:
			if (doExecute) {
				this.pc++;
				a = unsignedByte(this.ram[pc]);

				if (a == 0)
					P.Z = 1;
				else
					P.Z = 0;

				if (a > 127)
					P.N = 1;
				else
					P.N = 0;
				this.pc++;

			} else {
				diassembled.append(
						String.format("$%04x", this.pc) + String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " lda #");
				this.pc++;
				diassembled.append(String.format("$%02x", this.ram[this.pc]) + "\n");
				this.pc++;
			}
			break;
			
		// bne
		// No flags...
		case 0xd0:
			if (doExecute) {
				
				
			}else {
				
			}
			
			
			break;
		}
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
