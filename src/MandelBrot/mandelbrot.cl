__kernel
void mandelbrot(__global short *data,__global float *spacing,__global int *iterations, __global int *n) {
	int gid = get_global_id(0);
	int gis = get_global_size(0);
	int rows = *n;
	int cols = *n;
	float s = *spacing;
	int iter = *iterations;
	
	for(int i = gid; i < rows; i += gis) {
		for(int j = 0; j < cols; j++) {
			float Zr = 0.0f;
			float Zi = 0.0f;
			float Cr = (j * s - 1.5f);
			float Ci = (i * s - 1.0f);
			
			float ZrN = 0;
			float ZiN = 0;
			int y = 0;
			
			for(y = 0; y < iter && ZiN + ZrN <= 4.0f; y++) {
				Zi = 2.0f * Zr * Zi + Ci;
				Zr = ZrN - ZiN + Cr;
				ZiN = Zi * Zi;
				ZrN = Zr * Zr;
			}
			
			data[i * rows + j] = (short)((y * 255) / iter);
		}
	}
}
