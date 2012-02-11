/*
 * ArithmeticKernel.h
 *
 *  Created on: Dec 20, 2011
 *      Author: ruedi
 */

#ifndef ARITHMETICSINGLEKERNEL_H_
#define ARITHMETICSINGLEKERNEL_H_


#include "sharedDOM/ArithmeticSingleKernelData.h"
#include <cmath>



enum ArithmeticOperation {
	ArithmeticOperation_ADD, ArithmeticOperation_MUL, ArithmeticOperation_MULADD,
};

class ArithmeticSingleKernel: public ArithmeticSingleKernelData {
public:
	double result;

	void initialize() {
	}
	;
	void run();
	void dispose() {
	}

}
;

#endif /* ARITHMETICKERNEL_H_ */
