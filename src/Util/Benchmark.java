package Util;

public interface Benchmark {
	public void runOnGPU(int arrayLen);
	public void runOnCPU(int arrayLen);
	public void runSeq(int arrayLen);
	public void runHandWritten(int arrayLen);
	public String getName();
}
