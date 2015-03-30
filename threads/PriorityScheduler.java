package nachos.threads;

import nachos.machine.*;

import java.util.*;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
    /**
     * Allocate a new priority scheduler.
     */
    public PriorityScheduler() {
	
    }
    
    /**
     * Allocate a new priority thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer priority from waiting threads
     *					to the owning thread.
     * @return	a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
	return new PriorityQueue(transferPriority);
    }

    public int getPriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getPriority();
    }

    public int getEffectivePriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	Lib.assertTrue(priority >= priorityMinimum &&
		   priority <= priorityMaximum);
	
	getThreadState(thread).setPriority(priority);
    }

    public boolean increasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMaximum)
	    return false;

	setPriority(thread, priority+1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    public boolean decreasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMinimum)
	    return false;

	setPriority(thread, priority-1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    public static void selfTest() {
	KThread thread1 = new KThread(new PriorityTest()).setName("thread1");
	System.out.println("Created thread 1");
	thread1.fork();
	boolean intStatus = Machine.interrupt().disable();
	ThreadedKernel.scheduler.setPriority(thread1, 3);
	Machine.interrupt().restore(intStatus);
	KThread thread2 = new KThread(new PriorityTest()).setName("thread2");
	System.out.println("Created thread 2");
	thread2.fork();
	intStatus = Machine.interrupt().disable();
	ThreadedKernel.scheduler.setPriority(thread2, 4);
	Machine.interrupt().restore(intStatus);
	KThread thread3 = new KThread(new PriorityTest()).setName("thread3");
	System.out.println("Created thread 3");
	thread3.fork();
	intStatus = Machine.interrupt().disable();
	ThreadedKernel.scheduler.setPriority(thread3, 5);
	Machine.interrupt().restore(intStatus);
	KThread thread4 = new KThread(new PriorityTest()).setName("thread4");
	System.out.println("Created thread 4");
	thread4.fork();
	intStatus = Machine.interrupt().disable();
	ThreadedKernel.scheduler.setPriority(thread4, 6);
	Machine.interrupt().restore(intStatus);
	KThread.currentThread().yield();
    }

    public static class PriorityTest implements Runnable {
	public void run() {
		System.out.println("Starting " + KThread.currentThread().getName());
		KThread.currentThread().yield();
		System.out.println("Ending " + KThread.currentThread().getName());
	}
    }

    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = 7;    

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param	thread	the thread whose scheduling state to return.
     * @return	the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {
	if (thread.schedulingState == null)
	    thread.schedulingState = new ThreadState(thread);

	return (ThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
    protected class PriorityQueue extends ThreadQueue {
	PriorityQueue(boolean transferPriority) {
	    this.transferPriority = transferPriority;
	}
	
	public void remove(Object o) {
	    waitQueue.remove(o);
	}

	public void waitForAccess(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    getThreadState(thread).waitForAccess(this);
	}

	public void acquire(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    getThreadState(thread).acquire(this);
	}

	public KThread nextThread() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    return waitQueue.poll().thread;
	}

	public void offer(ThreadState thread) {
		waitQueue.offer(thread);
	}

	/**
	 * Return the next thread that <tt>nextThread()</tt> would return,
	 * without modifying the state of this queue.
	 *
	 * @return	the next thread that <tt>nextThread()</tt> would
	 *		return.
	 */
	protected ThreadState pickNextThread() {
	    return waitQueue.peek();
	}
	
	public void print() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    System.out.println(waitQueue.toString());
	}

	/**
	 * <tt>true</tt> if this queue should transfer priority from waiting
	 * threads to the owning thread.
	 */
	public boolean transferPriority;
   
	Comparator<ThreadState> comparator = new Comparator<ThreadState>() {
		
		@Override
		public int compare(ThreadState o1, ThreadState o2) {
			if (o1.getEffectivePriority() < o2.getEffectivePriority()) {
				return 1;
			}
			if (o1.getEffectivePriority() > o2.getEffectivePriority()) {
				return -1;
			}
			if (o1.getTime() < o2.getTime()) {
				return -1;
			}
			return 1;
		}

	};

	
	private java.util.PriorityQueue<ThreadState> waitQueue = new java.util.PriorityQueue<ThreadState>(INITSIZE, comparator);
	
     }

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see	nachos.threads.KThread#schedulingState
     */
    protected class ThreadState {
	/**
	 * Allocate a new <tt>ThreadState</tt> object and associate it with the
	 * specified thread.
	 *
	 * @param	thread	the thread this state belongs to.
	 */
	public ThreadState(KThread thread) {
	    this.thread = thread;
	    this.addTime = Machine.timer().getTime();
	    this.queuething = new PriorityQueue(false);
	    setPriority(priorityDefault);
	    effectivePriority = priority;
	}

	public String toString() {
		return this.thread.getName() + " (" + this.priority + ")";
	}

	/**
	 * Return the priority of the associated thread.
	 *
	 * @return	the priority of the associated thread.
	 */
	public int getPriority() {
	    return priority;
	}

	/**
	 * Return the effective priority of the associated thread.
	 *
	 * @return	the effective priority of the associated thread.
	 */
	public int getEffectivePriority() {
	    return effectivePriority;
	}

	/**
	 * Set the priority of the associated thread to the specified value.
	 *
	 * @param	priority	the new priority.
	 */
	public void setPriority(int priority) {
	    if (this.priority == priority) {
		return;
	    }
	    queuething.remove(this);
	    this.priority = priority;
	    queuething.offer(this);
	}

	/**
	 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
	 * the associated thread) is invoked on the specified priority queue.
	 * The associated thread is therefore waiting for access to the
	 * resource guarded by <tt>waitQueue</tt>. This method is only called
	 * if the associated thread cannot immediately obtain access.
	 *
	 * @param	waitQueue	the queue that the associated thread is
	 *				now waiting on.
	 *
	 * @see	nachos.threads.ThreadQueue#waitForAccess
	 */
	public void waitForAccess(PriorityQueue waitQueue) {
	    queuething = waitQueue;
	    this.addTime = Machine.timer().getTime();
	    waitQueue.offer(this);
	    System.out.println(this.thread.getName() + " added to queue");
	}

	/**
	 * Called when the associated thread has acquired access to whatever is
	 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
	 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
	 * <tt>thread</tt> is the associated thread), or as a result of
	 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
	 *
	 * @see	nachos.threads.ThreadQueue#acquire
	 * @see	nachos.threads.ThreadQueue#nextThread
	 */
	public void acquire(PriorityQueue waitQueue) {
	    KThread oldthread = thread.getOldThreadQ().nextThread();
	    if (oldthread != null) {
		if (priority < ThreadedKernel.scheduler.getEffectivePriority(oldthread)) {
		    effectivePriority = ThreadedKernel.scheduler.getEffectivePriority(oldthread);
		    return;
		}
	    }
	    effectivePriority = priority;
	}

	public float getTime() {
	    return this.addTime;
	}
	/** The thread with which this object is associated. */	   
	protected KThread thread;
	/** The priority of the associated thread. */
	protected int priority;
	protected float addTime;
	protected PriorityQueue queuething;
	protected int effectivePriority;
	
    }
    static final int INITSIZE = 10;
}
