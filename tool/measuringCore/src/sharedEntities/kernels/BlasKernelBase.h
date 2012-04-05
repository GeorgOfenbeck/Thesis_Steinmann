/*
 * BlasKernelBase.h
 *
 *  Created on: Apr 5, 2012
 *      Author: ruedi
 */

#ifndef BLASKERNELBASE_H_
#define BLASKERNELBASE_H_

#include "sharedEntities/kernels/BlasKernelBaseData.h"

class BlasKernelBase: public BlasKernelBaseData {
protected:

public:
	void initialize();

	virtual ~BlasKernelBase();
};

#endif /* BLASKERNELBASE_H_ */
