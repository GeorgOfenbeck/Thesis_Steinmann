/*
 * SpiralSKernel.h
 *
 *  Created on: Feb 20, 2012
 *      Author: ruedi
 */
#ifndef SPIRALSKERNEL_H_
#define SPIRALSKERNEL_H_
#include "sharedEntities/kernels/SpiralSKernelData.h"
typedef struct {
        double* input;
        double* output;
} spiral_t;

class SpiralSKernel: public SpiralSKernelData {
	spiral_t dummy;
	double *x,*y;
	double alpha;
protected:
	std::vector<std::pair<void*, long> > getBuffers();
public:
	virtual ~SpiralSKernel();
	void initialize();
	void run();
	void dispose();
	void warmCodeCache();
};

#endif /* SPIRALSKERNEL_H_ */
