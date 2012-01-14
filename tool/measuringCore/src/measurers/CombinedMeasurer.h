/*
 * CombinedMeasurer.h
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#ifndef COMBINEDMEASURER_H_
#define COMBINEDMEASURER_H_

#include "baseClasses/MeasurerBase.h"
#include "sharedDOM/CombinedMeasurerDescription.h"
#include "sharedDOM/CombinedMeasurerOutput.h"
#include "sys/time.h"
#include "typeRegistry/TypeRegistry.h"
#include <boost/foreach.hpp>

#define foreach         BOOST_FOREACH
#define reverse_foreach BOOST_REVERSE_FOREACH
using namespace std;

class CombinedMeasurer: public Measurer<CombinedMeasurerDescription> {
	vector<MeasurerBase*> measurers;
public:
	CombinedMeasurer(CombinedMeasurerDescription *desc) :
			Measurer(desc) {
		foreach(MeasurerDescriptionBase *measurerDescription, desc->getMeasurers())
				{
					measurers.push_back(
							TypeRegistry<MeasurerBase>::createObject(
									measurerDescription));
				}
	}
	virtual ~CombinedMeasurer();

	void start() {
		foreach(MeasurerBase *measurer, measurers)
		{
			measurer->start();
		}
	}
	void stop() {
		reverse_foreach(MeasurerBase *measurer, measurers)
		{
			measurer->stop();
		}
	}

	MeasurerOutputBase *read() {
		CombinedMeasurerOutput *result=new CombinedMeasurerOutput();
		foreach(MeasurerBase *measurer, measurers)
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
