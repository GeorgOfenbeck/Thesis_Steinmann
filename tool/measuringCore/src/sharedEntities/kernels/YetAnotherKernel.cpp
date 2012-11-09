#include "YetAnotherKernel.h"

#include <cstring>
#include <stdio.h>


void YetAnotherKernel::initialize() {
	srand48(0);
	size_t ssize = getSize() * sizeof(double);

	// allocate the buffers
	a = (double*) malloc(ssize);
	b = (double*) malloc(ssize);
	c = (double*) malloc(ssize);

	// initialize the buffers
	for (long i=0; i<getSize(); i++){
		a[i]=drand48();
		b[i]=drand48();
		c[i]=drand48();
	}
}

std::vector<std::pair<void*, long> > YetAnotherKernel::getBuffers() {
	size_t ssize = getSize() * sizeof(double);

	std::vector<std::pair<void*, long> > result;
	result.push_back(std::make_pair((void*) a, ssize));
	result.push_back(std::make_pair((void*) b, ssize));
	result.push_back(std::make_pair((void*) c, ssize));
	return result;
}

void YetAnotherKernel::run() {
	for (long p = 0; p < 1; p++) {
		for (long i = 0; i < getSize(); i++) {
			a[i] = b[i]*b[i] + 2.34 * c[i]*c[i];
		}
	}
	printf("withouti %lf",a[0]);
}

void YetAnotherKernel::dispose() {
	free(a);
	free(b);
	free(c);
}
