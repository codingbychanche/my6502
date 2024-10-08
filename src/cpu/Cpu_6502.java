package cpu;

import java.util.Scanner;

/**
 * Emulates the infamous 8- Bit CPU
 * 
 * @author Berthold
 *
 */
public class Cpu_6502 {
	public static final String cpuTypeLiteral = "MOS 6502";

	private Cpu_6502 cpu;
	private long clockSpeed;

	private static final int LOW = 0;
	private static final int HIGH = 1;
	private static final String MESSAGE_ID = "[6502_STATE]";

	//
	// 6502 address space
	//
	private static final int START_ADDRESS_OF_STACK = 0x01ff;
	private static final int END_ADDRESS_OF_STACK = 0x0100;

	//
	// RAM
	//
	// Just remeber! When a byte from ram is to be taken, use the 'unsignedByte'
	// method!
	//
	private byte[] ram;

	//
	// NMI
	// Non maskable interrupts
	//
	// ToDo: Not impelemnted yet...
	//
	public static int NMI_VECTOR = 0xfffa;

	//
	// Reset vector and reset line
	//
	public int RESET_LINE;
	public static int RESET_VECTOR = 0xfffc;

	// IRQ
	// Init Vectors for maskable interupt requests of our processor.
	// IRQ= Maskable Interrupt requests.
	//
	// Maskable Interrupts can be ignored.
	//
	// The handler routine is called whenever an IRQ occured.
	// When an interrupt occurs, the pc and status register are pushed on the stack
	// and
	// the program counter is read from $FFFE (PCL) and $FFFF (PCH).

