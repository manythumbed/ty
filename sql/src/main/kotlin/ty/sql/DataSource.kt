package ty.sql

import java.sql.Connection
import javax.sql.DataSource

fun <T> DataSource.use(block: (Connection) -> T): T {
	val connection = getConnection()
	if (connection != null) {
		try {
			return block(connection)
		}
		finally {
			connection.close()
		}
	}

	throw IllegalStateException("No Connection can be obtained from $this")
}
