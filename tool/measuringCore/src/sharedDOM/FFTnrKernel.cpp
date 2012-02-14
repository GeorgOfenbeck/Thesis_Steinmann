/*
 * FFTnrKernel.cpp
 *
 *  Created on: Feb 14, 2012
 *      Author: ruedi
 */

#include "FFTnrKernel.h"
#include <cmath>

FFTnrKernel::~FFTnrKernel() {
	// TODO Auto-generated destructor stub
}

void FFTnrKernel::initialize() {
	srand48(0);
	if (posix_memalign((void**) (((&doubleData))), 16,
			getBufferSize() * 2 * sizeof(double)) != 0) {
		throw "could not allocate memory";
	}
	// initialize buffer
	for (size_t i = 0; i < getBufferSize() * 2; i++) {
		doubleData[i] = drand48();
	}
}

#define SWAP(a,b) tempr=(a);(a)=(b);(b)=tempr

void four1(double data[], unsigned long nn, int isign) {
	unsigned long n, mmax, m, j, istep, i;
	double wtemp, wr, wpr, wpi, wi, theta, tempr, tempi;

	//binary inversion (note that the indexes
	//start from 0 witch means that the
	//real part of the complex is on the even-indexes
	//and the complex part is on the odd-indexes)
	n = nn << 1;
	j = 1;
	for (i = 1; i < n; i += 2) {
		if (j > i) {
			SWAP(data[j-1], data[i-1]);
			SWAP(data[j], data[i]);
		}
		m = n >> 1;
		while (m >= 2 && j > m) {
			j -= m;
			m >>= 1;
		}
		j += m;
	}
	//end of the bit-reversed order algorithm

	//Danielson-Lanzcos routine
	mmax = 2;
	while (n > mmax) {
		istep = mmax << 1;
		theta = isign * (6.28318530717959 / mmax);
		wtemp = sin(0.5 * theta);
		wpr = -2.0 * wtemp * wtemp;
		wpi = sin(theta);
		wr = 1.0;
		wi = 0.0;
		for (m = 1; m < mmax; m += 2) {
			for (i = m; i <= n; i += istep) {
				j = i + mmax;
				tempr = wr * data[j - 1] - wi * data[j];
				tempi = wr * data[j] + wi * data[j - 1];
				data[j - 1] = data[i - 1] - tempr;
				data[j] = data[i] - tempi;
				data[i - 1] += tempr;
				data[i] += tempi;
			}
			wr = (wtemp = wr) * wpr - wi * wpi + wr;
			wi = wi * wpr + wtemp * wpi + wi;
		}
		mmax = istep;
	}
	//end of the algorithm
}

void FFTnrKernel::run() {
	four1(doubleData, getBufferSize(), 1);
}

void FFTnrKernel::dispose() {
	free(doubleData);
}

void FFTnrKernel::warmCaches() {
	dummy = 0;
	for (size_t i = 0; i < getBufferSize() * 2; i++) {
		dummy += doubleData[i];
		dummy *= 0.9;
	}

}

