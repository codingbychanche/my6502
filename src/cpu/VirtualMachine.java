package cpu;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/**
 * Model for a virtual machine.
 * 
 * Executes code via an attached cpu.
 * 
 * @author Berthold
 *
 */
public class VirtualMachine implements VirtualMachineReceiver {

	private static final boolean DEBUG = false;

	private byte[] ram;
	private Cpu_6502 cpu;
	private long clockSpeed;

	//
	//
	private static final int CRLF = 0xff9b;

	// Some OS- Routines to be emulated
	//
	private static final int PRINT = 4000; // Text to console.....
	private static final int INPUT = 8000; // Text from console

	/**
	 * Creates a new virtual machine.
	 * 
	 * @param cpu     Cpu emulator.
	 * @param ramSize Soze of ram.
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public VirtualMachine(int ramSize, String pathOfBiosFile) throws FileNotFoundException, IOException {

		System.out.println("VM INITIALIZES.....");

		cpu = new Cpu_6502(this);
		ram = new byte[ramSize];

		//
		// This part reads an Atari Dos 2.x binary file into memory
		//

		File file = new File(pathOfBiosFile);
		byte[] bytes = new byte[(int) file.length()];

		System.out.println("READING BIOS ROM FROM:" + pathOfBiosFile);
		try (FileInputStream fis = new FileInputStream(file)) {

			int b;
			int junksRead = 0;

			// 2 time $ff
			b = fis.read();
			b = fis.read();

			while (b != -1) {

				// Junks
				junksRead++;
				System.out.println("Reading junk #" + junksRead);

				// Start address
				int bl = fis.read();
				int bh = fis.read();
				int start = bl + 256 * bh;
				System.out.println("Start:" + start);

				// (end-start)+1 address
				bl = fis.read();
				bh = fis.read();
				int end = (bl + 256 * bh) + 1;
				int size = end - start;
				System.out.println("Length:" + size);
				System.out.println("Next free byte:" + end);

				//
				// Read # of bytes and write to asociated address
				// into ram.
				//
				System.out.println("Reading:");
				for (int n = 0; n <= size - 1; n++) {
					b = fis.read();
					System.out.print(String.format("%02x", b) + ",");
					if (start < ram.length && start > 0)
						ram[start + n] = (byte) b;
				}
				System.out.println();
				System.out.println("---------------------------------------------------------------------");
			}
		}

		//
		// IRQ vector is set manually, because 'ATASM' seems not to be able
		// to compile addresses $FFFE- $FFFF
		//
		this.ram[cpu.IRQ_VECTOR] = 0;
		this.ram[cpu.IRQ_VECTOR + 1] = 0;
	}

	/**
	 * Executor....
	 * 
	 */
	public void run(long clockSpeed) {
		this.clockSpeed = clockSpeed;

		System.out.println("STARTING VM (" + cpu.cpuTypeLiteral + ")");

		Thread t = new Thread(new Runnable() {

			public void run() {

				cpu.execute(ram, clockSpeed);

				System.out.println(dumpRam(2000, 2020));
			}

		});
		t.start();

		// FROM HERE IMPLEMENT THE LOOP FOR THE VIRTUAL MACHINE WHICH
		// CONSTITUTES THE INTERFACE BETWEEN THE REAL HARDWARE AND THE
		// VM AND TO THE ATTACHED PROCESSOR....
		//
		// FOR EXAMPLE: WE COULD IMPLEMENT KEYBOARD POLLING AND
		// WAIT FOR AN 'OF' KEY WHICH ENDED THE EMULATION OR AN
		// INTERRUPT NOTIFING THE PROCESSOR.....

	}

	/**
	 * Dumps the ram content.
	 * 
	 * @param start Start address.
	 * @param end   End address.
	 * @return String containing a hex- listing of the ram contents.
	 */
	private String dumpRam(int start, int end) {
		StringBuilder ramListing = new StringBuilder();

		int address = start;

		while (address <= end) {
			ramListing.append(String.format("%04x ", address));
			for (int i = 0; i <= 7; i++) {
				int b = unsignedByte(this.ram[address]);
				ramListing.append(String.format("%02x ", b));
				address++;
			}
			ramListing.append("\n");
		}

		return ramListing.toString();
	}

	public int unsignedByte(byte b) {
		return b & 0xff;
	}

	/**
	 * Receieves processor status
	 * 
	 */

	@Override
	public void getProcessorState(String s) {
		if (DEBUG)
			System.out.println(s);
	}

	/**
	 * Receives the opcode and the human readable instruction of the last
	 * instruction executed.
	 */
	@Override
	public void getComandExecuted(String s) {
		if (DEBUG)
			System.out.println(s);
	}

	/**
	 * JMP + JSR are trapped here
	 * 
	 * Contains a collection of emulated hardware components og the emnulated
	 * machine.
	 * 
	 */
	@Override
	public void jmpAddressTrap(int a) {
		int low, high, address;
		int i;
		char c;
		
		if (DEBUG)
			System.out.println("====>>> jmp trapped =>" + a);

		switch (a) {

		// Writes a string stored in ram to the console
		// x=low
		// y=high
		// jsr PRINT
		case PRINT:
			low = this.cpu.getX();
			high = this.cpu.getY();
			address = low + 256 * high;

			i = 0;

			while ((c = (char) this.ram[address + i++]) != CRLF)
				System.out.print(c);
			System.out.println();
			break;

		// Waits for user input and stores the
		// received string in ram
		// x=low
		// y=high
		// jsr input
		case INPUT:

			low = this.cpu.getX();
			high = this.cpu.getY();
			address = low + 256 * high;

			Scanner s = new Scanner(System.in);
			String input = s.nextLine();

			for (i = 0; i <= input.length() - 1; i++)
				this.ram[address + i] = (byte) input.charAt(i);
			this.ram[address + i] = (byte) CRLF;

			break;
		}
	}
}
