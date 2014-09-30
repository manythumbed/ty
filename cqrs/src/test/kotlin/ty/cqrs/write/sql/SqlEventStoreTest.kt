package ty.cqrs.write.sql

import junit.framework.TestCase
import com.google.gson.GsonBuilder
import kotlin.test.assertNotNull
import kotlin.test.failsWith
import com.google.gson.JsonSyntaxException
import org.h2.jdbcx.JdbcConnectionPool
import ty.sql.use
import ty.sql.update
import ty.cqrs.Event
import ty.cqrs.write.Key
import kotlin.test.assertEquals
import ty.cqrs.EventKey
import kotlin.test.assertTrue
import ty.sql.query
import ty.sql.forEach
import ty.cqrs.Version
import ty.cqrs.Id

class SqlEventStoreTest() : TestCase() {
	private val datasource = JdbcConnectionPool.create("jdbc:h2:mem:test", "username", "password")
	private val gson = GsonBuilder().create()!!

	private val streamsTable = "streams"
	private val versionsTable = "versions"

	private val thingId = Id("thing")
	private val thingKey = Key("test-stream")

	private val bertId = Id("bert")
	private val bertKey = Key("bert-stream")

	private val createStream = """
			create table streams (
				id varchar(50),
				stream varchar(100),
				version long,
				type varchar(200),
				data varchar(1000),
				timestamp timestamp,
				tracking long auto_increment,
				primary key(id, stream, version)
			)
		"""

	private val createVersion = """
			create table versions (
				id varchar(50),
				stream varchar(100),
				version long,
				primary key(id, stream)
			)
		"""

	override fun setUp() {
		if (datasource != null) {
			datasource.use {
				it.update(createStream)

				it.update("insert into streams (id, stream, version, type, data) values (?, ?, ?, ?, ?)", listOf("id", "key", 1L, "ty.cqrs.write.sql.TestEvent1", "{}"))
				it.update("insert into streams (id, stream, version, type, data) values (?, ?, ?, ?, ?)", listOf("id", "key", 2L, "ty.cqrs.write.sql.TestEvent1", "{}"))
				it.update("insert into streams (id, stream, version, type, data) values (?, ?, ?, ?, ?)", listOf("id", "key", 3L, "ty.cqrs.write.sql.TestEvent2", "{'things':'111'}"))
				it.update("insert into streams (id, stream, version, type, data) values (?, ?, ?, ?, ?)", listOf("id", "key", 4L, "ty.cqrs.write.sql.TestEvent2", "{'things':'222'}"))
				it.update("insert into streams (id, stream, version, type, data) values (?, ?, ?, ?, ?)", listOf("id", "key", 5L, "ty.cqrs.write.sql.TestEvent2", "{'things':'333'}"))

				it.update("insert into streams (id, stream, version, type, data) values (?, ?, ?, ?, ?)", listOf("bert", "bert-stream", 1L, "ty.cqrs.write.sql.TestEvent1", "{}"))
				it.update("insert into streams (id, stream, version, type, data) values (?, ?, ?, ?, ?)", listOf("bert", "bert-stream", 2L, "ty.cqrs.write.sql.TestEvent1", "{}"))
				it.update("insert into streams (id, stream, version, type, data) values (?, ?, ?, ?, ?)", listOf("bert", "bert-stream", 3L, "ty.cqrs.write.sql.TestEvent2", "{'things':'111'}"))

				it.update(createVersion)
				it.update("insert into versions (id, stream, version) values (?, ?, ?)", listOf("bert", "bert-stream", 3L))
			}
		}
	}

	override fun tearDown() {
		if (datasource != null) {
			datasource.use {
				it.update("drop table streams")
				it.update("drop table versions")
			}
		}
	}

	fun testStream() {
		if (datasource != null) {
			val store = SqlEventStore(datasource, streamsTable, versionsTable, gson)

			val stream = store.stream(Key("key"), Id("id"))
			assertNotNull(stream) {
				assertEquals(5L, it.version.value, "Should be at version 5 but at version ${it.version.value}")
				assertEquals(5, it.events.size, "Should have 5 events in stream but has ${it.events.size}")

				assertTrue(it.events.get(0) is TestEvent1)
				assertTrue(it.events.get(1) is TestEvent1)
				assertTrue(it.events.get(2) is TestEvent2)
				assertTrue(it.events.get(3) is TestEvent2)
				assertTrue(it.events.get(4) is TestEvent2)

				val e1 = it.events.get(2)
				if (e1 is TestEvent2) {
					assertEquals("111", e1.things)
				}

				val e2 = it.events.get(3)
				if (e2 is TestEvent2) {
					assertEquals("222", e2.things)
				}

				val e3 = it.events.get(4)
				if (e3 is TestEvent2) {
					assertEquals("333", e3.things)
				}
			}
		}
	}


