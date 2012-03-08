/*
 * ExecutionTimeMeasurer.cpp
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#include "Logger.h"
#include "PerfEventMeasurer.h"
#include "sharedEntities/measurers/PerfEventMeasurerOutput.h"
#include "sharedEntities/measurers/PerfEventCount.h"
#include "sharedEntities/measurers/PerfEventDefinition.h"
#include "baseClasses/SystemInitializer.h"

#include <string>
#include <sys/types.h>
#include <inttypes.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <errno.h>
#include <unistd.h>
#include <string.h>
#include <locale.h>
#include <sys/ioctl.h>
#include <err.h>

#include <perfmon/pfmlib.h>
#include <perfmon/pfmlib_perf_event.h>
#include <perfmon/perf_event.h>

using namespace std;

PerfEventMeasurer::~PerfEventMeasurer() {
	// TODO Auto-generated destructor stub
}

int registerEvent(int parentFd, string eventName){
	perf_event_attr_t attr;
	pfm_perf_encode_arg_t arg;

	int ret,fd;

	// initialize arg, link it to attr
	memset(&arg, 0, sizeof(arg));
	memset(&attr, 0, sizeof(attr));
	arg.size=sizeof(arg);
	arg.attr=&attr;

	/*
	 * 1st argument: event string
	 * 2nd argument: default privilege level (used if not specified in the event string)
	 * 3rd argument: which struct is used
	 * 4th argument: the pfm_perf_encode_arg_t to initialize
	 */
	ret = pfm_get_os_event_encoding(eventName.c_str(), PFM_PLM0|PFM_PLM3,PFM_OS_PERF_EVENT_EXT, &arg);
	if (ret != PFM_SUCCESS)
		errx(1, "cannot find encoding for %s: %s",eventName.c_str(), pfm_strerror(ret));

	/*
	 * request timing information because event may be multiplexed
	 * and thus it may not count all the time. The scaling information
	 * will be used to scale the raw count as if the event had run all
	 * along
	 */
	attr.read_format = PERF_FORMAT_TOTAL_TIME_ENABLED|PERF_FORMAT_TOTAL_TIME_RUNNING;

	/* do not start immediately after perf_event_open() */
	if (parentFd==-1)
		attr.disabled = 1;

	/*
	 * create the event and attach to self
	 * Note that it attaches only to the main thread, there is no inheritance
	 * to threads that may be created subsequently.
	 *
	 * if mulithreaded, then getpid() must be replaced by gettid()
	 */
	int tid=syscall(__NR_gettid);
	fd = perf_event_open(&attr, tid, -1, parentFd, 0);
	if (fd < 0)
		err(1, "cannot create event: %s",eventName.c_str());
	return fd;
}

void PerfEventMeasurer::initialize(){
	groupFd=-1;
	for (size_t i=0; i<getEvents().size(); i++){
		PerfEventDefinition *definition=getEvents()[i];
		LDEBUG("Measuring Event: %s -> %s",definition->getName().c_str(),definition->getDefinition().c_str());

		int fd=registerEvent(groupFd,definition->getDefinition());
		fds.push_back(fd);
		if (groupFd==-1){
			groupFd=fd;
		}
	}

}

void PerfEventMeasurer::dispose(){
	for (size_t i=0; i<fds.size(); i++){
		close(fds[i]);
	}

}

MeasurerOutputBase *PerfEventMeasurer::doRead(){
	uint64_t values[3];
	int ret;

	PerfEventMeasurerOutput *output=new PerfEventMeasurerOutput();
	output->setMeasurerId(getId());
	for (size_t i=0; i<fds.size();i++){
		/*
		 * read the count + scaling values
		 *
		 * It is not necessary to stop an event to read its value
		 */
		ret = ::read(fds[i], values, sizeof(values));
		if (ret != sizeof(values)){
			LERROR("cannot read results: %s", strerror(errno))
			err(1, "cannot read results: %s", strerror(errno));
		}

		/*	 * values[0] = raw count
			 * values[1] = TIME_ENABLED
			 * values[2] = TIME_RUNNING
		 	 */
		PerfEventCount *count=new PerfEventCount();
		count->setDefinition(getEvents()[i]);
		count->setRawCount(values[0]);
		count->setTimeEnabled(values[1]);
		count->setTimeRunning(values[2]);
		LDEBUG("%llu %llu %llu",count->getRawCount(),count->getTimeEnabled(),count->getTimeRunning());
		output->getEventCounts().push_back(count);
	}

	return output;
}

// define and register a system initializer.
static class PerfEventMeasurerInitializer: public SystemInitializer{
	void start(){
		LENTER
		int ret;
		/*
		 * Initialize libpfm library (required before we can use it)
		 */
		ret = pfm_initialize();
		if (ret != PFM_SUCCESS)
			errx(1, "cannot initialize library: %s", pfm_strerror(ret));
	}

	void stop(){
		LENTER
		pfm_terminate();
		LLEAVE
	}
} dummy2;
