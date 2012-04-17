/*
 * FFTfftwKernel.cpp
 *
 *  Created on: Feb 14, 2012
 *      Author: ruedi
 */

#include "FFTfftwKernel.h"
#include "baseClasses/SystemInitializer.h"
#include <cstdio>

FFTfftwKernel::~FFTfftwKernel() {
	// TODO Auto-generated destructor stub
}

void FFTfftwKernel::initialize() {
	fftw_plan_with_nthreads(getNumThreads());

	fftwData = (fftw_complex*) fftw_malloc(
			getBufferSize() * sizeof(fftw_complex));

	fftwPlan = fftw_plan_dft_1d(getBufferSize(), fftwData, fftwData,
			FFTW_FORWARD, FFTW_MEASURE);

	// initialize buffer
	for (int64_t i = 0; i < getBufferSize(); i++) {
		fftwData[i][0] = drand48();
		fftwData[i][1] = drand48();
	}
}

void FFTfftwKernel::run() {
	fftw_execute(fftwPlan);
}

std::vector<std::pair<void*, long> > FFTfftwKernel::getBuffers() {
	std::vector<std::pair<void*, long> > result;
	result.push_back(
			std::make_pair((void*) fftwData,
					getBufferSize() * sizeof(double) * 2));
	return result;
}

void FFTfftwKernel::dispose() {
	fftw_destroy_plan(fftwPlan);
	free(fftwData);
}

// define and register a system initializer.
static class FFTfftwKernelInitializer: public SystemInitializer{
	void start(){
		// initialize fftw
		fftw_init_threads();

		// import wisdom
		FILE *wisdomFile=fopen("fftwWisdom","r");
		if (wisdomFile!=NULL){
			fftw_import_wisdom_from_file(wisdomFile);
			fclose(wisdomFile);
		}
	}

	void stop(){
		// export wisdom
		FILE *wisdomFile=fopen("fftwWisdom","w");
		fftw_export_wisdom_to_file(wisdomFile);
		fclose(wisdomFile);

		// clean up fftw
		fftw_cleanup_threads();
		fftw_cleanup();
	}
} dummy2;
