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
class ExecutionTimeMeasurer: public Measurer<ExecutionTimeMeasurerDescription> {
public:
	ExecutionTimeMeasurer(ExecutionTimeMeasurerDescription *desc): Measurer(desc){}
	virtual ~ExecutionTimeMeasurer();
};

#endif /* EXECUTIONTIMEMEASURER_H_ */
