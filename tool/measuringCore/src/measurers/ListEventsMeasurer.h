/*
 * ListEventsMeasurer.h
 *
 *  Created on: Dec 19, 2011
 *      Author: ruedi
 */

#ifndef LISTEVENTSMEASURER_H_
#define LISTEVENTSMEASURER_H_

#include "baseClasses/MeasurerBase.h"
#include "sharedDOM/ListEventsMeasurerDescription.h"

class ListEventsMeasurer: public Measurer<ListEventsMeasurerDescription>{
	typedef Measurer<ListEventsMeasurerDescription> super;
public:
	ListEventsMeasurer(ListEventsMeasurerDescription *desc): Measurer(desc){}
	// do nothing on start and stop, read the events in read()
	void start(){};
	void stop(){};

	MeasurerOutputBase *read();
};

#endif /* LISTEVENTSMEASURER_H_ */
