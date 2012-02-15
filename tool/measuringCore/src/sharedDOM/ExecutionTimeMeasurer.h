/*
 * ExecutionTimeMeasurer.h
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#ifndef EXECUTIONTIMEMEASURER_H_
#define EXECUTIONTIMEMEASURER_H_

#include "sharedDOM/ExecutionTimeMeasurerData.h"
#include "sharedDOM/ExecutionTimeMeasurerOutput.h"
#include "sys/time.h"
class ExecutionTimeMeasurer: public ExecutionTimeMeasurerData {
	timeval startTime;
	timeval endTime;
public:
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
		output->setMeasurerId(getId());
		output->setUSecs(result);
		output->setMeasurerId(getId());
		return output;
	}
};

#endif /* EXECUTIONTIMEMEASURER_H_ */
