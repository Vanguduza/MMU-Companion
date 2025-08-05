package com.aeci.mmucompanion.data.local

import androidx.room.TypeConverter
import com.aeci.mmucompanion.domain.model.FormStatus
import com.aeci.mmucompanion.domain.model.FormType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromFormType(formType: FormType): String = formType.name

    @TypeConverter
    fun toFormType(formType: String): FormType = FormType.valueOf(formType)

    @TypeConverter
    fun fromFormStatus(formStatus: FormStatus): String = formStatus.name

    @TypeConverter
    fun toFormStatus(formStatus: String): FormStatus = FormStatus.valueOf(formStatus)

    @TypeConverter
    fun fromStringList(value: List<String>): String = Gson().toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromStringMap(value: Map<String, Any>): String = Gson().toJson(value)

    @TypeConverter
    fun toStringMap(value: String): Map<String, Any> {
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        return Gson().fromJson(value, mapType)
    }
}
