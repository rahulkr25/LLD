#include<iostream>
#include<vector>
using namespace std;

class Solution {
public:
   int minTransfers(vector<vector<int>>&transactions){
    int balance[12]={0};
    for(auto& transaction: transactions){
        int from = transaction[0];
        int to = transaction[1];
        int amount = transaction[2];

        balance[from]-= amount; // person 'from' owes this amount
        balance[to]+=amount; //person 'to' is owed this amount
    }
    vector<int>nonZeroBalances;
    for(int personBalance: balance){
        if(personBalance!=0){
            nonZeroBalances.push_back(personBalance);
        }
    }
    int numPeople = nonZeroBalances.size();
    int totalStates = (1<<numPeople);
    int dp[totalStates];
    memset(dp, 0x3f, sizeof(dp));
    dp[0]=0;

    for(int mask =1;mask<totalStates;mask++){
        int sumOfBalances=0;
        for(int person = 0;person<numPeople;person++){
            if((mask>>person)&1){// checks if person is in current subset
                sumOfBalances+=nonZeroBalances[person];
            }
        }

        if(sumOfBalances==0){
            int peopleCount = __builtin_popcount(mask);
            dp[mask] = peopleCount -1;

            //option2: split into smaller zero sum subsets;
            for(int subset = (mask-1)&mask;subset>0;subset=(subset-1)&mask){
                int complement = mask ^ subset;
                dp[mask] = min(dp[mask], dp[subset]+dp[complement]);
            }

        }
    }
    return dp[(1<<numPeople)-1];

   }
};

int main(){
    
}