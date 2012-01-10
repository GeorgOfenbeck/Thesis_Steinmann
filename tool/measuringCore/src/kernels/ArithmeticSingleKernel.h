/*
 * ArithmeticKernel.h
 *
 *  Created on: Dec 20, 2011
 *      Author: ruedi
 */

#ifndef ARITHMETICSINGLEKERNEL_H_
#define ARITHMETICSINGLEKERNEL_H_

#include "macros/RMT_ARITHMETIC_OPERATION.h"
#include "baseClasses/KernelBase.h"
#include "sharedDOM/ArithmeticSingleKernelDescription.h"
#include <cmath>

#ifdef __SSE2__
#include <emmintrin.h>
#endif

enum ArithmeticOperation {
	ArithmeticOperation_ADD, ArithmeticOperation_MUL, ArithmeticOperation_MULADD,
};

enum Unroll {
	Unroll_None, Unroll_2, Unroll_4, Unroll_8, Unroll_16, Unroll_32
};

class ArithmeticSingleKernel: public Kernel<ArithmeticSingleKernelDescription> {
	// solves base**exponent=result, with b unknown
	static double getBase(double exponent, double result);

	template<int UNROLL>
	struct addHelper {

		static double doIt(long iterations) {
			double result;
#ifdef __SSE2__
			float tmp[4];
			__m128 a[UNROLL/4], c;
			float t;

			t=1.1;

			for (int i=0; i<UNROLL/4; i++) {
				tmp[0] = t;
				t+=0.1;
				tmp[1] = t;
				t+=0.1;
				tmp[2] = t;
				t+=0.1;
				tmp[3] = t;
				t+=0.1;
				a[i] = _mm_loadu_ps(tmp);
			}

			tmp[0] = 1;
			tmp[1] = 1;
			tmp[2] = 1;
			tmp[3] = 1;
			c = _mm_loadu_ps(tmp);

			for (long i = 0; i < iterations; i++) {
				for (int j=0;j<UNROLL/4; j++) {
					a[j] = _mm_add_ps(a[j], c);
				}
			}

			result=0;
			for (int i=0; i<UNROLL/4; i++) {
				_mm_storeu_ps(tmp, a[i]);
				result += tmp[0];
				result += tmp[1];
				result += tmp[2];
				result += tmp[3];
			}
#else
			float r[UNROLL];
			float t = 1.1;
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
			return result;
		}
	};

	template<int UNROLL>
	struct mulHelper {

		static double doIt(long iterations) {
			double result;
			double base = getBase(iterations, 4);
#ifdef __SSE2__
			float tmp[4];
			__m128 a[UNROLL/4], c;
			float t;

			t=1.1;

			for (int i=0; i<UNROLL/4; i++) {
				tmp[0] = t;
				t+=0.1;
				tmp[1] = t;
				t+=0.1;
				tmp[2] = t;
				t+=0.1;
				tmp[3] = t;
				t+=0.1;
				a[i] = _mm_loadu_ps(tmp);
			}

			tmp[0] = base;
			tmp[1] = base;
			tmp[2] = base;
			tmp[3] = base;
			c = _mm_loadu_ps(tmp);

			for (long i = 0; i < iterations; i++) {
				for (int j=0;j<UNROLL/4; j++) {
					a[j] = _mm_mul_ps(a[j], c);
				}
			}

			result=0;
			for (int i=0; i<UNROLL/4; i++) {
				_mm_storeu_ps(tmp, a[i]);
				result += tmp[0];
				result += tmp[1];
				result += tmp[2];
				result += tmp[3];
			}
#else
			float r[UNROLL];
			float t = 1.1;
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
			return result;
		}
	};

	template<int UNROLL>
	struct addMulHelper {

