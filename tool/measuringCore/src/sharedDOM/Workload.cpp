/*
 * Workload.cpp
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#include "Workload.h"


#include "utils.h"
#include <sched.h>


Workload::~Workload() {
	// TODO Auto-generated destructor stub
}

static int dummy;

void Workload::clearCaches() {
	clearL1ICache();
	// just access 10M of memory, which is the maximum cache size present in current processors
	size_t blockSize=10*(1<<20);
	char *buffer=(char*)malloc(blockSize);
	for (size_t i=0; i<blockSize; i++){
		dummy+=buffer[i];
		//buffer[i]=0;
	}
	free((void*)buffer);
}

void Workload::start()
{

/*

MeasurementRunOutput *measure(){
		// set cpu affinity
		{
			int cpu=super::description->getCpu();
			cpu_set_t *mask=CPU_ALLOC(cpu);
			size_t size=CPU_ALLOC_SIZE(cpu);
			CPU_ZERO_S(size,mask);
			CPU_SET_S(cpu, size,mask);
			sched_setaffinity(0,size,mask);
		}

		// notify configurators
		foreach (ConfiguratorBase *configurator, *super::configurators){
			configurator->beforeRun();
		}

		// start validation measurers. They should validate the warmup, too
		foreach (MeasurerBase *measurer, *super::validationMeasurers){
			measurer->start();
		}

		// prepare caches
		super::warmOrClearCaches();

		// warm up main measurer
		super::measurer.start();
		super::measurer.stop();

		// warm up additional measurers
		foreach (MeasurerBase *additionalMeasurer, *super::additionalMeasurers){
			additionalMeasurer->start();
			additionalMeasurer->stop();
		}

		// perform measurement

		// start additional measurers
		foreach (MeasurerBase *additionalMeasurer, *super::additionalMeasurers){
			additionalMeasurer->start();
		}

		// start main measurer
		super::measurer.start();

		// run kernel
		super::kernel.run();

		// stop main measurer
		super::measurer.stop();

		// stop additional measurers
		reverse_foreach (MeasurerBase *additionalMeasurer, *super::additionalMeasurers){
			additionalMeasurer->stop();
		}

		// stop the validation measurers
		reverse_foreach (MeasurerBase *measurer, *super::validationMeasurers){
			measurer->stop();
		}

		// notify the configurators
		reverse_foreach (ConfiguratorBase *configurator, *super::configurators){
			configurator->afterRun();
		}

		MeasurementRunOutput *result=new MeasurementRunOutput();
		result->setMainMeasurerOutput(super::measurer.read());

		foreach (MeasurerBase *additionalMeasurer, *super::additionalMeasurers){
					result->getAdditionalMeasurerOutputs().push_back(additionalMeasurer->read());
				}

		foreach (MeasurerBase *measurer, *super::validationMeasurers){
			result->getValidationMeasurerOutputs().push_back(measurer->read());
		}

		return result;
	}*/
}
