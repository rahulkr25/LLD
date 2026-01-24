#include <iostream>
#include <unordered_map>
#include <vector>
#include <string>
using namespace std;

class TransactionalKeyValueStore{
    private:
    const string TOMBSTONE = "__DELETED__";

    //permanent store
    unordered_map<string, string> store;

    //stack of transactions
    vector<unordered_map<string, string>> transaction_stack;

    bool inTransaction() const{
        return !transaction_stack.empty();
    }

    public:
    void set(string key, string value){
        if(inTransaction()){
            transaction_stack.back()[key] = value;
        } else {
            store[key] = value;
        }
    }
    string get(string key){
        for(int i=transaction_stack.size()-1;i>=0;i--){
            unordered_map<string, string> txn = transaction_stack[i];
            if(txn.find(key) != txn.end()){
                return txn[key] == TOMBSTONE ? "null" : txn[key];
            }
        }
        return store.count(key)? store[key] : "null";
    }
    void deleteKey(string key){
        if(inTransaction()){
            transaction_stack.back()[key] = TOMBSTONE;
        } else {
           store.erase(key); 
        }
    }
    void begin(){
        transaction_stack.push_back(unordered_map<string, string>());
    }
    bool rollback(){
        if(!inTransaction()){
            return false;
        }
        transaction_stack.pop_back();
        return true;
    }
    bool commit(){
        if(!inTransaction()){
            return false;
        }
         unordered_map<string, string> topTransaction = transaction_stack.back();
         transaction_stack.pop_back();
         if(inTransaction()){
            for(auto & entry:topTransaction){
                transaction_stack.back()[entry.first] = entry.second;
            }
         } else{
            //Apply to permanent store
            for(auto& entry: topTransaction){
                if(entry.second == TOMBSTONE){
                    store.erase(entry.first);
                } else {
                    store[entry.first] = entry.second;
                }
            }
         }
        return true;
    }
};

int main(){
    TransactionalKeyValueStore kv;
    cout << "=== Basic Operations ===" << endl;
    kv.set("a", "10");
    cout << kv.get("a") << endl; // 10
    kv.deleteKey("a");
    cout << kv.get("a") << endl; // null

    cout << "\n=== Single Transaction ===" << endl;
    kv.set("a", "10");
    kv.begin();
    kv.set("a", "20");
    cout << kv.get("a") << endl; // 20
    kv.rollback();
    cout << kv.get("a") << endl; // 10

    cout << "\n=== Commit Transaction ===" << endl;
    kv.begin();
    kv.set("a", "30");
    kv.commit();
    cout << kv.get("a") << endl; // 30

    cout << "\n=== Nested Transactions ===" << endl;
    kv.begin();
    kv.set("b", "40");
    kv.begin();
    kv.set("b", "50");
    cout << kv.get("b") << endl; // 50
    kv.rollback();
    cout << kv.get("b") << endl; // 40
    kv.commit();
    cout << kv.get("b") << endl; // 40


    cout << "\n=== Delete in Transaction ===" << endl;
    kv.begin();
    kv.deleteKey("a");
    cout << kv.get("a") << endl; // null
    kv.rollback();
    cout << kv.get("a") << endl; // 30

    cout << "\n=== Delete and Commit ===" << endl;
    kv.begin();
    kv.deleteKey("a");
    kv.commit();
    cout << kv.get("a") << endl; // null

    cout << "\nAll tests executed successfully." << endl;
    return 0;
}
