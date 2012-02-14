/*
 * FFTnrKernel.h
 *
 *  Created on: Feb 14, 2012
 *      Author: ruedi
 */

#ifndef FFTNRKERNEL_H_
#define FFTNRKERNEL_H_

#include "sharedDOM/FFTnrKernelData.h"
class FFTnrKernel: public FFTnrKernelData {
	double *doubleData;
	double dummy;
public:
	virtual ~FFTnrKernel();

	void initialize();
		void run();
		void dispose();
		void warmCaches();
};

#endif /* FFTNRKERNEL_H_ */
