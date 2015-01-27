#Authors: Stephen Robinson and Camden McKone 

SET R1 0   #num1
SET R2 1   #num2
SET R3 10  #count

#Push some fib numbers to the stack
:loop
ADD R4 R1 R2
PUSH R4
COPY R1 R2
COPY R2 R4

SET R4 1   #count increment
SUB R3 R3 R4
SET R4 0
BNE R3 R4 loop

#Pop our numbers and save all the numbers >= 5 to memory starting at addr 100
SET R2 1   #increment
SET R3 5   #min number
SET R4 200 #mem location

:memloop
POP R1
BLT R1 R3 endmemloop
SAVE R1 R4
ADD R4 R4 R2
BRANCH memloop
:endmemloop

#Read the first number we saved to mem and multiply it by 3/4
SET R2 200
SET R3 3
SET R4 4
LOAD R1 R2
MULT R1 R1 R3
DIV R1 R1 R4

