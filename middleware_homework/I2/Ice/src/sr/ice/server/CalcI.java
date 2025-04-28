package sr.ice.server; // Upewnij się, że pakiet jest zgodny

import Demo.Calc;
import Demo.NoInput;
import com.zeroc.Ice.Current;

import java.util.Arrays;
import java.util.HashSet;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;



public class CalcI implements Calc {
	private static final long serialVersionUID = -2448962912780867770L;
	long counter = 0;

	@Override
	public int getMin(int[] list, Current current) throws NoInput {
		System.out.println("SERWER: Wywołano getMin na liście: " + Arrays.toString(list) + " | Kontekst: " + current.ctx);
		counter++;

		if (list == null || list.length == 0) {
			System.err.println("SERWER: getMin - Pusta lista, rzucam NoInput");
			throw new NoInput();
		}
		OptionalInt minVal = Arrays.stream(list).min();
		int result = minVal.getAsInt();
		System.out.println("SERWER: getMin - Zwracam minimum: " + result);
		return result;
	}

	@Override
	public int getMax(int[] list, Current current) throws NoInput {
		System.out.println("SERWER: Wywołano getMax na liście: " + Arrays.toString(list) + " | Kontekst: " + current.ctx);
		counter++;

		if (list == null || list.length == 0) {
			System.err.println("SERWER: getMax - Pusta lista, rzucam NoInput");
			throw new NoInput();
		}
		OptionalInt maxVal = Arrays.stream(list).max();
		int result = maxVal.getAsInt();
		System.out.println("SERWER: getMax - Zwracam maximum: " + result);
		return result;
	}

	@Override
	public float getAvg(int[] list, Current current) throws NoInput {
		System.out.println("SERWER: Wywołano getAvg na liście: " + Arrays.toString(list) + " | Kontekst: " + current.ctx);
		counter++;

		if (list == null || list.length == 0) {
			System.err.println("SERWER: getAvg - Pusta lista, rzucam NoInput");
			throw new NoInput();
		}
		OptionalDouble avg = Arrays.stream(list).average();
		float result = (float) avg.orElse(0.0);
		System.out.println("SERWER: getAvg - Zwracam średnią: " + result);
		return result;
	}

	@Override
	public int[] intersection(int[] list1, int[] list2, Current current) throws NoInput {
		System.out.println("SERWER: Wywołano intersection na listach: " + Arrays.toString(list1) + " i " + Arrays.toString(list2) + " | Kontekst: " + current.ctx);
		counter++;

		if (list1 == null || list2 == null || list1.length == 0 || list2.length == 0) {
			System.out.println("SERWER: intersection - Jedna lub obie listy są puste/null. Zwracam pustą listę.");
			return new int[0];
		}
		Set<Integer> set1 = Arrays.stream(list1).boxed().collect(Collectors.toSet());
		Set<Integer> set2 = Arrays.stream(list2).boxed().collect(Collectors.toSet());
		set1.retainAll(set2);
		int[] result = set1.stream().mapToInt(Integer::intValue).toArray();
		System.out.println("SERWER: intersection - Zwracam przecięcie: " + Arrays.toString(result));
		return result;
	}

	@Override
	public int[] difference(int[] list1, int[] list2, Current current) throws NoInput {
		System.out.println("SERWER: Wywołano difference na listach: " + Arrays.toString(list1) + " i " + Arrays.toString(list2) + " | Kontekst: " + current.ctx);
		counter++;

		if (list1 == null || list1.length == 0) {
			System.out.println("SERWER: difference - Pierwsza lista jest pusta/null. Zwracam pustą listę.");
			return new int[0];
		}
		if (list2 == null || list2.length == 0) {
			System.out.println("SERWER: difference - Druga lista jest pusta/null. Zwracam kopię pierwszej listy.");
			return Arrays.copyOf(list1, list1.length);
		}
		Set<Integer> set1 = Arrays.stream(list1).boxed().collect(Collectors.toSet());
		Set<Integer> set2 = Arrays.stream(list2).boxed().collect(Collectors.toSet());
		set1.removeAll(set2);
		int[] result = set1.stream().mapToInt(Integer::intValue).toArray();
		System.out.println("SERWER: difference - Zwracam różnicę (list1 \\ list2): " + Arrays.toString(result));
		return result;
	}
}