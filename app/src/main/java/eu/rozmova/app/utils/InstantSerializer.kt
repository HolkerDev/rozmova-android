package eu.rozmova.app.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.Instant
import java.time.format.DateTimeFormatter

val instantSerializer =
    object : JsonSerializer<Instant> {
        override fun serialize(
            src: Instant,
            typeOfSrc: Type,
            context: JsonSerializationContext,
        ): JsonElement = JsonPrimitive(DateTimeFormatter.ISO_INSTANT.format(src))
    }

val instantDeserializer =
    object : JsonDeserializer<Instant> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext,
        ): Instant = Instant.parse(json.asString)
    }
