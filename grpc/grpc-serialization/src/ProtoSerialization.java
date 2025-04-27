import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import sr.proto.AddressBookProtos.Person;

public class ProtoSerialization {

    public static void main(String[] args)
    {
        try {
            new ProtoSerialization().performProtoSerialization();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void performProtoSerialization() throws IOException
    {
        Person person =
                Person.newBuilder()
                        .setId(123456)
                        .setName("Włodzimierz Wróblewski")
                        .setEmail("wrobel@poczta.com")
                        .setIncomePercentage(12.0/7.0)
                        .addPhones(
                                Person.PhoneNumber.newBuilder()
                                        .setNumber("+48-12-555-4321")
                                        .setType(Person.PhoneType.HOME))
                        .addPhones(
                                Person.PhoneNumber.newBuilder()
                                        .setNumber("+48-699-989-796")
                                        .setType(Person.PhoneType.MOBILE))
                        .build();

        byte[] personSer = null;

        long number = 10000000;
        System.out.println("Performing proto serialization " + number + " times...");
        long start = System.currentTimeMillis();
        for(long i = 0; i < number; i++)
        {
            person.toByteArray();
        }
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println("... finished (" +  timeElapsed  + " ms).");
        personSer = person.toByteArray();
        System.out.println("Serialized data: " + new String(personSer, StandardCharsets.UTF_8));
        System.out.println("Serialized data in hex format: ");
        //print data as hex values
        for (byte b : personSer) { System.out.print(String.format("%02X", b)); }
        System.out.println("\nSize of the serialized data: " + personSer.length + " bytes.");

        //serialize again (only once) and write to a file
        FileOutputStream file = new FileOutputStream("person-proto.ser");
        file.write(person.toByteArray());
        file.close();

    }
}
