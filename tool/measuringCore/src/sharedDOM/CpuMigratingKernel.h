/*
 * CpuMigratingKernel.h
 *
 *  Created on: Jan 31, 2012
 *      Author: ruedi
 */

#ifndef CPUMIGRATINGKERNEL_H_
#define CPUMIGRATINGKERNEL_H_

#include "sharedDOM/CpuMigratingKernelData.h"

class CpuMigratingKernel: public CpuMigratingKernelData {
protected:
	std::vector<std::pair<void*,long> > getBuffers();

public:

	void initialize() {
	}
	void run();
	void dispose() {
	}
};

#endif /* CPUMIGRATINGKERNEL_H_ */
