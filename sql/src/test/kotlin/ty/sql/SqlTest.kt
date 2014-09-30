package ty.sql

import junit.framework.TestCase
import org.h2.jdbcx.JdbcConnectionPool
import kotlin.test.assertEquals

class SqlTest() : TestCase() {
	private val datasource = JdbcConnectionPool.create("jdbc:h2:mem:test", "username", "password")

	private val createTable = """
			create table things (
				id varchar(50),
				stream varchar(100),
				version int,
				type varchar(200),
				data varchar(1000),
				timestamp timestamp,
				tracking int auto_increment,
				primary key(id, stream, version)
			)
		"""

	override fun setUp() {
		if (datasource != null) {
			datasource.use { it.update(createTable) }
		}
	}

	override fun tearDown() {
		if (datasource != null) {
			datasource.use { it.update("drop table things") }
		}
	}

	fun testQuery() {
		test.assertNotNull(datasource) { ds ->
			ds.use { connection ->
				connection.query("select count(*) from things", listOf()) { rs ->
					rs.forEach { assertEquals(0, rs.getInt(1)) }
				}

				connection.update("insert into things (id, stream, version) values (?, ?, ?)", listOf("id", "stream", 1))

				connection.query("select count(*) from things", listOf()) { rs ->
					rs.forEach { assertEquals(1, rs.getInt(1)) }
				}

				connection.query("select count(*) from things", listOf()) { rs ->
					rs.forEach { assertEquals(1, rs.getInt(1)) }
				}

				connection.query("select count(*) from things", listOf()) { rs ->
					val count = rs.map { rs.getInt(1) }.first()
					assertEquals(1, count)
				}
			}
		}
	}

	fun testTransaction() {
		test.assertNotNull(datasource) { ds ->
			ds.use {
				it.inTransaction { connection ->
					connection.update("insert into things (id, stream, version) values (?, ?, ?)", listOf("id", "stream", 2))
					connection.update("insert into things (id, stream, version) values (?, ?, ?)", listOf("id", "stream", 3))

					connection.query("select count(*) from things", listOf()) { rs ->
						assertEquals(2, rs.map { rs.getInt(1) }.first())
					}
				}

				it.query("select count(*) from things", listOf()) { rs ->
					assertEquals(2, rs.map { rs.getInt(1) }.first())
				}
			}
		}
	}

	fun testTransactionRollback() {
		test.assertNotNull(datasource) { ds ->
			ds.use {
				it.inTransaction { connection ->
					connection.update("insert into things (id, stream, version) values (?, ?, ?)", listOf("id", "stream", 2))
					connection.update("insert into things (id, stream, version) values (?, ?, ?)", listOf("id", "stream", 3))

					connection.query("select count(*) from things", listOf()) { rs ->
						assertEquals(2, rs.map { rs.getInt(1) }.first(), "Should be 2 things before transaction rollback")
					}
					throw IllegalStateException()
				}

				it.query("select count(*) from things", listOf()) { rs ->
					assertEquals(0, rs.map { rs.getInt(1) }.first(), "Should be 0 things after transaction rollback")
				}
			}
		}
	}
}