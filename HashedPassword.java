import java.io.Serializable;

public class HashedPassword implements Serializable {
    private String password;
    private String hash;

    public HashedPassword(String password, String hash) {
        this.password = password;
        this.hash = hash;
    }

    public boolean hashEquals(String otherHash) {
        return hash.equals(otherHash);
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
