package ty.cqrs.read

import ty.cqrs.Id
import ty.cqrs.Event

trait Publisher {
	fun publish(id: Id, event: Event)
}

class StandardPublisher(private val externalPublisher: Publisher, private val subscribers: List<Subscriber>) : Publisher {
	override fun publish(id: ty.cqrs.Id, event: ty.cqrs.Event) {
		try {
			externalPublisher.publish(id, event)
		}
		catch(ignored: Exception) {
		}
		catch(ignored: RuntimeException) {
		}

		subscribers.forEach {
			try {
				it.process(id, event)
			}
			catch(ignored: Exception) {
			}
			catch(ignored: RuntimeException) {
			}
		}
	}
}

