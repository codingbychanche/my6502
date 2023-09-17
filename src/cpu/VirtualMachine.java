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
	}

	/**
	 * Executor....
	 * 
	 */
	public void run(long clockSpeed) {
		this.clockSpeed = clockSpeed;

		Thread t = new Thread(new Runnable() {

			public void run() {
				ram[0x0600] = (byte) 0x18;
				ram[0x0601] = (byte) 0xa9;
				ram[0x0602] = (byte) 250;
				ram[0x0603] = (byte) 0x69;
				ram[0x0604] = (byte) 0x01;
				ram[0x0605]=(byte) 0xd0;
				ram[0x0606]=(byte) 0xfc;
				ram[0x0607]=(byte) 0x00;
				ram [0x0608]=(byte) 0x00;
							
				cpu.execute(ram, 0x600,clockSpeed);

			}

		});
		t.start();

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
	 * Receives the opcode and the human readable instruction
	 * of the last instruction executed.
	 */
	
	@Override
	public void getComandExecuted (String s){
		System.out.println(s);
	}
	
	@Override
	public void jmpAddressTrap(int a) {
		System.out.println("====>>> jmp trapped =>"+a);
		
	}

	/**
	 * This method checks wether an address passed belongs an 
	 * emulated subroutne or not. If a matching address could be 
	 * found the subroutine is executed...
	 * 
	 * 
	 * @param address
	 */
	private void virtualMachineEmulatedOSRoutines(int address){
		
	}
}
