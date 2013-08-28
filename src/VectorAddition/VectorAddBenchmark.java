package VectorAddition;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.jocl.*;

import Util.Benchmark;
import Util.Helpers;

import com.edinburgh.parallel.opencl.ParallelOptions;
import com.edinburgh.parallel.opencl.ParallelUtil;
import com.oracle.graal.api.runtime.Graal;
import com.oracle.graal.nodes.StructuredGraph;
import com.oracle.graal.nodes.spi.GraalCodeCacheProvider;

public class VectorAddBenchmark implements Benchmark {
	
	public void vectorAdd(int[] a, int[] b, int[] c) {
		for(int i = 0; i < a.length; i++) {
			c[i] = a[i] + b[i];
		}
	}
	
	public void runOnGPU(int arrayLen) {
		GraalCodeCacheProvider runtime = Graal.getRequiredCapability(GraalCodeCacheProvider.class);
		ParallelOptions.EnableComments.setValue(false);
		ParallelOptions.PrintCode.setValue(true);
		ParallelOptions.Execute.setValue(true);
		ParallelOptions.UseCPU.setValue(false);

		
		Method m = null;
		try {
			m = VectorAddBenchmark.class.getDeclaredMethod("vectorAdd", int[].class, int[].class, int[].class);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StructuredGraph sg = new StructuredGraph(runtime.lookupJavaMethod(m));

		int[] a = new int[arrayLen];
		int[] b = new int[arrayLen];
		int[] c = new int[arrayLen];
		
		Arrays.fill(a, 1);
		Arrays.fill(b, 2);
		Arrays.fill(c, 0);
		
		ParallelOptions.WorkSize.setValue(arrayLen);
		Object[] params = new Object[3];
		params[0] = a;
		params[1] = b;
		params[2] = c;
		
		long startTime = System.nanoTime();
		ParallelUtil.run(sg, params);
		long endTime = System.nanoTime();
		
		System.out.println("Total Time: " + (endTime - startTime));
	}
	
	public void runOnCPU(int arrayLen) {
		GraalCodeCacheProvider runtime = Graal.getRequiredCapability(GraalCodeCacheProvider.class);
		ParallelOptions.EnableComments.setValue(false);
		ParallelOptions.PrintCode.setValue(false);
		ParallelOptions.Execute.setValue(true);
		ParallelOptions.UseCPU.setValue(true);

		Method m = null;
		try {
			m = VectorAddBenchmark.class.getDeclaredMethod("vectorAdd", int[].class, int[].class, int[].class);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StructuredGraph sg = new StructuredGraph(runtime.lookupJavaMethod(m));

		int[] a = new int[arrayLen];
		int[] b = new int[arrayLen];
		int[] c = new int[arrayLen];
		
		Arrays.fill(a, 1);
		Arrays.fill(b, 2);
		Arrays.fill(c, 0);
		
		ParallelOptions.WorkSize.setValue(arrayLen);
		Object[] params = new Object[3];
		params[0] = a;
		params[1] = b;
		params[2] = c;
		
		long startTime = System.nanoTime();
		ParallelUtil.run(sg, params);
		long endTime = System.nanoTime();
		
		System.out.println("Total Time: " + (endTime-startTime));
	}

	public void runSeq(int arrayLen) {
		int[] a = new int[arrayLen];
		int[] b = new int[arrayLen];
		int[] c = new int[arrayLen];
		
		Arrays.fill(a, 1);
		Arrays.fill(b, 2);
		Arrays.fill(c, 0);
		
		long startTime = System.nanoTime();
		vectorAdd(a,b,c);
		long endTime = System.nanoTime();
		
		System.out.println("Total Time: " + (endTime-startTime));
	}
	
	public void runHandWritten(int arrayLen) {
		String kernelSource = "";
		try {
			kernelSource = Helpers.getKernel("src/VectorAddition/vector_add.cl");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		String kernelName = "vector_add";
		
		//create data.
		int[] a = new int[arrayLen];
		int[] b = new int[arrayLen];
		int[] c = new int[arrayLen];
		
		Arrays.fill(a, 1);
		Arrays.fill(b, 2);
		Arrays.fill(c, 0);

		
		long startTime = System.nanoTime();
		
        final int platformIndex = 0;
        final long deviceType = CL.CL_DEVICE_TYPE_GPU;
        final int deviceIndex = 0;

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        // Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        CL.clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        CL.clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL.CL_CONTEXT_PLATFORM, platform);

        // Obtain the number of devices for the platform
        int numDevicesArray[] = new int[1];
        CL.clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];

        // Obtain a device ID
        cl_device_id devices[] = new cl_device_id[numDevices];
        CL.clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];

        // Create a context for the selected device
        cl_context context = CL.clCreateContext(contextProperties, 1, new cl_device_id[]{device}, null, null, null);

        // Create a command-queue for the selected device
        cl_command_queue commandQueue = CL.clCreateCommandQueue(context, device, CL.CL_QUEUE_PROFILING_ENABLE, null);
        
        // Create the program from the source code
        cl_program program = CL.clCreateProgramWithSource(context, 1, new String[]{kernelSource}, null, null);

        // Build the program
        long buildStartTime = System.nanoTime();
        CL.clBuildProgram(program, 0, null, null, null, null);
        long buildEndTime = System.nanoTime();
        
        System.out.println("Build Time: " + (buildEndTime-buildStartTime));
        
        // Create the kernel
        cl_kernel kernel = CL.clCreateKernel(program, kernelName, null);

        // Set the arguments for the kernel
        // Allocate the memory objects for the input- and output data
        
        long transferStartTime = System.nanoTime();
        cl_mem memObjects[] = new cl_mem[4];
        memObjects[0] = CL.clCreateBuffer(context, CL.CL_MEM_COPY_HOST_PTR | CL.CL_MEM_READ_ONLY,
        		Sizeof.cl_int * arrayLen, Pointer.to(a), null);
        memObjects[1] = CL.clCreateBuffer(context, CL.CL_MEM_COPY_HOST_PTR | CL.CL_MEM_READ_ONLY,
        		Sizeof.cl_int * arrayLen, Pointer.to(b), null);
        memObjects[2] = CL.clCreateBuffer(context, CL.CL_MEM_COPY_HOST_PTR | CL.CL_MEM_READ_WRITE,
        		Sizeof.cl_int * arrayLen, Pointer.to(c), null);
        memObjects[3] = CL.clCreateBuffer(context, CL.CL_MEM_COPY_HOST_PTR | CL.CL_MEM_READ_ONLY,
        		Sizeof.cl_int, Pointer.to(new int[] {arrayLen}), null);
        for (int i = 0; i < memObjects.length; i++) {
            CL.clSetKernelArg(kernel, i, Sizeof.cl_mem, Pointer.to(memObjects[i]));
        }
        long transferEndTime = System.nanoTime();
        
        System.out.println("Transfer Data Time: " + (transferEndTime-transferStartTime));

        // Set the work-item dimensions
        long global_work_size[] = new long[]{arrayLen};

        cl_event event = CL.clCreateUserEvent(context, null);
        
        // Execute the kernel
        CL.clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, global_work_size, null, 0, null, event);
        
        // Wait for kernel completion.
        CL.clWaitForEvents(1, new cl_event[]{event});

        long[] time_start = new long[1];
        long[] time_end = new long[1];
        long total_time;

        CL.clGetEventProfilingInfo(event, CL.CL_PROFILING_COMMAND_START, Sizeof.cl_long, Pointer.to(time_start), null);
        CL.clGetEventProfilingInfo(event, CL.CL_PROFILING_COMMAND_END, Sizeof.cl_long, Pointer.to(time_end), null);
        total_time = time_end[0] - time_start[0];
        System.out.println("Kernel Execution Time: " + (total_time));
        
        // Read the output data  
        long readStartTime = System.nanoTime();
        CL.clEnqueueReadBuffer(commandQueue, memObjects[2], CL.CL_TRUE, 0, Sizeof.cl_int * arrayLen, 
        		Pointer.to(c), 0, null, null);
        long readEndTime = System.nanoTime();
        System.out.println("Read Data Time: " + (readEndTime-readStartTime));
        
        // Release kernel, program, and memory objects
        for (int i = 0; i < memObjects.length; i++) {
        	CL.clReleaseMemObject(memObjects[i]);
        }
        
        CL.clReleaseKernel(kernel);
        CL.clReleaseProgram(program);
        CL.clReleaseCommandQueue(commandQueue);
        CL.clReleaseContext(context);
        
        long endTime = System.nanoTime();
        System.out.println("Total Time: " + (endTime-startTime));
	}

	@Override
	public String getName() {
		return "Vector Addition";
	}
}
