#ifndef SYSTEMINITIALIZER_H_
#define SYSTEMINITIALIZER_H_

#include <vector>
#include <cstddef>

/*
 * Helper class to implement a modular system initialization system.
 * If a part of the program needs to run code on system startup or shutdown,
 * it can implement a subclass of SystemInitializer and instantiate it using
 * a static global variable.
 */
class SystemInitializer {
	// returns the singleton system initializer list
	static std::vector<SystemInitializer*> &get_initializers() {
		// initialized during the first call to the method
		// remains the same afterwards
		static std::vector<SystemInitializer*> initializers;
		return initializers;
	}

protected:
	SystemInitializer() {
		// register the system initializer instance
		get_initializers().push_back(this);
	}

public:
	virtual ~SystemInitializer();

	// called on each system initializer during system startup
	virtual void start()=0;

	// called on each system initializer during sytem shutdown
	virtual void stop()=0;

	// called once durin system startup
	static void initialize() {
		// iterate over all registered system initializers and call
		// the start method
		for (size_t i = 0; i < get_initializers().size(); i++) {
			get_initializers()[i]->start();
		}
	}

	// called once durin system shutdown
	static void shutdown() {
		// iterate overa all registered system initializers and
		// call the stop method
		for (size_t i = 0; i < get_initializers().size(); i++) {
			get_initializers()[i]->stop();
		}
	}
};

#endif /* SYSTEMINITIALIZER_H_ */
