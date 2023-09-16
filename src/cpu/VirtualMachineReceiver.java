package cpu;

public interface VirtualMachineReceiver {
	
	public void getProcessorState (String s);
	
	public void getComandExecuted(String s);
	
	/**
	 * If a jmp. instruction wasexecuted, this method receives
	 * the target address.
	 * 
	 * @param a Target address of the jump instruction.
	 */
	public void jmpAddressTrap (int a);
}
