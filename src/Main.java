import cpu.Cpu_6502;

public class Main {

	public static void main(String[] args) {
		
		Cpu_6502 myCPU=new Cpu_6502();
		byte [] ram=new byte[64000];
		
		ram[0x0600]=(byte) 169;
		ram[0x601]=(byte) 30;
		ram[0x602]=(byte) 0x48;
		ram[0x0603]=(byte) 169;
		ram[0x604]=(byte) 0;
		ram[0x605]=(byte) 0x68;
	
		myCPU.execute(ram,0x600);
		System.out.println(myCPU.getP().printStatus());
		System.out.println(myCPU.dumpStack(ram));
		
		
		System.out.println(myCPU.dissasemble(ram, 0x0600, 0x0610));
		

	}

}
