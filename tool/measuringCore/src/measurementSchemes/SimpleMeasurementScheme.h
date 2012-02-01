/*
 * KBestMeasurementScheme.h
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#ifndef SIMPLEMEASUREMENTSCHEME_H_
#define SIMPLEMEASUREMENTSCHEME_H_
#include "baseClasses/MeasurementSchemeBase.h"
#include "sharedDOM/SimpleMeasurementSchemeDescription.h"

#include "utils.h"
#include <sched.h>

template<class TKernel, class TMeasurer>
class SimpleMeasurementScheme : public MeasurementScheme<SimpleMeasurementSchemeDescription,TKernel,TMeasurer>{
	typedef MeasurementScheme<SimpleMeasurementSchemeDescription,TKernel,TMeasurer> super;
public:
	SimpleMeasurementScheme(SimpleMeasurementSchemeDescription *desc, TKernel *kernel, TMeasurer *measurer)
		:super::MeasurementScheme(desc,kernel,measurer)
	{
	}

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
	}

};

#endif /* SIMPLEMEASUREMENTSCHEME_H_ */
