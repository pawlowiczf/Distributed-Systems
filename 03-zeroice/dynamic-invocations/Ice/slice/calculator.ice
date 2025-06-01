
module DemoService
{
    exception NoInput {};


    sequence<int> numberSequence;
    sequence<string> stringSequence;

    interface Calculator
    {
        int getMinNumber(numberSequence numbers) throws NoInput;
        int getMaxNumber(numberSequence numbers) throws NoInput;
        float getAvgNumber(numberSequence numbers) throws NoInput;
    };

    interface Sorter {
        stringSequence sortStrings(stringSequence names) throws NoInput;
        numberSequence sortNumbers(numberSequence numbers) throws NoInput;
    };
};