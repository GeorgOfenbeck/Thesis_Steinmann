/*
 * TriadKernel.cpp
 *
 *  Created on: Dec 19, 2011
 *      Author: ruedi
 */

#include "TriadKernel.h"

#include <cstring>

void TriadKernel::initialize() {
	size_t size = getBufferSize() * sizeof(double);
	a = (double*) malloc(size);
	b = (double*) malloc(size);
	c = (double*) malloc(size);

	memset(a, 0, size);
	memset(b, 0, size);
	memset(c, 0, size);
}

std::vector<std::pair<void*, long> > TriadKernel::getBuffers() {
	size_t size = getBufferSize() * sizeof(double);

	std::vector<std::pair<void*, long> > result;
	result.push_back(std::make_pair((void*) a, size));
	result.push_back(std::make_pair((void*) b, size));
	result.push_back(std::make_pair((void*) c, size));
	return result;
}

void TriadKernel::dispose() {
	free(a);
	free(b);
	free(c);
}
