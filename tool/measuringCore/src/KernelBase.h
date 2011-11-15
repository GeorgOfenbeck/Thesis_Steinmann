/*
 * KernelBase.h
 *
 *  Created on: Nov 9, 2011
 *      Author: ruedi
 */

#ifndef KERNELBASE_H_
#define KERNELBASE_H_

#include "generatedC/KernelDescriptionBase.h"

class KernelBase {
	KernelDescriptionBase *kernelDescription;
public:
	KernelBase();
	virtual ~KernelBase();
};

#endif /* KERNELBASE_H_ */
