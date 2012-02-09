/*
 * FileMeasurer.h
 *
 *  Created on: Jan 25, 2012
 *      Author: ruedi
 */

#ifndef FILEMEASURER_H_
#define FILEMEASURER_H_

#include "sharedDOM/MeasurerBase.h"
#include "sharedDOM/FileMeasurerData.h"
#include "sharedDOM/FileMeasurerOutput.h"
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
