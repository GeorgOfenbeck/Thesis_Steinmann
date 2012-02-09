/*
 * Workload.cpp
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#include "Workload.h"


Workload::~Workload() {
	// TODO Auto-generated destructor stub
}

static int dummy;

void WorkloadBase::clearCaches() {
	clearL1ICache();
	// just access 10M of memory, which is the maximum cache size present in current processors
	size_t blockSize=10*(1<<20);
	char *buffer=(char*)malloc(blockSize);
	for (size_t i=0; i<blockSize; i++){
		dummy+=buffer[i];
		//buffer[i]=0;
	}
	free((void*)buffer);
}

