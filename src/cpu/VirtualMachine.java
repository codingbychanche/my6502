package cpu;

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
	 */
	public VirtualMachine(int ramSize) {
		cpu = new Cpu_6502(this);
		ram = new byte[ramSize];

		initRam();
	}

	/**
	 * Executor....
	 * 
	 */
	public void run(long clockSpeed) {
		this.clockSpeed = clockSpeed;

		Thread t = new Thread(new Runnable() {

			public void run() {
				/*
				ram[1536] = (byte) 0xa2;
				ram[1537] = (byte) 0x03;
				ram[1538] = (byte) 0xa9;
				ram[1539] = (byte) 0xfe;
				ram[1540] = (byte) 0x8d;
				ram[1541] = (byte) 0xa4;
				ram[1542] = (byte) 0x06;
				ram[1543] = (byte) 0x8d;
				
				ram[1544] = (byte) 0xa5;
				ram[1545] = (byte) 0x06;
				ram[1546] = (byte) 0x18;
				ram[1547] = (byte) 0xad;
				ram[1548] = (byte) 0xa4;
				ram[1549] = (byte) 0x06;
				ram[1550] = (byte) 0x69;
				ram[1551] = (byte) 0x01;
				
				ram[1552] = (byte) 0x8d;
				ram[1553] = (byte) 0xa4;
				ram[1554] = (byte) 0x06;
				ram[1555] = (byte) 0xad;
				ram[1556] = (byte) 0xa5;
				ram[1557] = (byte) 0x06;
				ram[1558] = (byte) 0x69;
				ram[1559] = (byte) 0x00;
				
				ram[1560] = (byte) 0x8d;
				ram[1561] = (byte) 0xa5;
				ram[1562] = (byte) 0x06;
				ram[1563] = (byte) 0xca;
				ram[1564] = (byte) 0xd0;
				ram[1565] = (byte) 0xec;
				ram[1566] = (byte) 0xae;
				ram[1567] = (byte) 0xa4;
				ram[1568] = (byte) 0x06;		
				ram[1569] = (byte) 0xac;
				ram[1570]=(byte) 0xa5;
				ram[1571]=(byte)0x06;
				*/
				
				ram[1536] = (byte) 0xa9;
				ram[1537] = (byte) 0x14;
				ram[1538] = (byte) 0x8d;
				ram[1539] = (byte) 0xa4;
				ram[1540] = (byte) 0x06;
				ram[1541] = (byte) 0xd0;
				ram[1542] = (byte) 0x01;
				ram[1543] = (byte) 0x00;
				ram[1544] = (byte) 0xa9;		
				ram[1545] = (byte) 0x01;
				ram [1546]=(byte) 0;
				cpu.execute(ram, 0x600, clockSpeed);

				System.out.println(dumpRam(1700, 1710));
				System.out.println(dumpRam(1536, 1565));
			}

		});
		t.start();

	}

	/**
	 * Inits the ram of our virtual machine
	 * 
	 */
	private void initRam() {

		this.ram[cpu.IRQ_VECTOR] = 6;
		this.ram[cpu.IRQ_VECTOR + 1] = 0;
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

		int address=start;
		
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
