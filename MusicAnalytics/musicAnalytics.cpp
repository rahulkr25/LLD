#include <iostream>
#include <unordered_map>
#include <unordered_set>
#include <list>
#include <string>
#include <vector>
#include <numeric>
using namespace std;
class MusicAnalytics{

/*
addSong(songId, title) - Add a new song to the system
playSong(userId, songId) - Record that a user played a song
printAnalytics() - Print most played songs by unique users (not total plays)
printRecentlyPlayed(userId)
printRecentlyPlayed(userId, k)
starSong(userId, songId)
unstarSong(userId, songId)
printRecentFavouriteSongs(userId, k)
*/
private:
  // songId -> title
  unordered_map<int, string> songs;
  // songId -> set of unique users who played it
  unordered_map<int, unordered_set<int>> songUsers;
  // userId -> recently played unique songs(most recent at front)
  unordered_map<int,list<int>>recentPlays;
  //userId -> favourite songs 
  unordered_map<int,unordered_set<int>> favourites;

  const int DEFAULT_RECENT = 3;

public:
   void addSong(int songId, string title){
    songs[songId]=title;
   }

   void playSong(int userId, int songId){
    if(!songs.count(songId)){
        cout<<"Song ID "<<songId<<" does not exist."<<endl;
        return;
    }
    //Track unique users for the song
    songUsers[songId].insert(userId);

    //Updare recent plays for the user
    list<int>&recentPlay = recentPlays[userId];
    recentPlay.remove(songId); //Remove if already exists
    recentPlay.push_front(songId); //Add to front   
   }

   void printAnalytics(){
    vector<pair<int,int>>stats;//{{uniqueUserCount, songId}}
    for(const auto&entry:songUsers){
        stats.push_back({entry.second.size(), entry.first});
    }
    sort(stats.begin(), stats.end(), greater<pair<int,int>>());
    cout<<"=== Most Played Songs by Unique Users ==="<<endl;
    for(const auto&entry:stats){
        cout<<"Song ID: "<<entry.second<<", Title: "<<songs[entry.second]<<", Unique Users: "<<entry.first<<endl;
    }
   }
    
   void printRecentlyPlayed(int userId){
    printRecentlyPlayed(userId, DEFAULT_RECENT);
   }
   
   void printRecentlyPlayed(int userId, int k){
    cout<<"=== Recently Played Songs for User "<<userId<<" ==="<<endl;
    int count=0;
    for(int songId: recentPlays[userId]){
        cout<<songs[songId]<<endl;
        if(++count>=k) break;
   }
  }

   void starSong(int userId, int songId){
    if(!songs.count(songId)){
        cout<<"Song ID "<<songId<<" does not exist."<<endl;
        return;
    }
    favourites[userId].insert(songId);
   }

    void unstarSong(int userId, int songId){
     if(favourites[userId].count(songId)){
          favourites[userId].erase(songId);
     } else {
          cout<<"Song ID "<<songId<<" is not in favourites for User "<<userId<<"."<<endl;
     }
    }

     void printRecentFavouriteSongs(int userId, int k){
      cout<<"=== Recent Favourite Songs for User "<<userId<<" ==="<<endl;
      int count=0;
      for(int songId: recentPlays[userId]){
        if(favourites[userId].count(songId)){
            cout<<songs[songId]<<endl;
            if(++count>=k) break;
        }
      }
     }
};

int main(){
    MusicAnalytics analytics;

    analytics.addSong(1, "Believer");
    analytics.addSong(2, "Shape of You");
    analytics.addSong(3, "Blinding Lights");
    analytics.addSong(4, "Levitating");

    analytics.playSong(101, 1);
    analytics.playSong(101, 2);
    analytics.playSong(101, 3);

    analytics.playSong(102, 1);
    analytics.playSong(102, 3);

    analytics.playSong(103, 1);
    analytics.playSong(103, 4);

    cout << "\n=== Analytics ===\n";
    analytics.printAnalytics();


    cout << "\n=== Recently Played ===\n";
    analytics.printRecentlyPlayed(101);

    cout << "\n=== Favourite Songs ===\n";
    analytics.starSong(101, 1);
    analytics.starSong(101, 3);

    analytics.playSong(101, 4);
    analytics.playSong(101, 1);

    analytics.printRecentFavouriteSongs(101, 2);

    return 0;
}