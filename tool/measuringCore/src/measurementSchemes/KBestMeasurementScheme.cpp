/*
 * KBestMeasurementScheme.cpp
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#include "KBestMeasurementScheme.h"
#include "typeRegistry/TypeRegisterer.h"
#include <typeinfo>
#include "kernels/MemoryLoadKernel.h"
#include "measurers/ExecutionTimeMeasurer.h"

static TypeRegisterer<KBestMeasurementScheme<MemoryLoadKernel,ExecutionTimeMeasurer>,MemoryLoadKernel,ExecutionTimeMeasurer> dummy;




