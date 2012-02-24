/*
 * Measurement.h
 *
 *  Created on: Feb 9, 2012
 *      Author: ruedi
 */

#ifndef MEASUREMENT_H_
#define MEASUREMENT_H_
#include "sharedEntities/MeasurementData.h"

#include "utils.h"
class Measurement: public MeasurementData {
public:
	virtual void cloneFrom(MeasurementData *c){
		MeasurementData::cloneFrom(c);

		// don't clone the configurators
		getConfigurators().clear();
		foreach(ConfiguratorBase *configurator, c->getConfigurators()){
			getConfigurators().push_back(configurator);
		}
	}
};

#endif /* MEASUREMENT_H_ */
