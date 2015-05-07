package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

/**
 * A <tt>UserProcess</tt> that supports demand-paging.
 */
public class VMProcess extends UserProcess {
    /**
     * Allocate a new process.
     */
    public VMProcess() {
	super();
        TLBIndex = 0;
    }

    /**
     * Save the state of this process in preparation for a context switch.
     * Called by <tt>UThread.saveState()</tt>.
     */
    public void saveState() {
	for (int i = 0; i < Machine.processor().getTLBSize(); i++) {
            Machine.processor().readTLBEntry(i).valid = false;
        }
    }

    /**
     * Restore the state of this process after a context switch. Called by
     * <tt>UThread.restoreState()</tt>.
     */
    public void restoreState() {
    }

    public boolean load(String name, String[] args) {
        boolean val = super.load(name, args);
        if (!val) {
            return false;
        } 
        int numPhysPages = Machine.processor().getNumPhysPages();
        boolean readonly;
        long key;
        TranslationEntry entry;
        for (int i = 0; i < coff.getNumSections(); i++) {
            for (int j = 0; j < coff.getSection(i).getLength(); j++) {
                readonly = coff.getSection(i).isReadOnly();
                key = getKeyFromVaddr(Machine.processor().makeAddress(coff.getSection(i).getFirstVPN() + j, 0));
                entry = new TranslationEntry(coff.getSection(i).getFirstVPN() + i, VMKernel.freePages.poll(), true,  readonly,  false, false);
                VMKernel.GIPT.put(key, entry);
            }
        }

        int pageNum;
        for (int i = 0; i < stackPages + numPages; i++) {
            key = getKeyFromVaddr(Machine.processor().makeAddress(i, 0));
            if (!VMKernel.GIPT.containsKey(key)) {
                pageNum = UserKernel.freePages.poll();
                entry = new TranslationEntry(i, pageNum, true, false, false, false);
                VMKernel.GIPT.put(key, entry);
            }
        }
        return val;
    }

    /**
     * Initializes page tables for this process so that the executable can be
     * demand-paged.
     *
     * @return	<tt>true</tt> if successful.
     */
    protected boolean loadSections() {
       if (numPages > Machine.processor().getNumPhysPages()) {
	    coff.close();
	    Lib.debug(dbgProcess, "\tinsufficient physical memory");
	    return false;
	}

	// load sections
	for (int s=0; s<coff.getNumSections(); s++) {
	    CoffSection section = coff.getSection(s);
	    
	    Lib.debug(dbgProcess, "\tinitializing " + section.getName()
		      + " section (" + section.getLength() + " pages)");

	    for (int i=0; i<section.getLength(); i++) {
		int vpn = section.getFirstVPN()+i;

		section.loadPage(i, pageTable[vpn].ppn);
	    }
	}
	
	return true;
    }

    /**
     * Release any resources allocated by <tt>loadSections()</tt>.
     */
    protected void unloadSections() {
	super.unloadSections();
    }    

    public int readVirtualMemory(int vaddr, byte[] data, int offset, int length) {
        int val = super.readVirtualMemory(vaddr, data, offset, length);
        int i;
        if ((i = getTLBEntryFromVaddr(vaddr)) != -1) {
            Machine.processor().readTLBEntry(i).used = true;
        }
        return val;
    }

    public int writeVirtualMemory(int vaddr, byte[] data, int offset, int length) {
        int val = super.writeVirtualMemory(vaddr, data, offset, length);
        int i;
        if ((i = getTLBEntryFromVaddr(vaddr)) != -1) {
            Machine.processor().readTLBEntry(i).used = true;
            Machine.processor().readTLBEntry(i).dirty = true;
        }
        return val;
    } 

    long getKeyFromVaddr(int vaddr) {
        long key = vaddr << 32;
        key += pid;
        return key;
    }

    private int getTLBEntryFromVaddr(int vaddr) {
        long key = getKeyFromVaddr(vaddr);
        int vpn = VMKernel.GIPT.get(key).vpn;
        for (int i = 0; i < Machine.processor().getTLBSize(); i++) {
            if (Machine.processor().readTLBEntry(i).vpn == vpn) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Handle a user exception. Called by
     * <tt>UserKernel.exceptionHandler()</tt>. The
     * <i>cause</i> argument identifies which exception occurred; see the
     * <tt>Processor.exceptionZZZ</tt> constants.
     *
     * @param	cause	the user exception that occurred.
     */
    public void handleException(int cause) {
	Processor processor = Machine.processor();

	switch (cause) {
	case Processor.exceptionTLBMiss:
            int badvaddr = processor.readRegister(Processor.regBadVAddr);
            long keything = badvaddr << 32;
            keything += pid;
            TranslationEntry entry = VMKernel.GIPT.get(keything);
            int place = findEntryInTLB(entry);
            if (place != -1) {
                processor.writeTLBEntry(place, entry);
            }
            else {
                processor.writeTLBEntry(TLBIndex%processor.getTLBSize(), entry);
                TLBIndex++;
            }
            break;
	default:
	    super.handleException(cause);
	    break;
	}
    }

    private int findEntryInTLB(TranslationEntry entry) {
        for (int i = 0; i < Machine.processor().getTLBSize(); i++) {
            if (Machine.processor().readTLBEntry(i).ppn == entry.ppn) {
                return i;
            }  
        }
        return -1;
    }
	
    private static final int pageSize = Processor.pageSize;
    private static final char dbgProcess = 'a';
    private static final char dbgVM = 'v';
    private static int TLBIndex;
}
