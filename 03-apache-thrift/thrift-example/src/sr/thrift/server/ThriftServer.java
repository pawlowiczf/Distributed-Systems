package sr.thrift.server;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.ServerContext;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServerEventHandler;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TTransportFactory;

import sr.gen.thrift.AdvancedCalculator;
import sr.gen.thrift.Calculator;

import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;

// Generated code


public class ThriftServer {


	public static void main(String [] args) 
	{
		try {	
			Runnable simple = new Runnable() {
				public void run() {
					simple();
				}
			};      
			Runnable multiplex = new Runnable() {
				public void run() {
					multiplex();
				}
			};

			new Thread(simple).start();
			new Thread(multiplex).start();

		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	
	public static void simple() 
	{
		try {
			// 1. Utworzenie obiektów handlerów (~serwantów) i powiązanie ich z procesorami
			Calculator.Processor<CalculatorHandler> processor1 = new Calculator.Processor<CalculatorHandler>(new CalculatorHandler(1));
			Calculator.Processor<CalculatorHandler> processor2 = new Calculator.Processor<CalculatorHandler>(new CalculatorHandler(2));
			AdvancedCalculator.Processor<AdvancedCalculatorHandler> processor3 = new AdvancedCalculator.Processor<AdvancedCalculatorHandler>(new AdvancedCalculatorHandler(11));

			// 2. Ustalenie transportu
			TServerTransport serverTransport = new TServerSocket(9080);

			//3. Ustalenie serializacji danych
			TProtocolFactory protocolFactory1 = new TCompactProtocol.Factory();
			TProtocolFactory protocolFactory2 = new TBinaryProtocol.Factory();
			TProtocolFactory protocolFactory3 = new TJSONProtocol.Factory();

			//4. Utworzenie serwerów - ale to chyba *** nie będzie działać *** jak w zamierzeniu - dlaczego?
			TServer server1 = new TSimpleServer(new Args(serverTransport).protocolFactory(protocolFactory1).processor(processor1));
			TServer server2 = new TSimpleServer(new Args(serverTransport).protocolFactory(protocolFactory1).processor(processor2));
			TServer server3 = new TSimpleServer(new Args(serverTransport).protocolFactory(protocolFactory1).processor(processor3));

			System.out.println("Starting simple server(s)...");
			server1.serve();
			System.out.println("Server 1 started.");
			server2.serve();
			System.out.println("Server 2 started.");
			server3.serve();
			System.out.println("Server 3 started.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public static void multiplex() 
	{
		try {
			Calculator.Processor<CalculatorHandler> processor1 = new Calculator.Processor<CalculatorHandler>(new CalculatorHandler(1));
			Calculator.Processor<CalculatorHandler> processor2 = new Calculator.Processor<CalculatorHandler>(new CalculatorHandler(2));
			AdvancedCalculator.Processor<AdvancedCalculatorHandler> processor3 = new AdvancedCalculator.Processor<AdvancedCalculatorHandler>(new AdvancedCalculatorHandler(11));
			AdvancedCalculator.Processor<AdvancedCalculatorHandler> processor4 = new AdvancedCalculator.Processor<AdvancedCalculatorHandler>(new AdvancedCalculatorHandler(12));

			TServerTransport serverTransport = new TServerSocket(9070);

			TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
			//TProtocolFactory protocolFactory = new TJSONProtocol.Factory();
			//TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
			
			TMultiplexedProcessor multiplex = new TMultiplexedProcessor();
            multiplex.registerProcessor("S1", processor1);
            multiplex.registerProcessor("S2", processor2);
            multiplex.registerProcessor("A1", processor3);
            multiplex.registerProcessor("A2", processor4);

			TServer server = new TSimpleServer(new Args(serverTransport).protocolFactory(protocolFactory).processor(multiplex)); 
			
			System.out.println("Starting the multiplex server...");
			server.serve(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}