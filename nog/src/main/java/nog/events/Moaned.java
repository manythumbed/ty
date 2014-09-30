package nog.events;

import ty.cqrs.CqrsPackage;

public class Moaned extends NogEvent {
	public Moaned() {
		super(CqrsPackage.eventKey(Moaned.class));
	}
}
