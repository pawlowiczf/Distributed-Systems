
module Demo
{
    exception NoInput {};


    sequence<int> numberSequence;


    interface Calc
    {

        int getMin(numberSequence list) throws NoInput;

        int getMax(numberSequence list) throws NoInput;


        float getAvg(numberSequence list) throws NoInput;


        numberSequence intersection(numberSequence list1, numberSequence list2) throws NoInput;


        numberSequence difference(numberSequence list1, numberSequence list2) throws NoInput;
    };
};