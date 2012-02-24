/*
 * FileMeasurer.h
 *
 *  Created on: Jan 25, 2012
 *      Author: ruedi
 */

#ifndef FILEMEASURER_H_
#define FILEMEASURER_H_

#include "sharedEntities/MeasurerBase.h"
#include "sharedEntities/measurers/FileMeasurerData.h"
#include "sharedEntities/measurers/FileMeasurerOutput.h"
#include "sys/time.h"
class FileMeasurer: public FileMeasurerData {
	FileMeasurerOutput *output;
public:
	virtual ~FileMeasurer();

	void start();
	void stop();
	MeasurerOutputBase *read();
};
#endif /* FILEMEASURER_H_ */
