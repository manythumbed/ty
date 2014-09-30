package nog.commands;

import nog.Nog;
import nog.NogRepository;
import org.jetbrains.annotations.NotNull;
import ty.cqrs.command.Handler;
import ty.cqrs.command.Result;

class GroanHandler extends Handler<Groan> {

	@NotNull
	private final NogRepository repository;

	public GroanHandler(@NotNull final NogRepository repository) {
		this.repository = repository;
	}

	@NotNull
	@Override
	public Result handle(@NotNull final Groan command) {
		final Nog nog = repository.fetch(command.nog);
		if (nog != null) {
			nog.groan();

			if (repository.save(command.nog, nog)) {
				return succeeded();
			}
		}

		return failed("Unable to find a nog with id " + command.nog);
	}
}
