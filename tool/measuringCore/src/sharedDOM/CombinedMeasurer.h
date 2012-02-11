/*
 * CombinedMeasurer.h
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#ifndef COMBINEDMEASURER_H_
#define COMBINEDMEASURER_H_

#include "sharedDOM/MeasurerBase.h"
#include "sharedDOM/CombinedMeasurerData.h"
#include "sharedDOM/CombinedMeasurerOutput.h"
#include "sys/time.h"
#include <boost/foreach.hpp>

#define foreach         BOOST_FOREACH
#define reverse_foreach BOOST_REVERSE_FOREACH
using namespace std;

class CombinedMeasurer: public CombinedMeasurerData {
public:

	virtual ~CombinedMeasurer();

	void start() {
		foreach(MeasurerBase *measurer, getMeasurers())
		{
			measurer->start();
		}
	}
	void stop() {
		reverse_foreach(MeasurerBase *measurer, getMeasurers())
		{
			measurer->stop();
		}
	}

	MeasurerOutputBase *read() {
		CombinedMeasurerOutput *result=new CombinedMeasurerOutput();
		result->setMeasurerId(getId());
		foreach(MeasurerBase *measurer, getMeasurers())
		{
			MeasurerOutputBase *tmp=measurer->read();
			if (tmp!=NULL){
				result->getOutputs().push_back(tmp);
			}
		}
		return result;
	}
};

#endif /* EXECUTIONTIMEMEASURER_H_ */
