/*
 * FFTfftwKernel.h
 *
 *  Created on: Feb 14, 2012
 *      Author: ruedi
 */

#ifndef FFTFFTWKERNEL_H_
#define FFTFFTWKERNEL_H_

#include "sharedEntities/kernels/FFTfftwKernelData.h"

class FFTfftwKernel: public  FFTfftwKernelData{
	double dummy;
	protected:
	std::vector<std::pair<void*,long> > getBuffers();

public:
	virtual ~FFTfftwKernel();

	void initialize();
	void run();
	void dispose();
};

#endif /* FFTFFTWKERNEL_H_ */
