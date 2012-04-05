/*
 * BlasKernelBase.cpp
 *
 *  Created on: Apr 5, 2012
 *      Author: ruedi
 */

#include "BlasKernelBase.h"

#include "macros/RMT_BLAS_KERNEL_BASE_USEMKL.h"
#include "macros/RMT_BLAS_KERNEL_BASE_USED.h"

#ifdef RMT_BLAS_KERNEL_BASE_USED__1
extern "C"
{
#ifdef RMT_BLAS_KERNEL_BASE_USEMKL__0
void openblas_set_num_threads(int num_threads);
#else
void MKL_Set_Num_Threads(int num_threads);
#endif
}
#endif

void BlasKernelBase::initialize() {
#ifdef RMT_BLAS_KERNEL_BASE_USED__1
#ifdef RMT_BLAS_KERNEL_BASE_USEMKL__0
	openblas_set_num_threads(getNumThreads());
#else
	MKL_Set_Num_Threads(getNumThreads());
#endif
#endif
}

BlasKernelBase::~BlasKernelBase() {
	// TODO Auto-generated destructor stub
}

