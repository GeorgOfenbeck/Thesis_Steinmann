/*
 * DummyKernel.h
 *
 *  Created on: Dec 19, 2011
 *      Author: ruedi
 */

#ifndef DUMMYKERNEL_H_
#define DUMMYKERNEL_H_

#include "generatedC/DummyKernelDescription.h"
#include "baseClasses/KernelBase.h"

class DummyKernel :public Kernel<DummyKernelDescription>{
public:
	DummyKernel(DummyKernelDescription *description):Kernel(description){};

	void initialize(){}
	void run(){
	}
	void dispose(){}
};

#endif /* DUMMYKERNEL_H_ */
