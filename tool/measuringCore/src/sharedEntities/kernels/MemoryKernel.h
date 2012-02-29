/*
 * LoadMemoryKernel.h
 *
 *  Created on: Nov 9, 2011
 *      Author: ruedi
 */

#ifndef LOADMEMORYKERNEL_H_
#define LOADMEMORYKERNEL_H_
#include "sharedEntities/kernels/MemoryKernelData.h"

#ifdef __SSE__
#include "xmmintrin.h"
#endif

class MemoryKernel: public MemoryKernelData {
protected:
	float *buffer;
	std::vector<std::pair<void*,long> > getBuffers();

public:
	char result;
	float fresult;

	void initialize();
	void run();
	void dispose();
};
#endif /* LOADMEMORYKERNEL_H_ */
