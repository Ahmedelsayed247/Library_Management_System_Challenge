package code81.Library_Management_System_Challenge.domain.model;

public enum Role {
    ADMINISTRATOR("ADMIN"),
    LIBRARIAN("LIBRARIAN"),
    STAFF("STAFF");

    private final String authority;

    Role(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }
}
