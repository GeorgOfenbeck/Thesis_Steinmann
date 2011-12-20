/*
 * ListEventsMeasurer.cpp
 *
 *  Created on: Dec 19, 2011
 *      Author: ruedi
 */

#include "ListEventsMeasurer.h"
#include "generatedC/ListEventsMeasurerOutput.h"
#include "typeRegistry/TypeRegisterer.h"

#include <perfmon/pfmlib.h>
#include <perfmon/pfmlib_perf_event.h>
#include <perfmon/perf_event.h>
#include <vector>
#include <cstring>
#include "err.h"

static TypeRegisterer<ListEventsMeasurer> dummy;

using namespace std;

ListEventsMeasurerOutput *list_pmu_events(pfm_pmu_t pmu) {
	ListEventsMeasurerOutput *result = new ListEventsMeasurerOutput();

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

	result->setPmuName(pinfo.name);

	for (i = pinfo.first_event; i != -1; i = pfm_get_event_next(i)) {
		ret = pfm_get_event_info(i, PFM_OS_PERF_EVENT_EXT, &info);
		if (ret != PFM_SUCCESS)
			errx(1, "cannot get event info: %s", pfm_strerror(ret));

		PerfEventDescription *eventDescription = new PerfEventDescription();
		result->getEvents().push_back(eventDescription);

		printf("%s Event: %s::%s (%llX)\n",
				pinfo.is_present ? "Active" : "Supported", pinfo.name,
				info.name, info.code);

		printf("%s\n", info.desc);
		eventDescription->setName(info.name);
		eventDescription->setDescription(info.desc);

		if (info.equiv != NULL) {
			printf("--> %s\n", info.equiv);
			eventDescription->setEquivalent(info.equiv);
		}

		// print attribute infos
		for (int aidx = 0; aidx < info.nattrs; aidx++) {
			// initialize the attribute info structure
			pfm_event_attr_info_t attr_info;
			memset(&attr_info, 0, sizeof(pfm_event_attr_info_t));
			attr_info.size = sizeof(pfm_event_attr_info_t);

			// retrieve the attribute info
			ret = pfm_get_event_attr_info(info.idx, aidx, PFM_OS_PERF_EVENT_EXT,
					&attr_info);

			// check if the operation succeeded
			if (ret != PFM_SUCCESS) {
				printf("cannot get event attribute info: %i %i %i %s",
						info.nattrs, info.idx, aidx, pfm_strerror(ret));
			} else {
				// add an attribute description to the result
				PerfEventAttributeDescription *attributeDescription =
						new PerfEventAttributeDescription();
				eventDescription->getAttributes().push_back(
						attributeDescription);

				printf(" * %s (%llX): %s\n", attr_info.name, attr_info.code,
						attr_info.desc);

				// fill the attribute info of the result
				attributeDescription->setName(attr_info.name);
				attributeDescription->setDescription(attr_info.desc);

				switch (attr_info.type) {
					case PFM_ATTR_NONE: /* no attribute */
						attributeDescription->setAttributeType("NONE");
						break;
					case PFM_ATTR_UMASK: /* unit mask */
						attributeDescription->setAttributeType("UMASK");
						break;
					case PFM_ATTR_MOD_BOOL: /* register modifier */
						attributeDescription->setAttributeType("MOD_BOOL");
						break;
					case PFM_ATTR_MOD_INTEGER: /* register modifier */
						attributeDescription->setAttributeType("MOD_INTEGER");
						break;
					case PFM_ATTR_RAW_UMASK: /* raw umask (not user visible) */
						attributeDescription->setAttributeType("RAW_UMASK");
						break;
					default:
						// do nothing
						break;
				}
			}
		}

		printf("\n");
	}
	return result;
}

MeasurerOutputBase *ListEventsMeasurer::read() {
	return list_pmu_events(
#ifdef RMT_PERF_EVENT_ARCHITECTURE
			RMT_PERF_EVENT_ARCHITECTURE
#else
			PFM_PMU_PERF_EVENT
#endif
			);
}
