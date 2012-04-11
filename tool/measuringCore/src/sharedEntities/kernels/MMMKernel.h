/*
 * MMMKernel.h
 *
 *  Created on: Jan 31, 2012
 *      Author: ruedi
 */

#ifndef MMMKERNEL_H_
#define MMMKERNEL_H_

#include "sharedEntities/kernels/MMMKernelData.h"
#include "macros/RMT_MMM_Nb.h"
#include "macros/RMT_MMM_Mu.h"
#include "macros/RMT_MMM_Nu.h"
#include "macros/RMT_MMM_Ku.h"
#include "macros/RMT_MMM_Algorithm.h"

#define Nb RMT_MMM_Nb
#define Mu RMT_MMM_Mu
#define Nu RMT_MMM_Nu
#define Ku RMT_MMM_Ku

enum MMMAlgorithm {
	MMMAlgorithm_TripleLoop,
	MMMAlgorithm_Blocked,
	MMMAlgorithm_Blocked_Restrict,
	MMMAlgorithm_Blas,
};

class MMMKernel: public MMMKernelData {
	double *a, *b, *c, *check;

	void tripleLoop(double *a, double *b, double *c) {
		long size = getMatrixSize();
		for (long i = 0; i < size; i++)
			for (long j = 0; j < size; j++)
				for (long k = 0; k < size; k++)
					c[i * size + j] += a[i * size + k] * b[k * size + j];
	}

#ifdef RMT_MMM_Algorithm__MMMAlgorithm_Blocked
	void blocked(double* a, double* b, double* c) {
#endif
#ifdef RMT_MMM_Algorithm__MMMAlgorithm_Blocked_Restrict
	void blocked(double* __restrict__ a, double* __restrict__ b, double* __restrict__ c) {
#endif
#if RMT_MMM_Algorithm__MMMAlgorithm_Blocked || RMT_MMM_Algorithm__MMMAlgorithm_Blocked_Restrict
		long size = getMatrixSize();
		for (long i = 0; i < size; i += Nb)
			for (long j = 0; j < size; j += Nb)
				for (long k = 0; k < size; k += Nb)

					for (long id = i; id < i + Nb; id += Mu)
						for (long jd = j; jd < j + Nb; jd += Nu)
							for (long kd = k; kd < k + Nb; kd += Ku)

								for (long kdd = kd; kdd < kd + Ku; kdd++)
									for (long idd = id; idd < id + Mu; idd++)
										for (long jdd = jd; jdd < jd + Nu;
												jdd++)
											c[idd * size + jdd] += a[idd * size
													+ kdd]
													* b[kdd * size + jdd];
	}
#endif

	void blas(double *a, double *b, double *c);
protected:
	std::vector<std::pair<void*,long> > getBuffers();

public:

	void initialize();
	void run() {
#ifdef  RMT_MMM_Algorithm__MMMAlgorithm_TripleLoop
			tripleLoop(a, b, c);
#endif

#if RMT_MMM_Algorithm__MMMAlgorithm_Blocked || RMT_MMM_Algorithm__MMMAlgorithm_Blocked_Restrict
			blocked(a, b, c);
#endif

		if (RMT_MMM_Algorithm == MMMAlgorithm_Blas) {
			blas(a, b, c);
		}

	}
	void dispose();
	void warmCodeCache();
};

#endif /* MMMKERNEL_H_ */
