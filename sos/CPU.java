package sos;

import java.util.*;

/**
 * This class is the centerpiece of a simulation of the essential hardware of a
 * microcomputer.  This includes a processor chip, RAM and I/O devices.  It is
 * designed to demonstrate a simulated operating system (SOS).
 *
 * Authors include: Stephen Robinson and Camden McKone
 *
 * @see RAM
 * @see SOS
 * @see Program
 * @see Sim
 */

public class CPU
{
    
    //======================================================================
    //Constants
    //----------------------------------------------------------------------

    //These constants define the instructions available on the chip
    public static final int SET    = 0;    /* set value of reg */
    public static final int ADD    = 1;    // put reg1 + reg2 into reg3
    public static final int SUB    = 2;    // put reg1 - reg2 into reg3
    public static final int MUL    = 3;    // put reg1 * reg2 into reg3
    public static final int DIV    = 4;    // put reg1 / reg2 into reg3
    public static final int COPY   = 5;    // copy reg1 to reg2
    public static final int BRANCH = 6;    // goto address in reg
    public static final int BNE    = 7;    // branch if not equal
    public static final int BLT    = 8;    // branch if less than
    public static final int POP    = 9;    // load value from stack
    public static final int PUSH   = 10;   // save value to stack
    public static final int LOAD   = 11;   // load value from heap
    public static final int SAVE   = 12;   // save value to heap
    public static final int TRAP   = 15;   // system call
    
    //These constants define the indexes to each register
    public static final int R0   = 0;     // general purpose registers
    public static final int R1   = 1;
    public static final int R2   = 2;
    public static final int R3   = 3;
    public static final int R4   = 4;
    public static final int PC   = 5;     // program counter
    public static final int SP   = 6;     // stack pointer
    public static final int BASE = 7;     // bottom of currently accessible RAM
    public static final int LIM  = 8;     // top of accessible RAM
    public static final int NUMREG = 9;   // number of registers

    //Misc constants
    public static final int NUMGENREG = PC; // the number of general registers
    public static final int INSTRSIZE = 4;  // number of ints in a single instr +
                                            // args.  (Set to a fixed value for simplicity.)

    //======================================================================
    //Member variables
    //----------------------------------------------------------------------
    /**
     * specifies whether the CPU should output details of its work
     **/
    private boolean m_verbose = true;

    /**
     * This array contains all the registers on the "chip".
     **/
    private int m_registers[];

    /**
     * A pointer to the RAM used by this CPU
     *
     * @see RAM
     **/
    private RAM m_RAM = null;

    //======================================================================
    //Callback Interface
    //----------------------------------------------------------------------
    /**
     * TrapHandler
     *
     * This interface should be implemented by the operating system to allow the
     * simulated CPU to generate hardware interrupts and system calls.
     */
    public interface TrapHandler
    {
        void interruptIllegalMemoryAccess(int addr);
        void interruptDivideByZero();
        void interruptIllegalInstruction(int[] instr);
        void systemCall();
    };//interface TrapHandler

    
    /**
     * a reference to the trap handler for this CPU.  On a real CPU this would
     * simply be an address that the PC register is set to.
     */
    private TrapHandler m_TH = null;


    //======================================================================
    //Methods
    //----------------------------------------------------------------------

    /**
     * CPU ctor
     *
     * Intializes all member variables.
     */
    public CPU(RAM ram)
    {
        m_registers = new int[NUMREG];
        for(int i = 0; i < NUMREG; i++)
        {
            m_registers[i] = 0;
        }
        m_RAM = ram;

    }//CPU ctor 

    /**
     * registerTrapHandler
     *
     * allows SOS to register itself as the trap handler 
     */
    public void registerTrapHandler(TrapHandler th)
    {
        m_TH = th;
    }

    /**
     * getPC
     *
     * @return the value of the program counter
     */
    public int getPC()
    {
        return m_registers[PC];
    }

