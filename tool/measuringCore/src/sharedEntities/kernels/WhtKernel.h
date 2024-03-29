/*
 * WhtKernel.h
 *
 *  Created on: Feb 22, 2012
 *      Author: ruedi
 */

#ifndef WHTKERNEL_H_
#define WHTKERNEL_H_
#include "sharedEntities/kernels/WhtKernelData.h"

extern "C" {
#include <spiral_wht.h>
}

class WhtKernel: public WhtKernelData {
	wht_value *buffer;
	Wht *tree;
protected:
	std::vector<std::pair<void*, long> > getBuffers();
public:
	virtual ~WhtKernel();
	void initialize();
	void run();
	void dispose();
	void warmCodeCache(){run();}
};

#endif /* WHTKERNEL_H_ */
