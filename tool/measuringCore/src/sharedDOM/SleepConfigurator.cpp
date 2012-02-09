/*
 * SleepConfigurator.cpp
 *
 *  Created on: Feb 1, 2012
 *      Author: ruedi
 */

#include "SleepConfigurator.h"

#include <unistd.h>


SleepConfigurator::~SleepConfigurator() {
	// TODO Auto-generated destructor stub
}

void SleepConfigurator::beforeMeasurement()
{
	usleep(getSleepBeforeMeasurement()*1000);
}



void SleepConfigurator::afterMeasurement()
{
	usleep(getSleepAfterMeasurement()*1000);
}



void SleepConfigurator::beforeRun()
{
	usleep(getSleepBeforeRun()*1000);
}



void SleepConfigurator::afterRun()
{
	usleep(getSleepAfterRun()*1000);
}