    /**
     * getSP
     *
     * @return the value of the stack pointer
     */
    public int getSP()
    {
        return m_registers[SP];
    }

    /**
     * getBASE
     *
     * @return the value of the base register
     */
    public int getBASE()
    {
        return m_registers[BASE];
    }

    /**
     * getLIM
     *
     * @return the value of the limit register
     */
    public int getLIM()
    {
        return m_registers[LIM];
    }

    /**
     * getRegisters
     *
     * @return the registers
     */
    public int[] getRegisters()
    {
        return m_registers;
    }

    /**
     * setPC
     *
     * @param v the new value of the program counter
     */
    public void setPC(int v)
    {
        m_registers[PC] = v;
    }

    /**
     * setSP
     *
     * @param v the new value of the stack pointer
     */
    public void setSP(int v)
    {
        m_registers[SP] = v;
    }

    /**
     * setBASE
     *
     * @param v the new value of the base register
     */
    public void setBASE(int v)
    {
        m_registers[BASE] = v;
    }

    /**
     * setLIM
     *
     * @param v the new value of the limit register
     */
    public void setLIM(int v)
    {
        m_registers[LIM] = v;
    }

    /**
     * regDump
     *
     * Prints the values of the registers.  Useful for debugging.
     */
    private void regDump()
    {
        for(int i = 0; i < NUMGENREG; i++)
        {
            System.out.print("r" + i + "=" + m_registers[i] + " ");
        }//for
        System.out.print("PC=" + m_registers[PC] + " ");
        System.out.print("SP=" + m_registers[SP] + " ");
        System.out.print("BASE=" + m_registers[BASE] + " ");
        System.out.print("LIM=" + m_registers[LIM] + " ");
        System.out.println("");
    }//regDump

    /**
     * printIntr
     *
     * Prints a given instruction in a user readable format.  Useful for
     * debugging.
     *
     * @param instr the current instruction
     */
    public static void printInstr(int[] instr)
    {
            switch(instr[0])
            {
                case SET:
                    System.out.println("SET R" + instr[1] + " = " + instr[2]);
                    break;
                case ADD:
                    System.out.println("ADD R" + instr[1] + " = R" + instr[2] + " + R" + instr[3]);
                    break;
                case SUB:
                    System.out.println("SUB R" + instr[1] + " = R" + instr[2] + " - R" + instr[3]);
                    break;
                case MUL:
                    System.out.println("MUL R" + instr[1] + " = R" + instr[2] + " * R" + instr[3]);
                    break;
                case DIV:
                    System.out.println("DIV R" + instr[1] + " = R" + instr[2] + " / R" + instr[3]);
                    break;
                case COPY:
                    System.out.println("COPY R" + instr[1] + " = R" + instr[2]);
                    break;
                case BRANCH:
                    System.out.println("BRANCH @" + instr[1]);
                    break;
                case BNE:
                    System.out.println("BNE (R" + instr[1] + " != R" + instr[2] + ") @" + instr[3]);
                    break;
                case BLT:
                    System.out.println("BLT (R" + instr[1] + " < R" + instr[2] + ") @" + instr[3]);
                    break;
                case POP:
                    System.out.println("POP R" + instr[1]);
                    break;
                case PUSH:
                    System.out.println("PUSH R" + instr[1]);
                    break;
                case LOAD:
                    System.out.println("LOAD R" + instr[1] + " <-- @R" + instr[2]);
                    break;
                case SAVE:
                    System.out.println("SAVE R" + instr[1] + " --> @R" + instr[2]);
                    break;
                case TRAP:
                    System.out.print("TRAP ");
                    break;
                default:        // should never be reached
                    System.out.println("?? ");
                    break;          
            }//switch

    }//printInstr


    /**
     * validMemory
     *
     * Determines if physical address respects BASE and LIM registers.
     *
     * @param addr the address to check
     *
     * @return true iff the address is valid.
     */
    public boolean validMemory(int addr){
        return (addr >= m_registers[BASE] && addr <= m_registers[LIM]);
    }

