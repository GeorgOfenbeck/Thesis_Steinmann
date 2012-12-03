/*
 * FFTSpiralKernel.cpp
 *
 *  Created on: Feb 22, 2012
 *      Author: ruedi
 */

#include "FFTSpiralKernel.h"
#include "FFTSpiral/spiral_fft.h"
#include "FFTSpiral/spiral_private.h"


#include "FFTSpiral/spiral_private.c"
#include "FFTSpiral/spiral_fft_double.c"
#include <cmath>

std::vector<std::pair<void*, long> > FFTSpiralKernel::getBuffers() {
	std::vector<std::pair<void*, long> > result;
	result.push_back(
			std::make_pair((void*) pSrc,
					getBufferSize() * sizeof(double) * 2));
	result.push_back(
				std::make_pair((void*) pDst,
						getBufferSize() * sizeof(double) * 2));
	return result;
}

FFTSpiralKernel::~FFTSpiralKernel() {
	// TODO Auto-generated destructor stub
}

void FFTSpiralKernel::initialize() {

	if (posix_memalign((void**) (&pSrc), 16,
			getBufferSize() * sizeof(double) * 2) != 0) {
		throw "could not allocate memory";
	}
	if (posix_memalign((void**) (&pDst), 16,
			getBufferSize() * sizeof(double) * 2) != 0) {
		throw "could not allocate memory";
	}

	// initialize buffer
	for (int64_t i = 0; i < getBufferSize() * 2; i++) {
		pSrc[i] = drand48();
	}
/*
	SPIRAL_API(spiral_status_t, spiral_fftfwd_double_2)(const double *pSrc, double *pDst );
SPIRAL_API(spiral_status_t, spiral_fftfwd_double_4)(const double *pSrc, double *pDst );
SPIRAL_API(spiral_status_t, spiral_fftfwd_double_8)(const double *pSrc, double *pDst );
SPIRAL_API(spiral_status_t, spiral_fftfwd_double_16)(const double *pSrc, double *pDst );
SPIRAL_API(spiral_status_t, spiral_fftfwd_double_32)(const double *pSrc, double *pDst );
SPIRAL_API(spiral_status_t, spiral_fftfwd_double_64)(const double *pSrc, double *pDst );
SPIRAL_API(spiral_status_t, spiral_fftfwd_double_128)(const double *pSrc, double *pDst );
SPIRAL_API(spiral_status_t, spiral_fftfwd_double_256)(const double *pSrc, double *pDst );
SPIRAL_API(spiral_status_t, spiral_fftfwd_double_512)(const double *pSrc, double *pDst );
SPIRAL_API(spiral_status_t, spiral_fftfwd_double_1024)(const double *pSrc, double *pDst );
SPIRAL_API(spiral_status_t, spiral_fftfwd_double_2048)(const double *pSrc, double *pDst );
SPIRAL_API(spiral_status_t, spiral_fftfwd_double_4096)(const double *pSrc, double *pDst );
SPIRAL_API(spiral_status_t, spiral_fftfwd_double_8192)(const double *pSrc, double *pDst );
*/
}

void FFTSpiralKernel::run() {

	spiral_status_t status = spiral_fft_double(getBufferSize(), 1, pSrc, pDst);
       // spiral_status_t status;
/*	switch(getBufferSize())
	{
	 case 2:
		 status = spiral_fftfwd_double_2(pSrc, pDst );
		 break;
	} */
	std::string statusStr;
	switch (status) {
	case SPIRAL_SIZE_NOT_SUPPORTED:
		statusStr = "SIZE_NOT_SUPPORTED";
		break;
	case SPIRAL_INVALID_PARAM:
		statusStr = "SPIRAL_INVALID_PARAM";
		break;
	case SPIRAL_OUT_OF_MEMORY:
		statusStr = "SPIRAL_OUT_OF_MEMORY";
		break;
	case SPIRAL_OK:
		return;
	}
	throw "FFT failed: " + statusStr;
}

void FFTSpiralKernel::warmCodeCache() {
        run();
}

void FFTSpiralKernel::dispose() {
	free(pSrc);
	free(pDst);
}

