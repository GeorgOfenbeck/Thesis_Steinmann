/*
 * LoadMemoryKernel.cpp
 *
 *  Created on: Nov 9, 2011
 *      Author: ruedi
 */

#include "MemoryKernel.h"
#include "typeRegistry/TypeRegisterer.h"
#include <cstdlib>
#include <cstring>

static TypeRegisterer<MemoryKernel> dummy;


void MemoryKernel::initialize(){
	size_t size=description->getBufferSize()*sizeof(char);
	if (posix_memalign((void**)(&buffer),16,size*sizeof(float))!=0){
		throw "could not allocate memory";
	}
	for (size_t i=0; i<size; i++){
		buffer[i]=0;
	}
}

void MemoryKernel::dispose() {
	free(buffer);
}

