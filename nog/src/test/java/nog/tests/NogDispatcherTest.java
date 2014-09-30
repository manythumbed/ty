package nog.tests;

import com.google.common.collect.Lists;
import junit.framework.TestCase;
import nog.Nog;
import nog.NogRepository;
import nog.commands.Groan;
import nog.commands.NogDispatcher;
import nog.events.Groaned;
import nog.events.Jigged;
import nog.events.Moaned;
import nog.events.NogEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ty.cqrs.Event;
import ty.cqrs.Id;
import ty.cqrs.Version;
import ty.cqrs.command.Result;
import ty.cqrs.write.*;

import java.util.ArrayList;
import java.util.List;

public class NogDispatcherTest extends TestCase {
	private final Id id = new Id("nog");

	private final Version version = new Version(10);

	@Nullable
	private final EventStore eventStore = new EventStore() {
		@Nullable
		@Override
		public Stream stream(@NotNull final Key key, @NotNull final Id id) {
			final ArrayList<NogEvent> events = Lists.newArrayList(new Groaned(), new Moaned(), new Jigged());
			return new Stream(new Version(events.size()), events);
		}

		@Nullable
		@Override
		public Stream streamFrom(@NotNull final Key key, @NotNull final Id id, @NotNull final Version version) {
			return null;
		}

		@NotNull
		@Override
		public Stored save(@NotNull final Key key, @NotNull final Id id, @NotNull final Version version, @NotNull final List<? extends Event> events) {
			return new Stored(true, version);
		}
	};

	@Nullable
	private final SnapshotStore<Nog> snapshotStore = new SnapshotStore<Nog>() {
		@Nullable
		@Override
		public Snapshot<Nog> snapshot(@NotNull final Key key, @NotNull final Id id) {
			return null;
		}

		@NotNull
		@Override
		public Stored save(@NotNull final Key key, @NotNull final Id id, @NotNull final Version version, final Nog snapshot) {
			return new Stored(true, version);
		}
	};

	@Nullable
	private final NogRepository repository = new NogRepository(eventStore, snapshotStore);

	public void testDispatcher() {
		final NogDispatcher dispatcher = new NogDispatcher(repository);

		final Result result = dispatcher.dispatch(new Groan(id, version));

		assertNotNull(result);
		assertEquals(true, result.getSuccess());
	}
}
