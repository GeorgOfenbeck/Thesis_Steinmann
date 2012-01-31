/*
 * MMMKernel.cpp
 *
 *  Created on: Jan 31, 2012
 *      Author: ruedi
 */

#include "MMMKernel.h"
#include <cstdlib>
#include <cstring>
#include <cmath>

#include "typeRegistry/TypeRegisterer.h"

static TypeRegisterer<MMMKernel> dummy;

void MMMKernel::initialize(){
	srand48(0);
	size_t size=description->getMatrixSize();
	// square the size
	size*=size;

	if (posix_memalign((void**)(&a),16,size*sizeof(double))!=0){
		throw "could not allocate memory";
	}
	if (posix_memalign((void**)(&b),16,size*sizeof(double))!=0){
			throw "could not allocate memory";
		}
	if (posix_memalign((void**)(&c),16,size*sizeof(double))!=0){
				throw "could not allocate memory";
			}
	if (posix_memalign((void**)(&check),16,size*sizeof(double))!=0){
					throw "could not allocate memory";
				}

	// initialize matrices
	for (size_t i=0; i<size; i++){
		a[i]=drand48();
		b[i]=drand48();
		c[i]=0;
		check[i]=0;
	}

	// run kernel once
	run();

	// calculate check value
	tripleLoop(a,b,check);

	// perform check
	for (size_t i=0; i<size; i++){
			if (fabs(c[i]-check[i])>1e-5)
				throw "matrix multiplication error";
			c[i]=0;
		}
}

void MMMKernel::dispose(){
	free(a);
	free(b);
	free(c);
	free(check);
}
