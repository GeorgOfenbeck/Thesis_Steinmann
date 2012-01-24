/*
 * MeasurementSchemeBase.h
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#ifndef MEASUREMENTSCHEMEBASE_H_
#define MEASUREMENTSCHEMEBASE_H_

#include "sharedDOM/MeasurementSchemeDescriptionBase.h"
#include "MeasurerBase.h"
#include "KernelBase.h"
#include "sharedDOM/MeasurementRunOutput.h"
#include <vector>

class MeasurementSchemeBase {
protected:
	std::vector<MeasurerBase*> *additionalMeasurers;

public:
	typedef MeasurementSchemeDescriptionBase tDescriptionBase;

	virtual ~MeasurementSchemeBase();
	virtual MeasurementSchemeDescriptionBase *getMeasurementSchemeDescription()=0;

	virtual MeasurementRunOutput *measure()=0;

	void setAdditionalMeasurers(std::vector<MeasurerBase*> *vec){
		additionalMeasurers=vec;
	}
    void clearCaches();
};

template<class TDescription, class TKernel, class TMeasurer>
class MeasurementScheme : public MeasurementSchemeBase{
protected:
	TDescription *description;

	// nested class to allow for optimization
	TKernel kernel;

	// nested class to allow for optimization
	TMeasurer measurer;

	void warmOrClearCaches(){
		// warm or clear caches
		if (description->getWarmCaches()){
			measurer.start();
			kernel.warmCaches();
			measurer.stop();
		}
		else
		{
			clearCaches();
		}
	}

public:
	typedef TDescription tDescription;
	typedef MeasurementSchemeBase tBase;

	MeasurementScheme(TDescription *description, TKernel *kernel, TMeasurer *measurer)
	:kernel(*kernel), measurer(*measurer){
		this->description=description;
		this->kernel.initialize();
		this->measurer.initialize();
	}

	~MeasurementScheme(){
		kernel.dispose();
		measurer.dispose();
	}

	MeasurementSchemeDescriptionBase *getMeasurementSchemeDescription(){
		return description;
	}

    TKernel &getKernel() const
    {
        return kernel;
    }

    TMeasurer &getMeasurer() const
    {
        return measurer;
    }
};
#endif /* MEASUREMENTSCHEMEBASE_H_ */
