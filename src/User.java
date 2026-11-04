public class User {

    private String userId;
    private String name;
    private String email;
    private String phone;

    public User(String userId, String name,
                String email, String phone) {

        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}