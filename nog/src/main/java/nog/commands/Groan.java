package nog.commands;

import org.jetbrains.annotations.NotNull;
import ty.cqrs.Id;
import ty.cqrs.Version;

public class Groan extends NogCommand {

	public Groan(@NotNull final Id nog, @NotNull final Version version) {
		super(nog, version);
	}
}
