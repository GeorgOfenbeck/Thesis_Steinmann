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
#include "sharedDOM/MeasurerBase.h"

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

	void start(){
		startValidationMeasurers();
		startAdditionalMeasurers();
		if (getMainMeasurer()!=NULL){
			getMainMeasurer()->start();
		}
	}

	void stop(){
		if (getMainMeasurer()!=NULL){
			getMainMeasurer()->stop();
		}
		stopAdditionalMeasurers();
		stopValidationMeasurers();
	}
	void initialize() {
		if (getMainMeasurer()!=NULL)
			getMainMeasurer()->initialize();

		foreach(MeasurerBase *measurer, getAdditionalMeasurers())
				{
					measurer->initialize();
				}
		foreach(MeasurerBase *measurer, getValidationMeasurers())
				{
					measurer->initialize();
				}
	}

	void dispose() {

		reverse_foreach(MeasurerBase *measurer, getValidationMeasurers())
				{
					measurer->dispose();
				}

		reverse_foreach(MeasurerBase *measurer, getAdditionalMeasurers())
				{
					measurer->dispose();
				}

		if (getMainMeasurer()!=NULL){
		getMainMeasurer()->dispose();
		}
	}

	MeasurerSetOutput* getOutput();
};

#endif /* MEASURERSET_H_ */
