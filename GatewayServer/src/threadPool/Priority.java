package threadPool;

public enum Priority{
    HIGH(10),
    MEDIUM(5),
    LOW(1);

    private final int priority;

    private Priority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
