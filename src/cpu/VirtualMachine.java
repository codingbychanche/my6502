package cpu;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Model for a virtual machine.
 * 
 * Executes code via an attached cpu.
 * 
 * @author Berthold
 *
 */
public class VirtualMachine implements VirtualMachineReceiver {

	private byte[] ram;
	private Cpu_6502 cpu;
	private long clockSpeed;

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

		
		System.out.println("READING BIOS ROM FROM:"+pathOfBiosFile);
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
					if (start < ram.length && start>0)
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
		this.ram[cpu.IRQ_VECTOR]=0;
		this.ram[cpu.IRQ_VECTOR+1]=0;
	}

	/**
	 * Executor....
	 * 
	 */
	public void run(long clockSpeed) {
		this.clockSpeed = clockSpeed;
		
		System.out.println("STARTING VM ("+cpu.cpuTypeLiteral+")");

		Thread t = new Thread(new Runnable() {

			public void run() {

				// FOR THE TIME BEEING WE HAVE A FIXED START ADDRESS
				// IN THE FUTURE THE CORRECT START UP SEQUENCE OF THE ASSOCIATED CPU
				// WILL BE EMEULATED AND DONE FROM WITHIN THE ASSOCIATED BIOS FILE...
				cpu.execute(ram, clockSpeed);

				System.out.println(dumpRam(3000, 3001));
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
		
		for (int i=0;i==0;);

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
		System.out.println(s);

	}

	/**
	 * Receives the opcode and the human readable instruction of the last
	 * instruction executed.
	 */

	@Override
	public void getComandExecuted(String s) {
		System.out.println(s);
	}

	@Override
	public void jmpAddressTrap(int a) {
		System.out.println("====>>> jmp trapped =>" + a);

	}

	/**
	 * This method checks wether an address passed belongs an emulated subroutne or
	 * not. If a matching address could be found the subroutine is executed...
	 * 
	 * 
	 * @param address
	 */
	private void virtualMachineEmulatedOSRoutines(int address) {

	}
}
