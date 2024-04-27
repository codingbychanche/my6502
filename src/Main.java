import cpu.Cpu_6502;
import cpu.VirtualMachine;

public class Main {

	public static void main(String[] args) {

		// Tell it, how much memory it will have.....
		VirtualMachine vt = new VirtualMachine(65536);
				
		// Start thread. Tell it, how much millisec pause between each instruction
		// executed.
		vt.run(250);

	}

}
