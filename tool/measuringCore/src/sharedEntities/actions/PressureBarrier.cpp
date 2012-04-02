/*
 * PressureBarrier.cpp
 *
 *  Created on: Mar 29, 2012
 *      Author: ruedi
 */

#include "PressureBarrier.h"

#include <cstdio>
#include <cstdlib>
#include <cstring>

void PressureBarrier::wait(int pressure) {
	spinLock.lock();
	this->pressure += pressure;

	bool doOpen = false;
	if (barrierClosed && this->pressure >= getThreshold())
		doOpen = true;

	spinLock.unLock();

	if (doOpen)
		barrierClosed = false;

	// wait for barrier to open
	while (barrierClosed)
		;
}

PressureBarrier::PressureBarrier() {
	barrierClosed = true;
	pressure = 0;
}

PressureBarrier::~PressureBarrier() {
}

void PressureBarrier::open() {
	barrierClosed = false;
}

