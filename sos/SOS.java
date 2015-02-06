package sos;

import java.util.*;

/**
 * This class contains the simulated operating system (SOS).  Realistically it
 * would run on the same processor (CPU) that it is managing but instead it uses
 * the real-world processor in order to allow a focus on the essentials of
 * operating system design using a high level programming language.
 *
 * Authors include: Stephen Robinson and Camden McKone
 *
 */
   
public class SOS implements CPU.TrapHandler
{
    //======================================================================
    //Member variables
    //----------------------------------------------------------------------

    /**
     * This flag causes the SOS to print lots of potentially helpful
     * status messages
     **/
    public static final boolean m_verbose = false;

    /**
     * The ProcessControlBlock of the current process
     **/
    private ProcessControlBlock m_currProcess = null;

    /**
     * A Vector of DeviceInfo objects
     **/
    private Vector<DeviceInfo> m_devices = null;
    
    /**
     * The CPU the operating system is managing.
     **/
    private CPU m_CPU = null;
    
    /**
     * The RAM attached to the CPU.
     **/
    private RAM m_RAM = null;

    //======================================================================
    //Constants
    //----------------------------------------------------------------------

    //These constants define the system calls this OS can currently handle
    public static final int SYSCALL_EXIT     = 0;    /* exit the current program */
    public static final int SYSCALL_OUTPUT   = 1;    /* outputs a number */
    public static final int SYSCALL_GETPID   = 2;    /* get current process id */
    public static final int SYSCALL_OPEN    = 3;    /* access a device */
    public static final int SYSCALL_CLOSE   = 4;    /* release a device */
    public static final int SYSCALL_READ    = 5;    /* get input from device */
    public static final int SYSCALL_WRITE   = 6;    /* send output to device */
    public static final int SYSCALL_COREDUMP = 9;    /* print process state and exit */


    /*======================================================================
     * Constructors & Debugging
     *----------------------------------------------------------------------
     */
    
    /**
     * The constructor does nothing special
     */
    public SOS(CPU c, RAM r)
    {
        //Init member list
        m_CPU = c;
        m_CPU.registerTrapHandler(this);
        m_RAM = r;

        m_currProcess = new ProcessControlBlock(42);
        m_devices = new Vector<DeviceInfo>();
    }//SOS ctor
    
    /**
     * Does a System.out.print as long as m_verbose is true
     **/
    public static void debugPrint(String s)
    {
        if (m_verbose)
        {
            System.out.print(s);
        }
    }
    
    /**
     * Does a System.out.println as long as m_verbose is true
     **/
    public static void debugPrintln(String s)
    {
        if (m_verbose)
        {
            System.out.println(s);
        }
    }
    
    /*======================================================================
     * Memory Block Management Methods
     *----------------------------------------------------------------------
     */

    //None yet!
    
    /*======================================================================
     * Device Management Methods
     *----------------------------------------------------------------------
     */

    //None yet!
    
    /*======================================================================
     * Process Management Methods
     *----------------------------------------------------------------------
     */

    //None yet!
    
    /*======================================================================
     * Program Management Methods
     *----------------------------------------------------------------------
     */

    /**
     * createProcess
     *
     * Creates one process for the CPU.
     *
     * @param prog The program class to be loaded into memory.
     * @param allocSize The amount of memory to allocate for the program.
     */
    public void createProcess(Program prog, int allocSize)
    {
        final int base = 4; //This is just an arbitrary base, hardcoded for now
        m_CPU.setBASE(base);
        m_CPU.setLIM(base + allocSize);
        m_CPU.setPC(0); //We are going to use a logical (not physical) PC

        int[] progArray = prog.export();

        for (int progAddr=0; progAddr<progArray.length; ++progAddr ){
            m_RAM.write(base + progAddr, progArray[progAddr]);
        }

        m_CPU.setSP(allocSize); //Stack starts at the bottom and grows up.

    }//createProcess
 

    /*======================================================================
     * Interrupt Handlers
     *----------------------------------------------------------------------
     */

    /**
     * interruptIllegalMemoryAccess
     *
     * Handles Illegal Memory Access interrupts.
     *
     * @param addr The address which was attempted to be accessed
     */
    public void interruptIllegalMemoryAccess(int addr){
        System.out.println("Error: Illegal Memory Access at addr " + addr);
        System.out.println("NOW YOU DIE!!!");
        System.exit(0);
    }

    /**
     * interruptDivideByZero
     *
     * Handles Divide by Zero interrupts.
     */
    public void interruptDivideByZero(){
        System.out.println("Error: Divide by Zero");
        System.out.println("NOW YOU DIE!!!");
        System.exit(0);
    }

    /**
     * interruptIllegalInstruction
     *
     * Handles Illegal Instruction interrupts.
     *
     * @param instr The instruction which caused the interrupt
     */
    public void interruptIllegalInstruction(int[] instr){
        System.out.println("Error: Illegal Instruction:");
        System.out.println(instr[0] + ", " + instr[1] + ", " + instr[2] + ", " + instr[3]);
        System.out.println("NOW YOU DIE!!!");
        System.exit(0);
    }
    
