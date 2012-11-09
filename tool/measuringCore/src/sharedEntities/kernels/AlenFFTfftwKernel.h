/*
 * AlenFFTfftwKernel.h
 *
 *  Created on: Nov 1, 2012
 *      Author: max
 */

#ifndef ALENFFTFFTWKERNEL_H_
#define ALENFFTFFTWKERNEL_H_

#include "sharedEntities/kernels/AlenFFTfftwKernelData.h"

typedef double* (*fPtr)(double*);

class AlenFFTfftwKernelData;

class AlenFFTfftwKernel : public AlenFFTfftwKernelData {

protected:
	std::vector<std::pair<void*,long> > getBuffers();
	double* input;
	double* output;
	fPtr	fp[66];

public:

	virtual ~AlenFFTfftwKernel();
	void initialize();
	void run();
	void dispose();
};

#endif /* ALENFFTFFTWKERNEL_H_ */
