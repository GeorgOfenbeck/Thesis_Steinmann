/*
 * FFTKernel.cpp
 *
 *  Created on: Feb 13, 2012
 *      Author: ruedi
 */

#include "FFTKernel.h"

#include "macros/RMT_FFT_Algorithm.h"

#include <stdlib.h>
#include <string>
#include <cmath>
#include "Exception.h"
#include "mkl_dfti.h"

using namespace std;

enum FFTAlgorithm {
	FFTAlgorithm_NR, FFTAlgorithm_MKL, FFTAlgorithm_FFTW
};

FFTKernel::~FFTKernel() {
	// TODO Auto-generated destructor stub
}

static long planSize;
static bool planInitialized = false;
static fftw_plan fftwPlan;
static fftw_complex *fftwData;

double *allocateDoubleData(size_t size) {
	double *result;
	if (posix_memalign((void**) (((&result))), 16, size * sizeof(double))
			!= 0) {
		throw "could not allocate memory";
	}
	// initialize buffer
	for (size_t i = 0; i < size; i++) {
		result[i] = drand48();
	}
	return result;
}

double _Complex *allocateComplexData(size_t size) {
	double _Complex *result;

	if (posix_memalign((void**) (&result), 16, size * sizeof(double _Complex))
			!= 0) {
		throw "could not allocate memory";
	}

	// initialize buffer
	for (size_t i = 0; i < size; i++) {
		result[i] = drand48() + drand48() * 1i;
	}
	return result;
}

#define SWAP(a,b) tempr=(a);(a)=(b);(b)=tempr

void FFTKernel::four1(double data[], unsigned long nn, int isign) {
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

void FFTKernel::fftwFFT(double _Complex *x, unsigned long nn) {
	fftw_execute(fftwPlan);
}
void FFTKernel::mklFFT(double _Complex *x, unsigned long nn) {

	MKL_LONG status;

	//status = DftiComputeForward(mklDescriptor, x);
	if (status != 0) {
		//throw Exception("MKL FFT " + string(DftiErrorMessage(status)));
	}
}

void FFTKernel::initialize() {
	srand48(0);

	MKL_LONG status;

	if (RMT_FFT_Algorithm == FFTAlgorithm_MKL || !getNoCheck()) {
		//status = DftiCreateDescriptor( &mklDescriptor, DFTI_DOUBLE,
		//		DFTI_COMPLEX, 1, getBufferSize());
		if (status != 0) {
			//throw Exception("MKL FFT " + string(DftiErrorMessage(status)));
		}

		//status = DftiCommitDescriptor(mklDescriptor);
		if (status != 0) {
			//throw Exception("MKL FFT " + string(DftiErrorMessage(status)));
		}
	}

	if (!getNoCheck()) {
		doubleData = allocateDoubleData(getBufferSize() * 2);
		complexData = allocateComplexData(getBufferSize());

		// overwrite double data with complex data
		for (size_t i = 0; i < getBufferSize(); i++) {
			doubleData[2 * i] = __real__ complexData[i];
			doubleData[2 * i + 1] = __imag__ complexData[i];
		}

		mklFFT(complexData, getBufferSize());
		four1(doubleData, getBufferSize(), 1);

		for (size_t i = 0; i < getBufferSize(); i++) {
			size_t p = (getBufferSize() - i) % getBufferSize();
			//printf("r %i: %lf %lf\n",i,doubleData[2*p],__real__ complexData[i]);
			//printf("i %i: %lf %lf\n",i,doubleData[2*p+1],__imag__ complexData[i]);

			if (fabs(doubleData[2 * p] - __real__ complexData[i]) > 1e-5) {
				throw Exception("fft error: real part");
			}
			if (fabs(doubleData[2 * p + 1] - __imag__ complexData[i]) > 1e-5) {
				throw Exception("fft error: imag part");
			}
		}

		free(doubleData);
		free(complexData);
		doubleData = NULL;
		complexData = NULL;
	}

	if (RMT_FFT_Algorithm == FFTAlgorithm_NR) {
		doubleData = allocateDoubleData(getBufferSize() * 2);
		complexData = NULL;
	}

	if (RMT_FFT_Algorithm == FFTAlgorithm_MKL) {
		doubleData = NULL;
		complexData = allocateComplexData(getBufferSize());
	}

	if (RMT_FFT_Algorithm == FFTAlgorithm_FFTW) {
		if (!planInitialized || planSize != getBufferSize()) {
			printf("replan \n");
			if (planInitialized) {
				fftw_destroy_plan(fftwPlan);
				free(fftwData);
			}

			fftwData=(fftw_complex*) fftw_malloc(getBufferSize()*sizeof(fftw_complex));

			fftwPlan = fftw_plan_dft_1d(getBufferSize(), fftwData, fftwData, FFTW_FORWARD,
					FFTW_MEASURE);

			planSize = getBufferSize();
			planInitialized = true;
		}
		// initialize buffer
		for (size_t i = 0; i < getBufferSize(); i++) {
			fftwData[i][0] = drand48();
			fftwData[i][1] = drand48();
		}
	}
}

void FFTKernel::run() {
	if (RMT_FFT_Algorithm == FFTAlgorithm_NR) {
		four1(doubleData, getBufferSize(), 1);
	}

	if (RMT_FFT_Algorithm == FFTAlgorithm_MKL) {
		mklFFT(complexData, getBufferSize());
	}

	if (RMT_FFT_Algorithm == FFTAlgorithm_FFTW) {
		fftwFFT(complexData, getBufferSize());
	}
}

void FFTKernel::dispose() {
	if (RMT_FFT_Algorithm == FFTAlgorithm_NR) {
		free(doubleData);
	}

	if (RMT_FFT_Algorithm == FFTAlgorithm_MKL) {
		MKL_LONG status;

		//status = DftiFreeDescriptor(&mklDescriptor);
		if (status != 0) {
			throw Exception("MKL FFT " + string(DftiErrorMessage(status)));
		}

		free(complexData);
	}

	if (RMT_FFT_Algorithm == FFTAlgorithm_FFTW) {
		// nothing to do
	}
}

void FFTKernel::warmCaches() {
	if (RMT_FFT_Algorithm == FFTAlgorithm_NR) {
		dummy = 0;
		for (size_t i = 0; i < getBufferSize() * 2; i++)
			dummy += doubleData[i];
	}

	if (RMT_FFT_Algorithm == FFTAlgorithm_MKL) {
		dummy = 0;
		for (size_t i = 0; i < getBufferSize(); i++)
			dummy += __real__ complexData[i] + __imag__ complexData[i];
	}
}

