import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ForkJoinPool;

public class Dehasher {
    public static final char[] CHAR_SET = "eothasinrdluymwfgcbpkvjqxz".toCharArray();
    // Letters in CHAR_SET are ordered from most common to least common.

    private BlockingQueue<HashedPassword> queue = new LinkedBlockingQueue<>();
    private ForkJoinPool pool = new ForkJoinPool();
    private Integer passwordLength;

    public Dehasher(int passwordLength) {
        this.passwordLength = passwordLength;
    }

    public Dehasher() {
        this.passwordLength = null;
    }

    public void put(HashedPassword p) throws InterruptedException {
        queue.put(p); // Pass thru put method to RecursiveHasher.
    }

    public String bruteForceHash(String hash) throws InterruptedException {
        pool.submit(new RecursiveHasher(this, passwordLength));
        // Begin the hash generating task asynchronously.
        HashedPassword pair;
        for(int i = (int)Math.pow(CHAR_SET.length, passwordLength); i > 0; i--) {
            pair = queue.take(); // Pop off oldest hash, password pair.
            if(pair.hashEquals(hash)) return pair.getPassword(); // Does the hash match?
        }
        return null; // No results were found.
    }

    public String searchRainbowTable(String hash) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        String line; // HASH,PASSWORD,\n
        while ((line = r.readLine()) != null) {
            int firstComma = line.indexOf(",");
            if(line.substring(0, firstComma).equals(hash)) // Does the hash match?
                return line.substring(firstComma + 1, line.length() - 1);
                // Return the password.
        }
        return null; // No results were found.
    }

    public void printRainbowTable() throws InterruptedException {
        pool.submit(new RecursiveHasher(this, passwordLength));
        // Begin the hash generating task asynchronously.
        HashedPassword pair;
        for(int i = (int)Math.pow(CHAR_SET.length, passwordLength); i > 0; i--) {
            pair = queue.take(); // Pop off oldest hash, password pair.
            System.out.printf("%s,%s,%n", pair.getHash(), pair.getPassword());
            // Output the hash and password to stdout.
        }
    }

    private static void failAndPrintHelp() {
        System.err.println("Usage: \njava Dehasher bruteforce <passwordLength> <hashString>");
        System.err.println("java Dehasher generate <passwordLength> > hashes.csv");
        System.err.println("java Dehasher search <hashString> < hashes.csv");
        System.exit(1);
    }

    public static void main(String[] args) {
        if(args.length < 2) {
            failAndPrintHelp();
        }
        try {
            Dehasher d;
            String password = null;
            String hashString;
            switch(args[0]) {
                case "bruteforce":
                    d = new Dehasher(Integer.parseInt(args[1]));
                    hashString = args[2].toLowerCase();
                    password = d.bruteForceHash(hashString);
                    break;
                case "generate":
                    d = new Dehasher(Integer.parseInt(args[1]));
                    d.printRainbowTable();
                    break;
                case "search":
                    d = new Dehasher();
                    hashString = args[1].toLowerCase();
                    password = d.searchRainbowTable(hashString);
                    break;
                default:
                    failAndPrintHelp();
            }
            if(password != null) System.out.println(password);
        } catch(NumberFormatException e) {
            failAndPrintHelp();
            // User entered something other than a number for the passwordLength.
        } catch(InterruptedException | IOException e) {
            System.err.println(e.getMessage());
            // Something unexpected happened (not sure why).
        }
    }
}
