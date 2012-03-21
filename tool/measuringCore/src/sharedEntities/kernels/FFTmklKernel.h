/*
 * FFTmklKernel.h
 *
 *  Created on: Feb 14, 2012
 *      Author: ruedi
 */

#ifndef FFTMKLKERNEL_H_
#define FFTMKLKERNEL_H_

#include "sharedEntities/kernels/FFTmklKernelData.h"
#include "mkl_dfti.h"

class FFTmklKernel : public FFTmklKernelData {
	double _Complex *complexData;
	DFTI_DESCRIPTOR_HANDLE mklDescriptor;
	double _Complex dummy;
	protected:
	std::vector<std::pair<void*,long> > getBuffers();

public:
	virtual ~FFTmklKernel();

	void initialize();
	void run();
	void dispose();
	void warmCodeCache();
};

#endif /* FFTMKLKERNEL_H_ */
