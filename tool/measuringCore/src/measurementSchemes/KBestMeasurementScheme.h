/*
 * KBestMeasurementScheme.h
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#ifndef KBESTMEASUREMENTSCHEME_H_
#define KBESTMEASUREMENTSCHEME_H_
#include "MeasurementSchemeBase.h"
#include "generatedC/KBestMeasurementSchemeDescription.h"

class KBestMeasurementScheme : public MeasurementScheme<KBestMeasurementSchemeDescription>{
public:
	KBestMeasurementScheme(KBestMeasurementSchemeDescription *desc):MeasurementScheme(desc){};
	virtual ~KBestMeasurementScheme();

	MeasurerOutputBase *measure();
};

#endif /* KBESTMEASUREMENTSCHEME_H_ */
