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
public:
	typedef KernelDescriptionBase tDescriptionBase;
	KernelBase();
	virtual ~KernelBase();

	virtual KernelDescriptionBase *getKernelDescription()=0;
};

template<class TDescription>
class Kernel : public KernelBase{
	TDescription *description;
public:
	typedef TDescription tDescription;
	typedef KernelBase tBase;

	Kernel(TDescription *description){
		this->description=description;
	}

	KernelDescriptionBase *getKernelDescription(){
		return description;
	}
};
#endif /* KERNELBASE_H_ */
