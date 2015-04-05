package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    
    static BoatGrader bg;

    static int adultsO;
    static int adultsM;
    static int childrenO;
    static int childrenM;
    
    static boolean doneReported;
    static int count;
    static boolean boatO;
    static Lock l1;
//    static Condition boatCapacity;
    static Condition pilot;
    static Condition boatAtO;
    static Condition boatAtM;

    
    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();
	
	System.out.println("\n ***Testing Boats with only 2 children***");
	begin(0, 10, b);
	for (int i=0; i<30; i++){
	    KThread.currentThread().yield();
	}

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
	adultsO = 0;
	adultsM = 0;
	childrenO = 0;
	childrenM = 0;
	
	doneReported = false;
	count = 0;
	boatO = true;
	l1 = new Lock();
//	boatCapacity = new Condition(l1);
	pilot = new Condition(l1);
	boatAtO = new Condition(l1);
	boatAtM = new Condition(l1);
	
	
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
	    
	System.out.println("finished begin");
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
	
	//bg.AdultRowToMolokai();
	//on Molokai
	
	//sleep until everyone arrives
	
    }

    static void ChildItinerary()
    {
	System.out.println("child started");
	boolean inOahu = true;
	boolean OahuIsEmpty = false;
	while(!doneReported)
	{
	    //on Oahu
	    l1.acquire();
	    while (!boatO){
		boatAtO.sleep();
	    }
	//    while (count == 2) {
	//	boatCapacity.sleep();
	//    }
	    if (count==1){ //OR no other children
		count++;
		bg.ChildRowToMolokai();
		//GO TO MOLOKAI...
		boatO=false;
		pilot.wake();
	    }
	    else{ //count==0 AND there are other children
		count++;
		pilot.sleep();
		bg.ChildRideToMolokai();
	    }
	    if (childrenO+adultsO == count){
		OahuIsEmpty = true;
	    }
	    //WE'RE ON MOLOKAI!
	    inOahu = false;
	    count--;
	    childrenO--;
	    childrenM++;
	    if (OahuIsEmpty){
		doneReported = true;
	    }
	    boatAtM.wakeAll();
	    
	    while(!doneReported){
		while (boatO){
		    boatAtM.sleep();
		}
		if (!doneReported){
		    //while (count == 1){
		    //	boatCapacity.sleep();
		    //}
		    bg.ChildRowToOahu();
		    inOahu = true;
		    boatO=true;
		    childrenO++;
		    childrenM--;
		    boatAtO.wakeAll();
		}
	    }
	    //finish
	    l1.release();
	}
	
	
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
