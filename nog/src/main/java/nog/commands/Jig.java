package nog.commands;

import org.jetbrains.annotations.NotNull;
import ty.cqrs.Id;
import ty.cqrs.Version;

public class Jig extends NogCommand {
	public Jig(@NotNull final Id nog, @NotNull final Version version) {
		super(nog, version);
	}
}
