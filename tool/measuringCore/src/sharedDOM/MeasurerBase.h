/*
 * MeasurerBase.h
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#ifndef MEASURERBASE_H_
#define MEASURERBASE_H_

#include "sharedDOM/MeasurerBaseData.h"
#include "sharedDOM/MeasurerOutputBase.h"

class MeasurerBase : public MeasurerBaseData {
public:
	virtual ~MeasurerBase();

	virtual void start()=0;
	virtual void stop()=0;
	virtual MeasurerOutputBase *read()=0;
	virtual void initialize(){};
	virtual void dispose(){};
};

#endif /* MEASURERBASE_H_ */