		static double doIt(long iterations) {
			double result;
			float base = getBase(iterations, 4);
#ifdef __SSE2__
			float tmp[4];
			__m128 a[UNROLL/4], c;
			__m128 aa[UNROLL/4], ac;
			float t;

			t=1.1;

			for (int i=0; i<UNROLL/4; i++) {
				tmp[0] = t;
				t+=0.1;
				tmp[1] = t;
				t+=0.1;
				tmp[2] = t;
				t+=0.1;
				tmp[3] = t;
				t+=0.1;
				a[i] = _mm_loadu_ps(tmp);
				aa[i]=a[i];
			}

			tmp[0] = base;
			tmp[1] = base;
			tmp[2] = base;
			tmp[3] = base;
			c = _mm_loadu_ps(tmp);

			tmp[0] = 1;
			tmp[1] = 1;
			tmp[2] = 1;
			tmp[3] = 1;
			ac = _mm_loadu_ps(tmp);

			for (long i = 0; i < iterations; i++) {
				for (int j=0;j<UNROLL/4; j++) {
					a[j] = _mm_mul_ps(a[j], c);
					aa[j] = _mm_add_ps(aa[j], ac);
				}
			}

			result=0;
			for (int i=0; i<UNROLL/4; i++) {
				_mm_storeu_ps(tmp, a[i]);
				result += tmp[0];
				result += tmp[1];
				result += tmp[2];
				result += tmp[3];
				_mm_storeu_ps(tmp, aa[i]);
				result += tmp[0];
				result += tmp[1];
				result += tmp[2];
				result += tmp[3];
			}
#else
			float r[UNROLL];
			float ar[UNROLL];
			float t = 1.1;
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
			return result;
		}
	};

	template<template<int I> class T, int UNROLL>
	struct invocationHelper {
		static double doIt(int unroll, long iterations) {
			if (unroll == UNROLL) {
				return T<UNROLL>::doIt(iterations);
			} else {
				return invocationHelper<T, UNROLL / 2>::doIt(unroll, iterations);
			}
		}
	};

	template<template<int I> class T>
	struct invocationHelper<T, 4> {
		static double doIt(int unroll, long iterations) {
			if (unroll == 4) {
				return T<4>::doIt(iterations);
			}
			printf("Unroll: %i\n", unroll);
			throw "unsupported unroll specified -->" + unroll;
		}
	};

	static const int maxUnroll = 128;
public:
	double result;
	ArithmeticSingleKernel(ArithmeticSingleKernelDescription * description) :
			Kernel(description) {
	}
	;

	void initialize() {
	}
	;
	void run() {
		long iterations = description->getIterations();
		int unroll = description->getUnroll();

		if (RMT_ARITHMETIC_OPERATION == ArithmeticOperation_ADD) {

			if (unroll < 4) {
				double r = 1;
				for (long i = 0; i < iterations; i++) {
					r += 1;
				}
				result = r;
			} else {
				result = invocationHelper<addHelper, maxUnroll>::doIt(
						description->getUnroll(), iterations);
			}
		}

		if (RMT_ARITHMETIC_OPERATION == ArithmeticOperation_MUL) {
			double base = getBase(iterations, 4);
			if (unroll < 4) {
				double r = 1;
				for (long i = 0; i < iterations; i++) {
					r *= base;
				}
				result = r;
			} else {
				result = invocationHelper<mulHelper, maxUnroll>::doIt(
						description->getUnroll(), iterations);
			}
		}

		if (RMT_ARITHMETIC_OPERATION == ArithmeticOperation_MULADD) {
			double base = getBase(iterations, 4);
			if (unroll < 4) {
				double r = 1, ra = 1;
				for (long i = 0; i < iterations; i++) {
					r *= base;
					ra += 1;
				}
				result = r + ra;
			} else {
				result = invocationHelper<addMulHelper, maxUnroll>::doIt(
						description->getUnroll(), iterations);
			}
		}
	}
	void dispose() {
	}

}
;

#endif /* ARITHMETICKERNEL_H_ */
