package nog.tests;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import junit.framework.TestCase;
import nog.Nog;
import nog.events.Groaned;
import nog.events.Moaned;
import ty.cqrs.write.EventManager;
import ty.cqrs.write.EventManagerDeserializer;
import ty.cqrs.write.EventManagerSerializer;

public class NogTest extends TestCase {
	private final Moaned moaned = new Moaned();

	private final Groaned groaned = new Groaned();

	public void testNogBehaviour() {
		final Nog nog = new Nog(Lists.newArrayList(moaned, moaned, groaned, groaned));

		assertEquals("Should be at version 4", 4, nog.version().getValue());
		assertEquals("Should have groaned twice", 2, nog.groans());
		assertEquals("Should have moaned twice", 2, nog.moans());

		nog.moan();
		nog.moan();
		nog.moan();
		nog.groan();

		assertEquals("Should have groaned thrice", 3, nog.groans());
		assertEquals("Should have moaned five times", 5, nog.moans());
		assertEquals("Should still be at version 4", 4, nog.version().getValue());

		assertEquals("Should have 2 changes recorded", 4, nog.changes().size());
		assertEquals("You can only query changes once", 0, nog.changes().size());

		nog.jig();
		assertEquals("Should have 5 changes recorded for a successful jig", 5, nog.changes().size());
	}

	public void testUpdating() {
		final Nog nog = new Nog(Lists.newArrayList(moaned, moaned, groaned, groaned));

		assertEquals("Should be at version 4", 4, nog.version().getValue());

		nog.update(Lists.newArrayList(moaned, moaned, moaned, groaned));

		assertEquals("Should have groaned thrice", 3, nog.groans());
		assertEquals("Should have moaned five times", 5, nog.moans());
		assertEquals("Should be at version 8", 8, nog.version().getValue());
	}

	public void testJsonStorage() {
		final Nog nog = new Nog(Lists.newArrayList(moaned, moaned, groaned, groaned));

		final Gson gson = gson();

		final String json = gson.toJson(nog);

		assertEquals("Should be at version 4", 4, nog.version().getValue());
		assertEquals("Should serialize sensibly", "{\"eventManager\":4,\"groans\":2,\"moans\":2}", json);

		final Nog nogsTwin = gson.fromJson(json, Nog.class);

		assertEquals("Nogs twin should be at version 4", 4, nog.version().getValue());
		assertEquals("Nogs twin has moaned the same number of times as Nog", nog.moans(), nogsTwin.moans());
		assertEquals("Nogs twin has groaned the same number of times as Nog", nog.groans(), nogsTwin.groans());
	}

	private Gson gson() {
		final GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(EventManager.class, new EventManagerSerializer());
		builder.registerTypeAdapter(EventManager.class, new EventManagerDeserializer());

		return builder.create();
	}
}
