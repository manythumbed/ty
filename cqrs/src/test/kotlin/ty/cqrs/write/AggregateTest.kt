package ty.cqrs.write

import kotlin.test.assertEquals
import com.google.gson.Gson
import ty.cqrs.Version
import ty.cqrs.Event
import ty.cqrs.EventKey


class AggregateTest() : junit.framework.TestCase() {

	fun testCreation() {
		val d = TestAggregate(listOf(ThingEvent("go")))

		assertEquals(1, d.thingCount)

		d.addThing("stop")

		kotlin.test.assertEquals(2, d.thingCount)
		kotlin.test.assertEquals(1, d.changes.size)
	}

	fun testUpdate() {
		val d = TestAggregate(listOf(ThingEvent("go")))
		kotlin.test.assertEquals(1, d.thingCount)
		assertEquals(Version(1), d.version)

		d.update(listOf(ThingEvent("stop")))
		kotlin.test.assertEquals(2, d.thingCount)
		kotlin.test.assertEquals(Version(2), d.version)

	}

	fun testJson() {
		val d = TestAggregate(listOf(ThingEvent("go")))
		d.addThing("stop")

		val gson = gson()
		val json = gson.toJson(d)

		assertEquals("""{"things":["go","stop"],"eventManager":1}""", json)

		kotlin.test.assertNotNull(gson.fromJson(json, javaClass<TestAggregate>())) { desired ->
			assertEquals(d.thingCount, desired.thingCount)
			kotlin.test.assertEquals(d.version, desired.version)
			assertEquals(0, desired.changes.size)

			desired.addThing("again")
			assertEquals(3, desired.thingCount)
			assertEquals(1, desired.changes.size)
		}
	}

	private fun gson(): Gson {
		val builder = com.google.gson.GsonBuilder()
		builder.registerTypeAdapter(javaClass<EventManager>(), EventManagerDeserializer())
		builder.registerTypeAdapter(javaClass<EventManager>(), EventManagerSerializer())

		return builder.create()!!
	}

	data class ThingEvent(val message: String) : Event(EventKey("ThingEvent"))

	class TestAggregate(events: List<Event>) {
		private val things: java.util.ArrayList<String> = arrayListOf()

		val thingCount: Int
			get() = things.size

		fun addThing(message: String) {
			eventManager.change(this, ThingEvent(message))
		}

		fun update(events: List<Event>) {
			eventManager.update(this, events)
		}

		private fun handle(event: ThingEvent) {
			things.add(event.message)
		}

		val changes: List<Event>
			get() = eventManager.changes

		val version: Version
			get() = eventManager.version

		private val eventManager = EventManager().build(this, events)
	}

}


