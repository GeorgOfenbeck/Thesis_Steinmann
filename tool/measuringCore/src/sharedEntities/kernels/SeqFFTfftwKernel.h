/*
 * SeqFFTfftwKernel.h
 *
 *  Created on: Oct 31, 2012
 *      Author: max
 */

#ifndef SEQFFTFFTWKERNEL_H_
#define SEQFFTFFTWKERNEL_H_

#include <fftw3.h>
#include "sharedEntities/kernels/SeqFFTfftwKernelData.h"

class SeqFFTfftwKernel: public  SeqFFTfftwKernelData {
	double dummy;
	protected:
	std::vector<std::pair<void*,long> > getBuffers();
	fftw_plan fftwPlan;
	fftw_complex *fftwDataIn;
	fftw_complex *fftwDataOut;

public:
	virtual ~SeqFFTfftwKernel();

	void initialize();
	void run();
	void dispose();
	void warmCodeCache();
};


#endif /* SEQFFTFFTWKERNEL_H_ */
