/*
 * MeasurementSchemeBase.h
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#ifndef MEASUREMENTSCHEMEBASE_H_
#define MEASUREMENTSCHEMEBASE_H_

#include "generatedC/MeasurementSchemeDescriptionBase.h"
#include "MeasurerBase.h"
#include "KernelBase.h"
#include "generatedC/MeasurerOutputBase.h"

class MeasurementSchemeBase {
protected:
	KernelBase *kernel;
	MeasurerBase *measurer;
public:
	typedef MeasurementSchemeDescriptionBase tDescriptionBase;

	virtual ~MeasurementSchemeBase();
	virtual MeasurementSchemeDescriptionBase *getMeasurementSchemeDescription()=0;

	void setKernel(KernelBase *kernel){
		this->kernel=kernel;
	}

	void setMeasurer(MeasurerBase *measurer){
		this->measurer=measurer;
	}

	virtual MeasurerOutputBase *measure()=0;
};

template<class TDescription>
class MeasurementScheme : public MeasurementSchemeBase{
	TDescription *description;
public:
	typedef TDescription tDescription;
	typedef MeasurementSchemeBase tBase;

	MeasurementScheme(TDescription *description){
		this->description=description;
	}

	MeasurementSchemeDescriptionBase *getMeasurementSchemeDescription(){
		return description;
	}
};
#endif /* MEASUREMENTSCHEMEBASE_H_ */
