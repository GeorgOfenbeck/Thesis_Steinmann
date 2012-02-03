/*
 * ConfiguratorBase.h
 *
 *  Created on: Feb 1, 2012
 *      Author: ruedi
 */

#ifndef CONFIGURATORBASE_H_
#define CONFIGURATORBASE_H_
#include "PolymorphicBase.h"
#include "sharedDOM/ConfiguratorDescriptionBase.h"

class ConfiguratorBase: public PolymorphicBase {
public:
public:
	typedef ConfiguratorDescriptionBase tDescriptionBase;
	virtual ~ConfiguratorBase();

	virtual ConfiguratorDescriptionBase *getConfiguratorDescription()=0;

	virtual void beforeMeasurement(){};
	virtual void afterMeasurement(){};
	virtual void beforeRun(){};
	virtual void afterRun(){};
};

template<class TDescription>
class Configurator : public ConfiguratorBase{
protected:
	TDescription *description;
public:
	typedef TDescription tDescription;
	typedef ConfiguratorBase tBase;

	Configurator(TDescription *description){
		this->description=description;
	}

	ConfiguratorDescriptionBase *getConfiguratorDescription(){
		return description;
	}
};

#endif /* CONFIGURATORBASE_H_ */

