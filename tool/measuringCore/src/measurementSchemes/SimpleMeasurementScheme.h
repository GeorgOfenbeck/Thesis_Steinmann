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

template<class TKernel, class TMeasurer>
class SimpleMeasurementScheme : public MeasurementScheme<SimpleMeasurementSchemeDescription,TKernel,TMeasurer>{
	typedef MeasurementScheme<SimpleMeasurementSchemeDescription,TKernel,TMeasurer> super;
public:
	SimpleMeasurementScheme(SimpleMeasurementSchemeDescription *desc, TKernel *kernel, TMeasurer *measurer)
		:super::MeasurementScheme(desc,kernel,measurer)
	{
	}

	MeasurementRunOutput *measure(){
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

		MeasurementRunOutput *result=new MeasurementRunOutput();
		result->setMainMeasurerOutput(super::measurer.read());

		foreach (MeasurerBase *additionalMeasurer, *super::additionalMeasurers){
					result->getAdditionalMeasurerOutputs().push_back(additionalMeasurer->read());
				}

		return result;
	}

};

#endif /* SIMPLEMEASUREMENTSCHEME_H_ */
