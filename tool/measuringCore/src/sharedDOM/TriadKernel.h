/*
 * TriadKernel.h
 *
 *  Created on: Dec 19, 2011
 *      Author: ruedi
 */

#ifndef TRIADKERNEL_H_
#define TRIADKERNEL_H_

#include "sharedDOM/TriadKernelData.h"

class TriadKernel : public TriadKernelData{
protected:
	double *a,*b,*c;
	std::vector<std::pair<void*,long> > getBuffers();

public:

	void initialize();
	void run(){
		for (long p=0;p<1;p++){
			for (long i=0; i<getBufferSize(); i++){
				a[i]=b[i]+2.34*c[i];
			}
		}
	}
	void dispose();
};

#endif /* TRIADKERNEL_H_ */
