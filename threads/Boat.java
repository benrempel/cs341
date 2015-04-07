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
    static Condition boatEmpty;
    static Condition boatAtO;
    static Condition boatAtM;

    
    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();
	
	System.out.println("\n ***Testing Boats with only 2 children***");
	begin(0, 2, b);
	for (int i=0; i<10; i++){
	    KThread.currentThread().yield();
	}

	System.out.println("\n ***Testing Boats with only 1 children***");
	begin(0, 1, b);
	for (int i=0; i<10; i++){
	    KThread.currentThread().yield();
	}

	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
  	begin(1, 2, b);
	for (int i=0; i<10; i++){
	    KThread.currentThread().yield();
	}
	
	System.out.println("\n ***Testing Boats with only 1 adult***");
	begin(1, 0, b);
	for (int i=0; i<10; i++){
	    KThread.currentThread().yield();
	}

  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
  	begin(3, 3, b);
	for (int i=0; i<40; i++){
	    KThread.currentThread().yield();
	}
	
	System.out.println("\n ***Testing Boats with 2 children, 10 adults***");
	begin(10, 2, b);
	for (int i=0; i<400; i++){
	    KThread.currentThread().yield();
	}
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
	bg = b;

	adultsO = 0;
	adultsM = 0;
	childrenO = 0;
	childrenM = 0;
	doneReported = false;
	count = 0;
	boatO = true;
	l1 = new Lock();
	pilot = new Condition(l1);
	boatEmpty = new Condition(l1);
	boatAtO = new Condition(l1);
	boatAtM = new Condition(l1);
	
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
	boolean OahuIsEmpty = false;
	l1.acquire();
	    
	while (!boatO || childrenO > 1){
	    boatAtO.sleep();
	}
	//System.out.print(KThread.currentThread().getName() + "  ");
	bg.AdultRowToMolokai();
	//remember how many people were left
	if (childrenO+adultsO == count){
	    OahuIsEmpty = true;
	}
	//GO TO MOLOKAI...
	boatO=false;
	
	adultsO--;
	adultsM++;
	if (OahuIsEmpty){
	    doneReported = true;
	}
	boatAtM.wakeAll();
	l1.release();
    }

    static void ChildItinerary()
    {
	boolean inOahu = true;
	boolean OahuIsEmpty = false;
	while(!doneReported)
	{
	    l1.acquire();
	    if (inOahu){
		while (!boatO || (childrenO==1 && adultsO>0)){
		    boatAtO.sleep();
		}
		if (count==1 || childrenO+adultsO==1){
		    count++;
		    //System.out.print(KThread.currentThread().getName() + "  ");
		    bg.ChildRowToMolokai();
		    //remember how many people were left
		    if (childrenO+adultsO == count){
			OahuIsEmpty = true;
		    }
		    //GO TO MOLOKAI...
		    boatO=false;
		    pilot.wake();
		}
		else{
		    count++;
		    pilot.sleep();
		    //System.out.print(KThread.currentThread().getName() + "  ");
		    bg.ChildRideToMolokai();
		}
		
		inOahu = false;
		count--;
		childrenO--;
		childrenM++;
		if (OahuIsEmpty){
		    doneReported = true;
		}
		if (count==1){
		    boatEmpty.sleep();
		    boatAtM.wakeAll();
		}
		else{
		    boatEmpty.wake();
		}
	    }
	    
	    if (!inOahu){
		while (boatO || count > 0){
		    boatAtM.sleep();
		}
		if (!doneReported){
		    //System.out.print(KThread.currentThread().getName() + "  ");
		    bg.ChildRowToOahu();
		    inOahu = true;
		    boatO=true;
		    childrenO++;
		    childrenM--;
		    boatAtO.wakeAll();
		}
	    }
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
