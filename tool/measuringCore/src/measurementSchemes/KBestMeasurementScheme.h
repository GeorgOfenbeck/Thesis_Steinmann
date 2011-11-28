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
#include "generatedC/PerfEventMeasurerDescription.h"
#include "generatedC/PerfEventMeasurerOutput.h"
#include "typeRegistry/TypeRegistry.h"

template<class TKernel, class TMeasurer>
class KBestMeasurementScheme : public MeasurementScheme<KBestMeasurementSchemeDescription,TKernel,TMeasurer>{
	typedef MeasurementScheme<KBestMeasurementSchemeDescription,TKernel,TMeasurer> super;
public:
	KBestMeasurementScheme(KBestMeasurementSchemeDescription *desc, TKernel *kernel, TMeasurer *measurer)
		:super::MeasurementScheme(desc,kernel,measurer)
	{
	}

	virtual ~KBestMeasurementScheme(){}

	MeasurerOutputBase *measure()
	{
		// initialize measurer to measure context switches
		PerfEventMeasurerDescription *switchMeasurerDescription
			=new PerfEventMeasurerDescription();
		PerfEventDefinition *eventDefinition=new PerfEventDefinition();
		eventDefinition->setName("contextSwitches");
		eventDefinition->setDefinition("perf::PERF_COUNT_SW_CONTEXT_SWITCHES");
		switchMeasurerDescription->getEvents().push_back(eventDefinition);

		MeasurerBase *switchMeasurer=TypeRegistry<MeasurerBase>::createObject(switchMeasurerDescription);
		switchMeasurer->initialize();

		for (int measurementNumber=1; measurementNumber<50; measurementNumber++){
			// do measurement
			switchMeasurer->start();
			super::measurer.start();
			super::kernel.run();
			super::measurer.stop();
			switchMeasurer->stop();

			// count switches which occurred
			PerfEventMeasurerOutput *switchOutput=(PerfEventMeasurerOutput *)switchMeasurer->read();
			unsigned long switchCount=switchOutput->getEventCounts()[0]->getRawCount();

			if (switchCount==0){
				return super::measurer.read();
			}
		}

		printf("Measured 50 times and a context switch occurred in each measurement. Aborting");
		return NULL;
	}
};

#endif /* KBESTMEASUREMENTSCHEME_H_ */
