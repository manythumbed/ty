package ty.cqrs.write

import ty.cqrs.Version
import ty.cqrs.Id
import ty.cqrs.Event

trait Repository<T : Any> {
	fun fetch(id: Id): T?
	fun save(id: Id, item: T): Boolean
}

abstract class BaseRepository<T : Any>(private val key: Key, private val events: EventStore, private val snapshots: SnapshotStore<T>) : Repository<T> {
	override fun fetch(id: Id): T? {
		val snapshot = snapshots.snapshot(key, id)
		if (snapshot != null) {
			val stream = events.streamFrom(key, id, snapshot.version)
			if (stream != null) {
				return update(snapshot.item, stream.events)
			}

			return snapshot.item
		}

		val stream = events.stream(key, id)
		if (stream != null) {
			return create(stream.events)
		}

		return null
	}

	override fun save(id: Id, item: T): Boolean {
		val stored = events.save(key, id, version(item), changes(item))

		return stored.succeeded
	}

	abstract fun update(item: T, changes: List<Event>): T
	abstract fun create(events: List<Event>): T
	abstract fun changes(item: T): List<Event>
	abstract fun version(item: T): Version
}

abstract class AggregateRepository<T : Aggregate>(key: Key, events: EventStore, snapshots: SnapshotStore<T>) : BaseRepository<T>(key, events, snapshots) {
	override fun update(item: T, changes: List<Event>): T {
		item.update(changes)
		return item
	}

	override fun changes(item: T): List<Event> {
		return item.changes()
	}

	override fun version(item: T): Version {
		return item.version()
	}
}



