/*
 * ArithmeticKernel.cpp
 *
 *  Created on: Dec 20, 2011
 *      Author: ruedi
 */

#include "ArithmeticSingleKernel.h"

#include <cmath>

double ArithmeticSingleKernel::getBase(double exponent, double result){
	return exp(log(result)/exponent);
}
