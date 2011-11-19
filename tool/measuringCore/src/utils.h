/*
 * utils.h
 *
 *  Created on: Nov 17, 2011
 *      Author: ruedi
 */

#ifndef UTILS_H_
#define UTILS_H_

#include <vector>
#include <list>
#include <cstddef>

/*
 * contains various utility functions
 */
struct utils{

	virtual ~utils();

	/*
	 * push all elements of parameter pack into a collection
	 * base case
	 */
	template<typename Tcoll>
	static void push_all_back(Tcoll &coll){
		// nothing to do
	}

	/*
	 * push all elements of parameter pack into a collection
	 */
	template<typename Tcoll, typename T, typename ...TArgs>
	static void push_all_back(Tcoll &coll, T value, TArgs... args){
		// append the element
		coll.push_back(value);

		// recursion
		push_all_back(coll,args...);
	}

	/*
	 * convert an parameter pack into a collection
	 */
	template<typename Tcoll, typename ...TArgs>
	static Tcoll toCollection(TArgs... args){
		Tcoll result;
		push_all_back(result,args...);
		return result;
	}

private:
	// Declaration of primary variadic template.
	template <typename... T>
	struct count;

	// Partial specialisation of the primary template for head and tail decomposition.
	template <typename H, typename... T>
	struct count<H, T...>
	{
	      static const int value = 1 + count<T...>::value;
	};

	// base case defined outside of the class (see bottom)

public:
	/*
	 * returns the size of a type argument pack
	 */
	template<typename ... TArgs>
	static size_t args_size(){
		return count<TArgs...>::value;
	}

private:

	// base case
	template<typename TColl>
	static bool can_cast_elements_imp(typename TColl::iterator &it,typename TColl::iterator &end){
		// check if the iterator is at the end
		return it==end;
	}

	template<typename TColl, typename T, typename ... TArgs>
	static bool can_cast_elements_imp(typename TColl::iterator &it, typename TColl::iterator &end){
		// null can always be casted
		if (*it!=NULL){
			// try to cast the collection element to the the current type
			T *tmp=dynamic_cast<T*>(*it);
			if (tmp==NULL){
				return false;
			}
		}

		// advance the iterator
		it++;

		// do the recursion
		return can_cast_elements_imp<TColl,TArgs...>(it,end);
	}
public:
	/*
	 * returns if all elements in coll can be casted to the types
	 * specified in TArgs. If an argument is null, it is considered to match.
	 */
	template<typename TColl, typename ... TArgs>
	static bool can_cast_elements(TColl coll){
		typename TColl::iterator it=coll.begin();
		typename TColl::iterator end=coll.end();
		return can_cast_elements_imp<TColl,TArgs...>(it,end);
	}

};

// base case for counting the number of types in a type pack
template <>
struct utils::count <>
{
      static const int value = 0;
};

#endif /* UTILS_H_ */
