import cpu.Cpu_6502;

public class Main {

	public static void main(String[] args) {
		
		Cpu_6502 myCPU=new Cpu_6502();
		byte [] ram=new byte[64000];
		
		ram[0x0600]=(byte) 0xa2;
		ram[0x0601]=(byte) 1;
		ram[0x0602]=(byte) 0xca;
		ram[0x0603]=(byte) 0xca;
		ram[0x0604]=(byte) 0xa0;
		ram[0x0605]=(byte) 1;
		ram[0x0606]=(byte) 0x88;
		ram[0x0607]=(byte) 0x88;

	
		myCPU.execute(ram,0x600);
		System.out.println(myCPU.getP().printStatus());
		System.out.println(myCPU.dumpStack(ram));
		
		
		System.out.println(myCPU.dissasemble(ram, 0x0600, 0x0610));
		

	}

}
