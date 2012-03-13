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
	for (int64_t i = 0; i < getBufferSize() * 2; i++) {
		pSrc[i] = drand48();
	}
}

void FFTSpiralKernel::run() {

	spiral_status_t status = spiral_fft_double(getBufferSize(), 1, pSrc, pDst);

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

void FFTSpiralKernel::dispose() {
	free(pSrc);
	free(pDst);
}

