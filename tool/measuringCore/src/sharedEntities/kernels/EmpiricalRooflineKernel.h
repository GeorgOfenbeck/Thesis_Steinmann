/*
 * IntroductionExample.h
 *
 *  Created on: Apr 19, 2012
 *      Author: ruedi
 */

#ifndef EMPIRICALROOFLINEKERNEL_H_
#define EMPIRICALROOFLINEKERNEL_H_

#include "sharedEntities/kernels/EmpiricalRooflineKernelData.h"

class EmpiricalRooflineKernel: public EmpiricalRooflineKernelData {
protected:
	double *buffer;
	std::vector<std::pair<void*, long> > getBuffers();
public:
	virtual ~EmpiricalRooflineKernel();

	void initialize();
	void run();
	void dispose();
};

#endif /* INTRODUCTIONEXAMPLEKERNEL_H_ */
