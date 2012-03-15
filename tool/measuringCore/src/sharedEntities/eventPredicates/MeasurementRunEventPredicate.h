/*
 * MeasurementRunEventPredicate.h
 *
 *  Created on: Mar 15, 2012
 *      Author: ruedi
 */

#ifndef MEASUREMENTRUNEVENTPREDICATE_H_
#define MEASUREMENTRUNEVENTPREDICATE_H_
#include "sharedEntities/eventPredicates/MeasurementRunEventPredicateData.h"

class MeasurementRunEventPredicate: public MeasurementRunEventPredicateData {
public:
	virtual ~MeasurementRunEventPredicate();
	bool doesMatch(EventBase *event);
};

#endif /* MEASUREMENTRUNEVENTPREDICATE_H_ */
