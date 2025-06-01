import java.util.List;

enum PhoneTypeJ { MOBILE,  HOME,  WORK};


class PhoneNumberJ implements java.io.Serializable {
    private static final long serialVersionUID = 2463673954867473008L;
    public String number;
    public PhoneTypeJ type;

    public PhoneNumberJ(String number, PhoneTypeJ type)
    {
        this.number = number;
        this.type = type;
    }
}


class PersonJ implements java.io.Serializable
{
    private static final long serialVersionUID = 2363673954867473008L;
    private int a;
    private String b;

    public String name;
    public int id;
    public double incomePercentage;
    public String email;
    public final PhoneNumberJ[] phones;

    public PersonJ(String name, int id, String email, double incomePercentage, List<PhoneNumberJ> phones)
    {
        this.name = name;
        this.id = id;
        this.email = email;
        this.incomePercentage = incomePercentage;
        this.phones = phones.toArray(new PhoneNumberJ[0]);
    }

}
