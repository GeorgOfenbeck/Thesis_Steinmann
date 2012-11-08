/*
 * SpiralSKernel.cpp
 *
 *  Created on: Feb 20, 2012
 *      Author: ruedi
 */

#include "SpiralSKernel.h"
#include "SpiralS_generated.cpp" 
std::vector<std::pair<void*, long> > SpiralSKernel::getBuffers() {
	std::vector<std::pair<void*, long> > result;
	result.push_back(std::make_pair(x,getVectorSize()*sizeof(double)));
	return result;

}

SpiralSKernel::~SpiralSKernel() {
}



void SpiralSKernel::initialize() {
	//BlasKernelBase::initialize();

	// seed random number generator for reproduceability
	srand48(0);

	long size=getVectorSize();

	// allocate memory
	if (posix_memalign((void**) (&x), 4*1024, size* sizeof(double)) != 0) {
			throw "could not allocate memory";
		}
	// initialize factors
	alpha=drand48();

	// initialize vectors
	for (long i=0; i<size; i++){
		x[i]=drand48();
	}



}

void SpiralSKernel::run() {
	fft(x);
	/*
	 void cblas_daxpy(const MKL_INT N, const double alpha, const double *X,
	 const MKL_INT incX, double *Y, const MKL_INT incY);
	 */
}

void SpiralSKernel::dispose() {
	free(x);
}

void SpiralSKernel::warmCodeCache() {
	run();
}


