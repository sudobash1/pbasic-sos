package sos;

import java.util.*;

/**
 * This class sets up the SOS simulation by creating the RAM, CPU and SOS
 * objects, loading appropriate programs, and calling {@link CPU#run} method on
 * the CPU.
 *
 * @see RAM
 * @see CPU
 * @see SOS
 * @see Program
 */
public class Sim
{

    private Program m_mainProgram = null;
    private ArrayList<Program> m_programs = null;
    private int m_ramAmount = 4000;
    private int m_ramLatency = 10;
    private ExitCatcher m_ec = null;
    private DoNothingHandler m_dnh = null;

    public Sim(String [] args) {

        m_programs = new ArrayList<Program>();

        //parse the command line arguments.
        parseArgs(args);

        //Start catching System.exit
        m_ec = new ExitCatcher();
        System.setSecurityManager(m_ec);

        m_dnh = new DoNothingHandler();
    }

    /*======================================================================-
     * Inner Classes
     *----------------------------------------------------------------------
     */
    
    /**
     * ExitCatcher
     *
     * is a security manager that prevents threads from calling System.exit().
     * This allows Sim.java to properly time the simulation.
     *
     */
    static class ExitCatcher extends SecurityManager
    {
        private boolean m_caught = false;

        public ExitCatcher()
        {
            super();
        }

        public boolean isExitCaught()
        {
            return m_caught;
        }

        public void checkExit(int status)
        {
            super.checkExit(status);
            if (!m_caught)
            {
                m_caught = true;
                throw new SecurityException();
            }
        }

        public void checkRead(String file) 
        {
        	//do nothing
        }
        
    }//ExitCatcher

    /**
     * DoNothingHandler
     *
     * needed to "handle" uncaught exceptions thrown by the device and CPU
     * threads (simulation will just end)
     */
    static class DoNothingHandler implements Thread.UncaughtExceptionHandler
    {
        public void uncaughtException(Thread t, Throwable th)
        {
            if (th instanceof SecurityException)
            {
                //do nothing (what, you thought I was kidding?)
            }
            else
            {
                //Report other exceptions to the user
                System.out.println("Exception in Current Thread:");
                th.printStackTrace();
            }
        }
    }//DoNothingHandler

    /*======================================================================-
     * Methods
     *----------------------------------------------------------------------
     */

    /**
     * printUsage
     *
     * Print the usage message and exits.
     */
    private void printUsage() {
        System.out.println(
            "Usage: java sos.sim [-r ram_size] [-l ram_latency]" +
            "prog.asm [-s size] [prog2.asm [-s size]] ..."
        );
        System.exit(-1337);
    }

    /**
     * parseArgs
     *
     * Parse the command line arguments and place the results in instance
     * variables.
     *
     * @param args The arguments passed in from the command line.
     */
    private void parseArgs(String[] args) {
        //Check that there are enough command line arguments
        if (args.length == 0) {
            printUsage();
        }

        Program prog = null;

        boolean ramSizeArgumentFound = false;
        boolean ramSizeArgumentNext = false;
        boolean ramLatencyArgumentFound = false;
        boolean ramLatencyArgumentNext = false;
        boolean sizeArgumentNext = false;
        for (int i=0; i < args.length; ++i) {

            //If we just saw an -r -s or -l flag
            if (ramSizeArgumentNext ||
                ramLatencyArgumentNext ||
                sizeArgumentNext)
            {
                int num = 0;
                try {
                    num = Integer.valueOf(args[i]);
                } catch (NumberFormatException e) {
                    System.out.println( 
                        "Invalid value for " + args[i-1] + ". Number expected."
                    );
                    printUsage();
                }

                if (ramSizeArgumentNext) { m_ramAmount = num; }
                if (ramLatencyArgumentNext) { m_ramLatency = num; }
                if (sizeArgumentNext) {
                    prog.setDefaultAllocSize(num);
                    prog = null;
                }

                ramSizeArgumentNext = false;
                ramLatencyArgumentNext = false;
                sizeArgumentNext = false;
                
                continue;
            }
            
            //If we are looking at an -r flag.
            if (args[i].equals("-r")) {
                if (ramSizeArgumentFound) {
                    System.out.println("Duplicate -r flag.");
                    printUsage();
                }
                if (m_mainProgram != null) {
                    System.out.println(
                        "Flag -r must be before program arguments."
                    );
                    printUsage();
                }
                ramSizeArgumentFound = true;
                ramSizeArgumentNext = true;

                continue;
            }

            //If we are looking at an -l flag.
            if (args[i].equals("-l")) {
                if (ramLatencyArgumentFound) {
                    System.out.println("Duplicate -l flag.");
                    printUsage();
                }
                if (m_mainProgram != null) {
                    System.out.println(
                        "Flag -l must be before program arguments."
                    );
                    printUsage();
                }
                ramLatencyArgumentFound = true;
                ramLatencyArgumentNext = true;

                continue;
            }

            //If we are looking at a -s flag
            if (args[i].equals("-s")) {
                if (prog == null) {
                    System.out.println(
                        "-s flag requires a progN.asm argument"
                    );
                    printUsage();
                }
                sizeArgumentNext = true;

                continue;
            }

            //We are looking at a filename argument
            prog = new Program();
            if (prog.load(args[i], false) != 0) {
                System.out.println("ERROR: Could not load `" + args[i] + "'");
                System.exit(-7);
            }

            //If this is our main program
            if (m_mainProgram == null) {
                m_mainProgram = prog;

                //By default we will give it twice the minimum mem needed.
                m_mainProgram.setDefaultAllocSize(
                    Math.min(prog.getSize() * 2, m_ramAmount - 1)
                );
            } else {
                m_programs.add(prog);
            }
        }

        if (m_mainProgram == null) {
            System.out.println("ERROR: Requires a prog.asm argument.");
            printUsage();
        }

        if (ramSizeArgumentNext || ramLatencyArgumentNext || sizeArgumentNext){
            System.out.println(
                "ERROR: " + args[args.length-1] + " expects an argument"
            );
            printUsage();
        }

    }

