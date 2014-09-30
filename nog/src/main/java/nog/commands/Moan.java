package nog.commands;

import org.jetbrains.annotations.NotNull;
import ty.cqrs.Id;
import ty.cqrs.Version;

public class Moan extends NogCommand {
	public Moan(@NotNull final Id nog, @NotNull final Version version) {
		super(nog, version);
	}
}
