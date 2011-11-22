/*
 * ExecutionTimeMeasurer.h
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#ifndef EXECUTIONTIMEMEASURER_H_
#define EXECUTIONTIMEMEASURER_H_

#include "baseClasses/MeasurerBase.h"
#include "generatedC/PerfEventMeasurerDescription.h"
#include "generatedC/PerfEventMeasurerOutput.h"
#include "sys/time.h"
class PerfEventMeasurer: public Measurer<PerfEventMeasurerDescription> {
	timeval startTime;
	timeval endTime;
public:
	PerfEventMeasurer(PerfEventMeasurerDescription *desc): Measurer(desc){}
	virtual ~ExecutionTimeMeasurer();

	void start(){
		gettimeofday(&startTime,NULL);
	}
	void stop(){
		gettimeofday(&endTime,NULL);
	}
	MeasurerOutputBase *read(){
		long result=(endTime.tv_sec-startTime.tv_sec)*1e6+endTime.tv_usec-startTime.tv_usec;
		ExecutionTimeMeasurerOutput *output=new ExecutionTimeMeasurerOutput();
		output->setUSecs(result);
		return output;
	}
};

#endif /* EXECUTIONTIMEMEASURER_H_ */
