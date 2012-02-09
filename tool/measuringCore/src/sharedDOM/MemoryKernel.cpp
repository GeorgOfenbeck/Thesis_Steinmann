/*
 * LoadMemoryKernel.cpp
 *
 *  Created on: Nov 9, 2011
 *      Author: ruedi
 */

#include "MemoryKernel.h"
#include <cstdlib>
#include <cstring>


void MemoryKernel::initialize(){
	size_t size=getBufferSize();
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

