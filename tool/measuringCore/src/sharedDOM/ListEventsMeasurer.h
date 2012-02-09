/*
 * ListEventsMeasurer.h
 *
 *  Created on: Dec 19, 2011
 *      Author: ruedi
 */

#ifndef LISTEVENTSMEASURER_H_
#define LISTEVENTSMEASURER_H_

#include "sharedDOM/ListEventsMeasurerData.h"

class ListEventsMeasurer: public ListEventsMeasurerData{
public:
	// do nothing on start and stop, read the events in read()
	void start(){};
	void stop(){};

	MeasurerOutputBase *read();
};

#endif /* LISTEVENTSMEASURER_H_ */
