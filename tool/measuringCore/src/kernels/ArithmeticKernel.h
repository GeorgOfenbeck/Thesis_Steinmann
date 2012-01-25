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

#include "boost/mpl/range_c.hpp"
#include "boost/mpl/for_each.hpp"
#include <boost/mpl/transform.hpp>

#ifdef __SSE2__
#include <emmintrin.h>
#endif

//#define UNROLL RMT_ARITHMETIC_UNROLL
//#define DLP RMT_ARITHMETIC_DLP

namespace mpl = boost::mpl;

enum ArithmeticOperation {
	ArithmeticOperation_ADD, ArithmeticOperation_MUL, ArithmeticOperation_MULADD,
};

class ArithmeticKernel: public Kernel<ArithmeticKernelDescription> {
	// solves base**exponent=result, with b unknown
	static double getBase(double exponent, double result);
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wuninitialized"

	struct addHelper {
		template<int DLP, int UNROLL>
		struct inner {
			static void apply(ArithmeticKernel *kernel) {
				long iterations = kernel->description->getIterations();
				double result;
#if RMT_ARITHMETIC_INSTRUCTION_SET==SSE
				{
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
				}
#else
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
#endif
				kernel->result = result;
			}
		};
	};

	struct mulHelper {
		template<int DLP, int UNROLL>
		struct inner {
			static void apply(ArithmeticKernel *kernel) {
				long iterations = kernel->description->getIterations();
				double result;
				double base = getBase(iterations, 4);
#if RMT_ARITHMETIC_INSTRUCTION_SET==SSE
				{
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
				}
#else
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

#endif
				kernel->result = result;
			}
		};
	};

	struct addMulHelper {
		template<int DLP, int UNROLL>
		struct inner {
			static void apply(ArithmeticKernel *kernel) {
				long iterations = kernel->description->getIterations();
				double result;
				double base = getBase(iterations, 4);
#if RMT_ARITHMETIC_INSTRUCTION_SET==SSE
				{
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
				}
#else
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

#endif
				kernel->result = result;
			}
		};
#pragma GCC diagnostic pop
	};

public:
	double result;

	ArithmeticKernel(ArithmeticKernelDescription * description) :
			Kernel(description) {
	}

	void initialize() {
	}

	template<int DLP, typename HELPER>
	struct invocationHelper2 {
		ArithmeticKernel * kernel;
		invocationHelper2(ArithmeticKernel *kernel) {
			this->kernel = kernel;
		}
		template<typename U>
		void operator()(U proposedUnroll) {
			if (proposedUnroll == kernel->description->getUnroll()) {
				HELPER::template inner<DLP, U::value>::apply(kernel);
			}
		}
	};

	template<typename HELPER>
	struct invocationHelper {
		ArithmeticKernel * kernel;
		invocationHelper(ArithmeticKernel *kernel) {
			this->kernel = kernel;
		}
		template<typename U>
		void operator()(U proposedDlp) {
			typedef mpl::range_c<int, 1, 20> unrolls;
			if (proposedDlp == kernel->description->getDlp()) {
				mpl::for_each<unrolls>(
						invocationHelper2<U::value, HELPER>(kernel));
			}
		}
	};

	void run() {
		typedef mpl::range_c<int, 1, 20> dlps;

		if (RMT_ARITHMETIC_OPERATION == ArithmeticOperation_ADD) {
			mpl::for_each<dlps>(invocationHelper<addHelper>(this));
		}
		if (RMT_ARITHMETIC_OPERATION == ArithmeticOperation_MUL) {
			mpl::for_each<dlps>(invocationHelper<mulHelper>(this));
		}
		if (RMT_ARITHMETIC_OPERATION == ArithmeticOperation_MULADD) {
			mpl::for_each<dlps>(invocationHelper<addMulHelper>(this));
		}
	}

	void dispose() {
	}

};
#endif /* ARITHMETICKERNEL_H_ */
