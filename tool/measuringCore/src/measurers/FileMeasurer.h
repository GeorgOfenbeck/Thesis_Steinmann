/*
 * FileMeasurer.h
 *
 *  Created on: Jan 25, 2012
 *      Author: ruedi
 */

#ifndef FILEMEASURER_H_
#define FILEMEASURER_H_

#include "baseClasses/MeasurerBase.h"
#include "sharedDOM/FileMeasurerDescription.h"
#include "sharedDOM/FileMeasurerOutput.h"
#include "sys/time.h"
class FileMeasurer: public Measurer<FileMeasurerDescription> {
	FileMeasurerOutput *output;
public:
	FileMeasurer(FileMeasurerDescription *desc): Measurer(desc){}
	virtual ~FileMeasurer();

	void start();
	void stop();
	MeasurerOutputBase *read();
};
#endif /* FILEMEASURER_H_ */