	public static int IRQ_VECTOR = 0xfffe; // IRQ- handler routine. Decides what caused the irq...

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
	boolean runEmulation = true;

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
					+ " // A=" + a + " X=" + x + " Y=" + y + String.format(" // Status=$%02x", getStatusRegister())
					+ " // " + "RST=" + RESET_LINE);
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

		// Once this is set to HIGH the processor set the programm counter
		// to the address stored at the reset vector.
		// The virtual machine is in charge of deciding when this can be done
		// e.g. when all hardware componets are ready...)

		this.RESET_LINE = LOW;
	}

	/**
	 * Executes a series of commands, stored from start address upwards in specified
	 * ram.
	 * 
	 * TODO: AFTER START THE PROCESSOR IS IN AN UNDEFINED STATE. WHEN THE RESET LINE
	 * IS SET TO HIGH STATE THE PROGRAM COUNTER IS SET TO THE ADDRESS STORED IN THE
	 * RESET VECTOR. THE VIRTUAL MACHINE IS IN CHARGE OF THE RESET LINE AND HAS TO
	 * DECIDE WHEN CONTROL IS GIVEN TO THE PROCESSOR AFTER RESTART OR A S SYSTEM
	 * RESET.
	 * 
	 * THE BEHAVIOUR ABOVE HAS YET TO BE IMPLEMENTED => SET/B RESET RESET LINE
	 * 
	 * @param ram        Asscoiated ram.
	 * @param address    The address from where to run....
	 * @param clockSpeed The speed which each cycle takes in [ms]
	 * @return The resulting ram contents.
	 */
	public byte[] execute(byte[] ram, long clockSpeed) {

		this.ram = ram;

		// AS LONG AS THE RESET LINE IS NOT SET TO HIGH
		// THE PROCESSOR IS IN AN UNDEFINED STATE. WHEN RESET LINE IS SET TO HIGH
		// THE PROCESSOR SETS THE PC TO THE ADDRESS STORED AT THE RESET VECTOR.
		// FOR THE TIME BEEING THE RESET LINE IS NOT CHECKED AND WE ASUME
		// IT IS HIGH!

		int start = this.ram[this.RESET_VECTOR] + this.ram[this.RESET_VECTOR + 1] * 256;
		this.pc = start;
		this.vt.getProcessorState("Got reset Vector:" + String.format("$%04x", this.RESET_VECTOR) + " PC set to:"
				+ String.format("$%04x", start));

		this.diassembled = new StringBuilder(); // FIND ANOTHER WAY THAN USING THIS GLOBAL VAR....

		//
		// Parse
		//
		while (runEmulation) {
			int command = unsignedByte(this.ram[pc]);
			parser(command);
			vt.getProcessorState(this.P.printStatus());

			//
			// Wait to emulate the speed of the cpu...
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
		int c, diff;
		int b, target;

		switch (command) {

		// brk
		// This works according to =>
		// Chapter 9.11:
		// https://archive.org/details/mos_microcomputers_programming_manual/page/n163/mode/2up
		//
		// push return address +2
		// push status flag
		//
		// This is a software interrupt. In the status register the B- flag is always
		// set and is never affected by the brk- instruction. To check if a soft- or
		// hardware
		// interrupt took place, one has the check if the P- register on the stack.
		// If the B- flag is set => Software intterupt. If not => hardware interrupt.
		case 0x00:

			// This will become obsolete once the proper routine for handling
			// IRQ interrupts is implemented with the virtual machine.
			// For the time beeing this will take care that the emulation is
			// not caught in an infite loop once a 'brk' instruction was
			// executed.
			//
			// Observe the pc after the emulation stops, it will point to the
			// address stored at the IRQ vector of the virtual machines ram....
			this.runEmulation = false;

			// Push pc+2 high, low byte on the stack. Finaly the status register.
			this.pc = this.pc + 2;
			this.s++;
			this.ram[START_ADDRESS_OF_STACK - s] = (byte) this.high(this.pc);
			this.s++;
			this.ram[START_ADDRESS_OF_STACK - s] = (byte) this.low(this.pc);
			this.s++;
			this.ram[START_ADDRESS_OF_STACK - s] = (byte) (P.getStatusRegister());

			this.vt.getComandExecuted(String.format("$%04x", this.pc)
					+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " brk // " + this.dumpStack(ram));

			// Get address from irq- vector in ram and set pc accordimngly....
			// FFFE pcl FFFF pch
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
			// this.ram[address] = (byte) this.a;
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

		// tay
		// n,z
		case 0xa8:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " tay ";

			this.y = this.a;

			this.vt.getComandExecuted(com);

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

		// tya
		// n,z
		case 0x98:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " tya ";

			this.a = this.y;

			this.vt.getComandExecuted(com);

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
			// this.ram[address] = (byte) this.a;
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

		// tax
		// n,z
		case 0xaa:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " tax ";

			this.x = this.a;

			this.vt.getComandExecuted(com);

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

		// txa
		// z,n
		case 0x8a:
			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " txa ";

			this.a = this.x;

			this.vt.getComandExecuted(com);

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

		// inx
		// N,Z
		case 0xe8:

			this.x++;
			if (this.x > 255)
				this.x = 0;
			if (x == 0)
				this.P.Z = 1;
			else
				this.P.Z = 0;
			if (this.x >= 0 && x <= 127)
				this.P.N = 0;
			else
				this.P.N = 1;

			this.vt.getComandExecuted(String.format("$%04x", this.pc)
					+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " inx");
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

		// iny
		// N,Z
		case 0xc8:

			this.y++;
			if (this.y > 255)
				this.y = 0;
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
					+ String.format(" $%02x", unsignedByte(this.ram[this.pc])) + " iny");
			break;

		// inc aaaa
		// n, z
		case 0xee:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " inc ";

			this.pc++;
			low = unsignedByte(this.ram[pc]);
			this.pc++;
			high = unsignedByte(this.ram[pc]);
			address = low + 256 * high;

			this.vt.getComandExecuted(com + String.format("$%04x", address));
			b = unsignedByte(ram[address]);
			b++;

			if (b > 255)
				b = 0;

			this.ram[address] = (byte) b;

			if (b == 0)
				P.Z = 1;
			else
				P.Z = 0;

			if (b > 127)
				P.N = 1;
			else
				P.N = 0;

			this.pc++;

			break;

		// dec aaaa
		// n, z
		case 0xce:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " dec ";

			this.pc++;
			low = unsignedByte(this.ram[pc]);
			this.pc++;
			high = unsignedByte(this.ram[pc]);
			address = low + 256 * high;

			this.vt.getComandExecuted(com + String.format("$%04x", address));
			b = unsignedByte(ram[address]);
			b--;

			if (b < 0)
				b = 255;

			this.ram[address] = (byte) b;

			if (b == 0)
				P.Z = 1;
			else
				P.Z = 0;

			if (b > 127)
				P.N = 1;
			else
				P.N = 0;

			this.pc++;

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
			// this.ram[address] = (byte) this.a;
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

		// lda b Zero Page
		// Z,N Flags
		case 0xa5:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " lda ";

			this.pc++;

			address = unsignedByte(this.ram[this.pc]);
			this.a = unsignedByte(this.ram[address]);

			this.vt.getComandExecuted(com + String.format("$%02x", address));

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

		// lda (lsb),y indirect y post indexed
		// loads accu with the contents of (lsb)+(lsb+1)*256
		// Z,N Flags
		case 0xb1:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " lda (";

			this.pc++;
			address = unsignedByte(this.ram[pc]);
			this.vt.getComandExecuted(com + String.format("$%02x", address) + "),y");

			low = unsignedByte(this.ram[address]);
			address++;
			high = unsignedByte(this.ram[address]);

			address = low + 256 * high;
			address = address + this.y;

			this.a = unsignedByte(this.ram[address]);

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

		// lda a,x absolut x
		// N,Z
		case 0xbd:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " lda ";

			this.pc++;
			low = unsignedByte(this.ram[pc]);
			this.pc++;
			high = unsignedByte(this.ram[pc]);
			address = low + 256 * high;
			// this.ram[address+this.x] = (byte) this.a;
			this.a = unsignedByte(this.ram[address + this.x]);

			this.vt.getComandExecuted(com + String.format("$%04x", address) + ",x");

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

		// lda a,y absolut y
		// N,Z
		case 0xb9:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " lda ";

			this.pc++;
			high = unsignedByte(this.ram[pc]);
			this.pc++;
			low = unsignedByte(this.ram[pc]);
			address = low + 256 * high;
			// this.ram[address+this.x] = (byte) this.a;
			this.a = unsignedByte(this.ram[address + this.y]);

			this.vt.getComandExecuted(com + String.format("$%04x", address) + ",y");

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

		// sta xx Absolute zero page
		//
		case 0x85:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " sta ";
			this.pc++;
			address = unsignedByte(this.ram[pc]);
			this.ram[address] = (byte) this.a;

			this.vt.getComandExecuted(com + String.format("$%02x", address));

			this.pc++;

			break;

		// cmp #b
		// c,z,n
		case 0xc9:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " cmp #";
			this.pc++;
			c = unsignedByte(this.ram[pc]);

			this.vt.getComandExecuted(com + String.format("$%02x", c));

			diff = a - c;

			if (diff == 0) {
				this.P.C = 1;
				this.P.Z = 1;
				this.P.N = 0;
			}

			if (diff > 0) {
				this.P.C = 1;
				this.P.Z = 0;
				this.P.N = 0;
			}

			if (diff < 0) {
				this.P.C = 0;
				this.P.Z = 0;
				this.P.N = 1;
			}

			this.pc++;

			break;

		// cpx #b
		// c,z,n
		case 0xe0:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " cpx #";
			this.pc++;
			c = unsignedByte(this.ram[pc]);

			this.vt.getComandExecuted(com + String.format("$%02x", c));

			diff = this.x - c;

			if (diff == 0) {
				this.P.C = 1;
				this.P.Z = 1;
				this.P.N = 0;
			}

			if (diff >0) {
				this.P.C = 1;
				this.P.Z = 0;
				this.P.N = 0;
			}

			if (diff < 0) {
				this.P.C = 0;
				this.P.Z = 0;
				this.P.N = 1;
			}

			this.pc++;

			break;

		// cpy #b
		// c,z,n
		case 0xc0:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " cmp #";
			this.pc++;
			c = unsignedByte(this.ram[pc]);

			this.vt.getComandExecuted(com + String.format("$%02x", c));

			diff = this.y - c;

			if (diff == 0) {
				this.P.C = 1;
				this.P.Z = 1;
				this.P.N = 0;
			}

			if (diff > 0) {
				this.P.C = 1;
				this.P.Z = 0;
				this.P.N = 0;
			}

			if (diff < 0) {
				this.P.C = 0;
				this.P.Z = 0;
				this.P.N = 1;
			}

			this.pc++;

			break;

		// adc #
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

			this.pc++;
			
			break;
			
		// sbc #nn
		// Carry, overvlow, negative and zero flag
		case 0xe9:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " sbc #";
			this.pc++;
			this.vt.getComandExecuted(com + String.format("$%02x", this.ram[pc]));

			// BCD?
			if (this.P.D == 1) {
				// TODO Insert adc BCD- mode...

			} else {
				// Non BCD
				// Sub with carry
				// TODO Debug Overflow flag.....

				this.a = this.a - unsignedByte(this.ram[pc]);

				if (this.a == 0) {
					this.P.Z = 1;
					this.P.N = 0;
				} else
					this.P.Z = 0;

				if (this.a < 0) {
					this.a = 255 + (this.a + this.P.C);
					this.P.C = 0;
					this.P.Z = 1;
					this.P.N = 0;

				}

			}

			this.pc++;

			break;

		// sbc nnnn
		// Carry, overvlow, negative and zero flag
		case 0xed:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " sbc ";
			this.pc++;
			low = unsignedByte(this.ram[pc]);
			this.pc++;
			high = unsignedByte(this.ram[pc]);
			address = low + 256 * high;
			this.vt.getComandExecuted(com + String.format("$%04x", address));

			// BCD?
			if (this.P.D == 1) {
				// TODO Insert adc BCD- mode...

			} else {
				// Non BCD
				// Sub with carry
				// TODO Debug Overflow flag.....

				this.a = this.a - unsignedByte(this.ram[address]);

				if (this.a == 0) {
					this.P.Z = 1;
					this.P.N = 0;
				} else
					this.P.Z = 0;

				if (this.a < 0) {
					this.a = 255 + (this.a + this.P.C);
					this.P.C = 0;
					this.P.Z = 1;
					this.P.N = 0;

				}

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
			b = this.ram[pc];

			// Currently pc points to the argument, not the instruction
			// so we have to correct that.
			b++;

			target = this.pc + b;

			// What ever the result of the previous instruction was, if it was zero
			// we do not branch!
			if (this.P.Z == 0) {

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

			// bcc
			// Branch on carry clear (or, branch on 'less then' when used with cmp
			// No flags...
			//
			// TODO: Check forward branches....
			case 0x90:

				com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " bcc ";

				// pc already points to the current instruction. Now get the argument.
				this.pc++;
				b = this.ram[pc];

				// Currently pc points to the argument, not the instruction
				// so we have to correct that.
				b++;

				target = this.pc + b;

				// What ever the result of the previous instruction was, if it was zero
				// we do not branch!
				if (this.P.C== 0) {

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

				// bcs
				// Branch on carry clear (or, branch on 'less then' when used with cmp
				// No flags...
				//
				// TODO: Check forward branches....
				case 0xb0:

					com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " bcs ";

					// pc already points to the current instruction. Now get the argument.
					this.pc++;
					b = this.ram[pc];

					// Currently pc points to the argument, not the instruction
					// so we have to correct that.
					b++;

					target = this.pc + b;

					// What ever the result of the previous instruction was, if it was zero
					// we do not branch!
					if (this.P.C== 1) {

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
			
			// bpl
			// Branch on result not negative.
			// No flags...
			//
			// TODO: Check forward branches....
			case 0x10:

				com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " bpl ";

				// pc already points to the current instruction. Now get the argument.
				this.pc++;
				b = this.ram[pc];

				// Currently pc points to the argument, not the instruction
				// so we have to correct that.
				b++;

				target = this.pc + b;

				// What ever the result of the previous instruction was, if negative
				// we do not branch!
				if (this.P.N == 0) {

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
				
		// beq
		// Branch on equal.
		// No flags...
		//
		case 0xf0:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " beq ";

			// pc already points to the current instruction. Now get the argument.
			this.pc++;
			b = this.ram[pc];

			// Currently pc points to the argument, not the instruction
			// so we have to correct that.
			b++;

			target = this.pc + b;

			// Branch if the zero flag is set to 1 (cmp => z=1 if equal...)
			// we do not branch!
			if (this.P.Z == 1) {

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
			low = unsignedByte(this.ram[pc]);
			this.pc++;
			high = unsignedByte(this.ram[pc]);
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

			vt.jmpAddressTrap(address);

			break;

		// rts
		//
		case 0x60:

			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " rts ";

			// pull return address from stack
			//
			low = unsignedByte(this.ram[START_ADDRESS_OF_STACK - s]);
			this.s--;
			high = unsignedByte(this.ram[START_ADDRESS_OF_STACK - s]);
			this.s--;

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
			low = unsignedByte(this.ram[pc]);
			this.pc++;
			high = unsignedByte(this.ram[pc]);
			address = low + 256 * high;
			this.vt.getComandExecuted(com + String.format("$%04x", address));

			this.pc = address;

			// Inform virtual machine.....
			vt.jmpAddressTrap(address);

			break;

		// lsr
		// Shifts all bits of the accu to the righ. The rightmost bit
		// is deleted and the carry set.
		//
		// N,Z,C
		case 0x4a:
			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " lsr ";

			if ((this.a & 1) == 1) // lsb is shifted into the carry
				this.P.C = 1;
			else
				this.P.C = 0;

			this.a = this.a >> 1;

			this.P.N = 0; // Result of a lsr is always positive

			if (this.a == 0)
				this.P.Z = 1;
			else
				this.P.Z = 0;

			this.vt.getComandExecuted(com);
			this.pc++;

			break;

		// asl
		// Shifts all bits of the accu to the righ. The rightmost bit
		// is deleted and the carry set.
		//
		// N,Z,C
		case 0x0a:
			com = String.format(String.format("$%04x", this.pc) + " $%02x", unsignedByte(this.ram[this.pc])) + " asl ";

			if ((this.a & 0xff) == 1) // lsb is shifted into the carry
				this.P.C = 1;
			else
				this.P.C = 0;

			this.a = this.a << 1;

			this.P.N = 0; // Result of a lsr is always positive

			if (this.a == 0)
				this.P.Z = 1;
			else
				this.P.Z = 0;

			this.vt.getComandExecuted(com);
			this.pc++;

			break;
		}
	}

	/**
	 * High byte part of an 16- bit integer
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

	public byte[] getRam() {
		return this.ram;
	}

	/**
	 * Sets the status of the reset line
	 * 
	 * @param resetLineState
	 */
	public void setResetLine(int resetLineState) {
		this.RESET_LINE = resetLineState;
	}
}
