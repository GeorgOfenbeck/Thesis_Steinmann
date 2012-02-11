/*
 * ArithmeticKernel.cpp
 *
 *  Created on: Dec 20, 2011
 *      Author: ruedi
 */

#include "ArithmeticSingleKernel.h"

#include "macros/RMT_ARITHMETIC_OPERATION.h"
#include "macros/RMT_ARITHMETIC_INSTRUCTION_SET.h"
#include "macros/RMT_ARITHMETIC_UNROLL.h"
#include "macros/RMT_ARITHMETIC_DLP.h"

#ifdef __SSE2__
#include <emmintrin.h>
#endif

#define UNROLL RMT_ARITHMETIC_UNROLL
#define DLP RMT_ARITHMETIC_DLP

#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wuninitialized"
void ArithmeticSingleKernel::run() {
	uint64_t iterations=getIterations();
	if (RMT_ARITHMETIC_OPERATION == ArithmeticOperation_ADD) {
#ifdef __SSE__
		if (RMT_ARITHMETIC_INSTRUCTION_SET == SSE) {
			float tmp[4];
			__m128 a[DLP], c;
			float t;

			t = 1.1;

			for (int i = 0; i < DLP; i++) {
				tmp[0] = t;
				t += 0.1;
				tmp[1] = t;
				t += 0.1;
				tmp[2] = t;
				t += 0.1;
				tmp[3] = t;
				t += 0.1;
				a[i] = _mm_loadu_ps(tmp);
			}

			tmp[0] = 1;
			tmp[1] = 1;
			tmp[2] = 1;
			tmp[3] = 1;
			c = _mm_loadu_ps(tmp);

			for (long i = 0; i < iterations; i++) {
				for (int p = 0; p < UNROLL; p++) {
					for (int j = 0; j < DLP; j++) {
						a[j] = _mm_add_ps(a[j], c);
					}
				}
			}

			result = 0;
			for (int i = 0; i < DLP; i++) {
				_mm_storeu_ps(tmp, a[i]);
				result += tmp[0];
				result += tmp[1];
				result += tmp[2];
				result += tmp[3];
			}
		} else
#endif
		{
			float r[DLP];
			float t = 1.1;
			for (int i = 0; i < DLP; i++) {
				r[i] = t;
				t += 0.1;
			}
			for (uint64_t i = 0; i < iterations; i++) {
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
#ifdef __SSE__
		if (RMT_ARITHMETIC_INSTRUCTION_SET == SSE) {
			float tmp[4];
			__m128 a[DLP], c;
			float t;

			t = 1.1;

			for (int i = 0; i < DLP; i++) {
				tmp[0] = t;
				t += 0.1;
				tmp[1] = t;
				t += 0.1;
				tmp[2] = t;
				t += 0.1;
				tmp[3] = t;
				t += 0.1;
				a[i] = _mm_loadu_ps(tmp);
			}

			tmp[0] = base;
			tmp[1] = base;
			tmp[2] = base;
			tmp[3] = base;
			c = _mm_loadu_ps(tmp);

			for (long i = 0; i < iterations; i++) {
				for (int p = 0; p < UNROLL; p++) {
					for (int j = 0; j < DLP; j++) {
						a[j] = _mm_mul_ps(a[j], c);
					}
				}
			}

			result = 0;
			for (int i = 0; i < DLP; i++) {
				_mm_storeu_ps(tmp, a[i]);
				result += tmp[0];
				result += tmp[1];
				result += tmp[2];
				result += tmp[3];
			}
		} else
#endif
		{
			float r[DLP];
			float t = 1.1;
			for (int i = 0; i < DLP; i++) {
				r[i] = t;
				t += 0.1;
			}
			for (uint64_t i = 0; i < iterations; i++) {
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

	if (RMT_ARITHMETIC_OPERATION == ArithmeticOperation_MUL) {
		float base = getBase(iterations, 4);
#ifdef __SSE__
		if (RMT_ARITHMETIC_INSTRUCTION_SET == SSE) {
			float tmp[4];
			__m128 a[DLP], c;
			__m128 aa[DLP], ac;
			float t;

			t = 1.1;

			for (int i = 0; i < DLP; i++) {
				tmp[0] = t;
				t += 0.1;
				tmp[1] = t;
				t += 0.1;
				tmp[2] = t;
				t += 0.1;
				tmp[3] = t;
				t += 0.1;
				a[i] = _mm_loadu_ps(tmp);
				aa[i] = a[i];
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
				for (int p = 0; p < UNROLL; p++) {
					for (int j = 0; j < DLP; j++) {
						a[j] = _mm_mul_ps(a[j], c);
						aa[j] = _mm_add_ps(aa[j], ac);
					}
				}
			}

			result = 0;
			for (int i = 0; i < DLP; i++) {
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
		} else
#endif
		{
			float r[DLP];
			float ar[DLP];
			float t = 1.1;
			for (int i = 0; i < DLP; i++) {
				r[i] = t;
				ar[i] = r[1];
				t += 0.1;
			}
			for (uint64_t i = 0; i < iterations; i++) {
				for (int p = 0; p < UNROLL; p++) {
					for (int j = 0; j < DLP; j++) {
						r[j] *= base;
						ar[j] += 1;
					}
				}
			}
			result = 0;
			for (int i = 0; i < DLP; i++) {
				result += r[i];
				result += ar[i];
			}
		}

	}
}
#pragma GCC diagnostic pop


