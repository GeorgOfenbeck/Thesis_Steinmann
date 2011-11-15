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
public:
	MemoryLoadKernel(MemoryLoadKernelDescription *description);
	virtual ~MemoryLoadKernel();
};

#endif /* LOADMEMORYKERNEL_H_ */
