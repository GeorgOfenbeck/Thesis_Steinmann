/*
 * ArithmeticKernel.cpp
 *
 *  Created on: Dec 20, 2011
 *      Author: ruedi
 */

#include "ArithmeticKernel.h"

#include <cmath>

#include "macros/RMT_ARITHMETIC_OPERATION.h"
#include "macros/RMT_ARITHMETIC_INSTRUCTION_SET.h"
#include "macros/RMT_ARITHMETIC_UNROLL.h"
#include "macros/RMT_ARITHMETIC_DLP.h"

#ifdef __SSE2__
#include <emmintrin.h>
#endif

#define UNROLL RMT_ARITHMETIC_UNROLL
#define DLP RMT_ARITHMETIC_DLP

double ArithmeticKernel::getBase(double exponent, double result) {
	return exp(log(result) / exponent);
}


#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wuninitialized"

void ArithmeticKernel::run() {
	long iterations = getIterations();
	if (RMT_ARITHMETIC_OPERATION == ArithmeticOperation_ADD) {
#ifdef __SSE2__
		if (RMT_ARITHMETIC_INSTRUCTION_SET == SSE) {
			double tmp[2];
			__m128d a[DLP], c;
			double t;

			t = 1.1;

			for (int i = 0; i < DLP; i++) {
				tmp[0] = t;
				t += 0.1;
				tmp[1] = t;
				t += 0.1;
				a[i] = _mm_loadu_pd(tmp);
			}

			tmp[0] = 1;
			tmp[1] = 1;
			c = _mm_loadu_pd(tmp);

			for (long i = 0; i < iterations; i++) {
				for (int p = 0; p < UNROLL; p++) {
					for (int j = 0; j < DLP; j++) {
						a[j] = _mm_add_pd(a[j], c);
					}
				}
			}

			result = 0;
			for (int i = 0; i < DLP; i++) {
				_mm_storeu_pd(tmp, a[i]);
				result += tmp[0];
				result += tmp[1];
			}
		} else
#endif
		{
			double r[DLP];
			double t = 1.1;
			for (int i = 0; i < DLP; i++) {
				r[i] = t;
				t += 0.1;
			}
			for (long i = 0; i < iterations; i++) {
				for (int p = 0; p < UNROLL; p++) {
					for (int j = 0; j < DLP; j++) {
						r[j] += 1;
					}
				}
			}
			result = 0;
			for (int i = 0; i < DLP; i++) {
				result += r[i];
			}
		}

	}

	if (RMT_ARITHMETIC_OPERATION == ArithmeticOperation_MUL) {
		double base = getBase(iterations, 4);
#ifdef __SSE2__
		if (RMT_ARITHMETIC_INSTRUCTION_SET == SSE) {
			double tmp[2];
			__m128d a[DLP], c;
			double t;

			t = 1.1;

			for (int i = 0; i < DLP; i++) {
				tmp[0] = t;
				t += 0.1;
				tmp[1] = t;
				t += 0.1;
				a[i] = _mm_loadu_pd(tmp);
			}

			tmp[0] = base;
			tmp[1] = base;
			c = _mm_loadu_pd(tmp);

			for (long i = 0; i < iterations; i++) {
				for (int p = 0; p < UNROLL; p++) {
					for (int j = 0; j < DLP; j++) {
						a[j] = _mm_mul_pd(a[j], c);
					}
				}
			}

			result = 0;
			for (int i = 0; i < DLP; i++) {
				_mm_storeu_pd(tmp, a[i]);
				result += tmp[0];
				result += tmp[1];
			}
		} else
#endif
		{
			double r[DLP];
			double t = 1.1;
			for (int i = 0; i < DLP; i++) {
				r[i] = t;
				t += 0.1;
			}
			for (long i = 0; i < iterations; i++) {
				for (int p = 0; p < UNROLL; p++) {
					for (int j = 0; j < DLP; j++) {
						r[j] *= base;
					}
				}

			}
			result = 0;
			for (int i = 0; i < DLP; i++) {
				result += r[i];
			}
		}

	}
	if (RMT_ARITHMETIC_OPERATION == ArithmeticOperation_MULADD) {
		double base = getBase(iterations, 4);
#ifdef __SSE2__
		if (RMT_ARITHMETIC_INSTRUCTION_SET == SSE) {
			double tmp[2];
			__m128d a[DLP], c;
			__m128d aa[DLP * 2], ac;
			double t;

			t = 1.1;

			for (int i = 0; i < DLP; i++) {
				tmp[0] = t;
				t += 0.1;
				tmp[1] = t;
				t += 0.1;
				a[i] = _mm_loadu_pd(tmp);
				aa[i * 2] = a[i];
				aa[i * 2 + 1] = a[i];
			}

			tmp[0] = base;
			tmp[1] = base;
			c = _mm_loadu_pd(tmp);

			tmp[0] = 1;
			tmp[1] = 1;
			ac = _mm_loadu_pd(tmp);

			for (long i = 0; i < iterations; i++) {
				for (int p = 0; p < UNROLL; p++) {
					for (int j = 0; j < DLP; j++) {
						a[j] = _mm_mul_pd(a[j], c);
						aa[j * 2] = _mm_add_pd(aa[j * 2], ac);
						aa[j * 2 + 1] = _mm_add_pd(aa[j * 2 + 1], ac);
					}
				}
			}

			result = 0;
			for (int i = 0; i < DLP; i++) {
				_mm_storeu_pd(tmp, a[i]);
				result += tmp[0];
				result += tmp[1];
				_mm_storeu_pd(tmp, aa[i * 2]);
				result += tmp[0];
				result += tmp[1];
				_mm_storeu_pd(tmp, aa[i * 2 + 1]);
				result += tmp[0];
				result += tmp[1];
			}
		} else
#endif
		{

			double r[DLP];
			double ar[DLP * 2];
			double t = 1.1;
			for (int i = 0; i < DLP; i++) {
				r[i] = t;
				ar[2 * i] = r[1];
				ar[2 * i + 1] = r[1];
				t += 0.1;
			}

			for (long i = 0; i < iterations; i++) {
				for (int p = 0; p < UNROLL; p++) {
					for (int j = 0; j < DLP; j++) {
						r[j] *= base;
						ar[j * 2] += 1;
						ar[j * 2 + 1] += 1;
					}
				}
			}

			result = 0;
			for (int i = 0; i < DLP; i++) {
				result += r[i];
				result += ar[i * 2];
				result += ar[i * 2 + 1];
			}
		}
	}
}
#pragma GCC diagnostic pop

