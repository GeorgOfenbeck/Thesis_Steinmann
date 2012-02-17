/*
 * FFTfftwKernel.cpp
 *
 *  Created on: Feb 14, 2012
 *      Author: ruedi
 */

#include "FFTfftwKernel.h"
#include <fftw3.h>

static long planSize;
static bool planInitialized = false;
static fftw_plan fftwPlan;
static fftw_complex *fftwData;

FFTfftwKernel::~FFTfftwKernel() {
	// TODO Auto-generated destructor stub
}

void FFTfftwKernel::initialize() {
	if (!planInitialized || planSize != getBufferSize()) {
		printf("replan \n");
		if (planInitialized) {
			fftw_destroy_plan(fftwPlan);
			free(fftwData);
		}

		fftwData = (fftw_complex*) fftw_malloc(
				getBufferSize() * sizeof(fftw_complex));

		fftwPlan = fftw_plan_dft_1d(getBufferSize(), fftwData, fftwData,
				FFTW_FORWARD, FFTW_MEASURE);

		planSize = getBufferSize();
		planInitialized = true;
	}

	// initialize buffer
	for (size_t i = 0; i < getBufferSize(); i++) {
		fftwData[i][0] = drand48();
		fftwData[i][1] = drand48();
	}
}

void FFTfftwKernel::run() {
	fftw_execute(fftwPlan);
}

std::vector<std::pair<void*,long > > FFTfftwKernel::getBuffers()
{
	std::vector<std::pair<void*,long > > result;
	result.push_back(std::make_pair((void*)fftwData,getBufferSize()*sizeof(double)*2));
	return result;
}

void FFTfftwKernel::dispose() {
	// nothing to do
}
