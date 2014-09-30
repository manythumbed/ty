package ty.cqrs.command

abstract class Command()

data class Result(val success: Boolean, val message: String)

abstract class Handler<T : Command>() {
	abstract fun handle(command: T): Result
	fun succeeded(): Result = Result(true, "")
	fun failed(message: String): Result = Result(false, message)
}