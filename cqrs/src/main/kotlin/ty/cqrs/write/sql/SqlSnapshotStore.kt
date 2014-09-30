package ty.cqrs.write.sql

import javax.sql.DataSource
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import ty.cqrs.write.SnapshotStore
import ty.cqrs.write.Key
import ty.cqrs.write.Snapshot
import ty.cqrs.Id
import ty.cqrs.Version
import ty.cqrs.write.Stored
import ty.sql.use
import ty.sql.query
import ty.sql.single
import ty.sql.inTransaction
import ty.sql.update

class SqlSnapshotStore<T>(private val ds: DataSource, private val snapshotTable: String, private val gson: Gson) : SnapshotStore<T> {
	override fun snapshot(key: Key, id: ty.cqrs.Id): Snapshot<T>? {
		val query = "select version, type, data from ${snapshotTable} where id = ? and stream = ?"

		var snapshot: Snapshot<T>? = null
		ds.use {
			it.query(query, listOf(id.value, key.value)) { rs ->
				rs.single {
					snapshot = snapshot(it.getString("data"), it.getString("type"), it.getLong("version"))
				}
			}
		}

		return snapshot
	}

	private fun snapshot(json: String?, itemType: String?, version: Long?): Snapshot<T>? {
		try {
			if (json != null && itemType != null && version != null) {
				val item = gson.fromJson<T>(json, Class.forName(itemType))
				if (item != null) {
					return Snapshot<T>(ty.cqrs.Version(version), item)
				}
			}
		}
		catch(ignored: JsonSyntaxException) {
		}
		catch(ignored: ClassNotFoundException) {
		}

		return null
	}

	override fun save(key: Key, id: Id, version: Version, snapshot: T): Stored {
		val insert = "insert into ${snapshotTable} (id, stream, version, type, data) values(?, ?, ?, ?, ?)"
		val update = "update ${snapshotTable} set version = ?, data = ?  where id = ? and stream = ?"

		ds.use {
			it.inTransaction { connection ->
				val exists = connection.query("select id from ${snapshotTable} where id = ? and stream = ?", listOf(id.value, key.value)) { rs ->
					rs.single { true }
				}

				if (exists != null) {
					it.update(update, listOf(version.value, toJson(snapshot), id.value, key.value))
				}
				else {
					it.update(insert, listOf(id.value, key.value, version.value, snapshot.javaClass.getName(), toJson(snapshot)))
				}
			}
		}

		return Stored(true, version)
	}

	private fun toJson(item: T): String {
		val json = gson.toJson(item)
		if (json != null) {
			return json
		}

		return "{}"
	}
}


