package Test;

import MandelBrot.MandelBrotBenchmark;
import MatrixMultiplication.MatrixMultiplicationBenchmark;
import RaggedArray.RaggedArrayBenchmark;
import Util.Benchmark;
import VectorAddition.VectorAddBenchmark;

public class Main {

	public static void runBenchmark(Benchmark benchmark, int iterations, int[] sizes) {
		System.out.println("Benchmark: " + benchmark.getName());
		System.out.println("Iterations: " + iterations);
		for(Integer size : sizes) {
			System.out.println("Input Size: " + size);
			
			
			System.out.println("Platform: Sequential");
			for(int i = 0; i < iterations; i++) {
				benchmark.runSeq(size);
			}
			
			System.out.println("Platform: Pre-Programmed GPU");
			for(int i = 0; i < iterations; i++) {
				benchmark.runHandWritten(size);
			}
			
			System.out.println("Platform: OpenCL CPU");
			for(int i = 0; i < iterations; i++) {
				benchmark.runOnCPU(size);
			}
			
			System.out.println("Platform: OpenCL GPU");
			for(int i = 0; i < iterations; i++) {
				benchmark.runOnGPU(size);
			}
		}
	}
	
	public static void testVecAdd() {
		final int size = 1000;
		Benchmark vectorAddBench = new VectorAddBenchmark();
		vectorAddBench.runOnGPU(size);
	}
	
	public static void testMatMul() {
		final int size = 500;
		Benchmark matMulBench = new MatrixMultiplicationBenchmark();
		matMulBench.runOnGPU(size);
	}
	
	public static void testRagArray() {
		final int size = 1000000;
		Benchmark ragOpBench = new RaggedArrayBenchmark();
		ragOpBench.runOnGPU(size);
	}
	
	public static void testMandelbrot() {
		final int size = 3000;
		Benchmark mandBench = new MandelBrotBenchmark();
		mandBench.runOnGPU(size);
	}
	
	public static void main(String[] args) {
		testMandelbrot();		
	}
}
