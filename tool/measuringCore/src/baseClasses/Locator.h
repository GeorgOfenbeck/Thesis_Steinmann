/*
 * EventDispatcher.h
 *
 *  Created on: Feb 15, 2012
 *      Author: ruedi
 */

#ifndef LOCATOR_H_
#define LOCATOR_H_

class Measurement;
class EventBase;
class Workload;
class KernelBase;

class Locator {
	static Measurement *measurement;
public:
	static void dispatchEvent(EventBase *event);
	static void setMeasurement(Measurement *measurement);

	static Workload* getWorkload(int id);
	static KernelBase* getKernel(int id);
};

#endif /* EVENTDISPATCHER_H_ */

