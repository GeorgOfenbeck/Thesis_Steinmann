/*
 * SleepConfigurator.h
 *
 *  Created on: Feb 1, 2012
 *      Author: ruedi
 */

#ifndef SLEEPCONFIGURATOR_H_
#define SLEEPCONFIGURATOR_H_

#include "sharedEntities/configurators/SleepConfiguratorData.h"

class SleepConfigurator: public SleepConfiguratorData{
public:
	virtual ~SleepConfigurator();

	void beforeMeasurement();
	void afterMeasurement();
	void beforeRun();
	void afterRun();
};

#endif /* SLEEPCONFIGURATOR_H_ */
