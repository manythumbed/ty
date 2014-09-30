package nog;

import nog.events.Groaned;
import nog.events.Jigged;
import nog.events.Moaned;
import org.jetbrains.annotations.NotNull;
import ty.cqrs.Event;
import ty.cqrs.Version;
import ty.cqrs.write.Aggregate;
import ty.cqrs.write.EventManager;

import java.util.List;

public class Nog implements Aggregate {
	private final EventManager eventManager = new EventManager();

	private int groans = 0;

	private int moans = 0;

	public Nog(@NotNull final List<? extends Event> events) {
		eventManager.build(this, events);
	}

	@SuppressWarnings("UnusedDeclaration")
	private void handle(final Moaned moan) {
		moans = moans + 1;
	}

	@SuppressWarnings("UnusedDeclaration")
	private void handle(final Groaned groan) {
		groans = groans + 1;
	}

	public final int moans() {
		return moans;
	}

	public final int groans() {
		return groans;
	}

	public final void moan() {
		eventManager.change(this, new Moaned());
	}

	public final void groan() {
		eventManager.change(this, new Groaned());
	}

	public final void jig() {
		if (moans() > 4) {
			eventManager.change(this, new Moaned());
			eventManager.change(this, new Moaned());
			eventManager.change(this, new Groaned());
			eventManager.change(this, new Jigged());
			eventManager.change(this, new Groaned());
		}
	}

	@Override
	public void update(@NotNull final List<? extends Event> events) {
		eventManager.update(this, events);
	}

	@NotNull
	@Override
	public Version version() {
		return eventManager.getVersion();
	}

	@NotNull
	@Override
	public List<Event> changes() {
		return eventManager.getChanges();
	}
}
