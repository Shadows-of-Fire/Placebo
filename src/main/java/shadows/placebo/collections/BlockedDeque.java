package shadows.placebo.collections;

import java.util.ArrayDeque;

public abstract class BlockedDeque<T> extends ArrayDeque<T> {

	private static final long serialVersionUID = -8194197029368437188L;

	public abstract boolean isBlocked(T t);

	@Override
	public void addFirst(T t) {
		if (isBlocked(t)) return;
		super.addFirst(t);
	}

	@Override
	public void addLast(T t) {
		if (isBlocked(t)) return;
		super.addLast(t);
	}

	@Override
	public boolean add(T t) {
		addLast(t);
		return !isBlocked(t);
	}

	@Override
	public boolean offerFirst(T t) {
		addFirst(t);
		return !isBlocked(t);
	}

	@Override
	public boolean offerLast(T t) {
		addLast(t);
		return !isBlocked(t);
	}
}
