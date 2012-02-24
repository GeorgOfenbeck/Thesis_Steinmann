/*
 * TriadKernel.h
 *
 *  Created on: Dec 19, 2011
 *      Author: ruedi
 */

#ifndef TRIADKERNEL_H_
#define TRIADKERNEL_H_

#include "sharedEntities/kernels/TriadKernelData.h"

class TriadKernel : public TriadKernelData{
protected:
	double *a,*b,*c;
	std::vector<std::pair<void*,long> > getBuffers();

public:

	void initialize();
	void run();
	void dispose();
};

#endif /* TRIADKERNEL_H_ */
