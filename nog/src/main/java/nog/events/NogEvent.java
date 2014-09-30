package nog.events;

import org.jetbrains.annotations.NotNull;
import ty.cqrs.Event;
import ty.cqrs.EventKey;

public abstract class NogEvent extends Event {
	public NogEvent(@NotNull final EventKey key) {
		super(key);
	}
}
