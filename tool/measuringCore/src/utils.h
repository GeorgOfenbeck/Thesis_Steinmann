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

struct utils{

	virtual ~utils();

	template<typename Tcoll>
	static void push_all_back(Tcoll &coll){
		// nothing to do
	}

	template<typename Tcoll, typename T, typename ...TArgs>
	static void push_all_back(Tcoll &coll, T value, TArgs... args){
		coll.push_back(value);
		push_all_back(coll,args...);
	}

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


public:
	template<typename ... TArgs>
	static size_t args_size(){
		return count<TArgs...>::value;
	}


	/*template<typename THead, typename ... TTail>
	static size_t args_size(){
		return 1+args_size<TTail...>();
	}*/
private:

	template<typename TColl>
	static bool can_cast_elements_imp(typename TColl::iterator &it,typename TColl::iterator &end){
		return it==end;
	}

	template<typename TColl, typename T, typename ... TArgs>
	static bool can_cast_elements_imp(typename TColl::iterator &it, typename TColl::iterator &end){
		if (*it!=NULL){
			T *tmp=dynamic_cast<T*>(*it);
			if (tmp==NULL){
				return false;
			}
		}
		it++;
		return can_cast_elements_imp<TColl,TArgs...>(it,end);
	}
public:
	template<typename TColl, typename ... TArgs>
	static bool can_cast_elements(TColl coll){
		typename TColl::iterator it=coll.begin();
		typename TColl::iterator end=coll.end();
		return can_cast_elements_imp<TColl,TArgs...>(it,end);
	}

};

template <>
struct utils::count <>
{
      static const int value = 0;
};

#endif /* UTILS_H_ */
