/*
 * SfoKernel.h
 *
 *  Created on: Apr 23, 2012
 *      Author: ruedi
 */

#ifndef SFOKERNEL_H_
#define SFOKERNEL_H_

#include "sharedEntities/kernels/SfoKernelData.h"

class SfoKernel : public SfoKernelData{
public:
	virtual ~SfoKernel();

protected:
	std::vector<std::pair<void*,long> > getBuffers();

public:
	void initialize();
	void run();
	void dispose();
};

#endif /* SFOKERNEL_H_ */
