package MandelBrot;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_event;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_platform_id;
import org.jocl.cl_program;

import com.edinburgh.parallel.opencl.ArrayUtil;
import com.edinburgh.parallel.opencl.ParallelOptions;
import com.edinburgh.parallel.opencl.ParallelUtil;
import com.oracle.graal.api.runtime.Graal;
import com.oracle.graal.api.runtime.GraalRuntime;
import com.oracle.graal.hotspot.HotSpotGraalRuntime;
import com.oracle.graal.hotspot.HotSpotVMConfig;
import com.oracle.graal.hotspot.amd64.AMD64HotSpotGraalRuntime;
import com.oracle.graal.hotspot.amd64.AMD64HotSpotRuntime;
import com.oracle.graal.hotspot.meta.HotSpotRuntime;
import com.oracle.graal.java.GraphBuilderConfiguration;
import com.oracle.graal.nodes.StructuredGraph;
import com.oracle.graal.nodes.spi.GraalCodeCacheProvider;

import Util.Benchmark;
import Util.Helpers;

public class MandelBrotBenchmark implements Benchmark{

	public void mandelbrot(short[][] data, float spacing, int iterations) {
		for(int i = 0; i < data.length; i++) {
			for(int j = 0; j < data[i].length; j++) {
				float Zr = 0.0f;
				float Zi = 0.0f;
				float Cr = (j * spacing - 1.5f);
				float Ci = (i * spacing - 1.0f);
				
				float ZrN = 0;
				float ZiN = 0;
				int y = 0;
				
				for(y = 0; y < iterations && ZiN + ZrN <= 4.0f; y++) {
					Zi = 2.0f * Zr * Zi + Ci;
					Zr = ZrN - ZiN + Cr;
					ZiN = Zi * Zi;
					ZrN = Zr * Zr;
				}
				data[i][j] = (short)((y * 255) / iterations);
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
		
		int height = arrayLen;
		int width = arrayLen;
		short[][] data = new short[height][width];
		float spacing = 2.0f / width;
		int iterations = 100;
	
		//store parameters.
		Object[] params = new Object[3];
		params[0] = data;
		params[1] = spacing;
		params[2] = iterations;
		
		//get handle to method.
		Method m = null;
		try {
			m = MandelBrotBenchmark.class.getDeclaredMethod("mandelbrot", short[][].class, float.class, int.class);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StructuredGraph sg = new StructuredGraph(runtime.lookupJavaMethod(m));

		ParallelOptions.WorkSize.setValue(arrayLen);
		
		long startTime = System.nanoTime();
		ParallelUtil.run(sg, params);
		long endTime = System.nanoTime();
		
		System.out.println("Total Time: " + (endTime - startTime));
		
		//save the output.
		File out = new File("mandelbrot_parallel.png");
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r = img.getRaster();
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				r.setSample(j, i, 0, data[i][j]);
			}
		}
		
		try {
			ImageIO.write(img, "png", out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void runOnCPU(int arrayLen) {
		GraalCodeCacheProvider runtime = Graal.getRequiredCapability(GraalCodeCacheProvider.class);
		ParallelOptions.EnableComments.setValue(false);
		ParallelOptions.PrintCode.setValue(false);
		ParallelOptions.Execute.setValue(true);
		ParallelOptions.UseCPU.setValue(true);
		
		int height = arrayLen;
		int width = arrayLen;
		short[][] data = new short[height][width];
		float spacing = 2.0f / width;
		int iterations = 100;
	
		//store parameters.
		Object[] params = new Object[3];
		params[0] = data;
		params[1] = spacing;
		params[2] = iterations;
		
		//get handle to method.
		Method m = null;
		try {
			m = MandelBrotBenchmark.class.getDeclaredMethod("mandelbrot", short[][].class, float.class, int.class);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StructuredGraph sg = new StructuredGraph(runtime.lookupJavaMethod(m));

		ParallelOptions.WorkSize.setValue(arrayLen);
		long startTime = System.nanoTime();
		ParallelUtil.run(sg, params);
		long endTime = System.nanoTime();
		
		System.out.println("Total Time: " + (endTime - startTime));
		
		//save the output.
		File out = new File("mandelbrot_parallel.png");
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r = img.getRaster();
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				r.setSample(j, i, 0, data[i][j]);
			}
		}
		
		try {
			ImageIO.write(img, "png", out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void runSeq(int arrayLen) {
		int height = arrayLen;
		int width = arrayLen;
		short[][] data = new short[height][width];
		float spacing = 2.0f / width;
		int iterations = 100;
		
		long startTime = System.nanoTime();
		mandelbrot(data, spacing, iterations);
		long endTime = System.nanoTime();
		System.out.println("Total Time: " + (endTime-startTime));
		
		File out = new File("mandelbrot_sequential.png");
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r = img.getRaster();
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				r.setSample(j, i, 0, data[i][j]);
			}
		}
		
		try {
			ImageIO.write(img, "png", out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void runHandWritten(int arrayLen) {
		String kernelSource = "";
		try {
			kernelSource = Helpers.getKernel("src/MandelBrot/mandelbrot.cl");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		String kernelName = "mandelbrot";
		
		//create data.
		short[][] data = new short[arrayLen][arrayLen];
		float spacing = 2.0f / arrayLen;
		int iterations = 100;

		long startTime = System.nanoTime();
		
		//flatten arrays (marshalling) for transferring to the OpenCL kernel.
        long marshallStartTime = System.nanoTime();
		short[] flatData = (short[]) ArrayUtil.transformArray(data);
		long marshallEndTime = System.nanoTime();
		
		System.out.println("Marshall Time: " + (marshallEndTime - marshallStartTime));
		
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
        memObjects[0] = CL.clCreateBuffer(context, CL.CL_MEM_COPY_HOST_PTR | CL.CL_MEM_READ_WRITE,
        		Sizeof.cl_short * flatData.length, Pointer.to(flatData), null);
        memObjects[1] = CL.clCreateBuffer(context, CL.CL_MEM_COPY_HOST_PTR | CL.CL_MEM_READ_ONLY,
        		Sizeof.cl_float, Pointer.to(new float[] {spacing}), null);
        memObjects[2] = CL.clCreateBuffer(context, CL.CL_MEM_COPY_HOST_PTR | CL.CL_MEM_READ_ONLY,
        		Sizeof.cl_int, Pointer.to(new int[] {iterations}), null);
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
        CL.clEnqueueReadBuffer(commandQueue, memObjects[0], CL.CL_TRUE, 0, Sizeof.cl_short * arrayLen * arrayLen, 
        		Pointer.to(flatData), 0, null, null);
        
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
        
        //rebuild array. (Unmarshall)
        long unmarshallStartTime = System.nanoTime();
        ArrayUtil.rebuild(flatData, data, new int[] {0});
        long unmarshallEndTime = System.nanoTime();
        
        System.out.println("UnMarshall Time: " + (unmarshallEndTime - unmarshallStartTime));

        long endTime = System.nanoTime();
        System.out.println("Total Time: " + (endTime-startTime));

        
        //SAVE OUTPUT
        
		File out = new File("mandelbrot_handwritten.png");
		BufferedImage img = new BufferedImage(arrayLen, arrayLen, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r = img.getRaster();
		
		for(int i = 0; i < arrayLen; i++) {
			for(int j = 0; j < arrayLen; j++) {
				r.setSample(j, i, 0, data[i][j]);
			}
		}
		
		try {
			ImageIO.write(img, "png", out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return "MandelBrot";
	}

}
