/*
 * CpuMigratingKernel.h
 *
 *  Created on: Jan 31, 2012
 *      Author: ruedi
 */

#ifndef CPUMIGRATINGKERNEL_H_
#define CPUMIGRATINGKERNEL_H_

#include "sharedDOM/CpuMigratingKernelDescription.h"
#include "baseClasses/KernelBase.h"

class CpuMigratingKernel :public Kernel<CpuMigratingKernelDescription>{
public:
	CpuMigratingKernel(CpuMigratingKernelDescription *description):Kernel(description){};

		void initialize(){}
		void run();
		void dispose(){}

};

#endif /* CPUMIGRATINGKERNEL_H_ */
