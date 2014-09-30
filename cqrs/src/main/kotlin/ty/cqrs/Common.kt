package ty.cqrs

data class Id(val value: String) {
	{
		require(value.isNotEmpty(), "An id cannot be empty")
	}
}

data class Version(val value: Long) {
	{
		require(value > 0, "A version must be greater than zero")
	}

	fun increase(amount: Int): Version {
		if (amount > 0) {
			return Version(this.value + amount)
		}

		return this
	}
}
