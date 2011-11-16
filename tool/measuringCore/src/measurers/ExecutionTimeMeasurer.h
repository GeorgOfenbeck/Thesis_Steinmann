/*
 * ExecutionTimeMeasurer.h
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#ifndef EXECUTIONTIMEMEASURER_H_
#define EXECUTIONTIMEMEASURER_H_

#include "MeasurerBase.h"
#include "generatedC/ExecutionTimeMeasurerDescription.h"
#include "generatedC/ExecutionTimeMeasurerOutput.h"
#include "sys/time.h"
class ExecutionTimeMeasurer: public Measurer<ExecutionTimeMeasurerDescription> {
	timeval startTime;
	timeval endTime;
public:
	ExecutionTimeMeasurer(ExecutionTimeMeasurerDescription *desc): Measurer(desc){}
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
