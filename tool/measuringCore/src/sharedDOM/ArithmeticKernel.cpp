/*
 * ArithmeticKernel.cpp
 *
 *  Created on: Dec 20, 2011
 *      Author: ruedi
 */

#include "ArithmeticKernel.h"

#include <cmath>

double ArithmeticKernel::getBase(double exponent, double result){
	return exp(log(result)/exponent);
}
