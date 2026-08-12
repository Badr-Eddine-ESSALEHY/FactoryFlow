package com.factoryflow.app.core.network

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import java.math.BigDecimal

class BigDecimalJsonAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): BigDecimal? = when (reader.peek()) {
        JsonReader.Token.NULL -> reader.nextNull()
        JsonReader.Token.NUMBER, JsonReader.Token.STRING -> reader.nextString().toBigDecimalOrNull()
        else -> throw IllegalArgumentException("Expected decimal or null")
    }

    @ToJson
    fun toJson(writer: com.squareup.moshi.JsonWriter, value: BigDecimal?) {
        if (value == null) writer.nullValue() else writer.value(value)
    }
}
