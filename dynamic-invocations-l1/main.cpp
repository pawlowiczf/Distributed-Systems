#include <Ice/Ice.h>
#include <iostream>
#include <vector>
#include <string>
#include <sstream>
#include <memory>

using namespace std; 

vector<int> readNumberSequence(const string& input) {
    cout << input << endl;
    cout.flush();

    string line;
    if (!getline(cin, line) || line.empty()) {
        return {};
    }

    vector<int> sequence;
    stringstream ss(line);
    int number;
    while (ss >> number) {
        sequence.push_back(number);
    }

    return sequence;
}

vector<string> readStringSequence(const string& input) {
    cout << input << endl;
    cout.flush();

    string line;
    if (!getline(cin, line) || line.empty()) {
        return {};
    }

    vector<string> sequence;
    stringstream ss(line);
    string text;
    while (ss >> text) {
        sequence.push_back(text);
    }

    return sequence;
}

template<typename T>
void printSequence(const string& label, const vector<T>& seq) {
    cout << label << " = [";
    for (size_t i = 0; i < seq.size(); ++i) {
        cout << seq[i] << (i == seq.size() - 1 ? "" : ", ");
    }
    cout << "]" << endl;
}

string getServerOption() {
    cout << "Select server interface: 'sorter' or 'calc'" << endl;
    cout << flush;

    string line; 
    getline(cin, line);
    return line; 
}

int main(int argc, char* argv[]) {
    int status = 0;

    Ice::CommunicatorPtr communicator;
    Ice::ObjectPrx proxy; 

    communicator = Ice::initialize(argc, argv);

    string option = getServerOption();
    if (option == "sorter") {
        proxy = communicator->stringToProxy("sorter/sorter1:tcp -h 127.0.0.1 -p 10000 -z : udp -h 127.0.0.1 -p 10000 -z");
    } else {
        proxy = communicator->stringToProxy("calc/calc1:tcp -h 127.0.0.1 -p 10000 -z : udp -h 127.0.0.1 -p 10000 -z");
    }

    if (proxy == NULL) {
        throw runtime_error("[Client] |> Provided bad proxy");
    }

    vector<Ice::Byte> params;
    vector<Ice::Byte> outParams;

    cout << "[Client] |> Connected to proxy" << endl;



    string line;
    
    while (true) {
        
        if (option == "calc") {      
            cout << "[Proxy] |> Available commands: getMinNumber, getMaxNumber, getAvgNumber" << endl;
            cout << "[Proxy] |> Press 'x' to exit the program." << endl;
            cout << "|> ";
            cout.flush();

            if (!std::getline(std::cin, line)) {
                break;
            }

            if (line == "x" || line.empty()) {
                break;
            }


            std::string operationName;

            if (line == "getMinNumber") {
                operationName = "getMinNumber";
                vector<int> inputList = readNumberSequence("Provide number sequence");

                Ice::OutputStream outStream(communicator);
                outStream.startEncapsulation();
                outStream.write(inputList);
                outStream.endEncapsulation();
                outStream.finished(params);


                bool ok = proxy->ice_invoke(operationName, Ice::Normal, params, outParams);
                if (ok) {
                    Ice::InputStream inStream(communicator, outParams);
                    inStream.startEncapsulation();
                    int resultNumber;
                    inStream.read(resultNumber);
                    inStream.endEncapsulation();        

                    cout << "[getMinNumber] |>  " << resultNumber << endl;             
                } else {
                    Ice::InputStream inStream(communicator, outParams);
                    inStream.startEncapsulation();
                    inStream.throwException();
                }

            } else if (line == "getMaxNumber") {
                operationName = "getMaxNumber";
                vector<int> inputList = readNumberSequence("Provide number sequence");

                Ice::OutputStream outStream(communicator);
                outStream.startEncapsulation();
                outStream.write(inputList);
                outStream.endEncapsulation();
                outStream.finished(params);


                bool ok = proxy->ice_invoke(operationName, Ice::Normal, params, outParams);
                if (ok) {
                    Ice::InputStream inStream(communicator, outParams);
                    inStream.startEncapsulation();
                    int resultNumber;
                    inStream.read(resultNumber);
                    inStream.endEncapsulation();        

                    cout << "[getMaxNumber] |>  " << resultNumber << endl;             
                } else {
                    Ice::InputStream inStream(communicator, outParams);
                    inStream.startEncapsulation();
                    inStream.throwException();
                }

            } else if (line == "getAvgNumber") {
                operationName = "getAvgNumber";
                vector<int> inputList = readNumberSequence("Provide number sequence");

                Ice::OutputStream outStream(communicator);
                outStream.startEncapsulation();
                outStream.write(inputList);
                outStream.endEncapsulation();
                outStream.finished(params);


                bool ok = proxy->ice_invoke(operationName, Ice::Normal, params, outParams);
                if (ok) {
                    Ice::InputStream inStream(communicator, outParams);
                    inStream.startEncapsulation();
                    int resultNumber;
                    inStream.read(resultNumber);
                    inStream.endEncapsulation();        

                    cout << "[getAvgNumber] |>  " << resultNumber << endl;             
                } else {
                    Ice::InputStream inStream(communicator, outParams);
                    inStream.startEncapsulation();
                    inStream.throwException();
                }
            } else if (line == "x") {
                break;
            } else {
                cout << "Unknown command: '" << line << "'" << endl;
            }
        } else {
            cout << "[Proxy] |> Available commands: sortNumbers, sortStrings" << endl;
            cout << "[Proxy] |> Press 'x' to exit the program." << endl;
            cout << "|> ";
            cout.flush();

            if (!std::getline(std::cin, line)) {
                break;
            }

            if (line == "x" || line.empty()) {
                break;
            }

            std::string operationName;

            if (line == "sortStrings") {
                operationName = "sortStrings";
                vector<string> inputList = readStringSequence("Provide string sequence");

                Ice::OutputStream outStream(communicator);
                outStream.startEncapsulation();
                outStream.write(inputList);
                outStream.endEncapsulation();
                outStream.finished(params);

                bool ok = proxy->ice_invoke(operationName, Ice::Normal, params, outParams);
                if (ok) {
                    Ice::InputStream inStream(communicator, outParams);
                    inStream.startEncapsulation();
                    vector<string> resultStrings;
                    inStream.read(resultStrings);
                    inStream.endEncapsulation();        

                    printSequence("[sortStrings] |>  ", resultStrings);            
                } else {
                    Ice::InputStream inStream(communicator, outParams);
                    inStream.startEncapsulation();
                    inStream.throwException();
                }
            } else if (line == "sortNumbers") {
                operationName = "sortNumbers";
                vector<int> inputList = readNumberSequence("Provide number sequence");

                Ice::OutputStream outStream(communicator);
                outStream.startEncapsulation();
                outStream.write(inputList);
                outStream.endEncapsulation();
                outStream.finished(params);

                bool ok = proxy->ice_invoke(operationName, Ice::Normal, params, outParams);
                if (ok) {
                    Ice::InputStream inStream(communicator, outParams);
                    inStream.startEncapsulation();
                    vector<int> resultNumbers;
                    inStream.read(resultNumbers);
                    inStream.endEncapsulation();        

                    printSequence("[sortNumbers] |>  ", resultNumbers);             
                } else {
                    Ice::InputStream inStream(communicator, outParams);
                    inStream.startEncapsulation();
                    inStream.throwException();
                }               
            } else if (line == "x") {
                break;
            } else {
                cout << "Unknown command: '" << line << "'" << endl;
            }
        }
    }

    if (communicator) {
        cout << "[Client] |> Closing connection" << endl;
    }

    return 0;
}