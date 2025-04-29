package sr.ice.server;

import DemoService.NoInput;
import DemoService.Sorter;
import com.zeroc.Ice.Current;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SorterImpl implements Sorter {
    Logger logger = Logger.getLogger(SorterImpl.class.getName());

    @Override
    public String[] sortStrings(String[] names, Current current) throws NoInput {
        return Arrays.stream(names)
                .sorted()
                .toList()
                .toArray(String[]::new);
    }

    @Override
    public int[] sortNumbers(int[] numbers, Current current) throws NoInput {
        return Arrays.stream(numbers)
                .sorted()
                .toArray();
    }
}
