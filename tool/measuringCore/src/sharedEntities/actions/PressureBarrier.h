/*
 * PressureBarrier.h
 *
 *  Created on: Mar 29, 2012
 *      Author: ruedi
 */

#ifndef PRESSUREBARRIER_H_
#define PRESSUREBARRIER_H_

#include "sharedEntities/actions/PressureBarrierData.h"

#include <pthread.h>
#include "SpinLock.h"

class PressureBarrier : public PressureBarrierData{
	SpinLock spinLock;
	volatile bool barrierClosed;
	int pressure;
public:
	PressureBarrier();
	virtual ~PressureBarrier();
	void wait(int pressure);
	void open();
};

#endif /* PRESSUREBARRIER_H_ */
