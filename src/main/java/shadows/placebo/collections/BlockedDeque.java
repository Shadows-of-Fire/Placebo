package shadows.placebo.collections;

import java.util.ArrayDeque;

@Deprecated
public abstract class BlockedDeque<T> extends ArrayDeque<T> {

    private static final long serialVersionUID = -8194197029368437188L;

    public abstract boolean isBlocked(T t);

    @Override
    public void addFirst(T t) {
        if (this.isBlocked(t)) return;
        super.addFirst(t);
    }

    @Override
    public void addLast(T t) {
        if (this.isBlocked(t)) return;
        super.addLast(t);
    }

    @Override
    public boolean add(T t) {
        this.addLast(t);
        return !this.isBlocked(t);
    }

    @Override
    public boolean offerFirst(T t) {
        this.addFirst(t);
        return !this.isBlocked(t);
    }

    @Override
    public boolean offerLast(T t) {
        this.addLast(t);
        return !this.isBlocked(t);
    }
}
