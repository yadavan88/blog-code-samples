//> using dep com.lihaoyi:fansi_3:0.5.0
import fansi.*;
import fansi.Color;

public class ScalaLibInJava {

    public static void main(String[] args) {
        Str redStr = Color.Red("Hello, World");
        //Str colorful = Color.Red("Hellow, World!")
        System.out.println(redStr);
    }
}
