package ty.cqrs.read

import ty.cqrs.Id
import ty.cqrs.Event

trait Subscriber {
	fun process(id: Id, event: Event)
}