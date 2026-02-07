### What is an Elevator System?
An elevator system manages multiple elevators serving different floors in a building. When someone requests an elevator, the system decides which one to dispatch. Once inside, passengers select their destination floors. The system needs to move elevators efficiently while handling multiple concurrent requests.


### Requirements
1. System manages 3 elevators serving 10 floors (0-9)
2. Users can request an elevator from any floor (hall call). System decides which elevator to dispatch.
3. Once inside, users can select one or more destination floors
4. Simulation runs in discrete time steps (e.g., a `step()` or `tick()` call advances time)
   1. In LLD interviews, we're almost always expected to build a simulation. Whether the problem involves elevators, parking lots, vending machines, or traffic lights, the pattern is the same. Abstract away the hardware and control time yourself with step() or tick(). This keeps the problem tractable in 35 minutes, makes the logic deterministic and testable, and lets us focus on object modeling and algorithms rather than concurrency and hardware interfaces.
5. Elevator stops come in two types:
    - Hall calls: Request from a floor with direction (UP or DOWN)
    - Destination: Request from inside elevator (no direction specified)
6. System handles multiple concurrent pickup requests across floors
7. Invalid requests should be rejected (return false)
    - Non-existent floor numbers
8. Requests for the current floor are treated as a no-op / already served (doors out of scope)

Out of scope:
- Weight capacity and passenger limits
- Door open/close mechanics
- Emergency stop functionality
- Dynamic floor/elevator configuration
- UI/rendering layer

### Entites
1. ElevatorController	- The orchestrator. Receives hall calls from people on floors, decides which elevator should handle each request, and coordinates the overall system. Doesn't need to know the internals of how elevators move. It just dispatches requests and tells elevators to advance.
2. Elevator	- Represents one elevator in the building. Maintains its current floor, direction, and queue of requests. Knows how to execute the movement behavior. Move one floor at a time, stop when needed, reverse when there are no more stops ahead. Doesn't know about other elevators.
3. Request	- Represents a stop the elevator needs to make. We're not sure yet if this needs to be its own class or just a floor number - we'll decide during class design once we understand the movement logic better.

### Class Design

#### Elevator Controller
1. System manages 3 elevator serving 10 floors.
2. It tracks the collection of elevator it controls.

class ElevatorController:
   - elevators: List<Elevator>

In our design, we'll choose to immediately dispatch hall calls to an elevator when they arrive so the controller only needs the list of elevators and doesn't maintain a queue of unassigned requests. It picks an elevator right away and tells that elevator to add the request to its queue. This keeps the controller stateless beyond just holding the elevators.

Note:- The immediate dispatch model is simpler for an interview, but if the interviewer asks "what if all elevators are busy?", you'd want the queue model. Always be ready to explain your tradeoffs.

Requirements:
1. Users can request an elevator from any floor.
  - requestElevator(floor,direction) for the hall call entry point
2. Discrete time steps
  - step() to advance all elevator one tick

class ElevatorController:
  - elevators: List<Elevator> (private method)

  + ElevatorController()
     elevators = [Elevator(), Elevator(), lEevator()]
  + requestElevator(floor,direction) -> boolean
  + step() -> void
    step is how time advances in our simulation. Each call represents one unit of time passing. The controller tells each elevator to take one step. Move one floor, handle stops, or update direction.

#### Elevator

Requirement
1. Elevators serving 10 floors(0-9)
  - current floor position
2. Continue in current direction servicing all requests
  - current direction of travel
3. Once inside users can select one or more destination floors
  - collection of floors to stop at
4. System manages 3 elevators.
  - No extra state needed beyond maintaining multiple instances

class Elevator:
   - currentFloor: int
   - direction: DIRECTION // UP, DOWN, IDLE
   - requests: Set<??>

##### What should request store?
1. Good solution: Store just floor numbers
class Elevator:
    - requests: Set<Integer>

    - addRequest(floor)
        requests.add(floor)
    - step()
        if requests.contains(currentFloor)
            requests.remove(currentFloor)
            // ... continue moving
###### Challenges
Lets assume an elevator at floor 5 going up with requests for [7,8] and User presees DOWN button on floor 7.
  - We add floor 7 to the requests, which is already there so nothing changes.
  - Elevator arrives at floor 7 going UP, it stops. The person waiting sees the elevator going UP and gets on, event though they wanted DOWN. now ther are riding up to floor 8, and then back to 7 and then down.
  - It's confusing. Real passengers see the direction indicator on the elevator. If they're waiting to go down and an UP elevator stops, it creates an awkward choice: get on and ride the wrong way, or wait and hope it comes back?

