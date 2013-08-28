package NBody;

import Util.Benchmark;

public class NBodyBenchmark implements Benchmark{

    public void computeForces(float[] rx, float[] ry, float[] mass, float[] fx, float[] fy, int N) {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (i != j) {
                	float rxresult = rx[j] - rx[i];
                	float ryresult = ry[j] - ry[i];
                	double G =  6.67e-11;
                	float dist = (float) Math.sqrt((rxresult * rxresult) + (ryresult * ryresult));
                	float F = (float)(G * mass[i] * mass[j]) / (dist * dist);
                	rxresult = (rxresult * (1.0f/dist));
                	ryresult = (ryresult * (1.0f/dist));
                	rxresult = rxresult * F;
                	ryresult = ryresult * F;
                	fx[i] += rxresult;
                	fy[i] += ryresult;
                }
            }
        }
    }

	
	@Override
	public void runOnGPU(int arrayLen) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runOnCPU(int arrayLen) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runSeq(int arrayLen) {
		
	}

	@Override
	public void runHandWritten(int arrayLen) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
