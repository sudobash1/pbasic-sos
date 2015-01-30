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
        m_RAM = r;
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

        m_CPU.setSP(progArray.length);

        m_CPU.registerTrapHandler(this);

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
        m_CPU.printInstr(instr);
        System.out.println("NOW YOU DIE!!!");
        System.exit(0);
    }
    
    /*======================================================================
     * System Calls
     *----------------------------------------------------------------------
     */
    
    /**
     * systemCall
     *
     * Occurs when TRAP is encountered in child process.
     */
    public void systemCall()
    {
        //%%%REPLACE THESE LINES WITH APPROPRIATE CODE
        System.out.println("TRAP handled!");
        System.exit(0);
    }

};//class SOS
