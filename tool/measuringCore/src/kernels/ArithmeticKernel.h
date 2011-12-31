/*
 * ArithmeticKernel.h
 *
 *  Created on: Dec 20, 2011
 *      Author: ruedi
 */

#ifndef ARITHMETICKERNEL_H_
#define ARITHMETICKERNEL_H_

#include "baseClasses/KernelBase.h"
#include "sharedDOM/ArithmeticKernelDescription.h"
#include <cmath>
#include "macros/RMT_ARITHMETIC_OPERATION.h"

#ifdef __SSE2__
#include <emmintrin.h>
#endif

enum ArithmeticOperation {
	ArithmeticOperation_ADD, ArithmeticOperation_MUL, ArithmeticOperation_MULADD,
};

enum Unroll {
	Unroll_None, Unroll_2, Unroll_4, Unroll_8
};

#ifndef RMT_UNROLL
#define RMT_UNROLL Unroll_8
#endif
class ArithmeticKernel: public Kernel<ArithmeticKernelDescription> {
	// solves base**exponent=result, with b unknown
	double getBase(double exponent, double result);

	template<int UNROLL>
	void addHelper(long iterations) {
#ifdef __SSE2__
		double tmp[2];
		__m128d a[UNROLL/2], c;
		double t;

		t=1.1;

		for (int i=0; i<UNROLL/2; i++) {
			tmp[0] = t;
			t+=0.1;
			tmp[1] = t;
			t+=0.1;
			a[i] = _mm_loadu_pd(tmp);
		}

		tmp[0] = 1;
		tmp[1] = 1;
		c = _mm_loadu_pd(tmp);

		for (long i = 0; i < iterations; i++) {
			for (int j=0;j<UNROLL/2; j++) {
				a[j] = _mm_add_pd(a[j], c);
			}
		}

		result=0;
		for (int i=0; i<UNROLL/2; i++) {
			_mm_storeu_pd(tmp, a[i]);
			result += tmp[0];
			result += tmp[1];
		}
#else
		double r[UNROLL];
		double t = 1.1;
		for (int i = 0; i < UNROLL; i++) {
			r[i] = t;
			t += 0.1;
		}
		for (long i = 0; i < iterations; i++) {
			for (int j = 0; j < UNROLL; j++) {
				r[j] += 1;
			}

		}
		result = 0;
		for (int i = 0; i < UNROLL; i++) {
			result += r[i];
		}

#endif
	}

	template<int UNROLL>
	void mulHelper(long iterations) {
		double base = getBase(iterations, 4);
#ifdef __SSE2__
		double tmp[2];
		__m128d a[UNROLL/2], c;
		double t;

		t=1.1;

		for (int i=0; i<UNROLL/2; i++) {
			tmp[0] = t;
			t+=0.1;
			tmp[1] = t;
			t+=0.1;
			a[i] = _mm_loadu_pd(tmp);
		}

		tmp[0] = base;
		tmp[1] = base;
		c = _mm_loadu_pd(tmp);

		for (long i = 0; i < iterations; i++) {
			for (int j=0;j<UNROLL/2; j++) {
				a[j] = _mm_mul_pd(a[j], c);
			}
		}

		result=0;
		for (int i=0; i<UNROLL/2; i++) {
			_mm_storeu_pd(tmp, a[i]);
			result += tmp[0];
			result += tmp[1];
		}
#else
		double r[UNROLL];
		double t = 1.1;
		for (int i = 0; i < UNROLL; i++) {
			r[i] = t;
			t += 0.1;
		}
		for (long i = 0; i < iterations; i++) {
			for (int j = 0; j < UNROLL; j++) {
				r[j] *= base;
			}

		}
		result = 0;
		for (int i = 0; i < UNROLL; i++) {
			result += r[i];
		}

#endif
	}

	template<int UNROLL>
	void addMulHelper(long iterations) {
		double base = getBase(iterations, 4);
#ifdef __SSE2__
		double tmp[2];
		__m128d a[UNROLL/2], c;
		__m128d aa[UNROLL/2], ac;
		double t;

		t=1.1;

		for (int i=0; i<UNROLL/2; i++) {
			tmp[0] = t;
			t+=0.1;
			tmp[1] = t;
			t+=0.1;
			a[i] = _mm_loadu_pd(tmp);
			aa[i]=a[i];
		}

		tmp[0] = base;
		tmp[1] = base;
		c = _mm_loadu_pd(tmp);

		tmp[0] = 1;
		tmp[1] = 1;
		ac = _mm_loadu_pd(tmp);

		for (long i = 0; i < iterations; i++) {
			for (int j=0;j<UNROLL/2; j++) {
				a[j] = _mm_mul_pd(a[j], c);
				aa[j] = _mm_add_pd(aa[j], ac);
			}
		}

		result=0;
		for (int i=0; i<UNROLL/2; i++) {
			_mm_storeu_pd(tmp, a[i]);
			result += tmp[0];
			result += tmp[1];
			_mm_storeu_pd(tmp, aa[i]);
			result += tmp[0];
			result += tmp[1];
		}
#else
		double r[UNROLL];
		double ar[UNROLL];
		double t = 1.1;
		for (int i = 0; i < UNROLL; i++) {
			r[i] = t;
			ar[i] = r[1];
			t += 0.1;
		}
		for (long i = 0; i < iterations; i++) {
			for (int j = 0; j < UNROLL; j++) {
				r[j] *= base;
				ar[j] += 1;
			}

		}
		result = 0;
		for (int i = 0; i < UNROLL; i++) {
			result += r[i];
			result += ar[i];
		}

#endif
	}

public:
	double result;
	ArithmeticKernel(ArithmeticKernelDescription * description) :
			Kernel(description) {
	}
	;

	void initialize() {
	}
	;
	void run() {
		long iterations = description->getIterations();

		if (RMT_ARITHMETIC_OPERATION == ArithmeticOperation_ADD) {

			if (description->getUnroll()==1) {
				double r = 1;
				for (long i = 0; i < iterations; i++) {
					r += 1;
				}
				result = r;
			}

			if (description->getUnroll()==2) {
				addHelper<2>(iterations);
			}
			if (description->getUnroll()==4) {
				addHelper<4>(iterations);
			}
			if (description->getUnroll()==8) {
				addHelper<8>(iterations);
			}
		}

		if (RMT_ARITHMETIC_OPERATION == ArithmeticOperation_MUL) {
			double base = getBase(iterations, 4);
			if (description->getUnroll()==1) {
				double r = 1;
				for (long i = 0; i < iterations; i++) {
					r *= base;
				}
				result = r;
			}
			if (description->getUnroll()==2) {
				mulHelper<2>(iterations);
			}
			if (description->getUnroll()==4) {
				mulHelper<4>(iterations);
			}
			if (description->getUnroll()==8) {
				mulHelper<8>(iterations);
			}
		}

		if (RMT_ARITHMETIC_OPERATION == ArithmeticOperation_MULADD) {
			double base = getBase(iterations, 4);
			if (description->getUnroll()==1) {
				double r = 1, ra=1;
				for (long i = 0; i < iterations; i++) {
					r *= base;
					ra+=1;
				}
				result = r+ra;
			}
			if (description->getUnroll()==2) {
				addMulHelper<2>(iterations);
			}
			if (description->getUnroll()==4) {
				addMulHelper<4>(iterations);
			}
			if (description->getUnroll()==8) {
				addMulHelper<8>(iterations);
			}
		}
	}
	void dispose() {
	}

}
;

#endif /* ARITHMETICKERNEL_H_ */
