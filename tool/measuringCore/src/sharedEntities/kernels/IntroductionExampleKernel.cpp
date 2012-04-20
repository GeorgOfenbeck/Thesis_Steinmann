/*
 * IntroductionExample.cpp
 *
 *  Created on: Apr 19, 2012
 *      Author: ruedi
 */

#include "IntroductionExampleKernel.h"
#include <utility>
#include <stdlib.h>
#include <cmath>

std::vector<std::pair<void*, long> > IntroductionExampleKernel::getBuffers() {
	std::vector<std::pair<void*, long> > result;
	result.push_back(
			std::make_pair((void*) buffer, getN() * getM() * sizeof(double)));
	return result;
}

IntroductionExampleKernel::~IntroductionExampleKernel() {
	// TODO Auto-generated destructor stub
}

void IntroductionExampleKernel::initialize() {
	// allocate buffer pointers
	if (posix_memalign((void**) (&buffer), CacheLineSize,
			getN() * getM() * sizeof(double)) != 0) {
		throw "could not allocate memory";
	}

	for (int j = 0; j < getM(); j++)
		for (int i = 0; i < getN(); i++)
			buffer[i * getM() + j] = drand48();
}

template<int exp>
double pow(double d){
	return d*pow<exp-1>(d);
}

template<>
double pow<1>(double d){return d;}

void IntroductionExampleKernel::run() {
	if (getVariant() == 0) {
		for (int i = 0; i < getM(); i++)
			for (int j = 0; j < getN(); j++)
				buffer[i * getN() + j] = 3 * j * j * buffer[i * getN() + j];
	}

	if (getVariant() == 1) {
		for (int j = 0; j < getN(); j++) {
			int tmp = 3 * j * j;
			for (int i = 0; i < getM(); i++)
				buffer[i * getN() + j] = tmp * buffer[i * getN() + j];
		}
	}
}

void IntroductionExampleKernel::dispose() {
	free(buffer);
}

