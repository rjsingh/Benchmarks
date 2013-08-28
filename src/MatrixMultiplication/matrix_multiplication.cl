__kernel
void matrix_multiplication(__global int *a, __global int *b, __global int *c, __global int *n) {
	int gid = get_global_id(0);
	int gis = get_global_size(0);
		
	int start = gid;
	int rows = *n;
	int cols = *n;
	
	for(int i = start; i < rows; i += gis) {
		for(int j = 0; j < cols; j++) {
			for(int y = 0; y < cols; y++) {
				c[i * rows + j] += (a[i * rows +y] * b[y * rows + j]);				
			}
		}
	}
	
}