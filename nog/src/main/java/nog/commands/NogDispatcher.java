package nog.commands;

import nog.NogRepository;
import org.jetbrains.annotations.NotNull;
import ty.cqrs.command.Dispatcher;
import ty.cqrs.command.Result;

public class NogDispatcher extends Dispatcher {
	@NotNull
	private final GroanHandler groanHandler;

	public NogDispatcher(@NotNull final NogRepository repository) {
		groanHandler = new GroanHandler(repository);
	}

	@NotNull
	@SuppressWarnings("UnusedDeclaration")
	private Result handle(@NotNull final Groan groan) {
		return groanHandler.handle(groan);
	}
}