    /**
     * runSimulation
     *
     * Runs a process from assembly file given on command line. Other programs
     * may be loaded by specifying additional assembly files.
     */
    private void runSimulation()
    {

        //Create the simulated hardware and OS
        RAM ram = new RAM(m_ramAmount, m_ramLatency);
        InterruptController ic = new InterruptController();
        KeyboardDevice kd = new KeyboardDevice(ic);
        ConsoleDevice cd = new ConsoleDevice(ic);
        kd.setId(0);
        cd.setId(1);
        CPU cpu = new CPU(ram, ic);
        SOS os  = new SOS(cpu, ram);

        //Register the device drivers with the OS
        os.registerDevice(kd, 0);
        os.registerDevice(cd, 1);

        //Load the program into RAM
        os.createProcess(m_mainProgram, m_mainProgram.getDefaultAllocSize());

        //Register other programs as ones that can be run via an Exec
        //system call
        for (Program prog : m_programs) {
            os.addProgram(prog);
        }

        //Start up the devices
        Thread t = new Thread(cd);
        t.setUncaughtExceptionHandler(m_dnh);
        t.start();
        t = new Thread(kd);
        t.setUncaughtExceptionHandler(m_dnh);
        t.start();
        
        //Run the simulation
        t = new Thread(cpu);
        t.setUncaughtExceptionHandler(m_dnh);
        t.start();

        //Wait until System.exit() is called
        while(!m_ec.isExitCaught())
        {
            try
            {
                t.join(1000);
            }
            catch(InterruptedException ie)
            {
                System.out.println("Interrupted!");
                return;
            }
        }//while
    }


    /**
     * This function makes the simulation go.
     */
    public int run()
    {
        //Do a timed run
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        try
        {
            //Run the simulation
            runSimulation();

            //Record the ending time
            endTime = System.currentTimeMillis();

            //Delay for any other threads that might be winding down
            Thread.sleep(1000);
        }
        catch(SecurityException se)
        {
            endTime = System.currentTimeMillis();
        }
        catch(Exception e)
        {
            endTime = System.currentTimeMillis();
            System.out.println("EXCEPTION THROWN DURING SIMULATION:");
            e.printStackTrace();
        }

        //If System.exit was not called by any thread then bypass that
        //protection now
        if (! m_ec.isExitCaught())
        {
            return -42;
        }

        //Print the final timing info for the user
        System.out.println("");
        System.out.println("");
        System.out.println("END OF SIMULATION");
        System.out.println("Total Simulation Time: " + (endTime - startTime) + "ms");

        return 0;
    }

    public static void main(String[] args){
        Sim sim = new Sim(args);
        try{ System.exit(sim.run()); } catch (SecurityException se) { }
    }
    
};
