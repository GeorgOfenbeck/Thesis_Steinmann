/*
 * LoadMemoryKernel.cpp
 *
 *  Created on: Nov 9, 2011
 *      Author: ruedi
 */

#include "MemoryLoadKernel.h"
#include "typeRegistry/TypeRegisterer.h"
#include <cstdlib>

static TypeRegisterer<MemoryLoadKernel> dummy;


MemoryLoadKernel::MemoryLoadKernel(MemoryLoadKernelDescription *description)
	: Kernel(description){
	buffer=(char*)malloc(description->getBlockSize());
}

MemoryLoadKernel::~MemoryLoadKernel() {
	free(buffer);
}

