/*
 * LoadMemoryKernel.h
 *
 *  Created on: Nov 9, 2011
 *      Author: ruedi
 */

#ifndef LOADMEMORYKERNEL_H_
#define LOADMEMORYKERNEL_H_
#include "baseClasses/KernelBase.h"
#include "generatedC/MemoryLoadKernelDescription.h"

class MemoryLoadKernel : public Kernel<MemoryLoadKernelDescription>{
protected:
	double *a,*b,*c;
	double result;
public:
	MemoryLoadKernel(MemoryLoadKernelDescription *description):Kernel(description){};

	void initialize();
	void run(){
		result=0;
		for (long p=0;p<1;p++){
			for (long i=0; i<description->getBufferSize(); i++){
				result+=a[i];
				//a[i]=b[i]+2.34*c[i];
				//result=result^buffer[i];
			}
		}
		//result=a[567];
	}
	void dispose();
};

class derivedTmp : public MemoryLoadKernel{
public:
	derivedTmp(MemoryLoadKernelDescription *description)
	:MemoryLoadKernel(description){}

	void run(){result=0;}
};
#endif /* LOADMEMORYKERNEL_H_ */
