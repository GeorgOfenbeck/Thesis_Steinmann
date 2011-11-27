/*
 * SystemInitializer.h
 *
 *  Created on: Nov 23, 2011
 *      Author: ruedi
 */

#ifndef SYSTEMINITIALIZER_H_
#define SYSTEMINITIALIZER_H_

#include <vector>
#include <cstddef>

class SystemInitializer {
	static std::vector<SystemInitializer*> &get_initializers(){
		// initialized during the first call to the method
		// remains the same afterwards
		static std::vector<SystemInitializer*> initializers;
		return initializers;
	}
protected:
	SystemInitializer(){
		get_initializers().push_back(this);
	}

public:
	virtual ~SystemInitializer();

	virtual void start()=0;
	virtual void stop()=0;

	static void initialize(){
		for (size_t i=0; i<get_initializers().size(); i++){
			get_initializers()[i]->start();
		}
	}

	static void shutdown(){
		for (size_t i=0; i<get_initializers().size(); i++){
					get_initializers()[i]->stop();
				}
	}
};

#endif /* SYSTEMINITIALIZER_H_ */
