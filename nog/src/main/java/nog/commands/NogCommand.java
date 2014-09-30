package nog.commands;

import org.jetbrains.annotations.NotNull;
import ty.cqrs.Id;
import ty.cqrs.Version;
import ty.cqrs.command.Command;

abstract public class NogCommand extends Command {
	@NotNull
	public final Id nog;

	@NotNull
	public final Version version;

	protected NogCommand(@NotNull final Id nog, @NotNull final Version version) {
		this.nog = nog;
		this.version = version;
	}
}
