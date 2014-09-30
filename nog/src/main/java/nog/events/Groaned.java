package nog.events;

import ty.cqrs.CqrsPackage;

public class Groaned extends NogEvent {
	public Groaned() {
		super(CqrsPackage.eventKey(Groaned.class));
	}
}
