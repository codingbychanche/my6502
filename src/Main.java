import java.io.FileNotFoundException;
import java.io.IOException;

import cpu.Cpu_6502;
import cpu.VirtualMachine;

public class Main {

	public static void main(String[] args) throws FileNotFoundException, IOException {

		
		if (args.length > 0) {
			String biosPath=args[0];	
			
			// init VM
			VirtualMachine vt = new VirtualMachine(65536, biosPath);
			// Start thread. Tell it, how much millisec pause between each instruction
			// executed.
			vt.run(250);

		} else
			System.out.println("No Bios file.....Abording.");

	}

}
