import cpu.Cpu_6502;
import cpu.VirtualMachine;

public class Main {

	public static void main(String[] args) {

		VirtualMachine vt = new VirtualMachine(65536);
				
		vt.run(1000);

	}

}
