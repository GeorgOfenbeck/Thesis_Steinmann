/*
 * ConfiguratorBase.h
 *
 *  Created on: Feb 1, 2012
 *      Author: ruedi
 */

#ifndef CONFIGURATORBASE_H_
#define CONFIGURATORBASE_H_
#include "sharedEntities/ConfiguratorBaseData.h"

class ConfiguratorBase: public ConfiguratorBaseData {
public:
public:
	virtual ~ConfiguratorBase();

	virtual void beforeMeasurement(){};
	virtual void afterMeasurement(){};
	virtual void beforeRun(){};
	virtual void afterRun(){};
};

#endif /* CONFIGURATORBASE_H_ */

