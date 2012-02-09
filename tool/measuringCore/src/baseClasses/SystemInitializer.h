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

protected:
	SystemInitializer() {
		// register the system initializer instance
		getInitializers().push_back(this);
	}

public:
	// returns the singleton system initializer list
	static std::vector<SystemInitializer*> &getInitializers() {
		// initialized during the first call to the method
		// remains the same afterwards

		// can't use a static class variable, since the order the
		// variables are initialized is not defined
		static std::vector<SystemInitializer*> initializers;
		return initializers;
	}

	virtual ~SystemInitializer();

	// called on each system initializer during system startup
	virtual void start()=0;

	// called on each system initializer during sytem shutdown
	virtual void stop()=0;
};

#endif /* SYSTEMINITIALIZER_H_ */
