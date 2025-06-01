package sr.thrift.client;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.transport.TTransport;

import sr.gen.thrift.AdvancedCalculator;
import sr.gen.thrift.Calculator;
import sr.gen.thrift.Calculator.AsyncClient.add_call;
import sr.gen.thrift.OperationType;

import org.apache.thrift.transport.TSocket;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;


public class ThriftClient 
{
	public static void main(String [] args) {

		String opt = "simple"; //"simple"; //simple | multiplex | non-block | asyn | multi-thread

		String host = "127.0.0.2";

		TProtocol protocol = null;
		TTransport transport = null;

		Calculator.Client synCalc1 = null;
		Calculator.Client synCalc2 = null;
		AdvancedCalculator.Client synAdvCalc1 = null;

		try {
			String line = null;
			java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

			System.out.println("Which server do you want to connect to? (simple/multiplex)");
			System.out.print("==> ");
			line = in.readLine();
			if (line.equals("simple"))
			{
				transport = new TSocket(host, 9080);

				//protocol = new TBinaryProtocol(transport);
				//protocol = new TJSONProtocol(transport);
				protocol = new TCompactProtocol(transport);

				synCalc1 = new Calculator.Client(protocol);
				synAdvCalc1 = new AdvancedCalculator.Client(protocol); //wskazuje na ten sam zdalny obiekt - dlaczego?

				System.out.println("Running client in the 'simple' mode");
			} else if (line.equals("multiplex"))
			{
				transport = new TSocket(host, 9070);

				protocol = new TBinaryProtocol(transport);
				//protocol = new TJSONProtocol(transport);
				//protocol = new TCompactProtocol(transport);

				synCalc1 = new Calculator.Client(new TMultiplexedProtocol(protocol, "S1"));
				synCalc2 = new Calculator.Client(new TMultiplexedProtocol(protocol, "S2"));
				synAdvCalc1 = new AdvancedCalculator.Client(new TMultiplexedProtocol(protocol, "A1"));

				System.out.println("Running client in the 'multiplex' mode");
			}
			else
			{
				System.out.println("No correct option chosen. Exitting");
				System.exit(1);
			}

			if (transport != null) transport.open();

			do {
				System.out.print("==> ");
				line = in.readLine();
				try {
					if (line == null) {
						break;
					} else if (line.equals("add1a")) {
						int arg1 = 44;
						int arg2 = 55;
						int res = synCalc1.add(arg1, arg2);
						System.out.println("add(" + arg1 + "," + arg2 + ") returned " + res);
					} else if (line.equals("add1b")) {
						int arg1 = 4400;
						int arg2 = 5500;
						int res = synCalc1.add(arg1, arg2);
						System.out.println("add(" + arg1 + "," + arg2 + ") returned " + res);
					} else if (line.equals("add2")) {
						int arg1 = 44;
						int arg2 = 55;
						int res = synCalc2.add(arg1, arg2);
						System.out.println("add(" + arg1 + "," + arg2 + ") returned " + res);
					} else if (line.equals("add3")) {
						int arg1 = 44;
						int arg2 = 55;
						int res = synAdvCalc1.add(arg1, arg2);
						System.out.println("add(" + arg1 + "," + arg2 + ") returned " + res);
					} else if (line.equals("op1")) {
						double res = synAdvCalc1.op(OperationType.AVG, new HashSet<Double>(Arrays.asList(4.0, 5.0, 3.1415926)));
						System.out.println("op(AVG, (4.0,5.0,3.1415926)) returned " + res);
					} else if (line.equals("op2")) {
						double res = synAdvCalc1.op(OperationType.AVG, new HashSet<Double>());
						System.out.println("op(AVG, ()) returned " + res);
					} else if (line.equals("x")) {
						// Nothing to do
					} else {
						System.out.println("???");
					}
				}
				catch (TException ex) {
					ex.printStackTrace();
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			while (!line.equals("x"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		transport.close();
	}
	//
	//}
}
