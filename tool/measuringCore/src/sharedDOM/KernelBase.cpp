/*
 * KernelBase.cpp
 *
 *  Created on: Nov 9, 2011
 *      Author: ruedi
 */

#include "KernelBase.h"
#include "utils.h"

using namespace std;

KernelBase::KernelBase() {
	keepRunning=true;
}

KernelBase::~KernelBase() {
	// TODO Auto-generated destructor stub
}

void KernelBase::clearCaches()
{
	typedef pair<void *,long> TBufferPair;
	foreach(TBufferPair bufferPair,getBuffers()){
		char *buffer=(char*)bufferPair.first;
		long size=bufferPair.second;

		for (long i=0; i<size; i+=CacheLineSize){
			flushCacheLine(buffer+i);
		}
	}
}

static char dummy;

void KernelBase::warmCaches()
{
	typedef pair<void *,long> TBufferPair;
	foreach(TBufferPair bufferPair,getBuffers()){
		char *buffer=(char*)bufferPair.first;
		long size=bufferPair.second;

		for (long i=0; i<size; i+=CacheLineSize){
			dummy^=buffer[i];
		}
	}
	warmCachesAdditional();
}




