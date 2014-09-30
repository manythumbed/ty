package nog.events;

import ty.cqrs.CqrsPackage;

public class Jigged extends NogEvent {
	public Jigged() {
		super(CqrsPackage.eventKey(Jigged.class));
	}
}
