/*
 * FFTKernel.h
 *
 *  Created on: Feb 13, 2012
 *      Author: ruedi
 */

#ifndef FFTKERNEL_H_
#define FFTKERNEL_H_

#include "sharedDOM/FFTKernelData.h"
#include "mkl_dfti.h"

class FFTKernel : public FFTKernelData {
	double *doubleData;
	double _Complex *complexData;

	DFTI_DESCRIPTOR_HANDLE mklDescriptor;
	double dummy;

	void four1(double data[], unsigned long nn, int isign);
	void mklFFT(double _Complex *x, unsigned long nn);
public:
	virtual ~FFTKernel();

	void initialize();
	void run();
	void dispose();
	void warmCaches();
};

#endif /* FFTKERNEL_H_ */
