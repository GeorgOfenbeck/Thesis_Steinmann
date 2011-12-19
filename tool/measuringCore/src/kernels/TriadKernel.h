/*
 * TriadKernel.h
 *
 *  Created on: Dec 19, 2011
 *      Author: ruedi
 */

#ifndef TRIADKERNEL_H_
#define TRIADKERNEL_H_

#include "generatedC/TriadKernelDescription.h"
#include "baseClasses/KernelBase.h"

class TriadKernel : public Kernel<TriadKernelDescription>{
protected:
	double *a,*b,*c;
public:
	TriadKernel(TriadKernelDescription *description):Kernel(description){};

	void initialize();
	void run(){
		for (long p=0;p<1;p++){
			for (long i=0; i<description->getBufferSize(); i++){
				a[i]=b[i]+2.34*c[i];
			}
		}
	}
	void dispose();
};

#endif /* TRIADKERNEL_H_ */
