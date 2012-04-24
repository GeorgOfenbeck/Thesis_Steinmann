/*
 * LoadMemoryKernel.cpp
 *
 *  Created on: Nov 9, 2011
 *      Author: ruedi
 */
#include "Logger.h"
#include "MemoryKernel.h"
#include <cstdlib>
#include <cstring>
#include <cstdio>
#include "macros/RMT_MEMORY_DLP.h"
#include "macros/RMT_MEMORY_UNROLL.h"
#include "macros/RMT_MEMORY_OPERATION.h"
#include "macros/RMT_MEMORY_PREFETCH_DIST.h"
#include "macros/RMT_MEMORY_PREFETCH_TYPE.h"
#include "macros/RMT_MEMORY_USE_STREAMING_OPERATIONS.h"
#include "ctime"
#include <stdint.h>

enum MemoryOperation {
		MemoryOperation_READ, MemoryOperation_WRITE,MemoryOperation_RandomRead,
	};

#define UNROLL RMT_MEMORY_UNROLL
#define DLP RMT_MEMORY_DLP

static uint32_t randState=0;
static float fastRand(){
	randState=randState*1103515245+12345;
	float f=1;
	void *p=&f;
	(*(long*) p)|=randState>>10;
	f=f-1;
	return f;
}
void MemoryKernel::initialize() {
	LENTER
	srand48(0);

	size_t size = getBufferSize();
	// allocate buffer pointers
	if (posix_memalign((void**) (&buffer), 4*1024,
			DLP * UNROLL * size * sizeof(float)) != 0) {
		throw "could not allocate memory";
	}

	for (int p = 0; p < DLP; p++) {
		for (size_t i = 0; i < size * UNROLL; i++) {
			buffer[p * size * UNROLL + i] = fastRand();
		}
	}
	LLEAVE
}

std::vector<std::pair<void*, long> > MemoryKernel::getBuffers() {
	LENTER
	std::vector<std::pair<void*, long> > result;
	result.push_back(
			std::make_pair((void*) buffer,
					UNROLL * DLP * getBufferSize() * sizeof(float)));
	LLEAVE
	return result;
}

void MemoryKernel::run() {
	long bufferSize = getBufferSize();
#ifdef __SSE__
	if (bufferSize % 4 * sizeof(float) != 0) {
		throw "Buffer size is not a multiple of 4*sizeof(float)";
	}
#endif
#ifdef RMT_MEMORY_OPERATION__MemoryOperation_READ
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
			for (int j = 0; j < UNROLL; j++) {
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
#endif

#ifdef RMT_MEMORY_OPERATION__MemoryOperation_WRITE
#ifdef __SSE__
		__m128 tmp=_mm_set_ps(1,2,3,4);
		for (long i = 0; i < bufferSize; i += 4) {
			for (int j = 0; j < UNROLL; j++) {
				for (int p = 0; p < DLP; p++) {
#ifdef RMT_MEMORY_USE_STREAMING_OPERATIONS__false
					_mm_store_ps(&(buffer[p*bufferSize*UNROLL+i * UNROLL + j * 4]),
							tmp);
#else
					_mm_stream_ps(&(buffer[p*bufferSize*UNROLL+i * UNROLL + j * 4]),
												tmp);
#endif
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
#endif

#ifdef RMT_MEMORY_OPERATION__MemoryOperation_RandomRead
			float tempRes=0;
			long idx=0;
			long modulo=bufferSize*DLP*UNROLL;
			for (int i=0; i<bufferSize; i++){
				// read the random value at the current index
				float tmp=buffer[idx];
				tempRes+=tmp;

				// shift the index to the left to get enough random bits
				idx=(idx<<10);

				// xor with random value
				void *p=&tmp;
				idx=idx^(*(long *) p);

				// constrain index to the buffer
				idx=idx%modulo;

				//printf ("%li\n",idx);
			}
			result=tempRes;
#endif
}

void MemoryKernel::dispose() {
	LENTER
	free(buffer);
	LLEAVE
}

