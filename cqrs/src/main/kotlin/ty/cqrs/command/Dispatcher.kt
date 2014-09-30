package ty.cqrs.command

import java.lang.reflect.InvocationTargetException

abstract class Dispatcher() {
	fun dispatch(command: Command): Result? {
		try {
			val method = this.javaClass.getDeclaredMethod("handle", command.javaClass)
			method.setAccessible(true)

			return method.invoke(this, command) as Result
		}
		catch (e: NoSuchMethodException) {
		}
		catch (e: InvocationTargetException) {
		}
		catch (e: IllegalAccessException) {
		}

		return null
	}
}