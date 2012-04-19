/*
 * IntroductionExample.h
 *
 *  Created on: Apr 19, 2012
 *      Author: ruedi
 */

#ifndef INTRODUCTIONEXAMPLEKERNEL_H_
#define INTRODUCTIONEXAMPLEKERNEL_H_

#include "sharedEntities/kernels/IntroductionExampleKernelData.h"

class IntroductionExampleKernel: public IntroductionExampleKernelData {
protected:
	double *buffer;
	std::vector<std::pair<void*, long> > getBuffers();
public:
	virtual ~IntroductionExampleKernel();

	void initialize();
	void run();
	void dispose();
};

#endif /* INTRODUCTIONEXAMPLEKERNEL_H_ */
