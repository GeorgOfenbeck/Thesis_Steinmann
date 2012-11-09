/*
 * SeqFFTfftwKernel.cpp
 *
 *  Created on: Oct 31, 2012
 *      Author: max
 */


#include "SeqFFTfftwKernel.h"
#include <cstdio>

SeqFFTfftwKernel::~SeqFFTfftwKernel() {
	// TODO Auto-generated destructor stub
}

void SeqFFTfftwKernel::initialize() {
	fftwDataIn = (fftw_complex*) fftw_malloc(getBufferSize() * sizeof(fftw_complex));
	fftwDataOut = (fftw_complex*) fftw_malloc(getBufferSize() * sizeof(fftw_complex));

	fftwPlan = fftw_plan_dft_1d(getBufferSize(), fftwDataIn, fftwDataOut,
			FFTW_FORWARD, FFTW_MEASURE);

	// initialize buffer
	for (int64_t i = 0; i < getBufferSize(); i++) {
		fftwDataIn[i][0] = drand48();
		fftwDataIn[i][1] = drand48();
	}
}

void SeqFFTfftwKernel::run() {
	fftw_execute(fftwPlan);
}

std::vector<std::pair<void*, long> > SeqFFTfftwKernel::getBuffers() {
	std::vector<std::pair<void*, long> > result;
	result.push_back(
			std::make_pair((void*) fftwDataIn,
					getBufferSize() * sizeof(double) * 2));
	result.push_back(std::make_pair((void*) fftwDataOut,
			getBufferSize() * sizeof(double) * 2));
	return result;
}

void SeqFFTfftwKernel::dispose() {
	fftw_destroy_plan(fftwPlan);
	free(fftwDataIn);
	free(fftwDataOut);
}

void SeqFFTfftwKernel::warmCodeCache() {
	this->run();
}
