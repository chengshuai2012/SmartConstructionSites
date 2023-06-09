package com.aoecloud.smartconstructionsites.network

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.internal.`$Gson$Types`
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class MyTypeAdapterFactory : TypeAdapterFactory {

    override fun <T : Any?> create(gson: Gson, typeToken: TypeToken<T>): TypeAdapter<T>? {
        val rawType = typeToken.rawType
        if (rawType == String::class.java) {
            return StringNullAdapter() as TypeAdapter<T>
        }
       	        
        //不用rawType==Collection::class.java，因为集合可能为ArrayList、List、或者其它继承自List的类型，写==就定死了，这个isAssignableFrom()方法表示该对象表示的类或接口与指定参数表示的类或接口相同，或其超类或超接口相同，则返回true
        if(Collection::class.java.isAssignableFrom(rawType)){
            //获取底层实例
            val type: Type = typeToken.type
            //获取此集合内的元素类型
            val elementType: Type = `$Gson$Types`.getCollectionElementType(type, rawType)
            //生成该元素类型的TypeAdapter
            val elementTypeAdapter = gson.getAdapter(TypeToken.get(elementType))
            return CollectionTypeAdapter(elementTypeAdapter) as TypeAdapter<T>
        }
        return null
    }
}