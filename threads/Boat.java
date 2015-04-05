package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static final int NO_PILOT = 0;
    static final int ADULT_PILOT = 1;
    static final int CHILD_PILOT = 2;
    
    static BoatGrader bg;
    static boolean boatO; //if false, assume boat on Molokai
    static int pilot;
    static boolean rider;
    static int adultsO;
    static int adultsM;
    static int childrenO;
    static int childrenM;
    
    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();
	
	System.out.println("\n ***Testing Boats with only 2 children***");
	begin(0, 2, b);

//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//  	begin(1, 2, b);

//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
//  	begin(3, 3, b);
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
	// Store the externally generated autograder in a class
	// variable to be accessible by children.
	bg = b;

	// Instantiate global variables here
	boatO = true;
	pilot = NO_PILOT;
	rider = false;
	adultsO = 0;
	adultsM = 0;
	childrenO = 0;
	childrenM = 0;
	
	
	// Create threads here. See section 3.4 of the Nachos for Java
	// Walkthrough linked from the projects page.
	
	for (int i=0; i<adults; i++){
	    Runnable r = new Runnable() {
		public void run() {
		    AdultItinerary();
		}
	    };
	    KThread t = new KThread(r);
	    t.setName("Adult Thread " + i);
	    adultsO++;
	    t.fork();
	}
	
	for (int i=0; i<children; i++){
	    Runnable r = new Runnable() {
		public void run() {
		    ChildItinerary();
		}
	    };
	    KThread t = new KThread(r);
	    t.setName("Child Thread " + i);
	    childrenO++;
	    t.fork();
	}
    }

    static void AdultItinerary()
    {
	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/
	
	
	
    }

    static void ChildItinerary()
    {
    }

    static void SampleItinerary()
    {
	// Please note that this isn't a valid solution (you can't fit
	// all of them on the boat). Please also note that you may not
	// have a single thread calculate a solution and then just play
	// it back at the autograder -- you will be caught.
	System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
	bg.AdultRowToMolokai();
	bg.ChildRideToMolokai();
	bg.AdultRideToMolokai();
	bg.ChildRideToMolokai();
    }
    
}
