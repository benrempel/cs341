package nachos.threads;

import java.util.LinkedList;
import nachos.machine.*;
import nachos.threads.*;

/**
 * A synchronized queue.
 */
public class SynchList {
    /**
     * Allocate a new synchronized queue.
     */
    public SynchList() {
	list = new LinkedList<Object>();
	lock = new Lock();
	listEmpty = new Condition2(lock);
    }

    /**
     * Add the specified object to the end of the queue. If another thread is
     * waiting in <tt>removeFirst()</tt>, it is woken up.
     *
     * @param	o	the object to add. Must not be <tt>null</tt>.
     */
    public void add(Object o) {
	Lib.assertTrue(o != null);
	
	lock.acquire();
	list.add(o);
	listEmpty.wake();
	lock.release();
    }

    public void addAll(Object o, int numThreads) {
    	Lib.assertTrue(o != null);

    	lock.acquire();
    	for(int i = 0; i < numThreads; i++)
    	    list.add(o);
    	listEmpty.wakeAll();
    	lock.release();
    }

	private static class AddAllTest implements Runnable {
    	AddAllTest(String name, SynchList monitor) {
    	    this.monitor = monitor;
    	    this.name = name;
    	}

	    public void run() {
	        System.out.println(name + " is running.");
	        System.out.println(name + " has removed " + monitor.removeFirst() + " from the list ");
	    }

    	private SynchList monitor;
    	private String name;
    }

    /**
     * Remove an object from the front of the queue, blocking until the queue
     * is non-empty if necessary.
     *
     * @return	the element removed from the front of the queue.
     */
    public Object removeFirst() {
	Object o;

	lock.acquire();
	while (list.isEmpty()) {
	    listEmpty.sleep();
	}
	o = list.removeFirst();
	lock.release();

	return o;
    }

    private static class PingTest implements Runnable {
	PingTest(SynchList ping, SynchList pong) {
	    this.ping = ping;
	    this.pong = pong;
	}
	
	public void run() {
	    for (int i=0; i<10; i++)
		pong.add(ping.removeFirst());
	}

	private SynchList ping;
	private SynchList pong;
    }

    /**
     * Test that this module is working.
     */
    public static void selfTest() {
/*	    Object stuffs = pong.removeFirst();
	    System.out.println(stuffs);
	    Lib.assertTrue(stuffs == o);
	    SynchList addAllList = new SynchList();
	    for (int i=1; i<5; i++)
	        new KThread(new AddAllTest("Grabber test " + Integer.toString(i), addAllList)).setName("Grabber thread " + Integer.toString(i)).fork();
	    System.out.println("We've added everything!");
	    KThread.currentThread().yield(); // Make sure all of our GrabberTest threads get to call removeFirst() before we advance
	    addAllList.addAll(new Integer(69), new Integer(3));
	    KThread.currentThread().yield(); // Make sure all of our GrabberTest threads get to be woken up after our call to addAll*/
    }

    private LinkedList<Object> list;
    private Lock lock;
    private Condition2 listEmpty;
}

