package repositories;

import com.google.inject.Inject;
import io.ebean.BeanRepository;
import io.ebean.Ebean;
import models.destinations.Destination;
import models.trips.TripDestination;

import java.util.List;

public class TripDestinationRepository extends BeanRepository<Long, TripDestination> {


    @Inject
    public TripDestinationRepository() {
        super(TripDestination.class, Ebean.getDefaultServer());
    }

    /**
     * Fetches one tripDestination thats contain the given destination for each unique trip id.
     *
     * @param destination   the destination being searched for.
     * @return              the set of tripDestinations found.
     */
    public List<TripDestination> fetchTripsContainingDestination(Destination destination) {

        return Ebean.find(TripDestination.class)
                .select("trip")
                .where()
                .eq("destination", destination)
                .setDistinct(true)
                .findList();
    }


    /**
     * Fetches a single TripDestination by the id given.
     *
     * @param tripDestinationId     the id of the TripDestination to be found.
     * @return                      the TripDestination object of the matching TripDestination.
     */
    public TripDestination fetch(Long tripDestinationId) {
        return super.findById(tripDestinationId);
    }


    /**
     * Save the TripDestination object.
     *
     * @param tripDestination       the TripDestination being saved.
     */
    public void save(TripDestination tripDestination) {
        tripDestination.save();
    }


    /**
     * Deletes the trip destination.
     *
     * @param tripDestination       the TripDestination to be deleted.
     */
    public boolean delete(TripDestination tripDestination) {
        return tripDestination.delete();
    }


    /**
     * Updates the TripDestination object.
     *
     * @param tripDestination       the TripDestination being updated.
     */
    public void update(TripDestination tripDestination) {
        tripDestination.update();
    }
}
