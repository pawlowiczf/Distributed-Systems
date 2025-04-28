package sr.grpc.server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;


public class grpcServer 
{
	private static final Logger logger = Logger.getLogger(grpcServer.class.getName());

	private String address = "127.0.0.1";
	private int port = 8080;
	private Server server;

	private SocketAddress socket;

	private void start() throws IOException 
	{
		try { socket = new InetSocketAddress(InetAddress.getByName(address), port);	} catch(UnknownHostException e) {};

		SslContext sslContext = loadTLSCredentials();

		server = NettyServerBuilder.forAddress(socket).executor((Executors.newFixedThreadPool(16)))
				.addService(new CalculatorImpl())
				.addService(new StreamTesterImpl())
				.sslContext(sslContext)
				.build()
				.start();

		logger.info("Server started, listening on " + port);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.err.println("Shutting down gRPC server...");
				grpcServer.this.stop();
				System.err.println("Server shut down.");
			}
		});
	}

	private void stop() {
		if (server != null) {
			server.shutdown();
		}
	}

	/**
	 * Await termination on the main thread since the grpc library uses daemon threads.
	 */
	private void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}

	public static SslContext loadTLSCredentials() throws SSLException {
		File serverCertFile = new File("cert/server-cert.pem");
		File serverKeyFile = new File("cert/server-key.pem");

		File clientCACertFile = new File("cert/ca-cert.pem");

		SslContextBuilder ctxBuilder = SslContextBuilder.forServer(serverCertFile, serverKeyFile)
				.clientAuth(ClientAuth.REQUIRE)
				.trustManager(clientCACertFile);

		return GrpcSslContexts.configure(ctxBuilder).build();
	}

	/**
	 * Main launches the server from the command line.
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		final grpcServer server = new grpcServer();
		server.start();
		server.blockUntilShutdown();
	}

}
