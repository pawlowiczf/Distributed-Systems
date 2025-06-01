/*
 * Copyright 2015, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 *    * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package sr.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import sr.grpc.gen.*;
import sr.grpc.gen.CalculatorGrpc.CalculatorBlockingStub;
import sr.grpc.gen.CalculatorGrpc.CalculatorFutureStub;
import sr.grpc.gen.CalculatorGrpc.CalculatorStub;
import sr.grpc.gen.Number;
import sr.grpc.gen.StreamTesterGrpc.StreamTesterBlockingStub;
import sr.grpc.gen.StreamTesterGrpc.StreamTesterStub;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;


public class grpcClient 
{
	private static final Logger logger = Logger.getLogger(grpcClient.class.getName());

	private final ManagedChannel channel;

	private final CalculatorBlockingStub calcBlockingStub;
	private final CalculatorStub calcNonBlockingStub;
	private final CalculatorFutureStub calcFutureStub;
	private final AdvancedCalculatorGrpc.AdvancedCalculatorBlockingStub advCalcBlockingStub;

	private final StreamTesterBlockingStub streamTesterBlockingStub;
	private final StreamTesterStub streamTesterNonBlockingStub;


	/** Construct client connecting to HelloWorld server at {@code host:port}. */
	public grpcClient(String remoteHost, int remotePort) 
	{
		channel = ManagedChannelBuilder.forAddress(remoteHost, remotePort)
				.usePlaintext() // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid needing certificates.
				.build();

		calcBlockingStub = CalculatorGrpc.newBlockingStub(channel);
		calcNonBlockingStub = CalculatorGrpc.newStub(channel);
		calcFutureStub = CalculatorGrpc.newFutureStub(channel);

		advCalcBlockingStub = AdvancedCalculatorGrpc.newBlockingStub(channel);

		streamTesterBlockingStub = StreamTesterGrpc.newBlockingStub(channel);
		streamTesterNonBlockingStub = StreamTesterGrpc.newStub(channel); //Blocking stubs do not support client-streaming or bidirectional-streaming RPCs.
	}
	
	

	public static void main(String[] args) throws Exception 
	{
		grpcClient client = new grpcClient("127.0.0.5", 50051);
		client.test();
	}

	public void shutdown() throws InterruptedException {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}


	public void test() throws InterruptedException
	{
		String line = null;
		java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
		ListenableFuture<ArithmeticOpResult> future1 = null;

		do 	{
			try	{
				System.out.print("==> ");
				System.out.flush();
				line = in.readLine();
				switch (line) {
					case "add1": {
						ArithmeticOpArguments request = ArithmeticOpArguments.newBuilder().setArg1(66).setArg2(65505).build();
						ArithmeticOpResult result = calcBlockingStub.add(request);
						System.out.println(result.getRes());
						break;
					}
					case "add2": {
						ArithmeticOpArguments request = ArithmeticOpArguments.newBuilder().setArg1(4444).setArg2(5555).build();
						ArithmeticOpResult result = calcBlockingStub.add(request);
						System.out.println(result.getRes());
						break;
					}
					case "add-deadline1":
						try {
							ArithmeticOpArguments request = ArithmeticOpArguments.newBuilder().setArg1(44).setArg2(55).build();
							ArithmeticOpResult result = calcBlockingStub.withDeadlineAfter(100, TimeUnit.MILLISECONDS).add(request);
							System.out.println(result.getRes());
						} catch (io.grpc.StatusRuntimeException e) {
							System.out.println("DEADLINE EXCEEDED");
						}
						break;
					case "add-deadline2":
						try {
							ArithmeticOpArguments request = ArithmeticOpArguments.newBuilder().setArg1(4444).setArg2(5555).build();
							ArithmeticOpResult result = calcBlockingStub.withDeadlineAfter(100, TimeUnit.MILLISECONDS).add(request);
							System.out.println(result.getRes());
						} catch (io.grpc.StatusRuntimeException e) {
							System.out.println("DEADLINE EXCEEDED");
						}
						break;
					case "complex-sum": {
						ComplexArithmeticOpArguments request = ComplexArithmeticOpArguments.newBuilder()
								.setOptype(OperationType.SUM).addAllArgs(Arrays.asList(4.0, 5.0, 3.1415926))
								.build();
						ComplexArithmeticOpResult result = advCalcBlockingStub.complexOperation(request);
						System.out.println(result.getRes());
						break;
					}
					case "complex-avg": {
						ComplexArithmeticOpArguments request = ComplexArithmeticOpArguments.newBuilder()
								.setOptype(OperationType.AVG).addAllArgs(Arrays.asList(4.0, 5.0, 8.5))
								.build();
						ComplexArithmeticOpResult result = advCalcBlockingStub.complexOperation(request);
						System.out.println(result.getRes());
						break;
					}
					case "nonblock-add": {
						ArithmeticOpArguments request = ArithmeticOpArguments.newBuilder().setArg1(4444).setArg2(5555).build();
						StreamObserver<ArithmeticOpResult> responseObserver = new StreamObserver<ArithmeticOpResult>() {
							@Override
							public void onError(Throwable t) {
								System.out.println("gRPC ERROR");
							}

							@Override
							public void onCompleted() {
							}

							@Override
							public void onNext(ArithmeticOpResult res) {
								System.out.println(res.getRes() + " (non-block)");
							}
						};
						calcNonBlockingStub.add(request, responseObserver);
						break;
					}
					case "future-add-1": {
						ArithmeticOpArguments request = ArithmeticOpArguments.newBuilder().setArg1(4444).setArg2(5555).build();
						ListenableFuture<ArithmeticOpResult> future2 = calcFutureStub.add(request);
						Futures.addCallback(future2, new FutureCallback<ArithmeticOpResult>() {
							@Override
							public void onSuccess(ArithmeticOpResult result) {
								System.out.println(result.getRes() + " (future)");
							}

							@Override
							public void onFailure(Throwable throwable) {
								System.out.println("gRPC ERROR");
							}
						}, MoreExecutors.directExecutor());
						break;
					}
					case "future-add-2a": {
						ArithmeticOpArguments request = ArithmeticOpArguments.newBuilder().setArg1(4444).setArg2(5555).build();
						future1 = calcFutureStub.add(request);
						break;
					}
					case "future-add-2b":
						try {
							ArithmeticOpResult result = future1.get();
							System.out.println(result.getRes() + " (future)");
						} catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}
						break;
					case "gen-prime": //rpc GeneratePrimeNumbers(Task) returns (stream Number) {}
						new PrimeNumbersFinderExecutor(streamTesterBlockingStub).start();
						break;
					case "count-prime": //rpc CountPrimeNumbers(stream Number) returns (Report) {}
						new PrimeCounterExecutor(streamTesterNonBlockingStub).start();
						break;
					case "x":
					case "":
						break;
					default:
						System.out.println("???");
						break;
				}
			}
			catch (java.io.IOException ex) {
				System.err.println(ex);
			}
		}
		while (!line.equals("x"));
		
		shutdown();
	}
}


