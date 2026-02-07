import java.util.ArrayList;
import java.util.List;

public class ElevatorController {
    private List<Elevator> elevators;

    public ElevatorController() {
        elevators = new ArrayList<>();
        elevators.add(new Elevator());
        elevators.add(new Elevator());
        elevators.add(new Elevator());
    }

    public boolean requestElevator(int floor, Direction direction) {
        if (floor < 0 || floor > 9) {
            return false;
        }
        if (direction != Direction.UP && direction != Direction.DOWN) {
            return false;
        }

        Elevator best = selectBestElevator(floor, direction);
        if (best == null) {
            return false;
        }
        RequestType type = (direction == Direction.UP) ? RequestType.PICKUP_UP : RequestType.PICKUP_DOWN;
        return best.addRequest(floor, type);
    }

    public void step() {
        for (Elevator elevator : elevators) {
            elevator.step();
        }
    }

    private Elevator selectBestElevator(int floor, Direction direction) {
        Elevator best = findCommittedToFloor(floor, direction);
        if (best != null) {
            return best;
        }

        best = findNearestIdle(floor);
        if (best != null) {
            return best;
        }

        return findNearest(floor);
    }

    private Elevator findCommittedToFloor(int floor, Direction direction) {
        Elevator nearest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Elevator e : elevators) {
            if (e.getDirection() != direction) {
                continue;
            }

            boolean isMovingToward =
                (direction == Direction.UP && e.getCurrentFloor() < floor) ||
                (direction == Direction.DOWN && e.getCurrentFloor() > floor);

            if (!isMovingToward) {
                continue;
            }

            if (!e.hasRequestsAtOrBeyond(floor, direction)) {
                continue;
            }

            int distance = Math.abs(e.getCurrentFloor() - floor);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = e;
            }
        }

        return nearest;
    }

    private Elevator findNearestIdle(int floor) {
        Elevator nearest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Elevator e : elevators) {
            if (e.getDirection() != Direction.IDLE) {
                continue;
            }

            int distance = Math.abs(e.getCurrentFloor() - floor);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = e;
            }
        }

        return nearest;
    }

    private Elevator findNearest(int floor) {
        Elevator nearest = elevators.get(0);
        int minDistance = Math.abs(elevators.get(0).getCurrentFloor() - floor);

        for (Elevator e : elevators) {
            int distance = Math.abs(e.getCurrentFloor() - floor);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = e;
            }
        }

        return nearest;
    }
}
