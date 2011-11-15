/*
 * TypeRegisterer.h
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#ifndef TYPEREGISTERER_H_
#define TYPEREGISTERER_H_

#include "TypeRegistry.h"
#include "TypeRegistryEntry.h"

template <class TObject>
class TypeRegisterer {
public:
	TypeRegisterer(){
		TypeRegistryEntry<TObject> *entry=new TypeRegistryEntry<TObject>();
		TypeRegistry<typename TObject::tBase>::add_entry(entry);
	}
};

#endif /* TYPEREGISTERER_H_ */
