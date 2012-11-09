/*
 * SeqFFTfftwSplitKernel.h
 *
 *  Created on: Nov 6, 2012
 *      Author: max
 */

#ifndef SEQFFTFFTWSPLITKERNEL_H_
#define SEQFFTFFTWSPLITKERNEL_H_

#include <fftw3.h>
#include "sharedEntities/kernels/SeqFFTfftwSplitKernelData.h"

class SeqFFTfftwSplitKernel : public SeqFFTfftwSplitKernelData {
	double dummy;
	protected:
	std::vector<std::pair<void*,long> > getBuffers();
	fftw_plan fftwPlan;
	fftw_iodim Dim;
	double* ri, *ii, *ro, *io;

public:
	virtual ~SeqFFTfftwSplitKernel();


	void initialize();
	void run();
	void dispose();
	void warmCodeCache();
};

#endif /* SEQFFTFFTWSPLITKERNEL_H_ */
