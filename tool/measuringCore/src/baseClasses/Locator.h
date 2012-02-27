/*
 * EventDispatcher.h
 *
 *  Created on: Feb 15, 2012
 *      Author: ruedi
 */

#ifndef LOCATOR_H_
#define LOCATOR_H_

#include <vector>
class Measurement;
class EventBase;
class Workload;
class KernelBase;

struct IEventListener{
	virtual void handleEvent(EventBase *event)=0;
	virtual ~IEventListener();
};

class Locator {
	static Measurement *measurement;
	static std::vector<IEventListener*> listeners;
public:
	static void dispatchEvent(EventBase *event);
	static void setMeasurement(Measurement *measurement);
	static void addEventListener(IEventListener *listener);

	static Workload* getWorkload(int id);
	static KernelBase* getKernel(int id);
};

#endif /* EVENTDISPATCHER_H_ */

