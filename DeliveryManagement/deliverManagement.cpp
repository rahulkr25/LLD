#include <iostream>
#include <unordered_map>
#include <vector>
#include <string>
#include <algorithm>
using namespace std;

class DeliveryManagement{
/*
“We are building an in-memory delivery cost tracking system that tracks drivers, their deliveries, and computes payment-related analytics.
The system should support incremental features like cost calculation, payment settlement, and analytics on driver activity.”

Drivers are paid per hour
Cost for a delivery = (endTime - startTime) * hourlyRate
Partial deliveries can be paid partially up to a given time

addDriver(driverId, hourlyRate)
addDelivery(driverId, startTime, endTime)
getTotalCost()

payUpToTime(upToTime): Settle all delivery cost incurred up to time T
This includes: Fully completed deliveries Partially completed deliveries

getCostToBePaid() = totalCost - totalPaidCost

Partial Payments: If a delivery is:
start = 10, end = 20
payUpToTime = 15
Only cost from [10 → 15] is paid. This implies: Deliveries must track: Already-paid time or cost,Payments must be idempotent

getMaxActiveDriversInLast24Hours(currentTime)
A driver is active if: They have at least one delivery overlapping: [currentTime - 24h, currentTime]
*/
private:
struct Delivery{
    int driverId;
    long start; //epoch time
    long end;
    long lastPaid;  //track partial payments
    double rate;
};

// driverId -> hourlyRate
unordered_map<int,double>drivers;

//all deliveries
vector<Delivery> deliveries;
double totalCost = 0.0;
double paidCost = 0.0;

public:

void addDriver(int driverId, double hourlyRate){
    drivers[driverId] = hourlyRate;
}
void addDelivery(int driverId, long startTime, long endTime){
    if(!drivers.count(driverId) || startTime >= endTime){
        cout<<"Invalid driver or time range"<<endl;
        return;
    }
    double rate = drivers[driverId];
    double cost = (endTime - startTime) * (rate / 3600.0 * 1000); // cost for miliseconds of delivery
    totalCost += cost;
    deliveries.push_back({driverId, startTime, endTime, startTime, rate});
}
double getTotalCost() const{
    return totalCost;
}

// startTime = 5, endTime = 15, lastPaid = 10, payUptoTime = 12
void payUptoTime(long upToTime){ // should be idempotent
    for(Delivery &d: deliveries){
        if(d.lastPaid<upToTime && d.end>d.lastPaid){
          long payableStart = d.lastPaid;
          long payableEnd = min(d.end, upToTime);

          if(payableEnd>payableStart){
            double cost = (payableEnd - payableStart) * (d.rate / 3600.0 * 1000);
            paidCost += cost;
            d.lastPaid = payableEnd;
        }
    }
}
}
double getCostToBePaid() const{
    return totalCost - paidCost;    
}

int getMaxActiveDriversInLast24Hours(long currentTime){
    long WindowStart = currentTime - 24*3600*1000; //24 hours in milliseconds
    vector<pair<long, long>>events; // {time, +1/-1} +1 for start, -1 for end
    for(const Delivery &d: deliveries){
        if(d.start<=currentTime && d.end>=WindowStart){
            events.push_back({max(d.start, WindowStart), +1});
            events.push_back({min(d.end, currentTime), -1});
        }
    }
    sort(events.begin(), events.end());
    int activeDrivers = 0;
    int maxActiveDrivers = 0;
    for(const auto &e: events){
        activeDrivers += e.second;
        maxActiveDrivers = max(maxActiveDrivers, activeDrivers); 
    }
    return maxActiveDrivers;
}
};

int main(){
    DeliveryManagement tracker;
    tracker.addDriver(1, 100); // $100/hr
    tracker.addDriver(2, 80);

    // epoch seconds
    tracker.addDelivery(1, 0, 3600);    // 1 hour
    tracker.addDelivery(2, 1800, 5400); // overlaps

    cout << "Total Cost: " << tracker.getTotalCost() << endl;

    tracker.payUptoTime(1800);
    cout << "Cost to be paid after first payment: "
         << tracker.getCostToBePaid() << endl;

    tracker.payUptoTime(3600);
    cout << "Cost to be paid after second payment: "
         << tracker.getCostToBePaid() << endl;

    cout << "Max active drivers in last 24 hours: "
         << tracker.getMaxActiveDriversInLast24Hours(7200)
         << endl;

    return 0;
}