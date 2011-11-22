/*
 * ExecutionTimeMeasurer.h
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#ifndef EXECUTIONTIMEMEASURER_H_
#define EXECUTIONTIMEMEASURER_H_

#include "baseClasses/MeasurerBase.h"
#include "generatedC/PerfEventMeasurerDescription.h"

#include "sys/time.h"
#include <sys/ioctl.h>
#include <perfmon/perf_event.h>
#include <err.h>
#include <vector>

class PerfEventMeasurer: public Measurer<PerfEventMeasurerDescription> {
	typedef Measurer<PerfEventMeasurerDescription> super;
	int groupFd;
	// contains all fds, including the groupFd
	std::vector<int> fds;
public:
	PerfEventMeasurer(PerfEventMeasurerDescription *desc): Measurer(desc){}
	virtual ~PerfEventMeasurer();

	void start(){
		int ret;
		/*
		 * clear counts
		 */
		ret = ioctl(groupFd, PERF_EVENT_IOC_RESET, 0);
		if (ret)
			err(1, "ioctl(reset) failed");
		/*
		 * start counting now
		 */
		ret = ioctl(groupFd, PERF_EVENT_IOC_ENABLE, 0);
		if (ret)
			err(1, "ioctl(enable) failed");

	}
	void stop(){
		/*
		 * stop counting
		 */
		int ret = ioctl(groupFd, PERF_EVENT_IOC_DISABLE, 0);
		if (ret)
			err(1, "ioctl(disable) failed");
	}

	void initialize();

	void dispose();
	MeasurerOutputBase *read();
};

#endif /* EXECUTIONTIMEMEASURER_H_ */
