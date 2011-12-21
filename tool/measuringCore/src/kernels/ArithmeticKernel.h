/*
 * ArithmeticKernel.h
 *
 *  Created on: Dec 20, 2011
 *      Author: ruedi
 */

#ifndef ARITHMETICKERNEL_H_
#define ARITHMETICKERNEL_H_

#include "baseClasses/KernelBase.h"
#include "generatedC/ArithmeticKernelDescription.h"
#include <cmath>
#include <xmmintrin.h>
#include <emmintrin.h>

enum ArithmeticOperation{
	ADD,
	MUL,
};

#ifndef RMT_ARITHMETIC_OPERATION
#define RMT_ARITHMETIC_OPERATION ADD
#endif

class ArithmeticKernel : public Kernel<ArithmeticKernelDescription>{
	// solves base**exponent=result, with b unknown
	double getBase(double exponent, double result);

public:
	double result;
	ArithmeticKernel(ArithmeticKernelDescription *description):Kernel(description){};

	void initialize(){};
	void run(){
		long iterations=description->getIterations();

		double base=getBase(iterations,4);

#ifdef RMT_UNROLL4
		double tmp[2];
		__m128d a,b,c;
		tmp[0]=1.1;
		tmp[1]=1.2;
		a=_mm_loadu_pd(tmp);
		tmp[0]=1.3;
		tmp[1]=1.4;
		b=_mm_loadu_pd(tmp);

		tmp[0]=1;
		tmp[1]=1;
		c=_mm_loadu_pd(tmp);
		//double r1=1.1, r2=1.2, r3=1.3, r4=1.4;
#else
		double r=1;
#endif

		for (long i=0; i<iterations; i++){
			switch (RMT_ARITHMETIC_OPERATION){
				case ADD:
#ifdef RMT_UNROLL4
					/*r1+=1;
					r2+=1;
					r3+=1;
					r4+=1;*/
					a=_mm_add_pd(a,c);
					b=_mm_add_pd(b,c);
#else
					r+=1;
#endif
					break;
				case MUL:
#ifdef RMT_UNROLL4
					/*r1*=base;
					r2*=base;
					r3*=base;
					r4*=base;*/
#else
					r*=base;
#endif
				break;
			}
		}
#ifdef RMT_UNROLL4
		//result=r1+r2+r3+r4;
		_mm_storeu_pd(tmp,a);
		result=tmp[0];
		result+=tmp[1];
		_mm_storeu_pd(tmp,b);
		result+=tmp[0];
		result+=tmp[1];
#else
		result=r;
#endif
	}
	void dispose(){};
};

#endif /* ARITHMETICKERNEL_H_ */