2.  Request clas with Type
Instead of storing floor numbers, create a Request that captures floor + request type(PICKUP_UP, PICKUP_DOWN, DESTINATION)

enum RequestType:
    PICKUP_UP      // Hall call going up
    PICKUP_DOWN    // Hall call going down
    DESTINATION    // Destination button (stop regardless of elevator direction)

class Request:
    - floor: int
    - type: RequestType

    + Request(floor, type)

class Elevator:
    - currentFloor: int
    - direction: DIRECTION // UP, DOWN, IDLE
    - requests: Set<Request>

    + addRequest(floor, type)
      requests.add(Request(floor, type))
    + step()
      // check if we should stop based on floor and type
      pickupType = (direction == UP)? PICKUP_UP: PICKUP_DOWN
      pickupRequest = Request(currentFloor, pickupType)
      destinationRequest = Request(currentFloor, DESTINATION)

      if(requests.contains(pickupRequest) || requests.contains(destinationRequest))
        requests.remove(pickupRequest)
        requests.remove(destinationRequest)
        //... continue moving

We can consider the same scenario but with these changes: elevator at floor 5 going UP, person on floor 7 wants DOWN.
- We add Request(7, PICKUP_DOWN) to requests. When the elevator reaches floor 7 going UP, it checks for Request(7, PICKUP_UP) or Request(7, DESTINATION). Neither exists, so it doesn't stop. It continues to floor 8, reverses, and comes back DOWN. Now when it reaches floor 7 going DOWN, it finds Request(7, PICKUP_DOWN) and stops.
- The person sees the elevator going the direction they want. No confusion, no awkward ride in the wrong direction.
- The Request class gives us direction-aware stopping, which is critical for proper elevator behavior.

Why does direction need an IDLE state? 
- When an elevator has no requests, it's not moving up or down. It's idle. We need an explicit state to represent "not moving" so the elevator doesn't keep drifting up or down forever. 


#### Final class Design

class ElevatorController:
    - elevators: List<Elevator>

    + ElevatorController()
    + requestElevator(floor, direction) -> boolean
    + step() -> void

class Elevator:
    - currentFloor: int
    - direction: Direction        // UP, DOWN, IDLE
    - requests: Set<Request>

    + Elevator()
    + addRequest(floor, type) -> boolean
    + step() -> void
    + getCurrentFloor() -> int
    + getDirection() -> Direction

class Request:
    - floor: int
    - type: RequestType

    + Request(floor, type)
    + getFloor() -> int
    + getType() -> RequestType

enum Direction:
    UP
    DOWN
    IDLE

enum RequestType:
    PICKUP_UP
    PICKUP_DOWN
    DESTINATION

### Implementation
The most interesting methods are 
 - ElevatorController.requestElevator() (dispatch logic) and 
 - Elevator.step() (the movement logic).   

ElevatorController.requestElevator()
Core Logic:
1. validate the floor numbers
2. pick which elevator should handle this request
3. tell that elevator to add the floor to its stops

Edge cases:
1. Floor out of bounds (less than 0 or greater than 9)
2. Invalid direction

requestElevator(floor, direction)
  // validate
  if floor < 0 || floor > 9
    return false
  if direction != UP && direction != DOWN
    return false

  // Find best elevator
  best = selectBestElevator(floor, direction)

  // convert directon to requestType for hall call
  type == (direction == UP)? PICKUP_UP: PICKUP_DOWN
  return best.add(floor, type)

selectBestElevator(floor, direction)
1. Bad Solution: Nearest Elevator(Ignore Direction)
- Nearest Elevator Strategy

selectBestElevator(floor, direction)
    nearest = elevators[0]
    minDistance = abs(elevators[0].getCurrentFloor() - floor)
    for e in elevators
        distance = abs(e.getCurrentFloor() - floor)
        if distance < minDistance
            minDistance = distance
            nearest = e
    return nearest

- Challenges: The problem becomes obvious with a simple example. Someone on floor 5 presses "up". The nearest elevator is on floor 6, but it's going down to floor 1. We'd send that elevator, even though it's heading the wrong way.
Thanks to our Request class with direction types, the elevator won't actually pick them up while going down - it'll pass by floor 5, go to floor 1, reverse, and come back up. But that's a long wait. The person on floor 5 watches the nearest elevator ignore them as it goes the wrong way. Poor experience.

2. Good Solution: Direction-Aware(Basic)
// Priority 1: Elevators moving toward the floor in the right direction
best = findMovingToward(floor, direction)
if(best!=null) return best

