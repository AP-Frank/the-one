package schedules;

public enum Tags {
    GO_HOME("exit"),
    EAT("eat");

    private final String tag;

    Tags(final String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return tag;
    }
}
