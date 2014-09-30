package ty.cqrs.write

import java.lang.reflect.Type
import java.lang.reflect.InvocationTargetException
import com.google.gson.JsonSerializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonPrimitive
import ty.cqrs.Event
import ty.cqrs.Version

class EventManager {
	private var currentVersion = ty.cqrs.Version(1)
	private var changeList = arrayListOf<Event>()

	val version: Version
		get() = currentVersion

	val changes: List<Event>
		get() {
			val list = arrayListOf<Event>()
			list.addAll(changeList)

			changeList.clear()
			return list
		}

	fun build(state: Any, events: List<Event>): EventManager {
		events.forEach { process(state, it) }

		if (!events.empty) {
			currentVersion = Version(events.size.toLong())
		}

		return this
	}

	fun change(state: Any, event: Event) {
		process(state, event)
		changeList.add(event)
	}

	fun update(state: Any, events: List<Event>) {
		changeList = arrayListOf<Event>()
		events.forEach { process(state, it) }
		currentVersion = currentVersion.increase(events.size())
	}

	private fun process(state: Any, event: Event) {
		try {
			val method = state.javaClass.getDeclaredMethod("handle", event.javaClass)
			method.setAccessible(true)

			method.invoke(state, event)
		}
		catch (e: NoSuchMethodException) {
		}
		catch (e: InvocationTargetException) {
		}
		catch (e: IllegalAccessException) {
		}
	}

	internal fun version(v: Version) {
		currentVersion = v
	}
}

class EventManagerSerializer() : JsonSerializer<EventManager> {
	override fun serialize(eventManager: EventManager?, typeParam: Type?, context: JsonSerializationContext?): JsonElement? {
		if (eventManager != null) {
			return JsonPrimitive(eventManager.version.value)
		}

		return null
	}
}

class EventManagerDeserializer() : JsonDeserializer<EventManager> {
	override fun deserialize(element: JsonElement?, typeParam: Type?, context: JsonDeserializationContext?): EventManager? {
		if (element != null && element is JsonPrimitive) {
			val eventManager = EventManager()
			eventManager.version(Version(element.getAsLong()))

			return eventManager
		}

		return null
	}
}

trait Aggregate {
	fun update(events: List<Event>): Unit
	fun version(): Version
	fun changes(): List<Event>
}
