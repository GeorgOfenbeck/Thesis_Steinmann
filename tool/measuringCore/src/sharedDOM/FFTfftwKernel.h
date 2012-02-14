/*
 * FFTfftwKernel.h
 *
 *  Created on: Feb 14, 2012
 *      Author: ruedi
 */

#ifndef FFTFFTWKERNEL_H_
#define FFTFFTWKERNEL_H_

#include "sharedDOM/FFTfftwKernelData.h"

class FFTfftwKernel: public  FFTfftwKernelData{
	double dummy;
public:
	virtual ~FFTfftwKernel();

	void initialize();
	void run();
	void dispose();
	void warmCaches();
};

#endif /* FFTFFTWKERNEL_H_ */
