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
public:
	typedef MeasurementSchemeDescriptionBase tDescriptionBase;

	virtual ~MeasurementSchemeBase();
	virtual MeasurementSchemeDescriptionBase *getMeasurementSchemeDescription()=0;

	virtual MeasurerOutputBase *measure()=0;
};

template<class TDescription, class TKernel, class TMeasurer>
class MeasurementScheme : public MeasurementSchemeBase{
protected:
	TDescription *description;
	TKernel *kernel;
	TMeasurer *measurer;
public:
	typedef TDescription tDescription;
	typedef MeasurementSchemeBase tBase;

	MeasurementScheme(TDescription *description, TKernel *kernel, TMeasurer *measurer){
		this->description=description;
		this->kernel=kernel;
		this->measurer=measurer;
	}

	MeasurementSchemeDescriptionBase *getMeasurementSchemeDescription(){
		return description;
	}

    TKernel *getKernel() const
    {
        return kernel;
    }

    TMeasurer *getMeasurer() const
    {
        return measurer;
    }

    void setKernel(TKernel *kernel)
    {
        this->kernel = kernel;
    }

    void setMeasurer(TMeasurer *measurer)
    {
        this->measurer = measurer;
    }

};
#endif /* MEASUREMENTSCHEMEBASE_H_ */
