/*
 * The goal of this test is to check if PAPI moves the performance counters from one core to the other,
 * or if the counters stick with the core.
 *
 * To check this, we set up the the counters and switch the core.
 */

#define CORE_SWITCH_TEST_CPP
#include "coreSwitchTest.hpp"

#include <sched.h>
#include <cstdio>
#include <unistd.h>
#include <papi.h>
#include <cstdlib>

using namespace std;

void coreSwitchTest(){
		printf("hello World, running on cpu %i\n",sched_getcpu());

		// switching to cpu 0
		cpu_set_t mask;
		CPU_ZERO(&mask);
		CPU_SET(0,&mask);
		sched_setaffinity(0,sizeof(mask),&mask);
		printf("running on cpu %i\n",sched_getcpu());

		int retval;
		int EventSet=PAPI_NULL;
		long_long values[1];

		// Initialize the PAPI library
		retval = PAPI_library_init(PAPI_VER_CURRENT);

		/* Create the Event Set */
		if (PAPI_create_eventset(&EventSet) != PAPI_OK)
		    exit(1);

		int event;
		if (PAPI_event_name_to_code("INSTRUCTION_RETIRED",&event)!=PAPI_OK)
			exit(1);

		/* Add Total Instructions Executed to our Event Set */
		if (PAPI_add_event(EventSet, event) != PAPI_OK)
		    exit(1);

		/* Start counting events in the Event Set */
		if (PAPI_start(EventSet) != PAPI_OK)
		    exit(1);

		//usleep(10000);

		// switching to cpu 1
		/*CPU_ZERO(&mask);
		CPU_SET(0,&mask);
		sched_setaffinity(0,sizeof(mask),&mask);
		printf("running on cpu %i\n",sched_getcpu());*/

		/* Stop the counting of events in the Event Set */
		if (PAPI_stop(EventSet, values) != PAPI_OK)
			exit(1);

		printf("After stopping the counters: %lld\n",values[0]);


}
