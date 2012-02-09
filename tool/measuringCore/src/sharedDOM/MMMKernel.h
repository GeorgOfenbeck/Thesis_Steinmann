/*
 * MMMKernel.h
 *
 *  Created on: Jan 31, 2012
 *      Author: ruedi
 */

#ifndef MMMKERNEL_H_
#define MMMKERNEL_H_

#include "sharedDOM/MMMKernelData.h"
#include "macros/RMT_MMM_Nb.h"
#include "macros/RMT_MMM_Mu.h"
#include "macros/RMT_MMM_Nu.h"
#include "macros/RMT_MMM_Ku.h"

#define Nb RMT_MMM_Nb
#define Mu RMT_MMM_Mu
#define Nu RMT_MMM_Nu
#define Ku RMT_MMM_Ku

class MMMKernel: public MMMKernelData {
	double *a, *b, *c, *check;

	void tripleLoop(double *a, double *b, double *c) {
		long size = getMatrixSize();
		for (long i = 0; i < size; i++)
			for (long j = 0; j < size; j++)
				for (long k = 0; k < size; k++)
					c[i * size + j] += a[i * size + k] * b[k * size + j];
	}

	void blocked(double *a, double *b, double *c) {
		long size = getMatrixSize();
		for (long i = 0; i < size; i += Nb)
			for (long j = 0; j < size; j += Nb)
				for (long k = 0; k < size; k += Nb)

					for (long id = i; id < i + Nb; id += Mu)
						for (long jd = j; jd < j + Nb; jd += Nu)
							for (long kd = k; kd < k + Nb; kd += Ku)

								for (long kdd = kd; kdd < kd + Ku; kdd++)
									for (long idd = id; idd < id + Mu; idd++)
										for (long jdd = jd; jdd < jd + Nu; jdd++)
											c[idd * size + jdd] += a[idd * size + kdd]
													* b[kdd * size + jdd];
	}

	void blas(double *a, double *b, double *c);
public:

	void initialize();
	void run() {
		blas(a, b, c);
	}
	void dispose();

};

#endif /* MMMKERNEL_H_ */
