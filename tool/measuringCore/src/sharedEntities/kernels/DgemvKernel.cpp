/*
 * DgemvKernel.cpp
 *
 *  Created on: Feb 20, 2012
 *      Author: ruedi
 */

#include "DgemvKernel.h"
#include <cblas.h>
#include <cstdlib>

std::vector<std::pair<void*, long> > DgemvKernel::getBuffers() {
	std::vector<std::pair<void*, long> > result;
	result.push_back(std::make_pair(a,getMatrixSize()*getMatrixSize()*sizeof(double)));
	result.push_back(std::make_pair(x,getMatrixSize()*sizeof(double)));
	result.push_back(std::make_pair(y,getMatrixSize()*sizeof(double)));
	return result;
}

DgemvKernel::~DgemvKernel() {
}

void DgemvKernel::initialize() {
	BlasKernelBase::initialize();


	// seed random number generator for reproduceability
	srand48(0);

	long size=getMatrixSize();

	// allocate memory
	if (posix_memalign((void**) (&a), 16, size*size* sizeof(double)) != 0) {
		throw "could not allocate memory";
	}
	if (posix_memalign((void**) (&x), 16, size* sizeof(double)) != 0) {
			throw "could not allocate memory";
		}
	if (posix_memalign((void**) (&y), 16, size* sizeof(double)) != 0) {
				throw "could not allocate memory";
			}
	// initialize factors
	alpha=drand48();
	beta=drand48();

	// initialize matrix
	for (long i=0; i<size*size; i++){
		a[i]=drand48();
	}

	// initialize vectors
	for (long i=0; i<size; i++){
		x[i]=drand48();
		y[i]=drand48();
	}
}

void DgemvKernel::run() {
	long size = getMatrixSize();
	cblas_dgemv(CblasRowMajor, CblasNoTrans, size, size, alpha, a, size, x, 1,
			beta, y, 1);

	/*		void cblas_dgemv(const enum CBLAS_ORDER order,
	 const enum CBLAS_TRANSPOSE TransA, const int M, const int N,
	 const double alpha, const double *A, const int lda,
	 const double *X, const int incX, const double beta,
	 double *Y, const int incY);
	 */
}

void DgemvKernel::dispose() {
	free(a);
	free(x);
	free(y);
}

void DgemvKernel::warmCodeCache() {
	run();
}


