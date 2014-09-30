package ty.cqrs.write

import ty.cqrs.Version
import ty.cqrs.Id
import ty.cqrs.Event

data class Key(val value: String) {
	{
		require(value.isNotEmpty(), "A key cannot be empty")
	}
}

data class Stream(val version: Version, val events: List<Event>)
data class Stored(val succeeded: Boolean, val version: Version)

trait EventStore {

	fun stream(key: Key, id: Id): Stream?
	fun streamFrom(key: Key, id: Id, version: Version): Stream?
	fun save(key: Key, id: Id, version: Version, events: List<Event>): Stored
}