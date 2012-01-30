/*
 * LoadMemoryKernel.h
 *
 *  Created on: Nov 9, 2011
 *      Author: ruedi
 */

#ifndef LOADMEMORYKERNEL_H_
#define LOADMEMORYKERNEL_H_
#include "baseClasses/KernelBase.h"
#include "sharedDOM/MemoryKernelDescription.h"

#include "macros/RMT_MEMORY_OPERATION.h"

#ifdef __SSE__
#include "xmmintrin.h"
#endif

class MemoryKernel: public Kernel<MemoryKernelDescription> {
	enum MemoryOperation {
		MemoryOperation_READ, MemoryOperation_WRITE,
	};
protected:
	float *buffer;

public:
	char result;
	float fresult;
	MemoryKernel(MemoryKernelDescription *description) :
			Kernel(description) {
	}
	;

	void initialize();
	void run() {
		long bufferSize = description->getBufferSize();
#ifdef __SSE__
		if (bufferSize % 8 * sizeof(float) != 0) {
			throw "Buffer size is not a multiple of 8*sizeof(float)";
		}
#endif
		if (RMT_MEMORY_OPERATION == MemoryOperation_READ) {
#ifdef __SSE__
#define DLP 4
			__m128 ch[DLP];
			for (int i = 0; i < DLP; i++) {
				ch[i] = _mm_setzero_ps();
			}
			bufferSize /= 2;
			for (long i = 0; i < bufferSize; i += DLP * 4 * sizeof(float)) {
				for (int p = 0; p < DLP; p++) {
					ch[p] = _mm_xor_ps(ch[p],
							_mm_load_ps(&(buffer[i + p * 4 * sizeof(float)])));
					ch[p] = _mm_xor_ps(
							ch[p],
							_mm_load_ps(
									&(buffer[i + p * 4 * sizeof(float)
											+ bufferSize])));
				}
			}

			result = 0;
			for (int i = 0; i < DLP; i++) {
				float tmp[4];
				_mm_storeu_ps(tmp, ch[i]);

				char *b = (char*) tmp;

				for (unsigned int i = 0; i < 4 * sizeof(float); i++) {
					result = result ^ b[i];
				}
			}
#else
			char *b=(char*) buffer;
			char ch=0;

			for (unsigned long i = 0; i < bufferSize*sizeof(float); i++) {
				ch = ch ^ b[i];
			}

			result = ch;
#endif
		}
		if (RMT_MEMORY_OPERATION == MemoryOperation_WRITE) {
#ifdef __SSE__
			for (long i = 0; i < bufferSize; i += 4 * sizeof(float)) {
				_mm_store_ps(&(buffer[i]), _mm_setzero_ps());
			}
#else
			for (unsigned long i = 0; i < bufferSize*sizeof(float); i++) {
				buffer[i]=0;
			}
#endif
		}
	}
	void dispose();
};
#endif /* LOADMEMORYKERNEL_H_ */
