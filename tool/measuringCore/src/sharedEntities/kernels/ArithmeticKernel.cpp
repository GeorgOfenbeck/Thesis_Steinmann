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
#include "macros/RMT_ARITHMETIC_ADDITIONS.h"
#include "macros/RMT_ARITHMETIC_MULTIPLICATIONS.h"
#include "macros/RMT_ARITHMETIC_MUL_ADD_MIX.h"

#ifdef __SSE2__
#include <emmintrin.h>
#endif

#define UNROLL RMT_ARITHMETIC_UNROLL
#define DLP RMT_ARITHMETIC_DLP
#define ADDITIONS RMT_ARITHMETIC_ADDITIONS
#define MULTIPLICATIONS RMT_ARITHMETIC_MULTIPLICATIONS

double ArithmeticKernel::getBase(double exponent, double result) {
	return exp(log(result) / exponent);
}

#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wuninitialized"

std::vector<std::pair<void*,long > > ArithmeticKernel::getBuffers()
{
	return std::vector<std::pair<void*,long > >();
}

void ArithmeticKernel::run() {
	do {
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
#ifdef RMT_ARITHMETIC_OPERATION__ArithmeticOperation_MULADD
		{
			double base = getBase(iterations, 4);
#ifdef __SSE2__
			if (RMT_ARITHMETIC_INSTRUCTION_SET == SSE) {
				double tmp[2];
				__m128d mulA[DLP * MULTIPLICATIONS], mulC;
				__m128d addA[DLP * ADDITIONS], addC;
				double t;

				t = 1.1;

				for (int i = 0; i < DLP; i++) {

					for (int p = 0; p < MULTIPLICATIONS; p++) {
						tmp[0] = t;
						t += 0.1;
						tmp[1] = t;
						t += 0.1;
						mulA[i * MULTIPLICATIONS + p] = _mm_loadu_pd(tmp);

					}
					for (int p = 0; p < ADDITIONS; p++) {
						tmp[0] = t;
						t += 0.1;
						tmp[1] = t;
						t += 0.1;
						addA[i * ADDITIONS + p] = mulA[i];
					}
				}

				tmp[0] = base;
				tmp[1] = base;
				mulC = _mm_loadu_pd(tmp);

				tmp[0] = 1;
				tmp[1] = 1;
				addC = _mm_loadu_pd(tmp);

#define MUL for (int h = 0; h < MULTIPLICATIONS; h++)\
				mulA[j * MULTIPLICATIONS + h] = _mm_mul_pd(\
						mulA[j * MULTIPLICATIONS + h], mulC);
#define ADD for (int h = 0; h < ADDITIONS; h++)\
				addA[j * ADDITIONS + h] = _mm_add_pd(\
						addA[j * ADDITIONS + h], addC);

				for (long i = 0; i < iterations; i++) {
					for (int p = 0; p < UNROLL; p++) {
						for (int j = 0; j < DLP; j++) {
							RMT_ARITHMETIC_MUL_ADD_MIX
						}
					}
				}

#undef MUL
#undef ADD
				result = 0;
				for (int i = 0; i < DLP; i++) {
					for (int p = 0; p < MULTIPLICATIONS; p++) {
						_mm_storeu_pd(tmp, mulA[i * MULTIPLICATIONS + p]);
						result += tmp[0];
						result += tmp[1];
					}
					for (int p = 0; p < ADDITIONS; p++) {
						_mm_storeu_pd(tmp, addA[i * ADDITIONS + p]);
						result += tmp[0];
						result += tmp[1];
					}
				}
			} else
#endif
			{

				double mulR[DLP * MULTIPLICATIONS];
				double addR[DLP * ADDITIONS];
				double t = 1.1;

				// initialization
				for (int i = 0; i < DLP; i++) {
					for (int p = 0; p < MULTIPLICATIONS; p++) {
						mulR[i * MULTIPLICATIONS + p] = t;
						t += 0.1;
					}
					for (int p = 0; p < ADDITIONS; p++) {
						addR[i * ADDITIONS + p] = t;
						t += 0.1;
					}
				}

#define MUL for (int h = 0; h < MULTIPLICATIONS; h++)\
				mulR[j * MULTIPLICATIONS + h] *= base;
#define ADD for (int h = 0; h < ADDITIONS; h++)\
				addR[j * ADDITIONS + h] += 1;
				// loop
				for (long i = 0; i < iterations; i++) {
					for (int p = 0; p < UNROLL; p++) {
						for (int j = 0; j < DLP; j++) {
							RMT_ARITHMETIC_MUL_ADD_MIX
						}
					}
				}
#undef MUL
#undef ADD
				result = 0;
				for (int i = 0; i < DLP; i++) {
					for (int p = 0; p < MULTIPLICATIONS; p++)
						result += mulR[i * MULTIPLICATIONS + p];
					for (int p = 0; p < ADDITIONS; p++)
						result += addR[i * ADDITIONS + p];
				}
			}
		}
#endif
	} while (getRunUntilStopped() && isKeepRunning());
}
#pragma GCC diagnostic pop

