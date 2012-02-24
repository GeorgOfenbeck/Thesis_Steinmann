/*
 * ExecutionTimeMeasurer.h
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#ifndef TSCMEASURER_H_
#define TSCMEASURER_H_

#include "sharedEntities/measurers/TscMeasurerData.h"
#include "sharedEntities/measurers/TscMeasurerOutput.h"
#include "stdint.h"
class TscMeasurer: public TscMeasurerData {
	uint64_t startTime;
	uint64_t endTime;

	/*
	 * union to combine two 32 bit values to one 64 bit value
	 */
	typedef union
	{       uint64_t int64;
	        struct {uint32_t lo, hi;} int32;
	} tscUnion;

	// read the tsc counter
	uint64_t rdtsc(){
		tscUnion result;
#if defined(__ia64__)
	#if defined(__INTEL_COMPILER)
		(result).int64=__getReg(3116)
	#else
		__asm__ __volatile__ ("mov %0=ar.itc" : "=r" ((result).int64) );
	#endif
#else
		__asm__ __volatile__ ("cpuid" : : "a" (0) : "bx", "cx", "dx" );
		__asm__ __volatile__ ("rdtsc" : "=a" ((result).int32.lo), "=d"((result).int32.hi));
#endif
		return result.int64;
	}

public:
	virtual ~TscMeasurer();

	void start(){
		// read the start tsc
		startTime=rdtsc();
	}
	void stop(){
		// read the end tsc
		endTime=rdtsc();
	}

	MeasurerOutputBase *read(){
		TscMeasurerOutput *output=new TscMeasurerOutput();
		output->setMeasurerId(getId());
		output->setTics(endTime-startTime);
		return output;
	}
};

#endif /* EXECUTIONTIMEMEASURER_H_ */
