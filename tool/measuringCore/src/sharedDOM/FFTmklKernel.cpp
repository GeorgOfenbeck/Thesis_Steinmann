/*
 * FFTmklKernel.cpp
 *
 *  Created on: Feb 14, 2012
 *      Author: ruedi
 */

#include "FFTmklKernel.h"
#include <stdlib.h>
#include "Exception.h"

using namespace std;

FFTmklKernel::~FFTmklKernel() {
	// TODO Auto-generated destructor stub
}

void FFTmklKernel::initialize() {
	srand48(0);
	MKL_LONG status;

	status = DftiCreateDescriptor( &mklDescriptor, DFTI_DOUBLE,
			DFTI_COMPLEX, 1, getBufferSize());
	if (status != 0) {
		throw Exception("MKL FFT " + string(DftiErrorMessage(status)));
	}

	status = DftiCommitDescriptor(mklDescriptor);
	if (status != 0) {
		throw Exception("MKL FFT " + string(DftiErrorMessage(status)));
	}

	if (posix_memalign((void**) (&complexData), 16,
			getBufferSize() * sizeof(double _Complex)) != 0) {
		throw "could not allocate memory";
	}

	// initialize buffer
	for (size_t i = 0; i < getBufferSize(); i++) {
		complexData[i] = drand48() + drand48() * 1i;
	}
}

void FFTmklKernel::run() {
	MKL_LONG status;

	status = DftiComputeForward(mklDescriptor, complexData);
	if (status != 0) {
		throw Exception("MKL FFT " + string(DftiErrorMessage(status)));
	}
}

void FFTmklKernel::dispose() {
	MKL_LONG status;

	status = DftiFreeDescriptor(&mklDescriptor);
	if (status != 0) {
		throw Exception("MKL FFT " + string(DftiErrorMessage(status)));
	}

	free(complexData);
}

void FFTmklKernel::warmCaches() {
	dummy = 0;
	for (size_t i = 0; i < getBufferSize(); i++) {
		dummy += complexData[i];
		dummy *=0.9;
	}
}

