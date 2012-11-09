/*
 * SeqFFTfftwSplitKernel.cpp
 *
 *  Created on: Nov 6, 2012
 *      Author: max
 */

#include "SeqFFTfftwSplitKernel.h"
#include <cstdio>

SeqFFTfftwSplitKernel::~SeqFFTfftwSplitKernel() {
	// TODO Auto-generated destructor stub
}

void SeqFFTfftwSplitKernel::initialize() {
	ri = (double*) fftw_malloc(getBufferSize() * sizeof(double));
	ii = (double*) fftw_malloc(getBufferSize() * sizeof(double));
	ro = (double*) fftw_malloc(getBufferSize() * sizeof(double));
	io = (double*) fftw_malloc(getBufferSize() * sizeof(double));

	Dim.n = getBufferSize();
	Dim.is = 1;
	Dim.os = 1;

	fftwPlan = fftw_plan_guru_split_dft(1, &Dim,  0, NULL, ri, ii, ro, io, FFTW_MEASURE);

	// initialize buffer
	for (int64_t i = 0; i < getBufferSize(); i++) {
		ri[i] = drand48();
		ii[i] = drand48();
	}
}

void SeqFFTfftwSplitKernel::run() {
	fftw_execute_split_dft(fftwPlan, ri, ii, ro, io);
}

std::vector<std::pair<void*, long> > SeqFFTfftwSplitKernel::getBuffers() {
	std::vector<std::pair<void*, long> > result;
	result.push_back(std::make_pair((void*) ri, getBufferSize() * sizeof(double)));
	result.push_back(std::make_pair((void*) ii, getBufferSize() * sizeof(double)));
	result.push_back(std::make_pair((void*) ro, getBufferSize() * sizeof(double)));
	result.push_back(std::make_pair((void*) io, getBufferSize() * sizeof(double)));
	return result;
}

void SeqFFTfftwSplitKernel::dispose() {
	fftw_destroy_plan(fftwPlan);
	free(ri); free(ro);
	free(ii); free(io);
}

void SeqFFTfftwSplitKernel::warmCodeCache() {
	this->run();
}
