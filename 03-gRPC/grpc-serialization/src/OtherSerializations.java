import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.fury.Fury;
import org.apache.fury.config.Language;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

//https://www.reddit.com/r/java/comments/174szbr/fury_fastest_serialization_framework_in/
//https://www.javacodegeeks.com/apache-fury-serialization-java-example.html
//https://medium.com/@shawn.ck.yang/fury-a-blazing-fast-multi-language-serialization-framework-powered-by-jit-and-zero-copy-fdd16f3215cb

public class OtherSerializations
{
    public static void main(String[] args)
    {
        try {
            List<PhoneNumberJ> phones = new ArrayList<PhoneNumberJ>();
            phones.add(new PhoneNumberJ("+48-12-555-4321", PhoneTypeJ.HOME));
            phones.add(new PhoneNumberJ("+48-699-989-796", PhoneTypeJ.MOBILE));

            PhoneNumberJ phone = new PhoneNumberJ("+48-12-555-4321", PhoneTypeJ.HOME);

            PersonJ person = new PersonJ("Włodzimierz Wróblewski", 123456, "wrobel@poczta.com", 12.0/7.0, phones);

            OtherSerializations os = new OtherSerializations();

            os.performFurySerialization(person, 10000000);
            os.performDefaultJavaSerialization(person, 10000000);
            os.performJsonSerialization(person, 10000000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    void performFurySerialization(PersonJ person, long number) throws IOException, InterruptedException {

        Fury fury = Fury.builder()
                .withLanguage(Language.JAVA)
                .withAsyncCompilation(true)
                .withRefTracking(true)
                .requireClassRegistration(true)
                .withJdkClassSerializableCheck(false)
                .build();
        fury.register(PhoneNumberJ.class);
        fury.register(PhoneTypeJ.class);
        fury.register(PersonJ.class);
        byte[] serializedData = fury.serialize(person);
        Thread.sleep(2000);

        System.out.println("\n\n**** Performing Fury Java serialization " + number + " times...");
        long start = System.currentTimeMillis();
        for(long i = 0; i < number; i++)
        {
            fury.serialize(person);
        }
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println("... finished (" +  timeElapsed  + " ms).");
        System.out.println("Serialized data: " + new String(serializedData, StandardCharsets.UTF_8));
        System.out.println("Size of the serialized data: " + serializedData.length + " bytes.");

        FileOutputStream file = new FileOutputStream("person-fury-java.ser");
        ObjectOutputStream out = new ObjectOutputStream(file);
        out.writeObject(serializedData);
        out.close();
        file.close();
    }

    void performDefaultJavaSerialization(PersonJ person, long number)
    {
        try
        {
            System.out.println("\n\n**** Performing default Java serialization " + number + " times...");
            long start = System.currentTimeMillis();
            for(long i = 0; i < number; i++)
            {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream obj = new ObjectOutputStream(bos);
                obj.writeObject(person);
                obj.flush();
                bos.toByteArray();
            }
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            System.out.println("... finished (" +  timeElapsed  + " ms).");

            //serialize again (only once) and write to a file
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream obj1 = new ObjectOutputStream(bos);
            obj1.writeObject(person);
            obj1.flush();
            System.out.println("Serialized data: " + new String(bos.toByteArray(), StandardCharsets.UTF_8));
            System.out.println("Size of the serialized data: " + bos.toByteArray().length + " bytes.");

            FileOutputStream file = new FileOutputStream("person1-default-java.ser");
            ObjectOutputStream obj2 = new ObjectOutputStream(file);
            obj2.writeObject(person);
            obj2.close();
            file.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    void performJsonSerialization(PersonJ person, long number) {
        var mapper = new ObjectMapper();
        try {
            System.out.println("\n\n**** Performing JSON serialization " + number + " times...");
            long start = System.currentTimeMillis();
            for(long i = 0; i < number; i++) {
                mapper.writeValueAsString(person);
            }
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            System.out.println("... finished (" +  timeElapsed  + " ms).");
            System.out.println("Serialized data: " + mapper.writeValueAsString(person));
            System.out.println("Size of the serialized data: " + mapper.writeValueAsString(person).length() + " bytes.");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }
}


