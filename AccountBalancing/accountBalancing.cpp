#include<iostream>
#include<vector>
#include<unordered_map>
using namespace std;

class DebtSettlement {
public:
   void settleAndPrint(vector<vector<int>>&transactions){
    unordered_map<int, int> balance;
    for(auto& transaction: transactions){
        int from = transaction[0];
        int to = transaction[1];
        int amount = transaction[2];
        
        balance[from]-= amount; // person 'from' owes this amount
        balance[to]+=amount; //person 'to' is owed this amount
    }
    vector<pair<int,int>>creditors; // {personId, amount}
    vector<pair<int,int>>debtors; // {personId, amount}
    for(const auto&entry: balance){
        if(entry.second<0){
            debtors.push_back({entry.first, -entry.second});
        }else if(entry.second>0){
            creditors.push_back({entry.first, entry.second});
        }
    }

    // Greedy Settlement
    int i=0,j=0;
    while(i<debtors.size() && j<creditors.size()){
        int pay = min(creditors[j].second, debtors[i].second);
        cout << "Person " << creditors[j].first
                 << " pays Person " << debtors[i].first
                 << " amount " << pay << "\n";
        debtors[i].second -= pay;
        creditors[j].second -= pay;
        if (debtors[i].second == 0) i++;
        if (creditors[j].second == 0) j++;

    }
   }
};

int main(){
      vector<vector<int>> transactions = {
        {0, 1, 10},
        {1, 2, 5},
        {2, 0, 3}
    };
    // 0 -7 
    // 1 5
    // 2 2
    // 


    DebtSettlement ds;
    ds.settleAndPrint(transactions);
    return 0;
}