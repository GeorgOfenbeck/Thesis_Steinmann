/*
 * This file has to be compiled without optimizations to make the clearCaches() function work.
 * The function depends on each template beeing instantiated. This causes the L1 cache to be flushed
 * when the function is called, since it's simply a lot of code to execute
 */

#include "MeasurementSchemeBase.h"

MeasurementSchemeBase::~MeasurementSchemeBase() {
	// TODO Auto-generated destructor stub
}

template<int N>
int clear(){
	return 1+clear<N-1>();
}

template<>
int clear<0>(){
	return 0;
}

static int dummy;

void MeasurementSchemeBase::clearCaches() {
	// analyzing the output shows that each instantiation of clear() takes 15 bytes
	// 2500*15=37500, which is more than the 32K code l1 cache of most pentiums
	dummy=clear<2500>();

	// just access 10M of memory, which is the maximum cache size present in current processors
	size_t blockSize=10*(1<<20);
	char *buffer=(char*)malloc(blockSize);
	for (size_t i=0; i<blockSize; i++){
		dummy+=buffer[i];
		buffer[i]=0;
	}
	free((void*)buffer);
}

