/*
 * Ia64Measurer.h
 *
 *  Created on: Mar 13, 2012
 *      Author: ruedi
 */

#ifndef IA64MEASURER_H_
#define IA64MEASURER_H_

#include "sharedEntities/measurers/Ia64MeasurerData.h"

class Ia64Measurer : public Ia64MeasurerData {
public:
	Ia64Measurer();
	virtual ~Ia64Measurer();

	void start();
	void stop();
	MeasurerOutputBase *read();
};

#endif /* IA64MEASURER_H_ */
