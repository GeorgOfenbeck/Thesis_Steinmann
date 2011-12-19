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
	size_t size=description->getBufferSize()*sizeof(char);
	buffer=(char*)malloc(size);
}

void MemoryLoadKernel::dispose() {
	free(buffer);
}

