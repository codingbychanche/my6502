package cpu;

/**
 * Model for a virtual machine.
 * 
 * Executes code via an attched cpu.
 * 
 * @author Berthold
 *
 */
public class VirtualMachine implements VirtualMachineReceiver {

	private byte[] ram;
	private Cpu_6502 cpu;

	/**
	 * Creates a new virtual machine.
	 * 
	 * @param cpu     Cpu emulator.
	 * @param ramSize Soze of ram.
	 */
	public VirtualMachine(int ramSize) {
		cpu = new Cpu_6502(this);
		ram = new byte[ramSize];
	}

	/**
	 * Executor....
	 * 
	 */
	public void run() {

		Thread t = new Thread(new Runnable() {

			public void run() {
				ram[0x0600] = (byte) 0xa2;
				ram[0x0601] = (byte) 3;
				ram[0x0602] = (byte) 0xca;
				ram[0x0603] = (byte) 0xd0;
				ram[0x0604] = (byte) 0xfd;

				cpu.execute(ram, 0x600);

			}
			
		});
		t.start();

		// System.out.println(cpu.getP().printStatus());
		// System.out.println(cpu.dumpStack(ram));

		// System.out.println(cpu.dissasemble(ram, 0x0600, 0x0610));

	}
	
	/**
	 * Receieves status of the currently executed instrucrion from the cpu...
	 * 
	 */

	@Override
	public void getProcessorState(String s) {
		System.out.println(s);
	}

}
