/*
 * MMMKernel.h
 *
 *  Created on: Jan 31, 2012
 *      Author: ruedi
 */

#ifndef MMMKERNEL_H_
#define MMMKERNEL_H_

#include "baseClasses/KernelBase.h"
#include "sharedDOM/MMMKernelDescription.h"

class MMMKernel :public Kernel<MMMKernelDescription>{
	double *a,*b,*c,*check;

	void tripleLoop(double *a, double *b, double *c){
		long size=description->getMatrixSize();
					for (int i=0; i<size; i++)
						for (int j=0; j<size; j++)
							for (int k=0; k<size; k++)
								c[i*size+j]+=a[i*size+k]*b[k*size+j];
	}
public:
	MMMKernel(MMMKernelDescription *description):Kernel(description){};

		void initialize();
		void run(){
			tripleLoop(a, b, c);
		}
		void dispose();

};

#endif /* MMMKERNEL_H_ */
