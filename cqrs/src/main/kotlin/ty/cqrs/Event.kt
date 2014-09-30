package ty.cqrs

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes

data class EventKey(val value: String) {
	{
		require(value.isNotEmpty(), "An event must have a unique key")
	}

	fun matches(key: String): Boolean {
		return value.equals(key)
	}
}

fun eventKey<T>(c: Class<T>) = EventKey(c.getName())

abstract class Event(val key: EventKey) {
	open fun conflict(event: Event): Boolean {
		return true
	}
}

class EventKeyExclusionStrategy : ExclusionStrategy {
	override fun shouldSkipField(p0: FieldAttributes?): Boolean {
		if (p0 != null) {
			return p0.getDeclaredClass() == javaClass<EventKey>()
		}

		return false
	}

	override fun shouldSkipClass(p0: Class<out Any?>?): Boolean {
		return false
	}
}