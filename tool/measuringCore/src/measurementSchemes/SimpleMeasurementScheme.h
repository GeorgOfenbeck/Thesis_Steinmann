/*
 * KBestMeasurementScheme.h
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#ifndef SIMPLEMEASUREMENTSCHEME_H_
#define SIMPLEMEASUREMENTSCHEME_H_
#include "baseClasses/MeasurementSchemeBase.h"
#include "generatedC/SimpleMeasurementSchemeDescription.h"

template<class TKernel, class TMeasurer>
class SimpleMeasurementScheme : public MeasurementScheme<SimpleMeasurementSchemeDescription,TKernel,TMeasurer>{
	typedef MeasurementScheme<SimpleMeasurementSchemeDescription,TKernel,TMeasurer> super;
public:
	SimpleMeasurementScheme(SimpleMeasurementSchemeDescription *desc, TKernel *kernel, TMeasurer *measurer)
		:super::MeasurementScheme(desc,kernel,measurer)
	{
	}

	MeasurerOutputBase *measure(){
		super::warmOrClearCaches();

		super::measurer.start();
		super::kernel.run();
		super::measurer.stop();
		return super::measurer.read();
	}

};

#endif /* SIMPLEMEASUREMENTSCHEME_H_ */
