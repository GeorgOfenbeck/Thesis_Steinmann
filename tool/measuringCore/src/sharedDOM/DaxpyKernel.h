/*
 * DaxpyKernel.h
 *
 *  Created on: Feb 20, 2012
 *      Author: ruedi
 */

#ifndef DAXPYKERNEL_H_
#define DAXPYKERNEL_H_

#include "sharedDOM/DaxpyKernelData.h"
class DaxpyKernel: public DaxpyKernelData {
	double *x,*y;
	double alpha;
protected:
	std::vector<std::pair<void*, long> > getBuffers();
public:
	virtual ~DaxpyKernel();
	void initialize();
	void run();
	void dispose();
};

#endif /* DAXPYKERNEL_H_ */