	fun testStreamFrom() {
		if (datasource != null) {
			val store = SqlEventStore(datasource, streamsTable, versionsTable, gson)

			val stream = store.streamFrom(Key("key"), Id("id"), Version(3L))
			assertNotNull(stream) {
				assertEquals(5L, it.version.value, "Should be at version 5 but at version ${it.version.value}")
				assertEquals(3, it.events.size, "Should have 3 events in stream but has ${it.events.size}")

				assertTrue(it.events.get(0) is TestEvent2)
				assertTrue(it.events.get(1) is TestEvent2)
				assertTrue(it.events.get(2) is TestEvent2)

				val e1 = it.events.get(0)
				if (e1 is TestEvent2) {
					assertEquals("111", e1.things)
				}

				val e2 = it.events.get(1)
				if (e2 is TestEvent2) {
					assertEquals("222", e2.things)
				}

				val e3 = it.events.get(2)
				if (e3 is TestEvent2) {
					assertEquals("333", e3.things)
				}
			}
		}
	}

	fun testSaveNewStream() {
		if (datasource != null) {
			val store = SqlEventStore(datasource, streamsTable, versionsTable, gson)

			val stored = store.save(thingKey, thingId, Version(1L), listOf(TestEvent1(), TestEvent2("things")))

			assertEquals(true, stored.succeeded, "Should have stored successfully")
			assertEquals(Version(2), stored.version, "Should be at version 2")

			datasource.use {
				it.query("select * from versions where id = ? and stream = ?", listOf(thingId.value, thingKey.value)) { rs ->
					var count = 0
					rs.forEach { row ->
						count = count + 1
						assertEquals(2L, row.getLong("version"))
						assertEquals(thingId.value, row.getString("id"))
						assertEquals(thingKey.value, row.getString("stream"))
					}

					assertEquals(1, count, "Only one row should be returned")
				}

				it.query("select * from streams where id = ? and stream = ? order by version", listOf(thingId.value, thingKey.value)) { rs ->
					var count = 0
					val data = arrayListOf<String>()
					rs.forEach { row ->
						count = count + 1
						assertEquals(thingId.value, row.getString("id"))
						assertEquals(thingKey.value, row.getString("stream"))
						assertEquals(count.toLong(), row.getLong("version"))
						data.add(row.getString("data") as String)
					}

					assertEquals(2, count, "Two rows should be returned")
					assertEquals(2, data.size, "Two pieces of data should have been extracted")
					assertEquals("""{"key":{"value":"test-event-1"}}""", data.get(0))
					assertEquals("""{"things":"things","key":{"value":"test-event-2"}}""", data.get(1))
				}
			}
		}
	}

	fun testShouldNotStoreWithWrongVersion() {
		if (datasource != null) {
			val store = SqlEventStore(datasource, streamsTable, versionsTable, gson)

			val stored = store.save(bertKey, bertId, Version(1L), listOf(TestEvent2("bert is great")))

			assertEquals(false, stored.succeeded, "Should not have stored")
			assertEquals(Version(3L), stored.version)
		}
	}

	fun testShouldUpdateWithCorrectVersion() {
		if (datasource != null) {
			val store = SqlEventStore(datasource, streamsTable, versionsTable, gson)

			val stored = store.save(bertKey, bertId, Version(3L), listOf(TestEvent2("bert is great")))

			assertEquals(true, stored.succeeded, "Should not have stored")
			assertEquals(Version(4L), stored.version)
		}
	}

	fun testGsonDeserialisation() {
		val gson = GsonBuilder().create()

		assertNotNull(gson) { gson ->
			failsWith(javaClass<JsonSyntaxException>()) {
				gson.fromJson("THIS IS NOT JSON", javaClass<Any>())
			}

			failsWith(javaClass<ClassNotFoundException>()) {
				val a: Any? = gson.fromJson("{}", Class.forName("I AM NOT A CLASS"))
				a
			}
		}
	}

}

class TestEvent1() : Event(EventKey("test-event-1"))
class TestEvent2(val things: String) : Event(EventKey("test-event-2"))