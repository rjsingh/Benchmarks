package RaggedArray;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.edinburgh.parallel.opencl.ParallelOptions;
import com.edinburgh.parallel.opencl.ParallelUtil;
import com.oracle.graal.api.runtime.Graal;
import com.oracle.graal.nodes.StructuredGraph;
import com.oracle.graal.nodes.spi.GraalCodeCacheProvider;

import MatrixMultiplication.MatrixMultiplicationBenchmark;
import Util.Benchmark;

public class RaggedArrayBenchmark implements Benchmark{

	private final int[] dims = new int[] {20, 50, 100};
	
	public void simpleRagOperation(float[][] data) {
		for(int i = 0; i < data.length; i++) {
			for(int j = 0; j < data[i].length; j++) {
				data[i][j] = 1f;
			}
		}
	}

	@Override
	public void runOnGPU(int arrayLen) {
		GraalCodeCacheProvider runtime = Graal.getRequiredCapability(GraalCodeCacheProvider.class);
		ParallelOptions.EnableComments.setValue(false);
		ParallelOptions.PrintCode.setValue(true);
		ParallelOptions.Execute.setValue(false);
		ParallelOptions.UseCPU.setValue(false);

		Method m = null;
		try {
			m = RaggedArrayBenchmark.class.getDeclaredMethod("simpleRagOperation", float[][].class);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		StructuredGraph sg = new StructuredGraph(runtime.lookupJavaMethod(m));

		float[][] a = new float[arrayLen][];
		
		for(int i = 0; i < a.length; i++) {
			a[i] = new float[dims[i%dims.length]];
		}
		
		ParallelOptions.WorkSize.setValue(arrayLen);
		Object[] params = new Object[1];
		params[0] = a;
		
		long startTime = System.nanoTime();
		ParallelUtil.run(sg, params);
		long endTime = System.nanoTime();
		
		System.out.println("Total Time: " + (endTime - startTime));
		
		//isCorrect(a);
	}

	@Override
	public void runOnCPU(int arrayLen) {
		GraalCodeCacheProvider runtime = Graal.getRequiredCapability(GraalCodeCacheProvider.class);
		ParallelOptions.EnableComments.setValue(false);
		ParallelOptions.PrintCode.setValue(false);
		ParallelOptions.Execute.setValue(true);
		ParallelOptions.UseCPU.setValue(true);

		Method m = null;
		try {
			m = RaggedArrayBenchmark.class.getDeclaredMethod("simpleRagOperation", float[][].class);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		StructuredGraph sg = new StructuredGraph(runtime.lookupJavaMethod(m));

		float[][] a = new float[arrayLen][];
		
		for(int i = 0; i < a.length; i++) {
			a[i] = new float[dims[i%dims.length]];
		}
		
		ParallelOptions.WorkSize.setValue(arrayLen);
		Object[] params = new Object[1];
		params[0] = a;
		
		long startTime = System.nanoTime();
		ParallelUtil.run(sg, params);
		long endTime = System.nanoTime();
		
		System.out.println("Total Time: " + (endTime - startTime));
		
		isCorrect(a);
	}

	@Override
	public void runSeq(int arrayLen) {
		float[][] a = new float[arrayLen][];
		
		for(int i = 0; i < a.length; i++) {
			a[i] = new float[dims[i%dims.length]];
		}

		long startTime = System.nanoTime();
		simpleRagOperation(a);
		long endTime = System.nanoTime();
		
		System.out.println("Total Time: " + (endTime-startTime));
		
		isCorrect(a);
	}

	public void isCorrect(float[][] data) {
		for(int i = 0; i < data.length; i++) {
			for(int j = 0; j < data[i].length; j++) {
				if(data[i][j] != 1f) {
					System.out.println("incorrect: " + data[i][j]);
				}
			}
		}
	}
	
	@Override
	public void runHandWritten(int arrayLen) {		
	}

	@Override
	public String getName() {
		return "Ragged Array";
	}
}
