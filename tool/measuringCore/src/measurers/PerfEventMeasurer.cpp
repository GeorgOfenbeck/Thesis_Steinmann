/*
 * ExecutionTimeMeasurer.cpp
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#include "PerfEventMeasurer.h"
#include "generatedC/PerfEventMeasurerOutput.h"
#include "typeRegistry/TypeRegisterer.h"
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

static TypeRegisterer<PerfEventMeasurer> dummy;
using namespace std;

PerfEventMeasurer::~PerfEventMeasurer() {
	// TODO Auto-generated destructor stub
}

void list_pmu_events(pfm_pmu_t pmu)
{
   pfm_event_info_t info;
   pfm_pmu_info_t pinfo;
   int i, ret;

   memset(&info, 0, sizeof(info));
   memset(&pinfo, 0, sizeof(pinfo));

   info.size = sizeof(info);
   pinfo.size = sizeof(pinfo);

   ret = pfm_get_pmu_info(pmu, &pinfo);
   if (ret != PFM_SUCCESS)
      errx(1, "cannot get pmu info");

   for (i = pinfo.first_event; i != -1; i = pfm_get_event_next(i)) {
      ret = pfm_get_event_info(i, PFM_OS_PERF_EVENT_EXT, &info);
      if (ret != PFM_SUCCESS)
        errx(1, "cannot get event info: %s",pfm_strerror(ret));

        printf("%s Event: %s::%s (%llX)\n",
               pinfo.is_present ? "Active" : "Supported",
               pinfo.name, info.name, info.code);

        printf("%s\n", info.desc);
        if (info.equiv!=NULL){
        	printf("--> %s\n", info.equiv);
        }

        // print attribute infos
        for (int aidx=0; aidx<info.nattrs; aidx++){
        	pfm_event_attr_info_t attr_info;
        	//printf("%i %i\n",info.idx,aidx);
        	ret=pfm_get_event_attr_info(info.idx,aidx,PFM_OS_PERF_EVENT_EXT,&attr_info);
        	if (ret != PFM_SUCCESS)
        	        errx(1, "cannot get event attribute info: %i %i %i %s",info.nattrs,info.idx,aidx,pfm_strerror(ret));
        	printf(" * %s (%llX): %s\n",attr_info.name,attr_info.code,attr_info.desc);
        }

        printf("\n");
  }
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
	attr.disabled = 1;

	/*
	 * create the event and attach to self
	 * Note that it attaches only to the main thread, there is no inheritance
	 * to threads that may be created subsequently.
	 *
	 * if mulithreaded, then getpid() must be replaced by gettid()
	 */
	fd = perf_event_open(&attr, getpid(), -1, parentFd, 0);
	if (fd < 0)
		err(1, "cannot create event: %s",eventName.c_str());
	return fd;
}

void PerfEventMeasurer::initialize(){
	groupFd=-1;
	for (size_t i=0; i<description->getEvents().size(); i++){
		PerfEventDefinition *definition=super::description->getEvents()[i];
		printf("Measuring Event: %s -> %s\n",definition->getName().c_str(),definition->getDefinition().c_str());

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

MeasurerOutputBase *PerfEventMeasurer::read(){
	uint64_t values[3];
	int ret;

	PerfEventMeasurerOutput *output=new PerfEventMeasurerOutput();
	for (size_t i=0; i<fds.size();i++){
		/*
		 * read the count + scaling values
		 *
		 * It is not necessary to stop an event to read its value
		 */
		ret = ::read(fds[i], values, sizeof(values));
		if (ret != sizeof(values))
			err(1, "cannot read results: %s", strerror(errno));

		/*	 * values[0] = raw count
			 * values[1] = TIME_ENABLED
			 * values[2] = TIME_RUNNING
		 	 */
		PerfEventCount *count=new PerfEventCount();
		count->setDefinition(description->getEvents()[i]);
		count->setRawCount(values[0]);
		count->setTimeEnabled(values[1]);
		count->setTimeRunning(values[2]);
		output->getEventCounts().push_back(count);
	}

	return output;
}

class Initializer: public SystemInitializer{
	void start(){
		int ret;
		/*
		 * Initialize libpfm library (required before we can use it)
		 */
		ret = pfm_initialize();
		if (ret != PFM_SUCCESS)
			errx(1, "cannot initialize library: %s", pfm_strerror(ret));

		//list_pmu_events(PFM_PMU_PERF_EVENT);
	}

	void stop(){
		pfm_terminate();
	}
};

// instantiate initializer. this will cause it to be registered
static Initializer dummy2;