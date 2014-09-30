package ty.cqrs.write

import ty.cqrs.Version
import ty.cqrs.Id

data class Snapshot<T>(val version: Version, val item: T)

trait SnapshotStore<T> {
	fun snapshot(key: Key, id: Id): Snapshot<T>?
	fun save(key: Key, id: Id, version: Version, snapshot: T): Stored
}
