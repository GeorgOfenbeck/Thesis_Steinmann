/*
 * TestKernel.cpp
 *
 *  Created on: Feb 20, 2012
 *      Author: ruedi
 */

#include "TestKernel.h"

std::vector<std::pair<void*, long> > TestKernel::getBuffers() {
	std::vector<std::pair<void*, long> > result;
	result.push_back(std::make_pair(x,getVectorSize()*sizeof(double)));
	return result;

}

TestKernel::~TestKernel() {
}



void TestKernel::initialize() {

	// seed random number generator for reproduceability
	srand48(0);

	long size=getVectorSize();

	// allocate memory
	if (posix_memalign((void**) (&x), 4*1024, size* sizeof(double)) != 0) {
			throw "could not allocate memory";
		}
	// initialize factors

	// initialize vectors
	for (long i=0; i<size; i++){
		x[i]=drand48();
	}



}

void TestKernel::run() {

	int size = getVectorSize();

	for(int i = 0; i < size; ++i) {
		x[i] += 1;
	}
	/*
	 void cblas_daxpy(const MKL_INT N, const double alpha, const double *X,
	 const MKL_INT incX, double *Y, const MKL_INT incY);
	 */
}

void TestKernel::dispose() {
	free(x);
}

void TestKernel::warmCodeCache() {
	run();
}

