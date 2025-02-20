package com.kodypay.playground;

import com.kodypay.grpc.pay.v1.KodyPayTerminalServiceGrpc;
import com.kodypay.grpc.pay.v1.PaymentMethod;
import com.kodypay.grpc.pay.v1.PaymentMethodType;
import com.kodypay.grpc.pay.v1.PayRequest.PaymentMethods;
import com.kodypay.grpc.pay.v1.PayRequest;
import com.kodypay.grpc.pay.v1.PayResponse;
import com.kodypay.grpc.pay.v1.TerminalsRequest;
import com.kodypay.grpc.pay.v1.TerminalsResponse;
import io.grpc.internal.DnsNameResolverProvider;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import java.util.Arrays;

import java.math.BigDecimal;

public class Main {
   public static final String HOSTNAME = "";
   public static final String API_KEY = "";

   public static void main(String[] args) {
        String storeId = "";
        String terminalId = ""; 
        BigDecimal amount = new BigDecimal("10.00");
        
        // First get list of terminals
        getTerminals(storeId);
        
        // Then initiate payment
        initiateCardPayment(storeId, terminalId, amount);
        initiateAlipayPayment(storeId, terminalId, amount);  
        initiateWeChatPayment(storeId, terminalId, amount);
   }

   private static void getTerminals(String storeId) {
        KodyPayTerminalServiceGrpc.KodyPayTerminalServiceBlockingStub paymentClient = createKodyTerminalPaymentsClient();
        TerminalsRequest terminalsRequest = TerminalsRequest.newBuilder()
                .setStoreId(storeId)
                .build();

        TerminalsResponse terminalsResponse = paymentClient.terminals(terminalsRequest);
        System.out.println("Terminals response: " + terminalsResponse);
   }

   private static void initiatePayment(String storeId, String terminalId, BigDecimal amount) {
        KodyPayTerminalServiceGrpc.KodyPayTerminalServiceBlockingStub client = createKodyTerminalPaymentsClient();

        PayRequest request = PayRequest.newBuilder()
                .setStoreId(storeId)
                .setAmount(amount.toString())
                .setTerminalId(terminalId)
                .setShowTips(false)
                .build();

        PayResponse response = client.pay(request).next();
        System.out.println("Payment initiated - Status: " + response.getStatus());
   }

    private static void initiateCardPayment(String storeId, String terminalId, BigDecimal amount) {
        KodyPayTerminalServiceGrpc.KodyPayTerminalServiceBlockingStub client = createKodyTerminalPaymentsClient();

        PaymentMethod paymentMethod = PaymentMethod.newBuilder()
                .setPaymentMethodType(PaymentMethodType.CARD)
                .build();

        PayRequest request = PayRequest.newBuilder()
                .setStoreId(storeId)
                .setTerminalId(terminalId)
                .setAmount(amount.toString())
                .setShowTips(false)
                .setPaymentMethod(paymentMethod)
                .addAllAcceptsOnly(Arrays.asList(
                    PaymentMethods.VISA,
                    PaymentMethods.MASTERCARD))
                .build();

        PayResponse response = client.pay(request).next();
        System.out.println("Card payment initiated - Status: " + response.getStatus());
    }

    private static void initiateAlipayPayment(String storeId, String terminalId, BigDecimal amount) {
        KodyPayTerminalServiceGrpc.KodyPayTerminalServiceBlockingStub client = createKodyTerminalPaymentsClient();

        PaymentMethod paymentMethod = PaymentMethod.newBuilder()
                .setPaymentMethodType(PaymentMethodType.E_WALLET)
                .setActivateQrCodeScanner(true)  // Activate QR code scanner
                .build();

        PayRequest request = PayRequest.newBuilder()
                .setStoreId(storeId)
                .setTerminalId(terminalId)
                .setAmount(amount.toString())
                .setShowTips(false)
                .setPaymentMethod(paymentMethod)
                .addAcceptsOnly(PaymentMethods.ALIPAY)
                .build();

        PayResponse response = client.pay(request).next();
        System.out.println("Alipay payment initiated - Status: " + response.getStatus());
    }

    private static void initiateWeChatPayment(String storeId, String terminalId, BigDecimal amount) {
        KodyPayTerminalServiceGrpc.KodyPayTerminalServiceBlockingStub client = createKodyTerminalPaymentsClient();

        PaymentMethod paymentMethod = PaymentMethod.newBuilder()
                .setPaymentMethodType(PaymentMethodType.E_WALLET)
                .setActivateQrCodeScanner(true)  // Activate QR code scanner
                .build();

        PayRequest request = PayRequest.newBuilder()
                .setStoreId(storeId)
                .setTerminalId(terminalId)
                .setAmount(amount.toString())
                .setShowTips(false)
                .setPaymentMethod(paymentMethod)
                .addAcceptsOnly(PaymentMethods.WECHAT)
                .build();

        PayResponse response = client.pay(request).next();
        System.out.println("WeChat payment initiated - Status: " + response.getStatus());
    }

   private static KodyPayTerminalServiceGrpc.KodyPayTerminalServiceBlockingStub createKodyTerminalPaymentsClient() {
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("X-API-Key", Metadata.ASCII_STRING_MARSHALLER), API_KEY);

        ManagedChannel channel = ManagedChannelBuilder.forTarget(HOSTNAME + ":443")
            .defaultLoadBalancingPolicy("round_robin")
            .nameResolverFactory(new DnsNameResolverProvider())
            .enableRetry()
            .maxRetryAttempts(3)
            .keepAliveWithoutCalls(true)
            .useTransportSecurity()
            .intercept(MetadataUtils.newAttachHeadersInterceptor(metadata))
            .build();

        return KodyPayTerminalServiceGrpc.newBlockingStub(channel);
   }
}
