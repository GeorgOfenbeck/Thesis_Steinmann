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

#ifdef __SSE2__
#include <emmintrin.h>
#endif

enum ArithmeticOperation {
	ArithmeticOperation_ADD, ArithmeticOperation_MUL,
};

enum Unroll {
	Unroll_None, Unroll_2, Unroll_4
};

#ifndef RMT_ARITHMETIC_OPERATION
#define RMT_ARITHMETIC_OPERATION ArithmeticOperation_ADD
#endif

#ifndef RMT_UNROLL
#define RMT_UNROLL Unroll_None
#endif
class ArithmeticKernel: public Kernel<ArithmeticKernelDescription> {
	// solves base**exponent=result, with b unknown
	double getBase(double exponent, double result);

public:
	double result;
	ArithmeticKernel(ArithmeticKernelDescription *description) :
			Kernel(description) {
	}
	;

	void initialize() {
	}
	;
	void run() {
		long iterations = description->getIterations();

		if (RMT_ARITHMETIC_OPERATION == ArithmeticOperation_ADD) {
			if (RMT_UNROLL == Unroll_None) {
				double r = 1;
				for (long i = 0; i < iterations; i++) {
					r += 1;
				}
				result = r;
			}
			if (RMT_UNROLL == Unroll_2) {
#ifdef __SSE2__
				double tmp[2];
				__m128d a, c;

				tmp[0] = 1.1;
				tmp[1] = 1.2;
				a = _mm_loadu_pd(tmp);

				tmp[0] = 1;
				tmp[1] = 1;
				c = _mm_loadu_pd(tmp);

				for (long i = 0; i < iterations; i++) {
					a = _mm_add_pd(a, c);
				}

				_mm_storeu_pd(tmp, a);
				result = tmp[0];
				result += tmp[1];
#else
				double r1 = 1.1, r2 = 1.2;
				for (long i = 0; i < iterations; i++) {
					r1 += 1;
					r2 += 1;
				}
				result = r1 + r2;
#endif
			}
			if (RMT_UNROLL == Unroll_4) {
#ifdef __SSE2__
				double tmp[2];
				__m128d a,b, c;

				tmp[0] = 1.1;
				tmp[1] = 1.2;
				a = _mm_loadu_pd(tmp);

				tmp[0] = 1.3;
				tmp[1] = 1.4;
				b = _mm_loadu_pd(tmp);

				tmp[0] = 1;
				tmp[1] = 1;
				c = _mm_loadu_pd(tmp);

				for (long i = 0; i < iterations; i++) {
					a = _mm_add_pd(a, c);
					b = _mm_add_pd(b, c);
				}

				_mm_storeu_pd(tmp, a);
				result = tmp[0];
				result += tmp[1];

				_mm_storeu_pd(tmp, b);
				result += tmp[0];
				result += tmp[1];
#else
				double r1 = 1.1, r2 = 1.2, r3 = 1.3, r4 = 1.4;
				for (long i = 0; i < iterations; i++) {
					r1 += 1;
					r2 += 1;
					r3 += 1;
					r4 += 1;
				}
				result = r1 + r2 + r3 + r4;
#endif
			}

		}

		if (RMT_ARITHMETIC_OPERATION == ArithmeticOperation_MUL) {
			double base = getBase(iterations, 4);
			if (RMT_UNROLL == Unroll_None) {
				double r = 1;
				for (long i = 0; i < iterations; i++) {
					r *= base;
				}
				result = r;
			}
			if (RMT_UNROLL == Unroll_2) {
#ifdef __SSE2__
				double tmp[2];
				__m128d a, c;

				tmp[0] = 1.1;
				tmp[1] = 1.2;
				a = _mm_loadu_pd(tmp);

				tmp[0] = base;
				tmp[1] = base;
				c = _mm_loadu_pd(tmp);

				for (long i = 0; i < iterations; i++) {
					a = _mm_mul_pd(a, c);
				}

				_mm_storeu_pd(tmp, a);
				result = tmp[0];
				result += tmp[1];
#else
				double r1 = 1.1, r2 = 1.2;
				for (long i = 0; i < iterations; i++) {
					r1 *= base;
					r2 *= base;
				}
				result = r1 + r2;
#endif
			}
			if (RMT_UNROLL == Unroll_4) {
#ifdef __SSE2__
				double tmp[2];
				__m128d a,b, c;

				tmp[0] = 1.1;
				tmp[1] = 1.2;
				a = _mm_loadu_pd(tmp);

				tmp[0] = 1.3;
				tmp[1] = 1.4;
				b = _mm_loadu_pd(tmp);

				tmp[0] = base;
				tmp[1] = base;
				c = _mm_loadu_pd(tmp);

				for (long i = 0; i < iterations; i++) {
					a = _mm_mul_pd(a, c);
					b = _mm_mul_pd(b, c);
				}

				_mm_storeu_pd(tmp, a);
				result = tmp[0];
				result += tmp[1];

				_mm_storeu_pd(tmp, b);
				result += tmp[0];
				result += tmp[1];
#else
				double r1 = 1.1, r2 = 1.2, r3 = 1.3, r4 = 1.4;
				for (long i = 0; i < iterations; i++) {
					r1 *= base;
					r2 *= base;
					r3 *= base;
					r4 *= base;
				}
				result = r1 + r2 + r3 + r4;
#endif
			}
		}
	}
	void dispose() {
	}
	;
};

#endif /* ARITHMETICKERNEL_H_ */
