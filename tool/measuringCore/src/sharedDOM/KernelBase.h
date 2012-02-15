/*
 * KernelBase.h
 *
 *  Created on: Nov 9, 2011
 *      Author: ruedi
 */

#ifndef KERNELBASE_H_
#define KERNELBASE_H_

#include "sharedDOM/KernelBaseData.h"

class KernelBase : public KernelBaseData {
	bool keepRunning;
public:
	KernelBase();
	virtual ~KernelBase();



	virtual void initialize()=0;
	virtual void run()=0;
	virtual void dispose()=0;
    bool isKeepRunning() const
    {
        return keepRunning;
    }

    void setKeepRunning(bool keepRunning)
    {
        this->keepRunning = keepRunning;
    }
	virtual void warmCaches(){
		run();
	}
};
#endif /* KERNELBASE_H_ */
