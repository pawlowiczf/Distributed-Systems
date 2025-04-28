#include <Ice/Ice.h>
#include <iostream>
#include <vector>
#include <string>
#include <sstream>
#include <memory>


std::vector<int> readIntSequence(const std::string& prompt) {
    std::cout << prompt << " (liczby oddzielone spacją): ";
    std::cout.flush();
    std::string line;
    if (!std::getline(std::cin, line) || line.empty()) {
        return {};
    }

    std::vector<int> sequence;
    std::stringstream ss(line);
    int number;
    while (ss >> number) {
        sequence.push_back(number);
    }

    if (!ss.eof()) {
        char remaining;
        ss >> remaining;
        if (!ss.fail() && !isspace(remaining)) {
            std::cerr << "Ostrzeżenie: Nie udało się sparsować całej linii jako liczby." << std::endl;
        }
    }
    return sequence;
}

void printIntSequence(const std::string& label, const std::vector<int>& seq) {
    std::cout << label << " = [";
    for (size_t i = 0; i < seq.size(); ++i) {
        std::cout << seq[i] << (i == seq.size() - 1 ? "" : ", ");
    }
    std::cout << "]" << std::endl;
}


int main(int argc, char* argv[]) {
    int status = 0;

    Ice::CommunicatorPtr communicator;

    try {

        communicator = Ice::initialize(argc, argv);

        std::string proxyString = "calc/calc11:tcp -h 127.0.0.1 -p 10000 -z : udp -h 127.0.0.1 -p 10000 -z";
        Ice::ObjectPrx base = communicator->stringToProxy(proxyString);
        if (!base) {
            throw std::runtime_error("Nieprawidłowe proxy: " + proxyString);
        }


        std::cout << "Połączono z proxy: " << proxyString << std::endl;
        std::cout << "Dynamiczny klient C++ gotowy." << std::endl;


        std::string line;
        while (true) {
            try {
                std::cout << "\nDostępne komendy: getMin, getMax, getAvg, intersection, difference, x (wyjście)" << std::endl;
                std::cout << "==> ";
                std::cout.flush();

                if (!std::getline(std::cin, line)) {
                    break;
                }

                line.erase(0, line.find_first_not_of(" \t\n\r\f\v"));
                line.erase(line.find_last_not_of(" \t\n\r\f\v") + 1);


                if (line == "x" || line.empty()) {
                    break;
                }


                std::string operationName;
                std::vector<Ice::Byte> params;
                std::vector<Ice::Byte> outParams;

                if (line == "getMin") {
                    operationName = "getMin";
                    std::vector<int> inputList = readIntSequence("Podaj listę liczb dla getMin");

                    // Zapis do bufora
                    Ice::OutputStream outStream(communicator);
                    outStream.startEncapsulation();
                    outStream.write(inputList);
                    outStream.endEncapsulation();
                    outStream.finished(params);


                    base->ice_invoke(operationName, Ice::Normal, params, outParams);


                    Ice::InputStream inStream(communicator, outParams);
                    inStream.startEncapsulation();
                    int resultInt;
                    inStream.read(resultInt);
                    inStream.endEncapsulation();
                    std::cout << "getMin RESULT = " << resultInt << std::endl;

                } else if (line == "getMax") {
                    operationName = "getMax";
                    std::vector<int> inputList = readIntSequence("Podaj listę liczb dla getMax");

                    Ice::OutputStream outStream(communicator);
                    outStream.startEncapsulation();
                    outStream.write(inputList);
                    outStream.endEncapsulation();
                    outStream.finished(params);

                    base->ice_invoke(operationName, Ice::Normal, params, outParams);


                    Ice::InputStream inStream(communicator, outParams);
                    inStream.startEncapsulation();
                    int resultInt;
                    inStream.read(resultInt);
                    inStream.endEncapsulation();
                    std::cout << "getMax RESULT = " << resultInt << std::endl;

                } else if (line == "getAvg") {
                    operationName = "getAvg";
                    std::vector<int> inputList = readIntSequence("Podaj listę liczb dla getAvg");


                    Ice::OutputStream outStream(communicator);
                    outStream.startEncapsulation();
                    outStream.write(inputList);
                    outStream.endEncapsulation();
                    outStream.finished(params);

                    base->ice_invoke(operationName, Ice::Normal, params, outParams);


                    Ice::InputStream inStream(communicator, outParams);
                    inStream.startEncapsulation();
                    float resultFloat;
                    inStream.read(resultFloat);
                    inStream.endEncapsulation();
                    std::cout << "getAvg RESULT = " << resultFloat << std::endl;

                } else if (line == "intersection") {
                    operationName = "intersection";
                    std::vector<int> list1 = readIntSequence("Podaj pierwszą listę liczb dla intersection");
                    std::vector<int> list2 = readIntSequence("Podaj drugą listę liczb dla intersection");


                    Ice::OutputStream outStream(communicator);
                    outStream.startEncapsulation();
                    outStream.write(list1);
                    outStream.write(list2);
                    outStream.endEncapsulation();
                    outStream.finished(params);

                    base->ice_invoke(operationName, Ice::Normal, params, outParams);


                    Ice::InputStream inStream(communicator, outParams);
                    inStream.startEncapsulation();
                    std::vector<int> resultList;
                    inStream.read(resultList);
                    inStream.endEncapsulation();
                    printIntSequence("intersection RESULT", resultList);

                } else if (line == "difference") {
                    operationName = "difference";
                    std::vector<int> list1 = readIntSequence("Podaj pierwszą listę liczb dla difference (A)");
                    std::vector<int> list2 = readIntSequence("Podaj drugą listę liczb dla difference (B)");


                    Ice::OutputStream outStream(communicator);
                    outStream.startEncapsulation();
                    outStream.write(list1);
                    outStream.write(list2);
                    outStream.endEncapsulation();
                    outStream.finished(params);

                    base->ice_invoke(operationName, Ice::Normal, params, outParams);

                    Ice::InputStream inStream(communicator, outParams);
                    inStream.startEncapsulation();
                    std::vector<int> resultList;
                    inStream.read(resultList);
                    inStream.endEncapsulation();
                    printIntSequence("difference (A \\ B) RESULT", resultList);

                } else {
                    std::cout << "Nieznana komenda: '" << line << "'" << std::endl;
                }
            } catch (const Ice::UnknownUserException& ex) {
                std::cerr << "BŁĄD: Operacja zgłosiła wyjątek użytkownika (np. NoInput): " << ex.unknown << std::endl;
            } catch (const Ice::OperationNotExistException& ex) {
                std::cerr << "BŁĄD: Operacja '" << ex.operation << "' nie istnieje dla obiektu." << std::endl;
            } catch (const Ice::CommunicatorDestroyedException&) {
                std::cerr << "BŁĄD: Communicator został zniszczony." << std::endl;
                break; // Zakończ pętlę, jeśli komunikator jest zniszczony
            } catch (const Ice::Exception& ex) {
                std::cerr << "Błąd komunikacji ICE: " << ex << std::endl;
            } catch (const std::exception& ex) {
                std::cerr << "Wystąpił błąd standardowy: " << ex.what() << std::endl;
            } catch (...) {
                std::cerr << "Wystąpił nieznany błąd." << std::endl;
            }
        } // koniec while(true)

    } catch (const Ice::Exception& ex) {
        std::cerr << "Błąd inicjalizacji ICE: " << ex << std::endl;
        status = 1;
    } catch (const std::exception& ex) {
        std::cerr << "Wystąpił błąd: " << ex.what() << std::endl;
        status = 1;
    } catch (...) {
        std::cerr << "Wystąpił nieznany błąd podczas inicjalizacji." << std::endl;
        status = 1;
    }


    if (communicator) {
        std::cout << "Niszczenie communicatora..." << std::endl;
    }

    return status;
}