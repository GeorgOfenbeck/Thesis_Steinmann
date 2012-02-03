/*
 * SleepConfigurator.h
 *
 *  Created on: Feb 1, 2012
 *      Author: ruedi
 */

#ifndef SLEEPCONFIGURATOR_H_
#define SLEEPCONFIGURATOR_H_

#include "baseClasses/ConfiguratorBase.h"
#include "sharedDOM/SleepConfiguratorDescription.h"

class SleepConfigurator: public Configurator<SleepConfiguratorDescription>{
public:
	virtual ~SleepConfigurator();
	SleepConfigurator(SleepConfiguratorDescription *description):Configurator(description){};

	void beforeMeasurement();
	void afterMeasurement();
	void beforeRun();
	void afterRun();
};

#endif /* SLEEPCONFIGURATOR_H_ */
