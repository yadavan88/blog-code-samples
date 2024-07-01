//> using javaProp user=yadu
//> using javaOpt -Xmx2g, -Dkey=myvalue

public class JavaOptions {

    public static void main(String[] args) {
        var key = System.getProperty("key");
        var user = System.getProperty("user");
        System.out.println("Value for key: " + key);
        System.out.println("Value for user: " + user);
    }
}
