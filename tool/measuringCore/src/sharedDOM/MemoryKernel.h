/*
 * LoadMemoryKernel.h
 *
 *  Created on: Nov 9, 2011
 *      Author: ruedi
 */

#ifndef LOADMEMORYKERNEL_H_
#define LOADMEMORYKERNEL_H_
#include "sharedDOM/MemoryKernelData.h"

#include "macros/RMT_MEMORY_OPERATION.h"

#ifdef __SSE__
#include "xmmintrin.h"
#endif

class MemoryKernel: public MemoryKernelData {
	enum MemoryOperation {
		MemoryOperation_READ, MemoryOperation_WRITE,
	};
protected:
	float *buffer;

public:
	char result;
	float fresult;

	void initialize();
	void run() {
		long bufferSize = getBufferSize();
#ifdef __SSE__
		if (bufferSize % 4 * sizeof(float) != 0) {
			throw "Buffer size is not a multiple of 4*sizeof(float)";
		}
#endif
		if (RMT_MEMORY_OPERATION == MemoryOperation_READ) {
#ifdef __SSE__
			__m128 ch = _mm_setzero_ps();
			for (long i = 0; i < bufferSize; i += 4 ) {
				ch = _mm_xor_ps(ch, _mm_load_ps(&(buffer[i])));
			}
			float tmp[4];
			_mm_storeu_ps(tmp, ch);

			char *b = (char*) tmp;

			result = 0;
			for (unsigned int i = 0; i < 4; i++) {
				result = result ^ b[i];
			}
#else
			char *b = (char*) buffer;
			char ch = 0;

			for (long i = 0; i < bufferSize ; i++) {
				ch = ch ^ b[i];
			}

			result = ch;
#endif
		}
		if (RMT_MEMORY_OPERATION == MemoryOperation_WRITE) {
#ifdef __SSE__
			for (long i = 0; i < bufferSize; i += 4) {
				_mm_store_ps(&(buffer[i]), _mm_setzero_ps());
			}
#else
			for (long i = 0; i < bufferSize; i++) {
				buffer[i] = 0;
			}
#endif
		}
	}
	void dispose();
};
#endif /* LOADMEMORYKERNEL_H_ */
