/*
 * concepts.cpp
 *
 * checks concept covering of the type registry
 */

#include "concepts.h"
#include "TypeRegistry.h"
#include "TypeRegistryEntry.h"
#include "TypeRegisterer.h"
#include "utils.h"

using namespace concepts;

//template <class T> inline void ignore_unused_method_warning(T const&) {}

static void dummy(){
	// check archetype classes
	BOOST_CONCEPT_ASSERT((ObjectBase_concept<ObjectBase_archetype>));
	BOOST_CONCEPT_ASSERT((Object_concept<Object_archetype>));
	BOOST_CONCEPT_ASSERT((Description_concept<Description_archetype>));
	BOOST_CONCEPT_ASSERT((DescriptionBase_concept<DescriptionBase_archetype>));
	BOOST_CONCEPT_ASSERT((constructible_concept<Object_archetype,Description_archetype>));

	// check implementation with archetypes
	TypeRegistry<ObjectBase_archetype>::find(NULL);
	TypeRegisterer<Object_archetype> reg;
	TypeRegistryEntry<Object_archetype> entry;

	//void (*ptr)()=dummy;
	//ignore_unused_variable_warning(ptr);
	boost::ignore_unused_variable_warning(&dummy);
}


