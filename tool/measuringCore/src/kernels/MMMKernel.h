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
		for (long i=0; i<size; i++)
			for (long j=0; j<size; j++)
				for (long k=0; k<size; k++)
					c[i*size+j]+=a[i*size+k]*b[k*size+j];
	}

#define BLOCK 8
	void blocked(double *a, double *b, double *c){
		long size=description->getMatrixSize();
		for (int i=0; i<size; i+=BLOCK)
			for (int j=0; j<size; j+=BLOCK)
				for (int k=0; k<size; k+=BLOCK)
					for (int id=i; id<i+BLOCK; id++)
						for (int jd=j; jd<j+BLOCK; jd++)
							for (int kd=k; kd<k+BLOCK; kd++)
								c[id*size+jd]+=a[id*size+kd]*b[kd*size+jd];
	}
public:
	MMMKernel(MMMKernelDescription *description):Kernel(description){};

		void initialize();
		void run(){
			blocked(a, b, c);
		}
		void dispose();

};

#endif /* MMMKERNEL_H_ */
