package nog;

import org.jetbrains.annotations.NotNull;
import ty.cqrs.Event;
import ty.cqrs.write.AggregateRepository;
import ty.cqrs.write.EventStore;
import ty.cqrs.write.Key;
import ty.cqrs.write.SnapshotStore;

import java.util.List;

public class NogRepository extends AggregateRepository<Nog> {
	public NogRepository(@NotNull final EventStore events, @NotNull final SnapshotStore<Nog> snapshots) {
		super(new Key("quarks.nog"), events, snapshots);
	}

	@NotNull
	@Override
	public Nog create(@NotNull final List<? extends Event> events) {
		return new Nog(events);
	}
}
