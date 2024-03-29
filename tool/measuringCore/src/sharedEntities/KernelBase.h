/*
 * KernelBase.h
 *
 *  Created on: Nov 9, 2011
 *      Author: ruedi
 */

#ifndef KERNELBASE_H_
#define KERNELBASE_H_

#include "sharedEntities/KernelBaseData.h"
#include <vector>
#include <utility>
#include "utils.h"

class KernelBase : public KernelBaseData {
	bool keepRunning;
protected:
	/**
	 * called when warming up
	 */
	virtual void warmDataCacheAdditional(){}
	virtual std::vector<std::pair<void*,long> > getBuffers()=0;

public:
	KernelBase();
	virtual ~KernelBase();

	virtual void initialize()=0;
	virtual void run()=0;
	virtual void dispose()=0;

	void flushBuffers();
	void warmDataCache();
	virtual void warmCodeCache();

	void flushCacheLine(void *p){
		__asm__ __volatile__ ("clflush %0" :: "m" (*(char*)p));
	}

    bool isKeepRunning() const
    {
        return keepRunning;
    }

    void setKeepRunning(bool keepRunning)
    {
        this->keepRunning = keepRunning;
    }


};
#endif /* KERNELBASE_H_ */
