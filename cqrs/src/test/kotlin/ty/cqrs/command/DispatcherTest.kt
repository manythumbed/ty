package ty.cqrs.command

import junit.framework.TestCase
import kotlin.test.assertEquals

class DispatcherTest() : TestCase() {

	fun testDispatch() {
		val dispatcher = TestDispatcher()
		dispatcher.dispatch(TestCommand1())
		dispatcher.dispatch(TestCommand2())
		dispatcher.dispatch(Unhandled())

		assertEquals(true, dispatcher.h1.called)
		assertEquals(true, dispatcher.h2.called)
		assertEquals(false, dispatcher.h3.called)

	}
}

class TestDispatcher() : Dispatcher() {
	val h1 = TestCommand1Handler()
	val h2 = TestCommand2Handler()
	val h3 = UnhandledHandler()

	private fun handle(command: TestCommand1) = h1.handle(command)

	private fun handle(command: TestCommand2) = h2.handle(command)
}

class TestCommand1() : Command()
class TestCommand2() : Command()
class Unhandled() : Command()

abstract class TestHandler<T : Command>() : Handler<T>() {
	var called = false
}

class TestCommand1Handler() : TestHandler<TestCommand1>() {
	override fun handle(command: TestCommand1): Result {
		called = true

		return succeeded()
	}
}

class TestCommand2Handler() : TestHandler<TestCommand2>() {
	override fun handle(command: TestCommand2): Result {
		called = true

		return succeeded()
	}
}

class UnhandledHandler() : TestHandler<Unhandled>() {
	override fun handle(command: Unhandled): Result {
		called = true

		return succeeded()
	}
}

