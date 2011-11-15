/*
 * LoadMemoryKernel.cpp
 *
 *  Created on: Nov 9, 2011
 *      Author: ruedi
 */

#include "MemoryLoadKernel.h"
#include "TypeRegisterer.h"

static TypeRegisterer<MemoryLoadKernel> dummy;


MemoryLoadKernel::MemoryLoadKernel(MemoryLoadKernelDescription *description)
	: Kernel(description){
	// TODO Auto-generated constructor stub
}

MemoryLoadKernel::~MemoryLoadKernel() {
	// TODO Auto-generated destructor stub
}

