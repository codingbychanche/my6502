import cpu.Cpu_6502;
import cpu.VirtualMachine;

public class Main {

	public static void main(String[] args) {

		VirtualMachine vt = new VirtualMachine(64000);
				
		vt.run(1500);

	}

}
