/*
 * SystemConfigurator.h
 *
 *  Created on: Feb 1, 2012
 *      Author: ruedi
 */

#ifndef RUNCOMMANDCONFIGURATOR_H_
#define RUNCOMMANDCONFIGURATOR_H_

#include "baseClasses/ConfiguratorBase.h"
#include "sharedDOM/RunCommandConfiguratorDescription.h"

class RunCommandConfigurator: public Configurator<RunCommandConfiguratorDescription> {
	void runConfigurator(std::vector<RunCommand*> &commands);
public:
	RunCommandConfigurator(RunCommandConfiguratorDescription *description):Configurator(description){};
	virtual ~RunCommandConfigurator();

	void beforeMeasurement();
	void afterMeasurement();
	void beforeRun();
	void afterRun();
};

#endif /* SYSTEMCONFIGURATOR_H_ */
