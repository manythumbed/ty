package ty.cqrs.write.sql

import junit.framework.TestCase
import org.h2.jdbcx.JdbcConnectionPool
import com.google.gson.GsonBuilder
import ty.sql.use
import ty.sql.update
import ty.cqrs.Id
import ty.cqrs.write.Key
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import ty.cqrs.Version

class SqlSnapshotStoreTest() : TestCase() {
	private val datasource = JdbcConnectionPool.create("jdbc:h2:mem:test", "username", "password")!!
	private val gson = GsonBuilder().create()!!

	private val snapshotsTable = "snapshots"

	private val createSnapshot = """
			create table snapshots (
				id varchar(50),
				stream varchar(100),
				version long,
				type varchar(200),
				data varchar(1000),
				primary key(id, stream)
			)
		"""

	private val id = Id("thing")
	private val key = Key("thing")
	private val thing = TestThing("thing", listOf(1, 2, 3))

	override fun setUp() {
		datasource.use {
			it.update(createSnapshot)

			val data: String = gson.toJson(thing)!!
			val className = javaClass<TestThing>().getName()
			it.update("insert into ${snapshotsTable} (id, stream, version, type, data) values(?, ?, ?, ?, ?)", listOf(id.value, key.value, 10L, className, data))
		}
	}

	override fun tearDown() {
		datasource.use {
			it.update("drop table ${snapshotsTable}")
		}
	}

	fun testSnapshot() {
		val store = SqlSnapshotStore<TestThing>(datasource, snapshotsTable, gson)

		assertNotNull(store.snapshot(key, id)) {
			assertEquals(Version(10), it.version)
			assertEquals(thing.thing, it.item.thing)
			assertEquals(thing.things, it.item.things)
		}
	}

	fun testSave() {
		val store = SqlSnapshotStore<TestThing>(datasource, snapshotsTable, gson)

		assertNotNull(store.snapshot(key, id)) {
			assertEquals(Version(10), it.version)
			assertEquals(thing.thing, it.item.thing)
			assertEquals(thing.things, it.item.things)
		}

		val update = TestThing("2", listOf(1, 2, 3, 4))

		val stored = store.save(key, id, Version(11L), update)
		assertEquals(true, stored.succeeded)

		assertNotNull(store.snapshot(key, id)) {
			assertEquals(Version(11L), it.version)
			assertEquals(update.thing, it.item.thing)
			assertEquals(update.things, it.item.things)
		}

		val anotherThing = Id("brand new")
		assertEquals(true, store.save(key, anotherThing, Version(11L), update).succeeded)

		assertNotNull(store.snapshot(key, anotherThing)) {
			assertEquals(Version(11L), it.version)
			assertEquals(update.thing, it.item.thing)
			assertEquals(update.things, it.item.things)
		}
	}

	data class TestThing(val thing: String, val things: List<Int>)
}
