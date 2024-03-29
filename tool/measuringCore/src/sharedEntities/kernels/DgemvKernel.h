/*
 * DgemvKernel.h
 *
 *  Created on: Feb 20, 2012
 *      Author: ruedi
 */

#ifndef DGEMVKERNEL_H_
#define DGEMVKERNEL_H_
#include "sharedEntities/kernels/DgemvKernelData.h"
class DgemvKernel: public DgemvKernelData {
private:
	double *a,*x,*y;
	double alpha,beta;
protected:
	std::vector<std::pair<void*, long> > getBuffers();
public:
	virtual ~DgemvKernel();
	void initialize();
	void run();
	void dispose();
	void warmCodeCache();
};

#endif /* DGEMVKERNEL_H_ */
