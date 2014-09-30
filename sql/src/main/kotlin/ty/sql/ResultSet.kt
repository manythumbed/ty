package ty.sql

import java.sql.ResultSet

fun ResultSet.forEach(block: (ResultSet) -> Unit) {
	while (this.next()) {
		block(this)
	}
}

fun <T : Any> ResultSet.single(block: (ResultSet) -> T): T? {
	if (this.next()) {
		return block(this)
	}

	return null
}

fun <T> ResultSet.map(fn: (ResultSet) -> T): List<T> {
	val result = arrayListOf<T>()
	while (this.next()) {
		result.add(fn(this))
	}

	return result
}

