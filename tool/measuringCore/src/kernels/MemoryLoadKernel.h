/*
 * LoadMemoryKernel.h
 *
 *  Created on: Nov 9, 2011
 *      Author: ruedi
 */

#ifndef LOADMEMORYKERNEL_H_
#define LOADMEMORYKERNEL_H_
#include "KernelBase.h"
#include "generatedC/MemoryLoadKernelDescription.h"

class MemoryLoadKernel : public Kernel<MemoryLoadKernelDescription>{
	char *buffer;
	char result;
public:
	MemoryLoadKernel(MemoryLoadKernelDescription *description);
	virtual ~MemoryLoadKernel();

	void run(){
		result=0;
		for (long i=0; i<description->getBlockSize(); i++){
			result=result^buffer[i];
		}
	}
};

#endif /* LOADMEMORYKERNEL_H_ */