// Priority 2: Idle elevators(pick nearest)
best = findNearestIdle(floor)
if(best!=null) return best

// Priority 3: Any elevator (pick nearest)
return findNearest(floor)

findMovingToward(floor, direction)
    nearest = null
    minDistance = INT_MAX;

    for e in elevators:
        if e.getDirection != direction
            continue
        isMovingToward = 
            (direction == UP && e.getCurrentFloor()<=floor) ||
            (direction == DOWN && e.getCurrentFloor()>=floor) 
        if(!isMovingToward) continue

        distance = abs(e.getCurrentFloor()-floor)
        if(distance < minDistance)
            nearest = e;
            minDistance = distance
    return nearest

Challenges
There's a subtle issue here. Imagine an elevator at floor 3 going UP with a single stop at floor 4. Someone on floor 7 presses UP. Our logic says "elevator is going UP and below floor 7, perfect match!" But the elevator will reach floor 4, have no more stops above, and reverse back DOWN. The person on floor 7 still has to wait for it to come all the way back.
We're not checking if the elevator's existing requests will actually take it to or past the requested floor. We only check its current direction, not where it's committed to going based on its request queue.

3. Great Solution: Direction-Aware with Request Queue Analysis
The proper solution checks not just the elevator's direction, but whether its queued requests will actually take it to or past the requested floor before reversing.

selectBestElevator(floor, direction)
    // Priority 1: Elevators with stops extending to/past the requested floor
    best = findCommittedToFloor(floor, direction)
    if best != null
        return best

    // Priority 2: Idle elevators (pick nearest)
    best = findNearestIdle(floor)
    if best != null
        return best

    // Priority 3: Any elevator (pick nearest)
    return findNearest(floor)

findCommittedToFloor(floor, direction)
    nearest = null
    minDistance = Integer.MAX_VALUE

    for e in elevators
        if e.getDirection() != direction
            continue

        // Check if elevator is moving toward the floor (or already there)
        isMovingToward =
            (direction == UP && e.getCurrentFloor() <= floor) ||
            (direction == DOWN && e.getCurrentFloor() >= floor)

        if !isMovingToward
            continue

        // NEW: Check if elevator has stops that will take it to/past this floor
        if !e.hasRequestsAtOrBeyond(floor, direction)
            continue

        distance = abs(e.getCurrentFloor() - floor)
        if distance < minDistance
            minDistance = distance
            nearest = e

    return nearest

hasRequestsAtOrBeyond(floor, direction)
    for request in requests:
        if(direction == UP && request.getFloor() >= floor)
          // Has a stop at or above the requested floor
          if(request.getType == PICKUP_UP || request.getType() == DESTINATION)
            return true
        if dir == DOWN && request.getFloor() <= floor
            // Has a stop at or below the requested floor
            if request.getType() == PICKUP_DOWN || request.getType() == DESTINATION
                return true
    return false 
Now the elevator at floor 3 going UP with only a stop at floor 4 won't be dispatched for the floor 7 UP request. It has no stops at or beyond floor 7, so it's not truly committed to going there.

The last method of ElevatorController is step():
step()
    for e in elevators
        e.step()
That's it. Just tell each elevator to advance one tick. The controller doesn't need to know how elevators move. That's encapsulated in Elevator.step().

Elevator
The heart of the elevator system is Elevator.step().
    + step()
      // check if we should stop based on floor and type
      pickupType = (direction == UP)? PICKUP_UP: PICKUP_DOWN
      pickupRequest = Request(currentFloor, pickupType)
      destinationRequest = Request(currentFloor, DESTINATION)

      if(requests.contains(pickupRequest) || requests.contains(destinationRequest))
        requests.remove(pickupRequest)
        requests.remove(destinationRequest)
        //... continue moving
1. Bad Solution(FIFO)
The simplest movement strategy is to service requests in the order they were received, regardless of where they are or which direction we're heading. If we modeled requests as a queue instead of a set:
FIFO Movement (using Queue<Request>)
step()
  if requestQueue.isEmpty():
    direction = IDLE
    return
  
  // peek at the oldest request(dont remove yet)
  target = requestQueue.peek();

  if(currentFloor < target.getFloor()) currentFloor++;
  if(currentFloor > target.getFloor()) currentFloor--;

  if(currentFloor == target.getFloor()) requestQueue.poll();

