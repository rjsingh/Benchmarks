__kernel
void vector_add(__global int *a, __global int *b, __global int *c, __global int *n) {
	int gid = get_global_id(0);
	int gis = get_global_size(0);
	
	int start = gid;
	int end = *n;
		
	for(int i = start; i < end; i += gis)
		c[i] = a[i] + b[i];
}