public class UserInfo {
    private String firstName;
    private String lastName;
    private String email;
    private String photoUrl;

    public UserInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public UserInfo(String firstName, String lastName, String email, String photoUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.photoUrl = photoUrl;
    }

    // Getters and setters (if needed)
}
