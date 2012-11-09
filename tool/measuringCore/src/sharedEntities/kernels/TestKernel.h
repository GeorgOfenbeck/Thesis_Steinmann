/*
 * TestKernel.h
 *
 *  Created on: Feb 20, 2012
 *      Author: ruedi
 */

#ifndef TESTKERNEL_H_
#define TESTKERNEL_H_

#include "sharedEntities/kernels/TestKernelData.h"
class TestKernel: public TestKernelData {
	double *x;
protected:
	std::vector<std::pair<void*, long> > getBuffers();
public:
	virtual ~TestKernel();
	void initialize();
	void run();
	void dispose();
	void warmCodeCache();
};

#endif /* TESTKERNEL_H_ */
