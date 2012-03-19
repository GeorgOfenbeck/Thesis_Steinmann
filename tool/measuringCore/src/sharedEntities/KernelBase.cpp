/*
 * KernelBase.cpp
 *
 *  Created on: Nov 9, 2011
 *      Author: ruedi
 */

#include "Logger.h"
#include "KernelBase.h"
#include "utils.h"
#include "Exception.h"
#include "typeinfo"

using namespace std;

KernelBase::KernelBase() {
	keepRunning=true;
}

KernelBase::~KernelBase() {
	// TODO Auto-generated destructor stub
}

void KernelBase::flushBuffers()
{
	LENTER
	typedef pair<void *,long> TBufferPair;
	foreach(TBufferPair bufferPair,getBuffers()){
		char *buffer=(char*)bufferPair.first;
		long size=bufferPair.second;

		for (long i=0; i<size; i+=CacheLineSize){
			flushCacheLine(buffer+i);
		}
	}
	LLEAVE
}

static char dummy;

void KernelBase::warmDataCache()
{
	typedef pair<void *,long> TBufferPair;
	foreach(TBufferPair bufferPair,getBuffers()){
		char *buffer=(char*)bufferPair.first;
		long size=bufferPair.second;

		for (long i=0; i<size; i+=CacheLineSize){
			dummy^=buffer[i];
		}
	}

	warmDataCacheAdditional();
}

void KernelBase::warmCodeCache() {
	LERROR("code cache warming not implemented in %s",typeid(*this).name())
	throw new Exception("code cache warming not implemented");
}





