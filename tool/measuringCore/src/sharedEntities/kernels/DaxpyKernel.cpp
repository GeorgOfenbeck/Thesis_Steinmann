/*
 * DaxpyKernel.cpp
 *
 *  Created on: Feb 20, 2012
 *      Author: ruedi
 */

#include "DaxpyKernel.h"
#include <cblas.h>

std::vector<std::pair<void*, long> > DaxpyKernel::getBuffers() {
	std::vector<std::pair<void*, long> > result;
	result.push_back(std::make_pair(x,getVectorSize()*sizeof(double)));
	result.push_back(std::make_pair(y,getVectorSize()*sizeof(double)));
	return result;

}

DaxpyKernel::~DaxpyKernel() {
}



void DaxpyKernel::initialize() {
	BlasKernelBase::initialize();

	// seed random number generator for reproduceability
	srand48(0);

	long size=getVectorSize();

	// allocate memory
	if (posix_memalign((void**) (&x), 16, size* sizeof(double)) != 0) {
			throw "could not allocate memory";
		}
	if (posix_memalign((void**) (&y), 16, size* sizeof(double)) != 0) {
				throw "could not allocate memory";
			}
	// initialize factors
	alpha=drand48();

	// initialize vectors
	for (long i=0; i<size; i++){
		x[i]=drand48();
		y[i]=drand48();
	}



}

void DaxpyKernel::run() {
	cblas_daxpy(getVectorSize(), alpha, x, 1, y, 1);

	/*
	 void cblas_daxpy(const MKL_INT N, const double alpha, const double *X,
	 const MKL_INT incX, double *Y, const MKL_INT incY);
	 */
}

void DaxpyKernel::dispose() {
	free(x);
	free(y);
}

