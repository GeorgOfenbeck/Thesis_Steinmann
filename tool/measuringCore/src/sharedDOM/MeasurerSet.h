/*
 * MeasurerSet.h
 *
 *  Created on: Feb 9, 2012
 *      Author: ruedi
 */

#ifndef MEASURERSET_H_
#define MEASURERSET_H_

#include "utils.h"

#include "sharedDOM/MeasurerSetData.h"
#include "sharedDOM/MeasurerSetOutput.h"

class MeasurerSet: public MeasurerSetData {
public:
	void startValidationMeasurers() {
		foreach (MeasurerBase *measurer, getValidationMeasurers())
				{
					measurer->start();
				}
	}
	void stopValidationMeasurers() {
		reverse_foreach (MeasurerBase *measurer, getValidationMeasurers())
				{
					measurer->stop();
				}
	}

	void startAdditionalMeasurers() {
		foreach (MeasurerBase *measurer, getAdditionalMeasurers())
				{
					measurer->start();
				}
	}
	void stopAdditionalMeasurers() {
		reverse_foreach (MeasurerBase *measurer, getAdditionalMeasurers())
				{
					measurer->stop();
				}
	}

	MeasurerSetOutput* getOutput();
};

#endif /* MEASURERSET_H_ */
