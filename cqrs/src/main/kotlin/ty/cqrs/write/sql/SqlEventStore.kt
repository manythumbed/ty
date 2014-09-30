package ty.cqrs.write.sql

import javax.sql.DataSource

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.sql.Connection
import java.sql.Timestamp
import java.util.Date
import ty.cqrs.write.EventStore
import ty.cqrs.write.Key
import ty.cqrs.Id
import ty.sql.use
import ty.cqrs.Version
import ty.cqrs.write.Stored
import ty.cqrs.Event
import ty.sql.query
import ty.sql.forEach
import ty.sql.inTransaction
import ty.sql.single
import ty.sql.update

class SqlEventStore(private val ds: DataSource, private val streamTable: String, private val versionTable: String, private val gson: Gson) : EventStore {

	override fun stream(key: Key, id: Id): ty.cqrs.write.Stream? {
		return streamFrom(key, id, Version(1))
	}

	override fun streamFrom(key: Key, id: ty.cqrs.Id, version: ty.cqrs.Version): ty.cqrs.write.Stream? {
		val query = "select version, type, data from ${streamTable} where id = ? and stream = ? and version >= ? order by version"

		var currentVersion = 0L
		val events = arrayListOf<ty.cqrs.Event>()
		ds.use { c ->
			c.query(query, listOf(id.value, key.value, version.value)) { rs ->
				rs.forEach {
					currentVersion = it.getLong("version")
					val event = event(it.getString("data"), it.getString("type"))
					if (event != null) {
						events.add(event)
					}
				}
			}
		}

		if (events.size > 0) {
			return ty.cqrs.write.Stream(ty.cqrs.Version(currentVersion), events)
		}

		return null
	}

	private fun event(json: String?, eventType: String?): ty.cqrs.Event? {
		try {
			if (json != null && eventType != null) {
				return gson.fromJson(json, Class.forName(eventType))
			}
		}
		catch(ignored: JsonSyntaxException) {
		}
		catch(ignored: ClassNotFoundException) {
		}

		return null
	}

	override fun save(key: Key, id: Id, version: Version, events: List<Event>): Stored {
		var result: Stored = Stored(false, version)

		ds.use {
			it.inTransaction { connection ->
				val current = currentVersion(key, id)
				if (current != null) {
					if (current != version) {
						result = Stored(false, current)
					}
					else {
						var count = current.value
						events.forEach {
							count = count + 1
							storeEvent(connection, key, id, ty.cqrs.Version(count), it)
						}
						val updated = current.increase(events.size)
						updateVersion(connection, key, id, updated)

						result = Stored(true, updated)
					}
				}
				else {
					var count = 0L
					events.forEach {
						count = count + 1
						storeEvent(connection, key, id, ty.cqrs.Version(count), it)
					}
					val updated = ty.cqrs.Version(events.size.toLong())
					storeVersion(connection, key, id, updated)

					result = Stored(true, updated)
				}
			}
		}

		return result
	}

	private fun currentVersion(key: Key, id: Id): Version? {
		val query = "select version from ${versionTable} where id = ? and stream = ?"

		var version: Version? = null
		ds.use {
			it.query(query, listOf(id.value, key.value)) { rs ->
				version = rs.single { ty.cqrs.Version(it.getLong("version")) }
			}
		}

		return version
	}

	private fun storeEvent(connection: Connection, key: Key, id: Id, version: Version, event: Event) {
		val update = "insert into ${streamTable} (id, stream, version, type, data, timestamp) values (?, ?, ?, ?, ?, ?)"

		connection.update(update, listOf(id.value, key.value, version.value, event.javaClass.getName(), json(event), now()))
	}

	private fun json(event: ty.cqrs.Event): String {
		try {
			val json = gson.toJson(event)
			if (json != null) {
				return json
			}
		}
		catch(ignored: RuntimeException) {
		}

		return "{}"
	}

	private fun now(): Timestamp = Timestamp(Date().getTime())

	private fun storeVersion(connection: Connection, key: Key, id: Id, version: Version) {
		val update = "insert into ${versionTable} (id, stream, version) values (?, ?, ?)"

		connection.update(update, listOf(id.value, key.value, version.value))
	}

	private fun updateVersion(connection: Connection, key: Key, id: Id, version: Version) {
		val update = "update ${versionTable} set version = ? where id = ? and stream = ?"

		connection.update(update, listOf(version.value, id.value, key.value))
	}
}