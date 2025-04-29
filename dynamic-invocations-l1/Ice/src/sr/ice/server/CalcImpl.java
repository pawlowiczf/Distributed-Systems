package sr.ice.server;

import DemoService.Calculator;
import DemoService.NoInput;
import com.zeroc.Ice.Current;

import java.util.Arrays;
import java.util.logging.Logger;

public class CalcImpl implements Calculator {
	private static final Logger logger = Logger.getLogger(CalcImpl.class.getName());

	@Override
	public int getMinNumber(int[] numbers, Current current) throws NoInput {
		logger.info("[getMinNumber] |> Received request: " + Arrays.toString(numbers));

		if (numbers == null || numbers.length == 0) {
			logger.severe("[getMinNumber] |> Empty input!");
			throw new NoInput();
		}

		int minNumber = Integer.MAX_VALUE;
        for (int number : numbers) {
            if (number < minNumber) {
                minNumber = number;
            }
        }

		logger.info("[getMinNumber] |> Sending response: " + minNumber);
		return minNumber;
	}

	@Override
	public int getMaxNumber(int[] numbers, Current current) throws NoInput {
		logger.info("[getMaxNumber] |> Received request: " + Arrays.toString(numbers));

		if (numbers == null || numbers.length == 0) {
			logger.severe("[getMaxNumber] |> Empty input!");
			throw new NoInput();
		}

		int maxNumber = Integer.MIN_VALUE;
        for (int number : numbers) {
            if (number > maxNumber) {
                maxNumber = number;
            }
        }

		logger.info("[getMaxNumber] |> Sending response: " + maxNumber);
		return maxNumber;
	}

	@Override
	public float getAvgNumber(int[] numbers, Current current) throws NoInput {
		logger.info("[getAverageNumber] |> Received request: " + Arrays.toString(numbers));

		if (numbers == null || numbers.length == 0) {
			logger.severe("[getAverageNumber] |> Empty input!");
			throw new NoInput();
		}

		int sum = 0;
        for (int number : numbers) {
            sum += number;
        }
		float average = (float) sum / numbers.length;

		logger.info("[getAverageNumber] |> Sending response: " + average);
		return average;
	}

}