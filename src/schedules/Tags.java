package schedules;

public enum Tags {
    GO_HOME("Entry"),
    EAT("Eat"),
    SEATING("Seating"),
    EATING("Eating");

    private final String tag;

    Tags(final String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return tag;
    }
}
