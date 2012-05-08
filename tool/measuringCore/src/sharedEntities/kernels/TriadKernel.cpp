/*
 * TriadKernel.cpp
 *
 *  Created on: Dec 19, 2011
 *      Author: ruedi
 */

#include "TriadKernel.h"

#include <cstring>
#include <stdio.h>


void TriadKernel::initialize() {
	srand48(0);
	size_t size = getBufferSize() * sizeof(double);

	// allocate the buffers
	a = (double*) malloc(size);
	b = (double*) malloc(size);
	c = (double*) malloc(size);

	// initialize the buffers
	for (long i=0; i<getBufferSize(); i++){
		a[i]=drand48();
		b[i]=drand48();
		c[i]=drand48();
	}
}

std::vector<std::pair<void*, long> > TriadKernel::getBuffers() {
	size_t size = getBufferSize() * sizeof(double);

	std::vector<std::pair<void*, long> > result;
	result.push_back(std::make_pair((void*) a, size));
	result.push_back(std::make_pair((void*) b, size));
	result.push_back(std::make_pair((void*) c, size));
	return result;
}

void TriadKernel::run() {
	for (long p = 0; p < 1; p++) {
		for (long i = 0; i < getBufferSize(); i++) {
			a[i] = b[i] + 2.34 * c[i];
		}
	}
	printf("withouti %lf",a[0]);
}

void TriadKernel::dispose() {
	free(a);
	free(b);
	free(c);
}
