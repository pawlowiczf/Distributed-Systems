package sr.ice.client;

import com.zeroc.Ice.*;
import Demo.*;


import java.lang.Exception;
import java.util.*;

public class IceClient {

	private static int[] readIntSequence(Scanner scanner, String prompt) {
		System.out.print(prompt + " (liczby oddzielone spacją): ");
		String line = scanner.nextLine();
		if (line == null || line.trim().isEmpty()) {
			return new int[0];
		}
		String[] numbers = line.trim().split("\\s+");
		int[] sequence = new int[numbers.length];
		for (int i = 0; i < numbers.length; i++) {
			try {
				sequence[i] = Integer.parseInt(numbers[i]);
			} catch (NumberFormatException e) {
				System.err.println("Błąd parsowania liczby: " + numbers[i] + ". Używam 0.");
				sequence[i] = 0;
			}
		}
		return sequence;
	}


	private static String sequenceToString(int[] seq) {
		return Arrays.toString(seq);
	}



	public static void main(String[] args) {
		int status = 0;
		Communicator communicator = null;
		Scanner scanner = new Scanner(System.in);

		try {

			communicator = Util.initialize(args);


			ObjectPrx base1 = communicator.stringToProxy("calc/calc11:tcp -h 127.0.0.1 -p 10000 -z : udp -h 127.0.0.1 -p 10000 -z");

			CalcPrx obj1 = CalcPrx.checkedCast(base1);
			if (obj1 == null) throw new Error("Invalid proxy");

			String line = null;


			do {
				try {
					System.out.println("\nDostępne komendy: getMin, getMax, getAvg, intersection, difference, compress on/off, set-proxy <mode>, flush, x (wyjście)");
					System.out.print("==> ");
					line = scanner.nextLine();
					if (line == null) break;

					switch (line.trim()) {
						case "getMin": {
							int[] inputList = readIntSequence(scanner, "Podaj listę liczb dla getMin");
							try {

								int resultInt = obj1.getMin(inputList);

								System.out.println("getMin RESULT = " + resultInt);

							} catch (NoInput ex) {
								System.err.println("BŁĄD: Operacja getMin zgłosiła NoInput (prawdopodobnie pusta lista).");
							}
							break;
						}
						case "getMax": {
							int[] inputList = readIntSequence(scanner, "Podaj listę liczb dla getMax");
							try {

								int resultInt = obj1.getMax(inputList);

								System.out.println("getMax RESULT = " + resultInt);

							} catch (NoInput ex) {
								System.err.println("BŁĄD: Operacja getMax zgłosiła NoInput (prawdopodobnie pusta lista).");
							}
							break;
						}
						case "getAvg": {
							int[] inputList = readIntSequence(scanner, "Podaj listę liczb dla getAvg");
							try {
								float avgResult = obj1.getAvg(inputList);
								System.out.println("getAvg RESULT = " + avgResult);
							} catch (NoInput ex) {
								System.err.println("BŁĄD: Operacja getAvg zgłosiła NoInput (prawdopodobnie pusta lista).");
							}
							break;
						}
						case "intersection": {
							int[] list1 = readIntSequence(scanner, "Podaj pierwszą listę liczb dla intersection");
							int[] list2 = readIntSequence(scanner, "Podaj drugą listę liczb dla intersection");
							try {
								int[] intersectionResult = obj1.intersection(list1, list2);
								System.out.println("intersection RESULT = " + sequenceToString(intersectionResult));
							} catch (NoInput ex) {
								System.err.println("BŁĄD: Operacja intersection zgłosiła NoInput.");
							}
							break;
						}
						case "difference": {
							int[] list1 = readIntSequence(scanner, "Podaj pierwszą listę liczb dla difference (A)");
							int[] list2 = readIntSequence(scanner, "Podaj drugą listę liczb dla difference (B)");
							try {
								int[] differenceResult = obj1.difference(list1, list2);
								System.out.println("difference (A \\ B) RESULT = " + sequenceToString(differenceResult));
							} catch (NoInput ex) {
								System.err.println("BŁĄD: Operacja difference zgłosiła NoInput.");
							}
							break;
						}



						case "compress on":
							obj1 = obj1.ice_compress(true);
							System.out.println("Compression enabled for obj1");
							break;
						case "compress off":
							obj1 = obj1.ice_compress(false);
							System.out.println("Compression disabled for obj1");
							break;
						case "set-proxy twoway":
							obj1 = obj1.ice_twoway();
							System.out.println("obj1 proxy set to 'twoway' mode");
							break;

						case "set-proxy oneway":
							obj1 = obj1.ice_oneway();
							System.out.println("obj1 proxy set to 'oneway' mode");
							break;
						case "set-proxy datagram":
							obj1 = obj1.ice_datagram();
							System.out.println("obj1 proxy set to 'datagram' mode");
							break;
						case "set-proxy batch oneway":
							obj1 = obj1.ice_batchOneway();
							System.out.println("obj1 proxy set to 'batch oneway' mode");
							break;
						case "set-proxy batch datagram":
							obj1 = obj1.ice_batchDatagram();
							System.out.println("obj1 proxy set to 'batch datagram' mode");
							break;
						case "flush":
							obj1.ice_flushBatchRequests();
							System.out.println("Flush DONE");
							break;

						case "x":
						case "":
							break;
						default:
							System.out.println("Nieznana komenda: '" + line.trim() + "'");
							break;
					}
				} catch (TwowayOnlyException ex) {
					System.err.println("BŁĄD: Ta operacja wymaga trybu 'twoway': " + ex.getMessage());
				} catch (LocalException ex) {
					System.err.println("Lokalny błąd ICE: " + ex.getMessage());
					ex.printStackTrace(System.err);
				} catch (Exception ex) {
					System.err.println("Nieoczekiwany błąd: " + ex.getMessage());
					ex.printStackTrace(System.err);
				}
			} while (!Objects.equals(line, "x"));

		} catch (LocalException e) {
			e.printStackTrace();
			status = 1;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			status = 1;
		} finally {
			if (communicator != null) {
				try {
					communicator.destroy();
				} catch (Exception e) {
					System.err.println(e.getMessage());
					status = 1;
				}
			}
			scanner.close();
		}
		System.exit(status);
	}
}