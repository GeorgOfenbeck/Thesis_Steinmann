/*
 * CpuMigratingKernel.cpp
 *
 *  Created on: Jan 31, 2012
 *      Author: ruedi
 */

#include "CpuMigratingKernel.h"
#include <sched.h>
#include <unistd.h>
#include "typeRegistry/TypeRegisterer.h"

static TypeRegisterer<CpuMigratingKernel> dummy;

void CpuMigratingKernel::run(){
	int cpu=description->getTargetCpu();

	// set cpu affinity
	{
		cpu_set_t *mask=CPU_ALLOC(cpu);
		size_t size=CPU_ALLOC_SIZE(cpu);
		CPU_ZERO_S(size,mask);
		CPU_SET_S(cpu, size,mask);
		sched_setaffinity(0,size,mask);
	}
	usleep(1000);
	if (sched_getcpu()!=cpu)
		throw "cpu migratin failed";
}
