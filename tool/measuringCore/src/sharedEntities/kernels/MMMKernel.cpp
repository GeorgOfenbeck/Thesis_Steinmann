/*
 * MMMKernel.cpp
 *
 *  Created on: Jan 31, 2012
 *      Author: ruedi
 */

#include "Logger.h"

#include "MMMKernel.h"
#include <cstdlib>
#include <cstring>
#include <cmath>
#include <cblas.h>
#include "Exception.h"

void MMMKernel::initialize() {
	BlasKernelBase::initialize();

	srand48(0);
	size_t size = getMatrixSize();
	// square the size
	size *= size;

	if (posix_memalign((void**) (&a), 16, size * sizeof(double)) != 0) {
		throw "could not allocate memory";
	}
	if (posix_memalign((void**) (&b), 16, size * sizeof(double)) != 0) {
		throw "could not allocate memory";
	}
	if (posix_memalign((void**) (&c), 16, size * sizeof(double)) != 0) {
		throw "could not allocate memory";
	}
	if (posix_memalign((void**) (&check), 16, size * sizeof(double)) != 0) {
		throw "could not allocate memory";
	}

	// initialize matrices
	for (size_t i = 0; i < size; i++) {
		a[i] = drand48();
		b[i] = drand48();
		c[i] = 0;
		check[i] = 0;
	}

	if (!getNoCheck()) {
		// run kernel once
		run();

		// calculate check value
		tripleLoop(a, b, check);

		// perform check
		for (size_t i = 0; i < size; i++) {
			if (fabs(c[i] - check[i]) > 1e-5) {
				throw Exception("matrix multiplication error");
			}
			c[i] = 0;
		}
	}
}

void MMMKernel::blas(double *a, double *b, double *c) {
#if RMT_MMM_Algorithm__MMMAlgorithm_Blas
	LENTER
	int size = getMatrixSize();
	cblas_dgemm(
			CblasRowMajor,
			CblasNoTrans, CblasNoTrans,
			size, size, size,
			1,
			a, size,
			b, size,
			1,
			c, size);
	LLEAVE
#endif
}

std::vector<std::pair<void*, long> > MMMKernel::getBuffers() {
	LENTER
	size_t size = getMatrixSize();
	// square the size
	size *= size;

	std::vector<std::pair<void*, long> > result;
	result.push_back(std::make_pair((void*) a, size * sizeof(double)));
	result.push_back(std::make_pair((void*) b, size * sizeof(double)));
	result.push_back(std::make_pair((void*) c, size * sizeof(double)));
	LLEAVE
	return result;
}

void MMMKernel::dispose() {
	free(a);
	free(b);
	free(c);
	free(check);
}

void MMMKernel::warmCodeCache() {
	run();
}

