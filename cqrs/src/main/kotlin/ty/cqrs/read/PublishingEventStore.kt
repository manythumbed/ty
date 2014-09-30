package ty.cqrs.read

import ty.cqrs.write.EventStore
import ty.cqrs.write.Key
import ty.cqrs.Event
import ty.cqrs.write.Stored
import ty.cqrs.Id

class PublishingEventStore(private val publisher: Publisher, private val store: EventStore) : EventStore {
	override fun stream(key: Key, id: Id): ty.cqrs.write.Stream? {
		return store.stream(key, id)
	}

	override fun streamFrom(key: Key, id: ty.cqrs.Id, version: ty.cqrs.Version): ty.cqrs.write.Stream? {
		return store.streamFrom(key, id, version)
	}

	override fun save(key: Key, id: ty.cqrs.Id, version: ty.cqrs.Version, events: List<Event>): Stored {
		val stored = store.save(key, id, version, events)
		if (stored.succeeded) {
			events.forEach {
				publisher.publish(id, it)
			}
		}

		return stored
	}
}

