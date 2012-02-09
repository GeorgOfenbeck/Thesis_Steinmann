/*
 * DummyKernel.h
 *
 *  Created on: Dec 19, 2011
 *      Author: ruedi
 */

#ifndef DUMMYKERNEL_H_
#define DUMMYKERNEL_H_

#include "sharedDOM/DummyKernelData.h"

class DummyKernel :public DummyKernelData{
public:
	void initialize(){}
	void run(){
	}
	void dispose(){}
};

#endif /* DUMMYKERNEL_H_ */
