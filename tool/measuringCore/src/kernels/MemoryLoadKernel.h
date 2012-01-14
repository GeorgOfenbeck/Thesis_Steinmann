/*
 * LoadMemoryKernel.h
 *
 *  Created on: Nov 9, 2011
 *      Author: ruedi
 */

#ifndef LOADMEMORYKERNEL_H_
#define LOADMEMORYKERNEL_H_
#include "baseClasses/KernelBase.h"
#include "sharedDOM/MemoryLoadKernelDescription.h"

#ifdef __SSE__
#include "xmmintrin.h"
#endif

class MemoryLoadKernel: public Kernel<MemoryLoadKernelDescription> {
protected:
	float *buffer;

public:
	char result;
	float fresult;
	MemoryLoadKernel(MemoryLoadKernelDescription *description) :
			Kernel(description) {
	}
	;

	void initialize();
	void run() {
#ifdef __SSE__
		__m128 ch = _mm_setzero_ps();
		for (long i = 0; i < description->getBufferSize(); i += 4*2) {
			ch = _mm_xor_ps(ch, _mm_load_ps(&(buffer[i])));
		}
		float tmp[4];
		_mm_storeu_ps(tmp, ch);

		char *b = (char*) tmp;
		result = 0;
		for (unsigned int i = 0; i < 4 * sizeof(float); i++) {
			result = result ^ b[i];
		}
#else
		char *b=(char*) buffer;
		char ch=0;
		long bufferSize=description->getBufferSize();

		for (unsigned long i = 0; i < bufferSize*sizeof(float); i++) {
			ch = ch ^ b[i];
		}

		result = ch;
#endif
	}
	void dispose();
};
#endif /* LOADMEMORYKERNEL_H_ */
