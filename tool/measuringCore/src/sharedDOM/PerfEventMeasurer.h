/*
 * ExecutionTimeMeasurer.h
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#ifndef PERFEVENTMEASURER_H_
#define PERFEVENTMEASURER_H_

#include "sharedDOM/PerfEventMeasurerData.h"

#include "sys/time.h"
#include <sys/ioctl.h>
#include <perfmon/perf_event.h>
#include <err.h>
#include <vector>

class PerfEventMeasurer: public PerfEventMeasurerData {
	int groupFd;
	// contains all fds, including the groupFd
	std::vector<int> fds;
public:
	virtual ~PerfEventMeasurer();

	void start(){
		printf("PerfEventMeasurer::start\n");
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
		printf("PerfEventMeasurer::stop()\n");
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
