/*
 * FFTfftwKernel.h
 *
 *  Created on: Feb 14, 2012
 *      Author: ruedi
 */

#ifndef FFTFFTWKERNEL_H_
#define FFTFFTWKERNEL_H_

#include <fftw3.h>
#include "sharedEntities/kernels/FFTfftwKernelData.h"

class FFTfftwKernel: public  FFTfftwKernelData{
	double dummy;
	protected:
	std::vector<std::pair<void*,long> > getBuffers();
	fftw_plan fftwPlan;
	fftw_complex *fftwData;

public:
	virtual ~FFTfftwKernel();

	void initialize();
	void run();
	void dispose();
};

#endif /* FFTFFTWKERNEL_H_ */
