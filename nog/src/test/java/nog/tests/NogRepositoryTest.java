package nog.tests;

import com.google.common.collect.Lists;
import junit.framework.TestCase;
import nog.Nog;
import nog.NogRepository;
import nog.events.Groaned;
import nog.events.Moaned;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ty.cqrs.Event;
import ty.cqrs.Id;
import ty.cqrs.Version;
import ty.cqrs.write.*;

import java.util.List;

public class NogRepositoryTest extends TestCase {

	private final Id first = new Id("first");

	private final Id second = new Id("second");

	private final Moaned moaned = new Moaned();

	private final Groaned groaned = new Groaned();

	@NotNull
	private EventStore events = new EventStore() {
		@Nullable
		@Override
		public Stream stream(@NotNull Key key, @NotNull Id id) {
			if (id.equals(first)) {
				return new Stream(new Version(4), Lists.newArrayList(moaned, groaned, moaned, groaned));
			}

			if (id.equals(second)) {
				return new Stream(new Version(4), Lists.newArrayList(moaned, groaned, moaned, groaned));
			}

			return null;
		}

		@Nullable
		@Override
		public Stream streamFrom(@NotNull Key key, @NotNull Id id, @NotNull Version version) {
			if (id.equals(first)) {
				return null;
			}

			if (id.equals(second)) {
				return new Stream(new Version(6), Lists.newArrayList(moaned, groaned));
			}

			return null;
		}

		@NotNull
		@Override
		public Stored save(@NotNull final Key key, @NotNull final Id id, @NotNull final Version version, @NotNull final List<? extends Event> events) {
			return new Stored(true, new Version(events.size()));
		}
	};

	@NotNull
	private SnapshotStore<Nog> snapshots = new SnapshotStore<Nog>() {
		@Nullable
		@Override
		public Snapshot<Nog> snapshot(@NotNull Key key, @NotNull Id id) {
			if (id.equals(first)) {
				return new Snapshot<Nog>(new Version(3), new Nog(Lists.newArrayList(moaned, groaned, moaned)));
			}

			if (id.equals(second)) {
				return new Snapshot<Nog>(new Version(4), new Nog(Lists.newArrayList(moaned, groaned, moaned, groaned)));
			}

			return null;
		}

		@NotNull
		@Override
		public Stored save(@NotNull Key key, @NotNull Id id, @NotNull Version version, Nog snapshot) {
			return new Stored(true, version);
		}
	};

	public void testShouldFetchFirstNogFromRepository() {
		final NogRepository repository = new NogRepository(events, snapshots);

		final Nog present = repository.fetch(first);
		assertNotNull(present);
		assertEquals("Should have two moans", 2, present.moans());
		assertEquals("Should have one groan", 1, present.groans());
	}

	public void testShouldFetchSecondNogFromRepository() {
		final NogRepository repository = new NogRepository(events, snapshots);

		final Nog present = repository.fetch(second);
		assertNotNull(present);
		assertEquals("Should have three moans", 3, present.moans());
		assertEquals("Should have three groans", 3, present.groans());
	}
}