    /*======================================================================
     * System Calls
     *----------------------------------------------------------------------
     */

    /**
     * syscallExit
     *
     * Exits from the current process.
     */
    private void syscallExit() {
        System.exit(0);
    }

    /**
     * syscallOutput
     *
     * Outputs the top number from the stack.
     */
    private void syscallOutput() {
        System.out.println("OUTPUT: " + m_CPU.popStack());
    }

    /**
     * syscallGetPID
     *
     * Pushes the PID to the stack.
     */
    private void syscallGetPID() {
        final int pid = 42; //just for now we will use a constant pid.
        m_CPU.pushStack(pid);
    }

    /**
     * syscallOpen
     *
     * Open a device.
     */
    private void syscallOpen() {
    }

    /**
     * syscallClose
     *
     * Close a device.
     */
    private void syscallClose() {
    }

    /**
     * syscallRead
     *
     * Read from an open device.
     */
    private void syscallRead() {
    }

    /**
     * syscallWrite
     *
     * Write to an open device.
     */
    private void syscallWrite() {
    }

    /**
     * syscallCoreDump
     *
     * Prints the registers and top three stack items, then exits the process.
     */
    private void syscallCoreDump() {

        System.out.println("\n\nCORE DUMP!");

        m_CPU.regDump();

        System.out.println("Top three stack items:");
        for (int i=0; i<3; ++i){
            if (m_CPU.validMemory(m_CPU.getSP() + 1 + m_CPU.getBASE())) {
                System.out.println(m_CPU.popStack());
            } else {
                System.out.println(" -- NULL -- ");
            }
        }
        syscallExit();
    }
    
    /**
     * systemCall
     *
     * Occurs when TRAP is encountered in child process.
     */
    public void systemCall()
    {
        switch (m_CPU.popStack()) {
            case SYSCALL_EXIT:
                syscallExit();
                break;
            case SYSCALL_OUTPUT:
                syscallOutput();
                break;
            case SYSCALL_GETPID:
                syscallGetPID();
                break;
            case SYSCALL_OPEN:
                syscallOpen();
                break;
            case SYSCALL_CLOSE:
                syscallClose();
                break;
            case SYSCALL_READ:
                syscallRead();
                break;
            case SYSCALL_WRITE:
                syscallWrite();
                break;
            case SYSCALL_COREDUMP:
                syscallCoreDump();
                break;
        }
    }


    //======================================================================
    // Inner Classes
    //----------------------------------------------------------------------

    /**
     * class ProcessControlBlock
     *
     * This class contains information about a currently active process.
     */
    private class ProcessControlBlock
    {
        /**
         * a unique id for this process
         */
        private int processId = 0;

        /**
         * constructor
         *
         * @param pid        a process id for the process.  The caller is
         *                   responsible for making sure it is unique.
         */
        public ProcessControlBlock(int pid)
        {
            this.processId = pid;
        }

        /**
         * @return the current process' id
         */
        public int getProcessId()
        {
            return this.processId;
        }

        
    }//class ProcessControlBlock

    /**
     * class DeviceInfo
     *
     * This class contains information about a device that is currently
     * registered with the system.
     */
    private class DeviceInfo
    {
        /** every device has a unique id */
        private int id;
        /** a reference to the device driver for this device */
        private Device device;
        /** a list of processes that have opened this device */
        private Vector<ProcessControlBlock> procs;

        /**
         * constructor
         *
         * @param d          a reference to the device driver for this device
         * @param initID     the id for this device.  The caller is responsible
         *                   for guaranteeing that this is a unique id.
         */
        public DeviceInfo(Device d, int initID)
        {
            this.id = initID;
            this.device = d;
            d.setId(initID);
            this.procs = new Vector<ProcessControlBlock>();
        }

        /** @return the device's id */
        public int getId()
        {
            return this.id;
        }

        /** @return this device's driver */
        public Device getDevice()
        {
            return this.device;
        }

        /** Register a new process as having opened this device */
        public void addProcess(ProcessControlBlock pi)
        {
            procs.add(pi);
        }
        
        /** Register a process as having closed this device */
        public void removeProcess(ProcessControlBlock pi)
        {
            procs.remove(pi);
        }

        /** Does the given process currently have this device opened? */
        public boolean containsProcess(ProcessControlBlock pi)
        {
            return procs.contains(pi);
        }
        
        /** Is this device currently not opened by any process? */
        public boolean unused()
        {
            return procs.size() == 0;
        }
        
    }//class DeviceInfo

    
    /*======================================================================
     * Device Management Methods
     *----------------------------------------------------------------------
     */

    /**
     * registerDevice
     *
     * adds a new device to the list of devices managed by the OS
     *
     * @param dev     the device driver
     * @param id      the id to assign to this device
     * 
     */
    public void registerDevice(Device dev, int id)
    {
        m_devices.add(new DeviceInfo(dev, id));
    } //registerDevice

};//class SOS
