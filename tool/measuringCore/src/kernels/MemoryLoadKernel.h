/*
 * LoadMemoryKernel.h
 *
 *  Created on: Nov 9, 2011
 *      Author: ruedi
 */

#ifndef LOADMEMORYKERNEL_H_
#define LOADMEMORYKERNEL_H_
#include "baseClasses/KernelBase.h"
#include "generatedC/MemoryLoadKernelDescription.h"

class MemoryLoadKernel : public Kernel<MemoryLoadKernelDescription>{
protected:
	char *buffer;
	char result;
public:
	MemoryLoadKernel(MemoryLoadKernelDescription *description):Kernel(description){};

	void initialize();
	void run(){
		result=0;
		for (long p=0;p<1;p++){
			for (long i=0; i<description->getBufferSize(); i++){
				result=result^buffer[i];
			}
		}
	}
	void dispose();
};
#endif /* LOADMEMORYKERNEL_H_ */