    /**
     * pushStack
     *
     * Pushes a value to the stack.
     *
     * @param value the value to push to the stack.
     */
    private void pushStack(int value) {
        m_RAM.write(m_registers[SP] + m_registers[BASE], value);
        //TODO: stack overflow check
        m_registers[SP]++;
    }

    /**
     * popStack
     *
     * Pops a value from the stack.
     *
     * @return The value poped from the stack.
     */
    private int popStack() {
        //TODO: empty stack pop
        m_registers[SP]--;
        return m_RAM.read(m_registers[SP] + m_registers[BASE]);
    }

    /**
     * run
     *
     * Start the CPU simulation. Exits only on chrash or exit trap.
     */
    public void run()
    {
       
        while (true) {

            //Fetch next instruction
            int instr[] = m_RAM.fetch(m_registers[BASE] + m_registers[PC]);

            //Debug information if enabled
            if (m_verbose) {
                System.out.println(".");
                regDump();
                printInstr(instr);
            }

            //Determine action to take for instruction
            int addr;
            switch(instr[0]) {
                case SET:
                    m_registers[instr[1]] = instr[2];
                    break;
                case ADD:
                    m_registers[instr[1]] = m_registers[instr[2]] +
                                            m_registers[instr[3]];
                    break;
                case SUB:
                    m_registers[instr[1]] = m_registers[instr[2]] -
                                            m_registers[instr[3]];
                    break;
                case MUL:
                    m_registers[instr[1]] = m_registers[instr[2]] *
                                            m_registers[instr[3]];
                    break;
                case DIV:
                    m_registers[instr[1]] = m_registers[instr[2]] /
                                            m_registers[instr[3]];
                    break;
                case COPY:
                    m_registers[instr[1]] = m_registers[instr[2]];
                    break;
                case BRANCH:
                    m_registers[PC] = instr[1] - 4;
                    break;
                case BNE:
                    if (m_registers[instr[1]] != m_registers[instr[2]]) {
                        m_registers[PC] = instr[3] - 4;
                    }
                    break;
                case BLT:
                    if (m_registers[instr[1]] < m_registers[instr[2]]) {
                        m_registers[PC] = instr[3] - 4;
                    }
                    break;
                case POP:
                    m_registers[instr[1]] = popStack();
                    break;
                case PUSH:
                    pushStack(m_registers[instr[1]]);
                    break;
                case LOAD:
                    addr = m_registers[instr[2]] + m_registers[BASE];
                    if (!validMemory(addr)) {
                        System.out.println("ERROR: LOAD out of bounds.");
                        System.out.println("NOW YOU DIE!!!!");
                        return;
                    }
                    m_registers[instr[1]] = m_RAM.read(addr);
                    break;
                case SAVE:
                    addr = m_registers[instr[2]] + m_registers[BASE];
                    if (!validMemory(addr)) {
                        System.out.println("ERROR: SAVE out of bounds.");
                        System.out.println("NOW YOU DIE!!!!");
                        return;
                    }
                    m_RAM.write(addr, m_registers[instr[1]]);
                    break;
                case TRAP:
                    m_TH.systemCall();
                    break;
                default: // This is bad. Why did this happen to me?
                    System.out.println("ERROR: unsupported opcode: " +
                                        instr[0]);
                    //So much more creative than SegFault
                    System.out.println("NOW YOU DIE!!!!");
                    return;
            }//switch

            m_registers[PC] += INSTRSIZE; //Increment the PC counter

            //Check for out of bounds PC
            if (!validMemory(m_registers[BASE] + m_registers[PC])) {
                System.out.println("ERROR: PC out of bounds.");
                System.out.println("NOW YOU DIE!!!!");
                return;
            }
        }

    }//run
    
};//class CPU
