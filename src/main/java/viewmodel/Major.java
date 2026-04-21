package viewmodel;


public enum Major {
    CS("Computer Science"),
    CPIS("Computer Programming and Information Systems"),
    ENGLISH("English");

    private final String displayName;

    Major(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}