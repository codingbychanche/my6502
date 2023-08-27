import cpu.Cpu_6502;

public class Main {

	public static void main(String[] args) {
		
		Cpu_6502 myCPU=new Cpu_6502();
		byte [] ram=new byte[64000];
		
		ram[0x0600]=(byte) 169;
		ram[0x601]=(byte) 255;
		ram[0x602]=(byte) 0x48;
		ram[0x603]=(byte) 0xa2;
		ram[0x604]=(byte) 34;
		ram[0x605]=(byte) 0xa0;
		ram[0x606]=(byte) 11;
		ram [0x607]=(byte)0x38;
		ram [0x610]=(byte)0;
		
				
				
		myCPU.execute(ram, 0x600,true,20);
		
		System.out.println(myCPU.getP().printStatus());
		System.out.println(myCPU.dumpStack(ram));
		
		myCPU.execute(ram, 0x600,false,20);
	

	}

}
