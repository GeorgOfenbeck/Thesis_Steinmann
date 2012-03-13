/*
 * Ia64Measurer.cpp
 *
 *  Created on: Mar 13, 2012
 *      Author: ruedi
 */

#include "Ia64Measurer.h"
#include "sharedEntities/measurers/Ia64MeasurerOutput.h"

Ia64Measurer::Ia64Measurer() {
	// TODO Auto-generated constructor stub

}

Ia64Measurer::~Ia64Measurer() {
	// TODO Auto-generated destructor stub
}

void Ia64Measurer::start() {
}

void Ia64Measurer::stop() {
}

MeasurerOutputBase* Ia64Measurer::read() {
	Ia64MeasurerOutput *output=new Ia64MeasurerOutput();
#ifdef __amd64__
	output->setIsIa64(true);
#else
	output->setIsIa64(false);
#endif
	return output;
}


