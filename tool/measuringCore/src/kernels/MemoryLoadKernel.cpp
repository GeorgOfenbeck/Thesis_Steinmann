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


void MemoryLoadKernel::initialize(){
	buffer=(char*)malloc(description->getBlockSize());
}

void MemoryLoadKernel::dispose() {
	free(buffer);
}

