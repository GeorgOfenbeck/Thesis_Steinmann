/*
 * KBestMeasurementScheme.h
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#ifndef KBESTMEASUREMENTSCHEME_H_
#define KBESTMEASUREMENTSCHEME_H_
#include "baseClasses/MeasurementSchemeBase.h"
#include "generatedC/KBestMeasurementSchemeDescription.h"

template<class TKernel, class TMeasurer>
class KBestMeasurementScheme : public MeasurementScheme<KBestMeasurementSchemeDescription,TKernel,TMeasurer>{
	typedef MeasurementScheme<KBestMeasurementSchemeDescription,TKernel,TMeasurer> super;
public:
	KBestMeasurementScheme(KBestMeasurementSchemeDescription *desc, TKernel *kernel, TMeasurer *measurer)
		:super::MeasurementScheme(desc,kernel,measurer)
	{
	}

	virtual ~KBestMeasurementScheme(){}

	MeasurerOutputBase *measure(){
		super::measurer->start();
		super::kernel->run();
		super::measurer->stop();
		return super::measurer->read();
	}

};

#endif /* KBESTMEASUREMENTSCHEME_H_ */