class PrimeNumbersFinderExecutor extends Thread
{	
	StreamTesterBlockingStub streamTesterBlockingStub;
	
	PrimeNumbersFinderExecutor(StreamTesterBlockingStub streamTesterBlockingStub)
	{
		this.streamTesterBlockingStub = streamTesterBlockingStub;
	}
	
	public void run()
	{
		Task request = Task.newBuilder().setMax(28).build();

		Iterator<Number> numbers;
		try {
			System.out.println("Calling GeneratePrimeNumbers(" + request.getMax() + ")...");
			numbers = streamTesterBlockingStub.generatePrimeNumbers(request); //rpc GeneratePrimeNumbers(Task) returns (stream Number) {}
			while(numbers.hasNext())
			{
				//wypisuje się z odstępami czasowymi, więc strumieniowanie DZIAŁA
				Number num = numbers.next();
				System.out.println("Service returned: " + num.getValue());
			}
			System.out.println("GeneratePrimeNumbers completed");
		} catch (StatusRuntimeException ex) {
			System.err.println("RPC failed: " + ex.getStatus());
			return;
		}
	}
}
	
	
class PrimeCounterExecutor extends Thread
{
	StreamTesterStub streamTesterNonBlockingStub;
	
	PrimeCounterExecutor(StreamTesterStub streamTesterNonBlockingStub)
	{
		this.streamTesterNonBlockingStub = streamTesterNonBlockingStub;
	}
	
	public void run()
	{
		StreamObserver<Report> responseObserver = new StreamObserver<Report>() 
		{
			int count = -1;
			@Override public void onNext(Report result)	{ //wołane tylko raz - na końcu wywołania
				count = result.getCount();
			}
			@Override public void onError(Throwable t) {
				System.out.println("RPC ERROR");
			}
			@Override public void onCompleted()	{
				System.out.println("Result received: found " + count + " prime numbers");
			}
		};
		
		StreamObserver<Number> requestObserver = streamTesterNonBlockingStub.countPrimeNumbers(responseObserver); //rpc CountPrimeNumbers(stream Number) returns (Report) {}
		try {
			for (int i = 0; i < 100; ++i) {
				if(isPrime(i)) {
					Number number = Number.newBuilder().setValue(i).build();	
					System.out.println("Streaming data to the service... (" + number.getValue() + ")");
					requestObserver.onNext(number);
				}
			}
		} catch (RuntimeException e) {
			// Cancel RPC
			requestObserver.onError(e);
			throw e;
		}
		// Mark the end of requests
		requestObserver.onCompleted();
	}

	

	private boolean isPrime(int val)
	{
		if(val % 2 == 0) return false; //oczywiście to nieprawda ;)
		try { Thread.sleep(1000); } catch(java.lang.InterruptedException ex) { } 
		return true; //oczywiście to nieprawda ;)
	}

}
