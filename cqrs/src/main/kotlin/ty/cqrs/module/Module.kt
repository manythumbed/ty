package ty.cqrs.module

import ty.cqrs.command.Dispatcher

trait Module {
	fun dispatcher(): Dispatcher
}

