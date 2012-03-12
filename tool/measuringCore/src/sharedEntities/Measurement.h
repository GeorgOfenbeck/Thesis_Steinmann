/*
 * Measurement.h
 *
 *  Created on: Feb 9, 2012
 *      Author: ruedi
 */

#ifndef MEASUREMENT_H_
#define MEASUREMENT_H_
#include "sharedEntities/MeasurementData.h"
#include "sharedEntities/ConfiguratorBase.h"

#include "utils.h"
class Measurement: public MeasurementData {
public:
	virtual void cloneFrom(MeasurementData *c, std::map<PolymorphicBase*,PolymorphicBase*> &map){
		MeasurementData::cloneFrom(c,map);

		// don't clone the configurators
		// they have the before and afterMeasuremen() methods,
		// which are more intuitively called always on the same instance
		foreach(ConfiguratorBase *configurator, getConfigurators()){
			delete configurator;
		}

		getConfigurators().clear();

		foreach(ConfiguratorBase *configurator, c->getConfigurators()){
			getConfigurators().push_back(configurator);
		}
	}
};

#endif /* MEASUREMENT_H_ */
