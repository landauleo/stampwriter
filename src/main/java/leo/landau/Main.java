package leo.landau;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            StampWriter everyPageStampWriter = new EveryPageStampWriter();
            everyPageStampWriter.createStamps();

            StampWriter lastPageStampWriter = new LastPageStampWriter();
            lastPageStampWriter.createStamps();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}