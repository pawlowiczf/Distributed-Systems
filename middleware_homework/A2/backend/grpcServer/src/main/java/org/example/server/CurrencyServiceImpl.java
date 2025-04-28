package org.example.server;

import io.grpc.stub.StreamObserver;
import org.example.CurrencyEnum;
import org.example.CurrencyMessage;
import org.example.CurrencyServiceGrpc;
import org.example.UserRequest;
import org.example.generator.Currency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CurrencyServiceImpl extends CurrencyServiceGrpc.CurrencyServiceImplBase {


    private static List<ServerSubscriber> subscriberList = new ArrayList<>();

    private static Map<ServerSubscriber, List<CurrencyMessage>> bufforMap = new HashMap<>();


    @Override
    public void connect(UserRequest request, StreamObserver<CurrencyMessage> responseObserver) {
        System.out.println(request.getCurrencyEnum());
        CurrencyEnum userSubscribedCurrencyEnum = request.getCurrencyEnum();

        ServerSubscriber newSubscriber = new ServerSubscriber(request.getUserId(), userSubscribedCurrencyEnum, responseObserver);



        if(!isUserSubscribed(newSubscriber.getUserId())){
            System.out.println("Nowy uzytkownik sie podlaczyl " + request.getUserId());
            subscriberList.add(newSubscriber);
            bufforMap.put(newSubscriber, new ArrayList<>());
        }else{
            System.out.println("Istniejacy uzytkownik " + request.getUserId() + " sie podlaczyl, sprawdzam bufor");
            ServerSubscriber serverSubscriberWithOldResponseObserver = subscriberList.stream()
                    .filter(sub -> sub.getUserId().equals(newSubscriber.getUserId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Subskrybent nie znaleziony"));

            serverSubscriberWithOldResponseObserver.setResponseObserver(newSubscriber.getResponseObserver());
            if(!bufforMap.get(newSubscriber).isEmpty()){
                System.out.println("Uzytkownik ma niewyslane " + bufforMap.get(newSubscriber).size() + ", wysylam!");
                bufferNotifier(newSubscriber);
            }
        };

    }




    public static void notifySubscribers(Currency currentCurrency){
        for(ServerSubscriber subscriber : subscriberList){
            if(subscriber.getCurrencyEnum().toString().equals(currentCurrency.getCurrencyEnum().toString())){
                CurrencyMessage currencyMessage = CurrencyMessage.newBuilder()
                        .setCurrencyEnum(subscriber.getCurrencyEnum())
                        .setValueInPLN(currentCurrency.getValueInPLN())
                        .setDate(currentCurrency.getDate().toString())
                        .build();

                try{
                    subscriber.getResponseObserver().onNext(currencyMessage);
                }catch (Exception e){
                    bufforMap.get(subscriber).add(currencyMessage);
                }
            }
        }
    }

    private boolean isUserSubscribed(String userId) {
        return subscriberList.stream()
                .anyMatch(sub -> sub.getUserId().equals(userId));
    }

    private void bufferNotifier(ServerSubscriber serverSubscriber){
        List<CurrencyMessage> currencyMessages = bufforMap.get(serverSubscriber);
        List<CurrencyMessage> messagesToRemove = new ArrayList<>();
        //send buffered messages
        currencyMessages.forEach(message -> {
            try{
                serverSubscriber.getResponseObserver().onNext(message);
                messagesToRemove.add(message);
            }catch (Exception e){
                //do nothing, still hold message in buffer
            }
        });

        currencyMessages.removeAll(messagesToRemove);
    }


}
