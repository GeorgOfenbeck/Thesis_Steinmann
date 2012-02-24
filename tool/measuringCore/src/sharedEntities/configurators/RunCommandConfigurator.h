/*
 * SystemConfigurator.h
 *
 *  Created on: Feb 1, 2012
 *      Author: ruedi
 */

#ifndef RUNCOMMANDCONFIGURATOR_H_
#define RUNCOMMANDCONFIGURATOR_H_

#include "sharedEntities/configurators/RunCommandConfiguratorData.h"
#include <vector>

class RunCommandConfigurator: public RunCommandConfiguratorData {
	void runConfigurator(std::vector<RunCommand*> &commands);
public:
	virtual ~RunCommandConfigurator();

	void beforeMeasurement();
	void afterMeasurement();
	void beforeRun();
	void afterRun();
};

#endif /* SYSTEMCONFIGURATOR_H_ */
