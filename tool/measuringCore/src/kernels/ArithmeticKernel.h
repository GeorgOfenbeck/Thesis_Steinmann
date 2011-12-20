/*
 * ArithmeticKernel.h
 *
 *  Created on: Dec 20, 2011
 *      Author: ruedi
 */

#ifndef ARITHMETICKERNEL_H_
#define ARITHMETICKERNEL_H_

#include "baseClasses/KernelBase.h"

class ArithmeticKernel : public Kernel<ArithmeticKernelDescription>{
public:
	ArithmeticKernel(ArithmeticKernelDescription *description):Kernel(description){};

	void initialize(){};
	void run(){}
	void dispose(){};
};

#endif /* ARITHMETICKERNEL_H_ */
