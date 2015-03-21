package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
	message = 0;
	count = 0;
	l1 = new Lock();
	canSpeak = new Condition(l1);
	canListen = new Condition(l1);
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
	l1.acquire();
	if (count == 1) {
		canSpeak.sleep();
	}
	message = word;
	count++;
	canListen.wake();
	l1.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
	l1.acquire();
	if (count == 0) {
		canListen.sleep();
	}
	int word = message;
	count--;
	canSpeak.wake();
	l1.release();
	return word;
    }

    private Lock l1;
    private int message;
    private int count;
    private Condition canSpeak;
    private Condition canListen;
}
