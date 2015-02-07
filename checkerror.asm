#This program test the errors returned by syscalls. If a status is returned
#from a syscall TRAP which is not expected, then a CORE DUMP will be triggered
#
#To tell where in this program the unexpected return code happened, look on
#the stack for the most recent negative number, and find where that number
#got pushed to the stack. We are using these numbers to identify each trial.
#
#Ideally there should be no output from this program.
#
#NOTE: The error codes are not defined in the SOS standard, so other students
#may have numbered them differently.


#Open the keyboard device
SET r0 0       #device #0 (keyboard)
PUSH r0        #push argument on stack
SET r4 3       #OPEN sys call id
PUSH r4        #push sys call id on stack
TRAP           #open the device

#Check for failure
POP r2         #get return code from the system call
SET r3 0       #Succes code

#Push trial number to the stack
SET r0 -1
PUSH r0
BNE r2 r3 err  #exit program on error

#Open the keyboard again
SET r0 0       #device #0 (keyboard)
PUSH r0        #push argument on stack
PUSH r4        #push sys call id on stack
TRAP           #open the device

#Check for failure
POP r2         #get return code from the system call
SET r3 3       #already open code

#Push trial number to the stack
SET r0 -2
PUSH r0
BNE r2 r3 err  #exit program on non-error

#Try to write to the keyboard device
SET r0 0       #device #0 (keyboard)
PUSH r0        #push device number
PUSH r0        #push address (arg not used by this device so any val will do)
PUSH r4        #push value to send to device
SET r0 6       #WRITE system call
PUSH r0        #push system call id
TRAP           #system call to write the value

#Check for failure
POP r2         #get return code from the system call
SET r3 5       #RO error code

#Push trial number to the stack
SET r0 -3
PUSH r0
BNE r2 r3 err  #exit program on non-error

#close the keyboard device
SET r0 0       #keyboard device id
PUSH r0        #push device number 0 (keyboard)
SET r4 4       #CLOSE sys call id
PUSH r4        #push the sys call id onto the stack
TRAP           #close the device

#Check for failure
POP r2         #get return code from the system call
SET r3 0       #Succes code

#Push trial number to the stack
SET r0 -4
PUSH r0
BNE r2 r3 err  #exit program on error

#close the already closed keyboard device
SET r0 0       #keyboard device id
PUSH r0        #push device number 0 (keyboard)
PUSH r4        #push the sys call id onto the stack
TRAP           #close the device

#Check for failure
POP r2         #get return code from the system call
SET r3 4       #not open error code

#Push trial number to the stack
SET r0 -5
PUSH r0
BNE r2 r3 err  #exit program on non-error

#Reserve the console device
SET r0 1       #device #1 (console output)
PUSH r0        #push argument on stack
SET r4 3       #OPEN sys call id
PUSH r4        #push sys call id on stack
TRAP           #open the device

#Check for failure
POP r2         #get return code from the system call
SET r3 0       #Succes code

#Push trial number to the stack
SET r0 -6
PUSH r0
BNE r2 r3 err  #exit program on error

#Try to read from the console
SET r0 1       #device #1 (console output)
PUSH r0        #push device number
PUSH r0        #push address (arg not used by this device so any val will do)
SET r0 5       #READ system call
PUSH r0        #push system call id
TRAP           #system call to read the value

#Check for failure
POP r2         #get return code from the system call
SET r3 6       #WO error code

#Push trial number to the stack
SET r0 -7
PUSH r0
BNE r2 r3 err  #exit program on non-error

#close the console device
SET r4 1       #keyboard device id
PUSH r4        #push device number 1 (console output)
SET r4 4       #CLOSE sys call id
PUSH r4        #push the sys call id onto the stack
TRAP           #close the device

#Check for failure
POP r2         #get return code from the system call
SET r3 0       #Succes code

#Push trial number to the stack
SET r0 -8
PUSH r0
BNE r2 r3 err  #exit program on error

#Reserve non-existant device
SET r0 2       #device #2 (does not exist)
PUSH r0        #push argument on stack
SET r4 3       #OPEN sys call id
PUSH r4        #push sys call id on stack
TRAP           #open the device

#Check for failure
POP r2         #get return code from the system call
SET r3 1       #DNE error

#Push trial number to the stack
SET r0 -9
PUSH r0
BNE r2 r3 err  #exit program on error

#Try to write to the closed console
SET r0 1       #device #1 (console output)
PUSH r0        #push device number
PUSH r0        #push address (arg not used by this device so any val will do)
PUSH r4        #push value to send to device
SET r0 6       #WRITE system call
PUSH r0        #push system call id
TRAP           #system call to write the value

#Check for failure
POP r2         #get return code from the system call
SET r3 4       #Not open error code

#Push trial number to the stack
SET r0 -10
PUSH r0
BNE r2 r3 err  #exit program on non-error

#Try to read from the closed keyboard
SET r0 0       #device #0 (keyboard)
PUSH r0        #push device number
PUSH r0        #push address (arg not used by this device so any val will do)
SET r0 5       #READ system call
PUSH r0        #push system call id
TRAP           #system call to read the value

#Check for failure
POP r2         #get return code from the system call
SET r3 4       #not open error code

#Push trial number to the stack
SET r0 -11
PUSH r0
BNE r2 r3 err  #exit program on non-error

#Everything is good. Skip the err routine
BRANCH exit

:err

#Core dump on error
#Find the most recent negative number on the stack to be able to tell at
#what point our program malfunctioned
SET r0 9
PUSH r0
TRAP

:exit

