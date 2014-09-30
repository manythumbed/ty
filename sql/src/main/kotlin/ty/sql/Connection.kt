package ty.sql

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp

fun <T> Connection.query(sql: String, block: (ResultSet) -> T): T {
	return this.query(sql, listOf(), block)
}

fun <T> Connection.query(sql: String, parameters: List<Any>, block: (ResultSet) -> T): T {
	return withStatement(this, sql, parameters) { st ->
		val rs = st.executeQuery()
		try {
			block(rs)
		}
		finally {
			rs.close()
			st.close()
		}
	}
}

fun Connection.update(sql: String) {
	this.update(sql, listOf())
}

fun Connection.update(sql: String, parameters: List<Any>) {
	withStatement(this, sql, parameters) {
		it.executeUpdate()
	}
}

fun Connection.inTransaction(block: (Connection) -> Unit) {
	this.setAutoCommit(false)
	this.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE)

	try {
		block(this)
		this.commit()
	}
	catch(e: RuntimeException) {
		this.rollback()
	}
	catch(e: Exception) {
		e.printStackTrace()
		this.rollback()
	}
	finally {
		this.setAutoCommit(true)
	}
}

private fun <T> withStatement(connection: Connection, sql: String, parameters: List<Any>, block: (PreparedStatement) -> T): T {
	val statement = buildStatement(connection, sql, parameters)

	if (statement != null) {
		try {
			return block(statement)
		}
		finally {
			statement.close()
		}
	}

	throw IllegalStateException("Unable to obtain prepared statement from connection $connection")
}

private fun buildStatement(connection: Connection, sql: String, parameters: List<Any>): PreparedStatement? {
	val statement = connection.prepareStatement(sql)
	if (statement != null) {
		var index = 1
		parameters.forEach {
			when (it) {
				is Int -> statement.setInt(index, it)
				is Long -> statement.setLong(index, it)
				is String -> statement.setString(index, it)
				is Timestamp -> statement.setTimestamp(index, it)
			}
			index = index + 1
		}
	}
	return statement
}
