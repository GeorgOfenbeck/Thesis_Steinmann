/*
 * MeasurerBase.h
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#ifndef MEASURERBASE_H_
#define MEASURERBASE_H_

#include "generatedC/MeasurerDescriptionBase.h"
#include "generatedC/MeasurerOutputBase.h"

class MeasurerBase {
public:
	typedef MeasurerDescriptionBase tDescriptionBase;

	virtual ~MeasurerBase();

	virtual void start()=0;
	virtual void stop()=0;
	virtual MeasurerOutputBase *read()=0;
};

template<class TDescription>
class Measurer : public MeasurerBase{
	TDescription *description;
public:
	typedef TDescription tDescription;
	typedef MeasurerBase tBase;

	Measurer(TDescription *description){
		this->description=description;
	}
};
#endif /* MEASURERBASE_H_ */
