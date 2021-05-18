package shadows.placebo.collections;

import java.util.ArrayList;
import java.util.Collection;

public abstract class BlockedList<T> extends ArrayList<T> {

	private static final long serialVersionUID = -7008599240020028661L;

	public abstract boolean isBlocked(T t);

	public abstract boolean isBlocked(Collection<? extends T> c);

	@Override
	public void add(int index, T t) {
		if (this.isBlocked(t)) return;
		super.add(index, t);
	}

	@Override
	public boolean add(T t) {
		if (this.isBlocked(t)) return false;
		return super.add(t);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		if (this.isBlocked(c)) return false;
		return super.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		if (this.isBlocked(c)) return false;
		return super.addAll(index, c);
	}
}
