/*
 * SleepConfigurator.cpp
 *
 *  Created on: Feb 1, 2012
 *      Author: ruedi
 */

#include "SleepConfigurator.h"
#include "typeRegistry/TypeRegisterer.h"

#include <unistd.h>

static TypeRegisterer<SleepConfigurator> dummy;

SleepConfigurator::~SleepConfigurator() {
	// TODO Auto-generated destructor stub
}

void SleepConfigurator::beforeMeasurement()
{
	usleep(description->getSleepBeforeMeasurement()*1000);
}



void SleepConfigurator::afterMeasurement()
{
	usleep(description->getSleepAfterMeasurement()*1000);
}



void SleepConfigurator::beforeRun()
{
	usleep(description->getSleepBeforeRun()*1000);
}



void SleepConfigurator::afterRun()
{
	usleep(description->getSleepAfterRun()*1000);
}