2. Good Solution: Always go to the nearest stop
step()
    if requests.isEmpty()
        direction = IDLE
        return

    // Find nearest request (with lowest floor as tiebreaker for determinism)
    nearest = null
    minDistance = Integer.MAX_VALUE

    for request in requests
        distance = abs(currentFloor - request.getFloor())
        if distance < minDistance || 
            (distance == minDistance && (nearest == null || request.getFloor() < nearest.getFloor()))
            minDistance = distance
            nearest = request

    // Move toward it
    if currentFloor < nearest.getFloor()
        currentFloor++
    else if currentFloor > nearest.getFloor()
        currentFloor--

    // Stop if we arrived
    if currentFloor == nearest.getFloor()
        requests.remove(nearest)
Challenges
We're still changing direction unnecessarily.

3. Great Solution: SCAN(Continue Until Clear, Then Reverse)
The optimal strategy is to continue in your current direction, servicing all stops along the way, and only reverse when there are no more stops ahead. This is the SCAN algorithm

Edge cases to handle
a. Empty Requests - should go idle and do nothing
b. stopped at a floor - need to remove the request, and check if we should reverse or go idle
c. idle with requests - need to pick a direction before doing anything else
d. no requests ahead in current direction - need to reverse

setp()
    // case 1: nothing to do
    if requests.isEmpty():
      direction = IDLE
      return
    
    //case 2: if idle, pick a direction based on nearest request
    if direction == IDLE
      // find the nearest request to establish initial direction
      nearest = null
      minDistance = INT_MAX

      for req in requests:
        distance = abs(req.getFloor() - currentFloor)
        if(distance < minDistance || (distance == minDistance &&
        (nearest == null || req.getFloor()< neareset.getFloor()))):
        minDistance = distance
        nearest = req
      direction = (nearest.getFloor()>currentFloor)?UP: DOWN;



    // case 3: check if we should stop at current floor
    // check pickup requests matching our direction, plus any destination requests
    pickupType = (dir==UP)PICKUP_UP:PICKUP_DOWN;
    pickupRequest = Request(currentFloor, pickupType)
    destinationRequest = Request(currentFloor, DESTINATION)
    
    if requests.contains(pickupRequest) || requests.contains(destinationRequest)
        requests.remove(pickupRequest)
        requests.remove(destinationRequest)

        // After stopping, check if we should go idle or reverse
        if(requests.isEmpty)
            direction = IDLE
            return
        
        if !hasRequestsAhead(durection)
            direction = (direction == UP)? DOWN: UP
            return // we stopped this tick, don't move
            // We return here without moving so the next tick can check if there's a stop at the current floor in the new direction.
    
    // case4: Reverse if no requests ahead
    if !hasRequestsAhead(durection)
        direction = (direction == UP)? DOWN: UP
        return // we stopped this tick, don't move
        // We return here without moving so the next tick can check if there's a stop at the current floor in the new direction.

    // case 5:     Move one floor
    if(direction == UP) currentFloor ++;
    else if (direction == DOWN) currentFloor --;

hasRequestsAhead(dir)
for request in requests
    if dir == UP && request.getFloor() > currentFloor
        return true
    if dir == DOWN && request.getFloor() < currentFloor
        return true
return false

addRequest(floor, type)
    if floor < 0 || floor > 9
        return false
    if floor == currentFloor
        return true  // already here; treat as no-op
    return requests.add(Request(floor, type))


### Extensions
1. "How would you add priority floors or an express elevator?"
A better approach is to keep the standard movement logic for normal elevators and add a separate isExpress flag. In the controller's dispatch logic, if the request is for a priority floor, send the express elevator. That elevator would have a restricted addRequest method that only accepts floors 0, 5, and 9. The Request class doesn't need to change at all - it still stores floor and type, but the elevator validates which floors it accepts."
class ElevatorController:
    - elevators: List<Elevator>
    - expressElevator: Elevator  // NEW: track which elevator is express

class Elevator:
    - isExpress: bool
    - expressFloors: Set<int> = {0, 5, 9}

addRequest(floor, type)
    if floor < 0 || floor > 9
        return false
    if floor == currentFloor
        return true  // already here; treat as no-op
    
    // NEW: Reject non-express floors if this is an express elevator
    if isExpress && !expressFloors.contains(floor)
        return false
    
    return requests.add(Request(floor, type))

// In ElevatorController dispatch logic:
selectBestElevator(floor, dir)
    // NEW: For express floors, prefer the express elevator when it's idle; otherwise fall through to normal selection
    if floor in {0, 5, 9} && expressElevator.getDirection() == IDLE
        return expressElevator
    // ... normal selection logic for regular elevators

2. "How would you add undo to cancel a floor request?"

removeRequest(floor, type)
    requests.remove(Request(floor, type))  // That's it - just remove from the set