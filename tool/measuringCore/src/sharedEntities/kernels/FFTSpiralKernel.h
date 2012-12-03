/*
 * FFTSpiralKernel.h
 *
 *  Created on: Feb 22, 2012
 *      Author: ruedi
 */

#ifndef FFTSPIRALKERNEL_H_
#define FFTSPIRALKERNEL_H_

#include "sharedEntities/kernels/FFTSpiralKernelData.h"
class FFTSpiralKernel: public FFTSpiralKernelData {
	double *pSrc;
	double *pDst;
protected:
	std::vector<std::pair<void*,long> > getBuffers();

public:
	virtual ~FFTSpiralKernel();

	void initialize();
	void run();
	void dispose();
	void warmCodeCache();
};

#endif /* FFTSPIRALKERNEL_H_ */
