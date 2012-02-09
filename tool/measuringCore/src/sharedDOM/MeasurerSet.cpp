/*
 * MeasurerSet.cpp
 *
 *  Created on: Feb 9, 2012
 *      Author: ruedi
 */

#include "MeasurerSet.h"



MeasurerSetOutput *MeasurerSet::getOutput()
{

	MeasurerSetOutput *result = new MeasurerSetOutput();
	result->setMainMeasurerOutput(getMainMeasurer()->read());

	foreach (MeasurerBase *additionalMeasurer, getAdditionalMeasurers())
			{
				result->getAdditionalMeasurerOutputs().push_back(
						additionalMeasurer->read());
			}

	foreach (MeasurerBase *measurer, getValidationMeasurers())
			{
				result->getValidationMeasurerOutputs().push_back(
						measurer->read());
			}

	return result;
}

