/*
 * LoadMemoryKernel.cpp
 *
 *  Created on: Nov 9, 2011
 *      Author: ruedi
 */

#include "MemoryKernel.h"
#include <cstdlib>
#include <cstring>
#include "macros/RMT_MEMORY_DLP.h"
#include "macros/RMT_MEMORY_UNROLL.h"
#include "macros/RMT_MEMORY_OPERATION.h"
#include "macros/RMT_MEMORY_PREFETCH_DIST.h"
#include "macros/RMT_MEMORY_PREFETCH_TYPE.h"

#define UNROLL RMT_MEMORY_UNROLL
#define DLP RMT_MEMORY_DLP

void MemoryKernel::initialize() {
	size_t size = getBufferSize();
	// allocate buffer pointers
	if (posix_memalign((void**) (&buffer), CacheLineSize,
			DLP * UNROLL * size * sizeof(float*)) != 0) {
		throw "could not allocate memory";
	}

	for (int p = 0; p < DLP; p++) {
		for (size_t i = 0; i < size * UNROLL; i++) {
			buffer[p * size * UNROLL + i] = 0;
		}
	}
}

std::vector<std::pair<void*, long> > MemoryKernel::getBuffers() {
	std::vector<std::pair<void*, long> > result;
	result.push_back(
			std::make_pair((void*) buffer,
					UNROLL * DLP * getBufferSize() * sizeof(float)));
	return result;
}

void MemoryKernel::run() {
	long bufferSize = getBufferSize();
#ifdef __SSE__
	if (bufferSize % 4 * sizeof(float) != 0) {
		throw "Buffer size is not a multiple of 4*sizeof(float)";
	}
#endif
	if (RMT_MEMORY_OPERATION == MemoryOperation_READ) {
#ifdef __SSE__
		__m128 ch[DLP];

		// initialize the array
		for (int p = 0; p < DLP; p++) {
			ch[p] = _mm_setzero_ps();
		}

		// calculate the xor
		for (long i = 0; i < bufferSize * UNROLL; i += 4 * UNROLL) {
#ifndef RMT_MEMORY_PREFETCH_DIST__0
			for (int p=0; p<DLP; p++)
				_mm_prefetch(&buffer[p*bufferSize*UNROLL+i+RMT_MEMORY_PREFETCH_DIST],RMT_MEMORY_PREFETCH_TYPE);
#endif
			for (int j = 0; j < UNROLL; j+=2) {
				for (int p = 0; p < DLP; p++) {
					//printf("%li\n",i + j * 4);
					__m128 tmp;
					tmp=_mm_load_ps(&(buffer[p*bufferSize*UNROLL+i + j * 4]));
					//__asm("MOVNTDQA %1,%0":"=x"(tmp):"m"(buffer[p*bufferSize*UNROLL+i + j * 4]));
					ch[p] = _mm_xor_ps(ch[p],tmp);
				}
			}
		}
		result = 0;

		// combine the results
		for (int p = 0; p < DLP; p++) {
			float tmp[4 * 4];
			_mm_storeu_ps(tmp, ch[p]);

			char *b = (char*) tmp;

			for (unsigned int i = 0; i < 4 * 4; i++) {
				result = result ^ b[i];
			}
		}
#else
		char ch[DLP];
		for (int p = 0; p < DLP; p++)
			ch[p] = 0;

		for (long i = 0; i < bufferSize; i++)
			for (long j = 0; j < UNROLL; j++)
				for (int p = 0; p < DLP; p++)
					ch[p] = ch[p]
							^ (char) buffer[p * bufferSize * UNROLL + i * UNROLL
									+ j];

		for (int p = 0; p < DLP; p++)
			result ^= ch[p];
#endif
	}
	if (RMT_MEMORY_OPERATION == MemoryOperation_WRITE) {
#ifdef __SSE__
		for (long i = 0; i < bufferSize; i += 4) {
			for (int j = 0; j < UNROLL; j++) {
				for (int p = 0; p < DLP; p++) {
					_mm_store_ps(&(buffer[p*bufferSize*UNROLL+i * UNROLL + j * 4]),
							_mm_setzero_ps());
				}
			}
		}
#else
		for (long i = 0; i < bufferSize; i++) {
			for (int j = 0; j < UNROLL; j++)
				for (int p = 0; p < DLP; p++)
					buffer[p * bufferSize * UNROLL + i * UNROLL + j] = 0;
		}
#endif
	}
}

void MemoryKernel::dispose() {
	free(buffer);
}

