package sr.ice.client;

import Demo.A;
import Demo.CalcPrx;
import com.zeroc.Ice.*;

import java.io.IOException;
import java.lang.Exception;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class IceClient {
	public static void main(String[] args) {
		int status = 0;
		Communicator communicator = null;

		try {
			// 1. Inicjalizacja ICE
			communicator = Util.initialize(args);

			// 2. Uzyskanie referencji obiektu na podstawie linii w pliku konfiguracyjnym (wówczas aplikację należy uruchomić z argumentem --Ice.config=config.client)
			//ObjectPrx base1 = communicator.propertyToProxy("Calc1.Proxy");

			// 2. Uzyskanie referencji obiektu - to samo co powyżej, ale mniej ładnie
			ObjectPrx base1 = communicator.stringToProxy("calc/calc11:tcp -h 127.0.0.2 -p 10000 -z : udp -h 127.0.0.2 -p 10000 -z"); //opcja -z włącza możliwość kompresji wiadomości

			// 3. Rzutowanie, zawężanie (do typu Calc)
			CalcPrx obj1 = CalcPrx.checkedCast(base1);
			//CalcPrx obj1 = CalcPrx.uncheckedCast(base1); //na czym polega różnica?
			if (obj1 == null) throw new Error("Invalid proxy");

			CompletableFuture<Long> cfl = null;
			String line = null;
			java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
			long r;
			A a;

			// 4. Wywołanie zdalnych operacji i zmiana trybu działania proxy obiektu obj1
			do {
				try {
					System.out.print("==> ");
					line = in.readLine();
					switch (line) {
						case "add":
							r = obj1.add(7, 8);
							System.out.println("RESULT = " + r);
							break;
						case "add2":
							r = obj1.add(7000, 8000);
							System.out.println("RESULT = " + r);
							break;
						case "subtract":
							r = obj1.subtract(7, 8);
							System.out.println("RESULT = " + r);
							break;
						case "op":
							a = new A((short) 11, 22, 33.0f, "ala ma kota");
							obj1.op(a, (short) 44);
							System.out.println("DONE");
							break;
						case "op2":
							a = new A((short) 11, 22, 33.0f, "ala ma kota ala ma kota ala ma kota ala ma kota ala ma kota ala ma kota ala ma kota ala ma kota ala ma kota ala ma kota ala ma kota ala ma kota ala ma kota ala ma kota ala ma kota ala ma kota ala ma kota ala ma kota ala ma kota ala ma kota ala ma kota ala ma kota ala ma kota ala ma kota ala ma kota ala ma kota ");
							obj1.op(a, (short) 44);
							System.out.println("DONE");
							break;
						case "op 10":
							a = new A((short) 11, 22, 33.0f, "ala ma kota");
							for (int i = 0; i < 10; i++) obj1.op(a, (short) 44);
							System.out.println("DONE");
							break;
						case "add-with-ctx": //wysłanie dodatkowych danych stanowiących kontekst wywołania
							Map<String, String> map = new HashMap<>();
							map.put("key1", "val1");
							map.put("key2", "val2");
							r = obj1.add(7, 8, map);
							System.out.println("RESULT = " + r);
							break;

						/* PONIŻEJ WYWOŁANIA REALIZOWANE W TRYBIE ASYNCHRONICZNYM (completable future) */

						case "add-asyn1":
							obj1.addAsync(7000, 8000).whenComplete((result, ex) -> System.out.println("RESULT (asyn) = " + result));
							break;
						case "add-asyn2-req":  // 1. wysłanie żądania
							cfl = obj1.addAsync(7000, 8000);
							break;
						case "add-asyn2-res":  // 2. odebranie wyniku
							r = cfl.join();
							System.out.println("RESULT = " + r);
							break;
						case "op-asyn1a 100": //co się dzieje "w sieci"? dlaczego "działa" tak wolno?
							a = new A((short) 11, 22, 33.0f, "ala ma kota");
							for (int i = 0; i < 100; i++) {
								obj1.opAsync(a, (short) 99);
							}
							System.out.println("DONE");
							break;
						case "op-asyn1b 100":
							a = new A((short) 11, 22, 33.0f, "ala ma kota");
							for (int i = 0; i < 100; i++) {
								obj1.opAsync(a, (short) 99).whenComplete((result, ex) ->
										System.out.println("CALL (asyn) finished")
								);
							}
							System.out.println("DONE");
							break;

						/* PONIŻEJ USTAWIANIE TRYBU PRACY PROXY */

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
						case "flush": //sensowne tylko dla operacji wywoływanych w trybie batch
							obj1.ice_flushBatchRequests();
							System.out.println("Flush DONE");
							break;
						case "x":
						case "":
							break;
						default:
							System.out.println("???");
					}
				} catch (IOException | TwowayOnlyException ex) {
					ex.printStackTrace(System.err);
				}
			}
			while (!Objects.equals(line, "x"));


		} catch (LocalException e) {
			e.printStackTrace();
			status = 1;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			status = 1;
		}
		if (communicator != null) { //clean
			try {
				communicator.destroy();
			} catch (Exception e) {
				System.err.println(e.getMessage());
				status = 1;
			}
		}
		System.exit(status);
	}

}