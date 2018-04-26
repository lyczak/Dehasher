import java.util.concurrent.RecursiveAction;
import org.apache.commons.codec.digest.DigestUtils;

public class RecursiveHasher extends RecursiveAction {
    private Dehasher dehasher; // Pointer to the Dehasher that started the action.
    private int length; // The remaining levels that we need to generate.
	private String password = ""; // The password we've assembled so far.

    public RecursiveHasher(Dehasher dehasher, int length) {
		// Called by the Dehasher to create the root.
		// The root always has a blank password.
        this.dehasher = dehasher;
        this.length = length;
    }

	private RecursiveHasher(Dehasher dehasher, int length, String password) {
		// Called by parent RecursiveHashers to create children.
        this(dehasher, length);
		this.password = password;
    }

    @Override
	public void compute() {
		if (length == 0) { // Are we done making the password?
			String hash = DigestUtils.sha1Hex(password);
			try {
				dehasher.put(new HashedPassword(password, hash));
				// Send the hashed pair to the Dehasher that called the root action.
			} catch(InterruptedException e) {
				e.printStackTrace();
				// This should not happen (if it does, we'll debug).
			}
		} else { // We need more children to help compute more passwords.
			// Add another character and pass the new password to a child (several times).
			// This should fork a new child for each character in the character set.
			for(int i = 0; i < dehasher.CHAR_SET.length; i++) {
				new RecursiveHasher(dehasher, length - 1,
						password + dehasher.CHAR_SET[i]).fork();
						// Children process asynchronously.
			}
		}
	}
}
