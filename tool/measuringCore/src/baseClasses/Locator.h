/*
 * EventDispatcher.h
 *
 *  Created on: Feb 15, 2012
 *      Author: ruedi
 */

#ifndef LOCATOR_H_
#define LOCATOR_H_

#include <vector>
#include "Mutex.h"
class Measurement;
class EventBase;
class Workload;
class KernelBase;
class MeasurerSetOutput;
class MeasurementRunOutput;
class Rule;

struct IEventListener{
	virtual void handleEvent(EventBase *event)=0;
	virtual ~IEventListener();
};

class Locator {
	static Measurement *measurement;
	static MeasurementRunOutput *runOutput;
	static std::vector<IEventListener*> listeners;
	static Mutex mutex;

public:
	static void dispatchEvent(EventBase *event);
	static void setMeasurement(Measurement *measurement, MeasurementRunOutput *runOutput);
	static void addEventListener(IEventListener *listener);

	static Workload* getWorkload(int id);
	static std::vector<Workload*> getWorkloads();

	static KernelBase* getKernel(int id);

	static void addMeasurerSetOutput(MeasurerSetOutput *setOutput);
	static void addRule(Rule *rule);
};

#endif /* EVENTDISPATCHER_H_ */

