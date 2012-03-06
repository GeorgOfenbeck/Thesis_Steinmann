/*
 * FFTSpiralKernel.cpp
 *
 *  Created on: Feb 22, 2012
 *      Author: ruedi
 */

#include "FFTSpiralKernel.h"
#include "FFTSpiral/spiral_fft.h"
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
	for (size_t i = 0; i < getBufferSize() * 2; i++) {
		pSrc[i] = drand48();
	}
}

void FFTSpiralKernel::run() {

	spiral_status_t status=spiral_fft_double(getBufferSize(), 1, pSrc, pDst);

	if (status != SPIRAL_OK) {
		std::string statusStr;
		switch(status){
		case SPIRAL_SIZE_NOT_SUPPORTED:
			statusStr="SIZE_NOT_SUPPORTED";
			break;
		case SPIRAL_INVALID_PARAM:
			statusStr="SPIRAL_INVALID_PARAM";
			break;
		case SPIRAL_OUT_OF_MEMORY:
			statusStr="SPIRAL_OUT_OF_MEMORY";
			break;
		}
		throw "FFT failed: "+statusStr;
	}
	/*
	 spiral_status_t spiral_fft_double)
	 (int n, int sign, const double *pSrc, double *pDst );
	 */

}

void FFTSpiralKernel::dispose() {
	free(pSrc);
	free(pDst);
}

