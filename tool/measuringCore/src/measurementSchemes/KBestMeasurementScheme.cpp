/*
 * KBestMeasurementScheme.cpp
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#include "KBestMeasurementScheme.h"
#include "TypeRegisterer.h"
#include <typeinfo>
#include "kernels/MemoryLoadKernel.h"
#include "measurers/ExecutionTimeMeasurer.h"

static TypeRegisterer<KBestMeasurementScheme> dummy;

KBestMeasurementScheme::~KBestMeasurementScheme() {
	// TODO Auto-generated destructor stub
}
template<class TKernel, class TMeasurer>
MeasurerOutputBase *KBestMeasurementScheme::measureImp(TKernel *kernel, TMeasurer *measurer){
	measurer->start();
	kernel->run();
	measurer->stop();
	return measurer->read();
}


MeasurerOutputBase *KBestMeasurementScheme::measure(){
	if (typeid(*measurer)==typeid(ExecutionTimeMeasurer)
			&& typeid(*kernel)==typeid(MemoryLoadKernel))
	{
	return measureImp<MemoryLoadKernel,ExecutionTimeMeasurer>(
			(MemoryLoadKernel*) kernel,
			(ExecutionTimeMeasurer*)measurer);
	}
	return measureImp<KernelBase,MeasurerBase>(kernel,measurer);

}

