/*
 * LoadMemoryKernel.cpp
 *
 *  Created on: Nov 9, 2011
 *      Author: ruedi
 */

#include "MemoryLoadKernel.h"
#include "typeRegistry/TypeRegisterer.h"
#include <cstdlib>
#include <cstring>

static TypeRegisterer<MemoryLoadKernel> dummy;


void MemoryLoadKernel::initialize(){
	size_t size=description->getBufferSize()*sizeof(double);
	a=(double*)malloc(size);
	b=(double*)malloc(size);
	c=(double*)malloc(size);

	memset(a,0,size);
	memset(b,0,size);
	memset(c,0,size);
}

void MemoryLoadKernel::dispose() {
	free(a);
	free(b);
	free(c);
}

